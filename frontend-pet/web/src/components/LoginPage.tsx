import { useCallback, useMemo, useState } from "react";
import {
  Button,
  Card,
  Checkbox,
  Cursor,
  Footer,
  Input,
  Tabs,
  type TabItem,
} from "animal-island-ui";
import { login, persistAuth, register, type AuthPayload } from "../lib/auth-api";
import { AUTH_REMEMBER_ME_PREF_KEY } from "../lib/auth-keys";

function readRememberMePref(): boolean {
  if (typeof window === "undefined") return true;
  return localStorage.getItem(AUTH_REMEMBER_ME_PREF_KEY) !== "0";
}

type Tab = "login" | "register";

type Props = {
  onSuccess: (payload: AuthPayload) => void;
};

function toast(msg: string, type: "success" | "warning" = "warning") {
  window.dispatchEvent(new CustomEvent("pawpal-toast", { detail: { msg, type } }));
}

export function LoginPage({ onSuccess }: Props) {
  const [tab, setTab] = useState<Tab>("login");
  const [error, setError] = useState<string | null>(null);

  const [loginEmail, setLoginEmail] = useState("");
  const [loginPassword, setLoginPassword] = useState("");
  const [loginPwVisible, setLoginPwVisible] = useState(false);
  const [loginLoading, setLoginLoading] = useState(false);

  const [regEmail, setRegEmail] = useState("");
  const [regInvite, setRegInvite] = useState("");
  const [regPassword, setRegPassword] = useState("");
  const [regNickname, setRegNickname] = useState("");
  const [regPwVisible, setRegPwVisible] = useState(false);
  const [regLoading, setRegLoading] = useState(false);
  const [rememberMe, setRememberMe] = useState(readRememberMePref);

  const updateRememberMe = useCallback((values: Array<string | number>) => {
    const checked = values.includes("remember");
    setRememberMe(checked);
    try {
      localStorage.setItem(AUTH_REMEMBER_ME_PREF_KEY, checked ? "1" : "0");
    } catch {
      /* ignore */
    }
  }, []);

  const switchTab = useCallback((t: Tab) => {
    setTab(t);
    setError(null);
  }, []);

  const onLogin = async () => {
    setError(null);
    const email = loginEmail.trim();
    if (!email) {
      setError("请输入邮箱");
      return;
    }
    if (!loginPassword) {
      setError("请输入密码");
      return;
    }
    setLoginLoading(true);
    try {
      const payload = await login(email, loginPassword);
      persistAuth(payload, rememberMe);
      onSuccess(payload);
      toast("欢迎回来", "success");
    } catch (e) {
      setError(e instanceof Error ? e.message : "登录失败");
    } finally {
      setLoginLoading(false);
    }
  };

  const onRegister = async () => {
    setError(null);
    const email = regEmail.trim();
    const inviteCode = regInvite.trim();
    const username = regNickname.trim();
    if (!email) {
      setError("请输入邮箱");
      return;
    }
    if (!inviteCode) {
      setError("请输入邀请码");
      return;
    }
    if (regPassword.length < 8) {
      setError("密码至少 8 位");
      return;
    }
    if (!username) {
      setError("请输入昵称");
      return;
    }
    if (username.length > 64) {
      setError("昵称最长 64 个字符");
      return;
    }
    setRegLoading(true);
    try {
      const payload = await register({
        email,
        inviteCode,
        password: regPassword,
        username,
      });
      persistAuth(payload, rememberMe);
      onSuccess(payload);
      toast("注册成功，欢迎使用 PawPal", "success");
    } catch (e) {
      setError(e instanceof Error ? e.message : "注册失败");
    } finally {
      setRegLoading(false);
    }
  };

  const rememberCheckbox = (
    <div className="login-form__row">
      <Checkbox
        size="small"
        options={[{ label: "记住我（关闭浏览器后保持登录）", value: "remember" }]}
        value={rememberMe ? ["remember"] : []}
        onChange={updateRememberMe}
      />
      {tab === "login" && (
        <span
          className="login-form__forgot"
          role="button"
          tabIndex={0}
          onClick={() => toast("请联系管理员重置密码")}
          onKeyDown={(e) => e.key === "Enter" && toast("请联系管理员重置密码")}
        >
          忘记密码？
        </span>
      )}
    </div>
  );

  const loginPanel = (
    <div className="login-form">
      <div className="login-form__field">
        <label className="login-form__label" htmlFor="loginEmail">
          邮箱
        </label>
        <Input
          id="loginEmail"
          type="email"
          autoComplete="username"
          placeholder="请输入邮箱"
          prefix={<span aria-hidden>✉️</span>}
          allowClear
          value={loginEmail}
          onChange={(e) => setLoginEmail(e.target.value)}
          onClear={() => setLoginEmail("")}
        />
      </div>

      <div className="login-form__field">
        <label className="login-form__label" htmlFor="loginPassword">
          密码
        </label>
        <Input
          id="loginPassword"
          type={loginPwVisible ? "text" : "password"}
          autoComplete="current-password"
          placeholder="请输入密码"
          prefix={<span aria-hidden>🔒</span>}
          suffix={
            <button
              type="button"
              className="login-form__eye"
              aria-label={loginPwVisible ? "隐藏密码" : "显示密码"}
              onClick={() => setLoginPwVisible((v) => !v)}
            >
              {loginPwVisible ? "🙈" : "👁"}
            </button>
          }
          value={loginPassword}
          onChange={(e) => setLoginPassword(e.target.value)}
        />
      </div>

      {rememberCheckbox}

      <Button
        type="primary"
        block
        size="large"
        loading={loginLoading}
        disabled={loginLoading}
        onClick={onLogin}
      >
        {loginLoading ? "登录中…" : "🐾 登录 PawPal"}
      </Button>

      <div className="login-form__divider">或使用第三方账号登录</div>
      <div className="login-form__social">
        <Button type="default" icon={<span aria-hidden>💬</span>} onClick={() => toast("微信登录开发中…")}>
          微信
        </Button>
        <Button type="default" icon={<span aria-hidden>🐧</span>} onClick={() => toast("QQ 登录开发中…")}>
          QQ
        </Button>
        <Button type="default" icon={<span aria-hidden>🍎</span>} onClick={() => toast("Apple 登录开发中…")}>
          Apple
        </Button>
      </div>

      <div className="login-form__terms">
        登录即代表同意
        <a
          role="button"
          tabIndex={0}
          onClick={() => toast("📄 服务条款", "warning")}
          onKeyDown={(e) => e.key === "Enter" && toast("📄 服务条款", "warning")}
        >
          《服务条款》
        </a>
        与
        <a
          role="button"
          tabIndex={0}
          onClick={() => toast("🔐 隐私政策", "warning")}
          onKeyDown={(e) => e.key === "Enter" && toast("🔐 隐私政策", "warning")}
        >
          《隐私政策》
        </a>
      </div>
    </div>
  );

  const registerPanel = (
    <div className="login-form">
      <div className="login-form__field">
        <label className="login-form__label" htmlFor="regEmail">
          邮箱
        </label>
        <Input
          id="regEmail"
          type="email"
          autoComplete="email"
          placeholder="用于登录与找回"
          prefix={<span aria-hidden>✉️</span>}
          allowClear
          value={regEmail}
          onChange={(e) => setRegEmail(e.target.value)}
          onClear={() => setRegEmail("")}
        />
      </div>

      <div className="login-form__field">
        <label className="login-form__label" htmlFor="regInvite">
          邀请码
        </label>
        <Input
          id="regInvite"
          type="text"
          autoComplete="off"
          placeholder="请输入有效邀请码"
          prefix={<span aria-hidden>🎟️</span>}
          allowClear
          value={regInvite}
          onChange={(e) => setRegInvite(e.target.value)}
          onClear={() => setRegInvite("")}
        />
      </div>

      <div className="login-form__field">
        <label className="login-form__label" htmlFor="regPassword">
          密码
        </label>
        <Input
          id="regPassword"
          type={regPwVisible ? "text" : "password"}
          autoComplete="new-password"
          placeholder="至少 8 位"
          prefix={<span aria-hidden>🔒</span>}
          suffix={
            <button
              type="button"
              className="login-form__eye"
              aria-label={regPwVisible ? "隐藏密码" : "显示密码"}
              onClick={() => setRegPwVisible((v) => !v)}
            >
              {regPwVisible ? "🙈" : "👁"}
            </button>
          }
          value={regPassword}
          onChange={(e) => setRegPassword(e.target.value)}
        />
      </div>

      <div className="login-form__field">
        <label className="login-form__label" htmlFor="regNickname">
          昵称
        </label>
        <Input
          id="regNickname"
          type="text"
          autoComplete="nickname"
          placeholder="在 PawPal 中显示的名称"
          maxLength={64}
          prefix={<span aria-hidden>😊</span>}
          allowClear
          value={regNickname}
          onChange={(e) => setRegNickname(e.target.value)}
          onClear={() => setRegNickname("")}
        />
      </div>

      {rememberCheckbox}

      <Button
        type="primary"
        block
        size="large"
        loading={regLoading}
        disabled={regLoading}
        onClick={onRegister}
      >
        {regLoading ? "提交中…" : "✨ 创建账号"}
      </Button>

      <div className="login-form__terms">
        注册即代表同意
        <a
          role="button"
          tabIndex={0}
          onClick={() => toast("📄 服务条款", "warning")}
          onKeyDown={(e) => e.key === "Enter" && toast("📄 服务条款", "warning")}
        >
          《服务条款》
        </a>
        与
        <a
          role="button"
          tabIndex={0}
          onClick={() => toast("🔐 隐私政策", "warning")}
          onKeyDown={(e) => e.key === "Enter" && toast("🔐 隐私政策", "warning")}
        >
          《隐私政策》
        </a>
      </div>
    </div>
  );

  const tabItems = useMemo<TabItem[]>(
    () => [
      { key: "login", label: "登录", children: loginPanel },
      { key: "register", label: "注册账号", children: registerPanel },
    ],
    // panels capture state via closures, so we depend on the inputs they read
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [
      tab,
      loginEmail,
      loginPassword,
      loginPwVisible,
      loginLoading,
      regEmail,
      regInvite,
      regPassword,
      regNickname,
      regPwVisible,
      regLoading,
      rememberMe,
    ]
  );

  return (
    <Cursor>
      <div className="login-page" id="loginPage">
        <div className="login-page__title">
          <Card type="title">
            <span className="login-brand-mark">
              <span className="login-brand-mark__paw" aria-hidden>
                🐾
              </span>
              PawPal
            </span>
            <span className="login-brand-mark__sub">你和宠物之间的智能小伙伴 🐶🐱</span>
          </Card>
        </div>

        <div className="login-page__card-wrap">
          {error && (
            <div className="login-page__error">
              <Card color="app-red">⚠️ {error}</Card>
            </div>
          )}

          <Card>
            <Tabs
              activeKey={tab}
              onChange={(key) => switchTab(key as Tab)}
              items={tabItems}
            />
          </Card>
        </div>

        <div className="login-page__footer">
          <Footer type="tree" />
        </div>
      </div>
    </Cursor>
  );
}
