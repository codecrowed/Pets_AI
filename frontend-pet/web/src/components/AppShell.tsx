import type { ReactNode } from "react";
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
          <div className="logo-icon">🐾</div>
          <div className="logo-text">
            PawPal<span>宠物AI助手</span>
          </div>
        </div>

        <div className="sidebar-section-label">主功能</div>
        <nav>
          <button
            type="button"
            className={`nav-item${currentPage === "chat" ? " active" : ""}`}
            onClick={() => onPageChange("chat")}
          >
            <span className="nav-icon">🤖</span>
            问AI助手
          </button>
          <button
            type="button"
            className={`nav-item${currentPage === "register" ? " active" : ""}`}
            onClick={() => onPageChange("register")}
          >
            <span className="nav-icon">🐶</span>
            宠物档案
          </button>
          <button
            type="button"
            className={`nav-item${currentPage === "diet" ? " active" : ""}`}
            onClick={() => onPageChange("diet")}
          >
            <span className="nav-icon">🍖</span>
            饮食记录
          </button>
        </nav>

        <div className="sidebar-section-label">健康中心</div>
        <nav>
          <button type="button" className="nav-item" onClick={() => stubToast("健康日历开发中…")}>
            <span className="nav-icon">📅</span>
            健康日历
          </button>
          <button type="button" className="nav-item" onClick={() => stubToast("疫苗提醒开发中…")}>
            <span className="nav-icon">💉</span>
            疫苗提醒
            <span className="nav-badge">3天</span>
          </button>
          <button type="button" className="nav-item" onClick={() => stubToast("体重趋势开发中…")}>
            <span className="nav-icon">📊</span>
            体重趋势
          </button>
        </nav>

        <div className="sidebar-bottom">
          <button type="button" className="user-card" onClick={onLogout} title="退出登录">
            <div className="user-avatar">😊</div>
            <div>
              <div className="user-name">{userDisplayName}</div>
              <div className="user-role">已养 {petCount} 只宠物</div>
            </div>
          </button>
        </div>
      </aside>

      <div className="main">{children}</div>
    </>
  );
}

function stubToast(msg: string) {
  window.dispatchEvent(new CustomEvent("pawpal-toast", { detail: { msg, type: "warning" as const } }));
}
