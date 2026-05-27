import type { ReactNode } from "react";
import { Button } from "animal-island-ui";
import type { PageName } from "../App";

type Props = {
  children: ReactNode;
  sidebarOpen: boolean;
  onCloseSidebar: () => void;
  userDisplayName: string;
  onLogout: () => void;
  currentPage: PageName;
  onPageChange: (page: PageName) => void;
  petCount?: number;
};

type NavEntry = {
  page: PageName;
  icon: string;
  label: string;
};

const MAIN_NAV: NavEntry[] = [
  { page: "chat", icon: "🤖", label: "问AI助手" },
  { page: "register", icon: "🐶", label: "宠物档案" },
  { page: "diet", icon: "🍖", label: "饮食记录" },
];

export function AppShell({
  children,
  sidebarOpen,
  onCloseSidebar,
  userDisplayName,
  onLogout,
  currentPage,
  onPageChange,
  petCount = 0,
}: Props) {
  return (
    <>
      <div
        className={`sidebar-overlay${sidebarOpen ? " open" : ""}`}
        onClick={onCloseSidebar}
        aria-hidden
      />
      <aside className={`sidebar${sidebarOpen ? " open" : ""}`} id="sidebar">
        <div className="sidebar-logo">
          <div className="logo-icon" aria-hidden>
            🐾
          </div>
          <div className="logo-text">
            PawPal<span>宠物AI助手</span>
          </div>
        </div>

        <div className="sidebar-section-label">主功能</div>
        <nav>
          {MAIN_NAV.map((entry) => (
            <button
              key={entry.page}
              type="button"
              className={`nav-item${currentPage === entry.page ? " active" : ""}`}
              onClick={() => onPageChange(entry.page)}
            >
              <span className="nav-icon" aria-hidden>
                {entry.icon}
              </span>
              {entry.label}
            </button>
          ))}
        </nav>

        <div className="sidebar-section-label">健康中心</div>
        <nav>
          <button type="button" className="nav-item" onClick={() => stubToast("健康日历开发中…")}>
            <span className="nav-icon" aria-hidden>
              📅
            </span>
            健康日历
          </button>
          <button type="button" className="nav-item" onClick={() => stubToast("疫苗提醒开发中…")}>
            <span className="nav-icon" aria-hidden>
              💉
            </span>
            疫苗提醒
            <span className="nav-badge">3天</span>
          </button>
          <button type="button" className="nav-item" onClick={() => stubToast("体重趋势开发中…")}>
            <span className="nav-icon" aria-hidden>
              📊
            </span>
            体重趋势
          </button>
        </nav>

        <div className="sidebar-bottom">
          <div className="user-card" title={`已登录：${userDisplayName}`}>
            <div className="user-avatar" aria-hidden>
              😊
            </div>
            <div className="user-card-body">
              <div className="user-name">{userDisplayName}</div>
              <div className="user-role">已养 {petCount} 只宠物</div>
            </div>
            <Button
              type="text"
              size="small"
              className="user-card-logout"
              onClick={onLogout}
              aria-label="退出登录"
              title="退出登录"
            >
              ⏻
            </Button>
          </div>
        </div>
      </aside>

      <div className="main">{children}</div>
    </>
  );
}

function stubToast(msg: string) {
  window.dispatchEvent(
    new CustomEvent("pawpal-toast", { detail: { msg, type: "warning" as const } })
  );
}
