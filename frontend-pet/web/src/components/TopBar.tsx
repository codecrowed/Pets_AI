import { useState, useCallback, useRef, useEffect } from "react";
import { Button } from "animal-island-ui";
import type { Pet } from "../lib/pet-types";
import { showToast } from "../lib/pet-types";

type TopBarProps = {
  title: string;
  subtitle?: string;
  onMenuClick: () => void;
  pets: Pet[];
  activePet: Pet | null;
  onSwitchPet: (petId: number) => void;
  onAddPet: () => void;
};

export function TopBar({
  title,
  subtitle,
  onMenuClick,
  pets,
  activePet,
  onSwitchPet,
  onAddPet,
}: TopBarProps) {
  const [showPetSwitcher, setShowPetSwitcher] = useState(false);
  const petSwitcherRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (petSwitcherRef.current && !petSwitcherRef.current.contains(e.target as Node)) {
        setShowPetSwitcher(false);
      }
    };
    if (showPetSwitcher) {
      document.addEventListener("mousedown", handleClickOutside);
    }
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, [showPetSwitcher]);

  const handleSwitchPet = useCallback(
    (petId: number) => {
      onSwitchPet(petId);
      setShowPetSwitcher(false);
      const selectedPet = pets.find((p) => p.id === petId);
      if (selectedPet) {
        showToast(`✅ 已切换到 ${selectedPet.name}`, "success");
      }
    },
    [pets, onSwitchPet]
  );

  const handleAddPet = useCallback(() => {
    setShowPetSwitcher(false);
    onAddPet();
  }, [onAddPet]);

  return (
    <header className="topbar">
      <Button
        type="text"
        size="small"
        className="topbar-menu-btn"
        onClick={onMenuClick}
        aria-label="打开侧边栏"
      >
        ☰
      </Button>

      <div className="topbar-title">
        {title} {subtitle && <span>{subtitle}</span>}
      </div>

      <div className="topbar-actions">
        <div className="pet-switcher-container" ref={petSwitcherRef}>
          <button
            type="button"
            className="pet-quick-select"
            onClick={() => setShowPetSwitcher((prev) => !prev)}
            aria-haspopup="listbox"
            aria-expanded={showPetSwitcher}
          >
            <div className="pqs-avatar" aria-hidden>
              {activePet?.emoji === "AI" ? "🤖" : activePet?.emoji || "🐕"}
            </div>
            <span className="pqs-name">{activePet?.name || "选择宠物"}</span>
            <span className="pqs-arrow" aria-hidden>
              {showPetSwitcher ? "▴" : "▾"}
            </span>
          </button>

          {showPetSwitcher && (
            <div className="pet-switcher-dropdown" role="listbox">
              <div className="psd-header">切换当前宠物</div>
              {pets.map((pet) => (
                <button
                  key={pet.id}
                  type="button"
                  role="option"
                  aria-selected={pet.isActive}
                  className={`psd-item${pet.isActive ? " active" : ""}`}
                  onClick={() => handleSwitchPet(pet.id)}
                >
                  <span className="psd-avatar" aria-hidden>
                    {pet.emoji === "AI" ? "🤖" : pet.emoji}
                  </span>
                  <div className="psd-info">
                    <span className="psd-name">{pet.name}</span>
                    <span className="psd-breed">{pet.breed || "未设置品种"}</span>
                  </div>
                  {pet.isActive && (
                    <span className="psd-check" aria-hidden>
                      ✓
                    </span>
                  )}
                </button>
              ))}
              <div className="psd-divider" />
              <button
                type="button"
                className="psd-item psd-add"
                onClick={handleAddPet}
              >
                <span className="psd-avatar" aria-hidden>
                  ＋
                </span>
                <span className="psd-name">添加新宠物</span>
              </button>
            </div>
          )}
        </div>

        <Button
          type="text"
          size="small"
          className="topbar-btn"
          onClick={() => showToast("🔔 暂无新通知", "success")}
          aria-label="通知"
          title="通知"
        >
          🔔
        </Button>
      </div>
    </header>
  );
}
