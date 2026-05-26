import { useCallback, useState } from "react";
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

  const updateRememberMe = useCallback((checked: boolean) => {
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

  return (
    <div className="login-page" id="loginPage">
      <div className="login-bg-deco" aria-hidden>
        <div className="deco-circle c1" />
        <div className="deco-circle c2" />
        <div className="deco-circle c3" />
        <div className="deco-paw p1">🐾</div>
        <div className="deco-paw p2">🐾</div>
        <div className="deco-paw p3">🐾</div>
        <div className="deco-paw p4">🐾</div>
        <div className="deco-paw p5">🐾</div>
      </div>

      <div className="login-card">
        <div className="login-brand">
          <div className="login-brand-icon">🐾</div>
          <div className="login-brand-name">
            Paw<span>Pal</span>
          </div>
          <div className="login-brand-sub">你和宠物之间的智能小伙伴 🐶🐱</div>
        </div>

        <div className="login-tabs" role="tablist">
          <button
            type="button"
            role="tab"
            className={`login-tab${tab === "login" ? " active" : ""}`}
            onClick={() => switchTab("login")}
            aria-selected={tab === "login"}
          >
            登录
          </button>
          <button
            type="button"
            role="tab"
            className={`login-tab${tab === "register" ? " active" : ""}`}
            onClick={() => switchTab("register")}
            aria-selected={tab === "register"}
          >
            注册账号
          </button>
        </div>

        {error && (
          <div className="lf-error show" role="alert">
            <span>⚠️</span>
            <span>{error}</span>
          </div>
        )}

        {tab === "login" && (
          <div className="login-panel active" id="panelLogin">
            <div className="lf-group">
              <label className="lf-label" htmlFor="loginEmail">
                邮箱
              </label>
              <div className="lf-input-wrap">
                <span className="lf-input-icon">✉️</span>
                <input
                  id="loginEmail"
                  className="lf-input"
                  type="email"
                  autoComplete="username"
                  placeholder="请输入邮箱"
                  value={loginEmail}
                  onChange={(e) => setLoginEmail(e.target.value)}
                />
              </div>
            </div>
            <div className="lf-group">
              <label className="lf-label" htmlFor="loginPassword">
                密码
              </label>
              <div className="lf-input-wrap">
                <span className="lf-input-icon">🔒</span>
                <input
                  id="loginPassword"
                  className="lf-input"
                  type={loginPwVisible ? "text" : "password"}
                  autoComplete="current-password"
                  placeholder="请输入密码"
                  value={loginPassword}
                  onChange={(e) => setLoginPassword(e.target.value)}
                />
                <button
                  type="button"
                  className="lf-eye-btn"
                  aria-label={loginPwVisible ? "隐藏密码" : "显示密码"}
                  onClick={() => setLoginPwVisible((v) => !v)}
                >
                  👁
                </button>
              </div>
            </div>

            <div className="lf-row">
              <label className="lf-remember">
                <input
                  className="lf-checkbox"
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(e) => updateRememberMe(e.target.checked)}
                />
                记住我（关闭浏览器后保持登录）
              </label>
              <span
                className="lf-forgot"
                role="button"
                tabIndex={0}
                onClick={() => toast("请联系管理员重置密码")}
                onKeyDown={(e) => e.key === "Enter" && toast("请联系管理员重置密码")}
              >
                忘记密码？
              </span>
            </div>

            <button
              type="button"
              className={`login-btn${loginLoading ? " loading" : ""}`}
              onClick={onLogin}
              disabled={loginLoading}
            >
              <span>{loginLoading ? "登录中…" : "🐾 登录 PawPal"}</span>
            </button>

            <div className="lf-divider">或使用第三方账号登录</div>

            <div className="lf-social">
              <button type="button" className="lf-social-btn" onClick={() => toast("微信登录开发中…")}>
                <span className="lf-social-icon">💬</span>微信
              </button>
              <button type="button" className="lf-social-btn" onClick={() => toast("QQ 登录开发中…")}>
                <span className="lf-social-icon">🐧</span>QQ
              </button>
              <button type="button" className="lf-social-btn" onClick={() => toast("Apple 登录开发中…")}>
                <span className="lf-social-icon">🍎</span>Apple
              </button>
            </div>

            <div className="lf-terms">
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
        )}

        {tab === "register" && (
          <div className="login-panel active" id="panelRegister">
            <div className="lf-group">
              <label className="lf-label" htmlFor="regEmail">
                邮箱
              </label>
              <div className="lf-input-wrap">
                <span className="lf-input-icon">✉️</span>
                <input
                  id="regEmail"
                  className="lf-input"
                  type="email"
                  autoComplete="email"
                  placeholder="用于登录与找回"
                  value={regEmail}
                  onChange={(e) => setRegEmail(e.target.value)}
                />
              </div>
            </div>
            <div className="lf-group">
              <label className="lf-label" htmlFor="regInvite">
                邀请码
              </label>
              <div className="lf-input-wrap">
                <span className="lf-input-icon">🎟️</span>
                <input
                  id="regInvite"
                  className="lf-input"
                  type="text"
                  autoComplete="off"
                  placeholder="请输入有效邀请码"
                  value={regInvite}
                  onChange={(e) => setRegInvite(e.target.value)}
                />
              </div>
            </div>
            <div className="lf-group">
              <label className="lf-label" htmlFor="regPassword">
                密码
              </label>
              <div className="lf-input-wrap">
                <span className="lf-input-icon">🔒</span>
                <input
                  id="regPassword"
                  className="lf-input"
                  type={regPwVisible ? "text" : "password"}
                  autoComplete="new-password"
                  placeholder="至少 8 位"
                  value={regPassword}
                  onChange={(e) => setRegPassword(e.target.value)}
                />
                <button
                  type="button"
                  className="lf-eye-btn"
                  aria-label={regPwVisible ? "隐藏密码" : "显示密码"}
                  onClick={() => setRegPwVisible((v) => !v)}
                >
                  👁
                </button>
              </div>
            </div>
            <div className="lf-group">
              <label className="lf-label" htmlFor="regNickname">
                昵称
              </label>
              <div className="lf-input-wrap">
                <span className="lf-input-icon">😊</span>
                <input
                  id="regNickname"
                  className="lf-input"
                  type="text"
                  autoComplete="nickname"
                  placeholder="在 PawPal 中显示的名称"
                  maxLength={64}
                  value={regNickname}
                  onChange={(e) => setRegNickname(e.target.value)}
                />
              </div>
            </div>

            <div className="lf-row" style={{ marginTop: 4 }}>
              <label className="lf-remember">
                <input
                  className="lf-checkbox"
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(e) => updateRememberMe(e.target.checked)}
                />
                记住我（关闭浏览器后保持登录）
              </label>
              <span />
            </div>

            <button
              type="button"
              className={`login-btn register-submit${regLoading ? " loading" : ""}`}
              onClick={onRegister}
              disabled={regLoading}
            >
              <span>{regLoading ? "提交中…" : "✨ 创建账号"}</span>
            </button>

            <div className="lf-terms">
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
        )}
      </div>
    </div>
  );
}
