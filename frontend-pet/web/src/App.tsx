import { useCallback, useEffect, useState, useRef, lazy, Suspense } from "react";
import { Card, Cursor } from "animal-island-ui";
import { AppShell } from "./components/AppShell";
import { LoginPage } from "./components/LoginPage";
import { logoutRemote, readStoredUser, type UserInfo } from "./lib/auth-api";
import { type Pet, showToast as emitToast } from "./lib/pet-types";

const AskAiPage = lazy(() =>
  import("./components/AskAiPage").then((m) => ({ default: m.AskAiPage }))
);
const PetProfilePage = lazy(() =>
  import("./components/PetProfilePage").then((m) => ({ default: m.PetProfilePage }))
);
const DietRecordPage = lazy(() =>
  import("./components/DietRecordPage").then((m) => ({ default: m.DietRecordPage }))
);

function PageFallback() {
  return (
    <div
      style={{
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        padding: "48px 24px",
        color: "var(--c-text-3)",
        fontSize: 14,
      }}
      role="status"
      aria-live="polite"
    >
      <span>🐾 加载中…</span>
    </div>
  );
}
import {
  listPets,
  createPet,
  updatePet,
  deletePet,
  petSummaryToFrontend,
  frontendPetToCreateRequest,
  frontendPetToUpdateRequest,
} from "./lib/pet-api";
import { setCurrentPetId, getCurrentPetId, clearCurrentPetId } from "./lib/pet-storage";

export type PageName = "chat" | "register" | "diet";

export default function App() {
  const [user, setUser] = useState<UserInfo | null>(() => readStoredUser());
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [currentPage, setCurrentPage] = useState<PageName>("chat");
  const [toast, setToast] = useState<{ msg: string; type: "success" | "warning" } | null>(null);
  const [pets, setPets] = useState<Pet[]>([]);
  const [_petsLoading, setPetsLoading] = useState(false);
  void _petsLoading;
  const petProfileRef = useRef<{ openAddModal: () => void } | null>(null);

  const activePet = pets.find((p) => p.isActive) || pets[0] || null;

  const onToast = useCallback((e: Event) => {
    const ce = e as CustomEvent<{ msg: string; type: "success" | "warning" }>;
    setToast(ce.detail);
  }, []);

  useEffect(() => {
    window.addEventListener("pawpal-toast", onToast);
    return () => window.removeEventListener("pawpal-toast", onToast);
  }, [onToast]);

  useEffect(() => {
    if (!toast) return;
    const t = window.setTimeout(() => setToast(null), 2800);
    return () => window.clearTimeout(t);
  }, [toast]);

  useEffect(() => {
    if (!user) return;
    setPetsLoading(true);
    listPets()
      .then((summaries) => {
        const storedPetId = getCurrentPetId();
        const loaded = summaries.map((s) => {
          const isActive = storedPetId ? s.id === storedPetId : false;
          return petSummaryToFrontend(s, isActive);
        });
        if (loaded.length > 0 && !loaded.some((p) => p.isActive)) {
          loaded[0].isActive = true;
          setCurrentPetId(loaded[0].id);
        }
        setPets(loaded);
      })
      .catch((e) => {
        console.error("Failed to load pets:", e);
        emitToast("加载宠物列表失败", "warning");
      })
      .finally(() => setPetsLoading(false));
  }, [user]);

  const handleLogout = useCallback(async () => {
    await logoutRemote();
    clearCurrentPetId();
    setPets([]);
    setUser(null);
    setCurrentPage("chat");
    setToast({ msg: "👋 已退出登录", type: "success" });
  }, []);

  const handlePageChange = useCallback((page: PageName) => {
    setCurrentPage(page);
    setSidebarOpen(false);
  }, []);

  const openSidebar = useCallback(() => {
    setSidebarOpen(true);
  }, []);

  const handleSwitchPet = useCallback((petId: number) => {
    setCurrentPetId(petId);
    setPets((prev) =>
      prev.map((p) => ({
        ...p,
        isActive: p.id === petId,
      }))
    );
  }, []);

  const handleAddPetFromSwitcher = useCallback(() => {
    setCurrentPage("register");
    setSidebarOpen(false);
    setTimeout(() => {
      petProfileRef.current?.openAddModal();
    }, 100);
  }, []);

  const handlePetsChange = useCallback(
    async (newPets: Pet[], changedPet?: Pet, action?: "add" | "update" | "delete") => {
      if (!action || !changedPet) {
        setPets(newPets);
        return;
      }

      try {
        if (action === "add") {
          const created = await createPet(frontendPetToCreateRequest(changedPet));
          const frontendPet: Pet = {
            ...changedPet,
            id: created.id,
          };
          setPets((prev) => {
            const filtered = prev.filter((p) => p.id !== changedPet.id);
            return [...filtered, frontendPet];
          });
        } else if (action === "update") {
          await updatePet(changedPet.id, frontendPetToUpdateRequest(changedPet));
          setPets(newPets);
        } else if (action === "delete") {
          await deletePet(changedPet.id);
          setPets(newPets);
        }
      } catch (e) {
        console.error(`Failed to ${action} pet:`, e);
        emitToast(e instanceof Error ? e.message : `操作失败`, "warning");
      }
    },
    []
  );

  if (!user) {
    return (
      <>
        <LoginPage onSuccess={(payload) => setUser(payload.user)} />
        <div className="toast-wrap" aria-live="polite">
          {toast && (
            <div className="toast show" role="status">
              <Card color={toast.type === "success" ? "app-green" : "app-yellow"}>
                {toast.msg}
              </Card>
            </div>
          )}
        </div>
      </>
    );
  }

  const renderPage = () => {
    switch (currentPage) {
      case "chat":
        return (
          <AskAiPage
            onMenuClick={openSidebar}
            pets={pets}
            activePet={activePet}
            onSwitchPet={handleSwitchPet}
            onAddPet={handleAddPetFromSwitcher}
          />
        );
      case "register":
        return (
          <PetProfilePage
            ref={petProfileRef}
            onMenuClick={openSidebar}
            pets={pets}
            activePet={activePet}
            onPetsChange={handlePetsChange}
            onSwitchPet={handleSwitchPet}
            onAddPet={handleAddPetFromSwitcher}
          />
        );
      case "diet":
        return (
          <DietRecordPage
            onMenuClick={openSidebar}
            pets={pets}
            activePet={activePet}
            onSwitchPet={handleSwitchPet}
            onAddPet={handleAddPetFromSwitcher}
          />
        );
      default:
        return (
          <AskAiPage
            onMenuClick={openSidebar}
            pets={pets}
            activePet={activePet}
            onSwitchPet={handleSwitchPet}
            onAddPet={handleAddPetFromSwitcher}
          />
        );
    }
  };

  return (
    <Cursor>
      <AppShell
        sidebarOpen={sidebarOpen}
        onCloseSidebar={() => setSidebarOpen(false)}
        userDisplayName={user.username || user.email || "用户"}
        onLogout={handleLogout}
        currentPage={currentPage}
        onPageChange={handlePageChange}
        petCount={pets.length}
      >
        <Suspense fallback={<PageFallback />}>{renderPage()}</Suspense>
      </AppShell>
      <div className="toast-wrap" aria-live="polite">
        {toast && (
          <div className="toast show" role="status">
            <Card color={toast.type === "success" ? "app-green" : "app-yellow"}>
              {toast.msg}
            </Card>
          </div>
        )}
      </div>
    </Cursor>
  );
}
