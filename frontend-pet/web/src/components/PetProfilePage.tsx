import {
  useState,
  useCallback,
  useRef,
  useEffect,
  useImperativeHandle,
  forwardRef,
  type ChangeEvent,
} from "react";
import { Button, Card, Input, Modal, Select } from "animal-island-ui";
import { TopBar } from "./TopBar";
import { type Pet, createEmptyPet, showToast } from "../lib/pet-types";
import { uploadPetAvatar } from "../lib/pet-api";

const MAX_FILE_SIZE = 5 * 1024 * 1024;

type PetProfilePageProps = {
  onMenuClick: () => void;
  pets: Pet[];
  activePet: Pet | null;
  onPetsChange: (pets: Pet[], changedPet?: Pet, action?: "add" | "update" | "delete") => void;
  onSwitchPet: (petId: number) => void;
  onAddPet: () => void;
};

export type PetProfilePageRef = {
  openAddModal: () => void;
};

const dogEmojis = ["🐕", "🐶", "🦮", "🐕‍🦺"];
const catEmojis = ["🐈", "🐱", "🐈‍⬛", "😺"];

const petTypeOptions = [
  { key: "dog", label: "🐕 狗狗" },
  { key: "cat", label: "🐈 猫咪" },
];

const dogBreeds = [
  "柴犬",
  "金毛寻回犬",
  "拉布拉多",
  "德国牧羊犬",
  "边境牧羊犬",
  "哈士奇",
  "萨摩耶",
  "阿拉斯加雪橇犬",
  "泰迪/贵宾犬",
  "博美犬",
  "比熊犬",
  "法国斗牛犬",
  "英国斗牛犬",
  "柯基犬",
  "雪纳瑞",
  "约克夏梗",
  "吉娃娃",
  "马尔济斯",
  "西施犬",
  "松狮犬",
  "秋田犬",
  "罗威纳",
  "杜宾犬",
  "大丹犬",
  "圣伯纳犬",
  "伯恩山犬",
  "可卡犬",
  "比格犬",
  "巴哥犬",
  "中华田园犬",
  "其他品种",
];

const catBreeds = [
  "英短蓝猫",
  "美国短毛猫",
  "布偶猫",
  "暹罗猫",
  "波斯猫",
  "缅因猫",
  "苏格兰折耳猫",
  "俄罗斯蓝猫",
  "孟买猫",
  "埃及猫",
  "阿比西尼亚猫",
  "挪威森林猫",
  "伯曼猫",
  "东方短毛猫",
  "德文卷毛猫",
  "斯芬克斯猫",
  "金吉拉",
  "加菲猫/异国短毛猫",
  "曼基康矮脚猫",
  "孟加拉豹猫",
  "缅甸猫",
  "土耳其安哥拉猫",
  "索马里猫",
  "日本短尾猫",
  "中华田园猫",
  "橘猫",
  "狸花猫",
  "三花猫",
  "奶牛猫",
  "其他品种",
];

const PET_CARD_PALETTE = ["app-yellow", "app-pink", "app-teal", "app-blue", "app-orange"] as const;
type PetCardColor = (typeof PET_CARD_PALETTE)[number];

function pickPetCardColor(petId: number, isActive: boolean): PetCardColor {
  if (isActive) return "app-yellow";
  const idx = Math.abs(petId) % (PET_CARD_PALETTE.length - 1);
  return PET_CARD_PALETTE[idx + 1];
}

function calculateAge(birthday: string): string {
  const birth = new Date(birthday);
  const now = new Date();
  const years = now.getFullYear() - birth.getFullYear();
  const months = now.getMonth() - birth.getMonth();
  const totalMonths = years * 12 + months;
  if (totalMonths < 1) return "不足1月";
  if (totalMonths < 12) return `${totalMonths}月`;
  const y = Math.floor(totalMonths / 12);
  const m = totalMonths % 12;
  return m > 0 ? `${y}岁${m}月` : `${y}岁`;
}

function hasFormChanges(current: Pet | null, original: Pet | null): boolean {
  if (!current || !original) return false;
  return JSON.stringify(current) !== JSON.stringify(original);
}

export const PetProfilePage = forwardRef<PetProfilePageRef, PetProfilePageProps>(function PetProfilePage(
  { onMenuClick, pets, activePet, onPetsChange, onSwitchPet, onAddPet: _onAddPet },
  ref
) {
  void _onAddPet;
  const [editingPet, setEditingPet] = useState<Pet | null>(null);
  const [originalPet, setOriginalPet] = useState<Pet | null>(null);
  const [isAddMode, setIsAddMode] = useState(false);
  const [selectedEmoji, setSelectedEmoji] = useState("🐕");
  const [useAiAvatar, setUseAiAvatar] = useState(false);
  const [showModal, setShowModal] = useState(false);
  const [showConfirmDialog, setShowConfirmDialog] = useState(false);
  const [isCustomBreed, setIsCustomBreed] = useState(false);
  const [customBreedInput, setCustomBreedInput] = useState("");
  const [avatarPreview, setAvatarPreview] = useState<string | null>(null);
  const [uploadingAvatar, setUploadingAvatar] = useState(false);
  const [, setPendingAvatarFile] = useState<File | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const originalEmojiRef = useRef("🐕");

  const currentEmojiOptions = editingPet?.type === "cat" ? catEmojis : dogEmojis;
  const currentBreedOptions = editingPet?.type === "cat" ? catBreeds : dogBreeds;

  const openAddModal = useCallback(() => {
    setIsAddMode(true);
    const newPet = createEmptyPet();
    setEditingPet(newPet);
    setOriginalPet(null);
    setSelectedEmoji("🐕");
    setUseAiAvatar(false);
    setAvatarPreview(null);
    setPendingAvatarFile(null);
    setIsCustomBreed(false);
    setCustomBreedInput("");
    originalEmojiRef.current = "🐕";
    setShowModal(true);
  }, []);

  useImperativeHandle(
    ref,
    () => ({
      openAddModal,
    }),
    [openAddModal]
  );

  const hasChanges = useCallback(() => {
    if (!editingPet) return false;
    if (isAddMode) {
      return (
        editingPet.name !== "" ||
        editingPet.breed !== "" ||
        editingPet.birthday !== "" ||
        editingPet.weight !== 0 ||
        editingPet.allergies !== "" ||
        editingPet.diseases !== "" ||
        editingPet.foodBrand !== "" ||
        editingPet.hospital !== "" ||
        editingPet.notes !== "" ||
        selectedEmoji !== "🐕" ||
        useAiAvatar
      );
    }
    const currentWithEmoji = { ...editingPet, emoji: useAiAvatar ? "AI" : selectedEmoji };
    return (
      hasFormChanges(currentWithEmoji, originalPet) ||
      selectedEmoji !== originalEmojiRef.current ||
      useAiAvatar
    );
  }, [editingPet, originalPet, isAddMode, selectedEmoji, useAiAvatar]);

  const openPetModal = useCallback(
    (idx: number) => {
      if (idx === -1) {
        openAddModal();
      } else {
        setIsAddMode(false);
        const pet = pets[idx];
        setEditingPet({ ...pet });
        setOriginalPet({ ...pet });
        setSelectedEmoji(pet.emoji === "AI" ? (pet.type === "cat" ? "🐈" : "🐕") : pet.emoji);
        setUseAiAvatar(pet.emoji === "AI");
        setAvatarPreview(pet.avatarUrl || null);
        const breedOptions = pet.type === "cat" ? catBreeds : dogBreeds;
        const isCustom = !!pet.breed && !breedOptions.includes(pet.breed);
        setIsCustomBreed(isCustom);
        setCustomBreedInput(isCustom ? pet.breed : "");
        originalEmojiRef.current = pet.emoji;
        setShowModal(true);
      }
    },
    [pets, openAddModal]
  );

  const resetModalState = useCallback(() => {
    setShowModal(false);
    setEditingPet(null);
    setOriginalPet(null);
    setIsAddMode(false);
    setUseAiAvatar(false);
    setAvatarPreview(null);
    setPendingAvatarFile(null);
    setIsCustomBreed(false);
    setCustomBreedInput("");
  }, []);

  const requestCloseModal = useCallback(() => {
    if (hasChanges()) {
      setShowConfirmDialog(true);
    } else {
      resetModalState();
    }
  }, [hasChanges, resetModalState]);

  const confirmCloseModal = useCallback(() => {
    setShowConfirmDialog(false);
    resetModalState();
    showToast("已放弃编辑", "warning");
  }, [resetModalState]);

  const cancelCloseModal = useCallback(() => {
    setShowConfirmDialog(false);
  }, []);

  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === "Escape" && showModal && !showConfirmDialog) {
        requestCloseModal();
      }
    };
    window.addEventListener("keydown", handleEscape);
    return () => window.removeEventListener("keydown", handleEscape);
  }, [showModal, showConfirmDialog, requestCloseModal]);

  const savePet = useCallback(() => {
    if (!editingPet) return;
    if (!editingPet.name.trim()) {
      showToast("请输入宠物昵称", "warning");
      return;
    }
    if (!editingPet.birthday) {
      showToast("请选择宠物生日", "warning");
      return;
    }
    if (isCustomBreed && !customBreedInput.trim()) {
      showToast("请输入宠物品种", "warning");
      return;
    }

    const finalEmoji = useAiAvatar ? "AI" : selectedEmoji;
    const finalBreed = isCustomBreed ? customBreedInput.trim() : editingPet.breed;
    const updatedPet = { ...editingPet, emoji: finalEmoji, breed: finalBreed };

    if (isAddMode) {
      onPetsChange([...pets, updatedPet], updatedPet, "add");
      if (useAiAvatar) {
        showToast(`✅ ${updatedPet.name} 已添加！AI正在生成头像…`, "success");
      } else {
        showToast(`✅ ${updatedPet.name} 已添加到档案！`, "success");
      }
    } else {
      onPetsChange(
        pets.map((p) => (p.id === updatedPet.id ? updatedPet : p)),
        updatedPet,
        "update"
      );
      showToast(`✅ ${updatedPet.name} 的档案已保存！`, "success");
    }
    resetModalState();
  }, [
    editingPet,
    selectedEmoji,
    isAddMode,
    useAiAvatar,
    isCustomBreed,
    customBreedInput,
    pets,
    onPetsChange,
    resetModalState,
  ]);

  const deletePet = useCallback(() => {
    if (!editingPet) return;
    if (window.confirm(`确定要删除 ${editingPet.name} 的档案吗？`)) {
      onPetsChange(
        pets.filter((p) => p.id !== editingPet.id),
        editingPet,
        "delete"
      );
      showToast(`🗑️ ${editingPet.name} 的档案已删除`, "success");
      resetModalState();
    }
  }, [editingPet, pets, onPetsChange, resetModalState]);

  const updateField = useCallback(<K extends keyof Pet>(field: K, value: Pet[K]) => {
    setEditingPet((prev) => (prev ? { ...prev, [field]: value } : null));
  }, []);

  const handleTypeChange = useCallback(
    (newType: string) => {
      updateField("type", newType);
      updateField("breed", "");
      setIsCustomBreed(false);
      setCustomBreedInput("");
      if (!useAiAvatar) {
        setSelectedEmoji(newType === "cat" ? "🐈" : "🐕");
      }
    },
    [updateField, useAiAvatar]
  );

  const handleBreedChange = useCallback(
    (value: string) => {
      if (value === "其他品种") {
        setIsCustomBreed(true);
        setCustomBreedInput("");
        updateField("breed", "");
      } else {
        setIsCustomBreed(false);
        setCustomBreedInput("");
        updateField("breed", value);
      }
    },
    [updateField]
  );

  const handleSelectAiAvatar = useCallback(() => {
    setUseAiAvatar(true);
    showToast("🤖 保存后将使用AI生成专属头像", "success");
  }, []);

  const handleSelectEmoji = useCallback(
    (emoji: string) => {
      setUseAiAvatar(false);
      setAvatarPreview(null);
      setPendingAvatarFile(null);
      updateField("avatarUrl", undefined);
      setSelectedEmoji(emoji);
    },
    [updateField]
  );

  const handleAvatarUpload = useCallback(
    async (e: ChangeEvent<HTMLInputElement>) => {
      const file = e.target.files?.[0];
      if (!file) return;

      const allowedTypes = ["image/jpeg", "image/jpg", "image/png"];
      if (!allowedTypes.includes(file.type)) {
        showToast("仅支持 JPG 或 PNG 格式图片", "warning");
        return;
      }

      if (file.size > MAX_FILE_SIZE) {
        showToast("图片大小不能超过5MB", "warning");
        return;
      }

      setUploadingAvatar(true);

      const reader = new FileReader();
      reader.onload = async (event) => {
        const dataUrl = event.target?.result as string;
        setAvatarPreview(dataUrl);
        setUseAiAvatar(false);

        if (editingPet && !isAddMode && editingPet.id) {
          try {
            const result = await uploadPetAvatar(editingPet.id, file);
            updateField("avatarUrl", result.avatarUrl);
            showToast("✅ 头像上传成功", "success");
          } catch (err) {
            console.error("Failed to upload avatar", err);
            showToast("头像上传失败，请重试", "warning");
          }
        } else {
          setPendingAvatarFile(file);
          showToast("✅ 头像已选择，保存后将上传", "success");
        }
        setUploadingAvatar(false);
      };
      reader.onerror = () => {
        showToast("图片读取失败，请重试", "warning");
        setUploadingAvatar(false);
      };
      reader.readAsDataURL(file);

      if (fileInputRef.current) {
        fileInputRef.current.value = "";
      }
    },
    [editingPet, isAddMode, updateField]
  );

  const triggerFileUpload = useCallback(() => {
    fileInputRef.current?.click();
  }, []);

  const removeAvatar = useCallback(() => {
    setAvatarPreview(null);
    setPendingAvatarFile(null);
    updateField("avatarUrl", undefined);
    showToast("已移除头像", "success");
  }, [updateField]);

  return (
    <>
      <TopBar
        title="宠物档案"
        subtitle="管理你的宠物信息"
        onMenuClick={onMenuClick}
        pets={pets}
        activePet={activePet}
        onSwitchPet={onSwitchPet}
        onAddPet={openAddModal}
      />

      <div className="page-content">
        <div className="pets-grid">
          {pets.map((pet, idx) => (
            <Card
              key={pet.id}
              color={pickPetCardColor(pet.id, !!pet.isActive)}
              className="pet-card"
              onClick={() => openPetModal(idx)}
            >
              {pet.isActive && <div className="pc-active-badge">当前</div>}
              <div
                className={`pc-avatar${pet.emoji === "AI" ? " ai-generated" : ""}${
                  pet.avatarUrl ? " has-image" : ""
                }`}
              >
                {pet.avatarUrl ? (
                  <img src={pet.avatarUrl} alt={pet.name} className="pc-avatar-img" />
                ) : pet.emoji === "AI" ? (
                  <span className="pc-ai-avatar">🤖</span>
                ) : (
                  pet.emoji
                )}
              </div>
              <div className="pc-name">{pet.name}</div>
              <div className="pc-breed">{pet.breed || "未设置品种"}</div>
              <div className="pc-tags">
                <span className="pc-tag">{pet.gender === "male" ? "公" : "母"}</span>
                {pet.sterilized && <span className="pc-tag">已绝育</span>}
                {pet.birthday && <span className="pc-tag">{calculateAge(pet.birthday)}</span>}
              </div>
            </Card>
          ))}
          <Card
            type="dashed"
            className="pet-card add-new"
            onClick={() => openPetModal(-1)}
          >
            <div className="add-new-icon" aria-hidden>
              ＋
            </div>
            <div className="add-new-text">添加宠物</div>
          </Card>
        </div>
      </div>

      {showModal && editingPet && (
        <Modal
          open={showModal}
          onClose={requestCloseModal}
          title={isAddMode ? "➕ 添加新宠物" : "✏️ 编辑宠物信息"}
          width="min(820px, 96vw)"
          maskClosable
          typewriter={false}
          footer={
            <div className="pet-modal-footer">
              <Button type="primary" onClick={savePet}>
                ✓ 保存档案
              </Button>
              <Button type="default" onClick={requestCloseModal}>
                取消
              </Button>
              {!isAddMode && (
                <Button type="default" danger onClick={deletePet} style={{ marginLeft: "auto" }}>
                  删除档案
                </Button>
              )}
            </div>
          }
        >
          <div className="pet-form-modal-body">
            <Card>
              <div className="form-card-header">
                <span className="fch-icon" aria-hidden>
                  📋
                </span>
                <span className="fch-title">基础信息</span>
                <span className="fch-sub">* 必填项</span>
              </div>
              <div className="form-body">
                <div className="avatar-upload-area">
                  <input
                    ref={fileInputRef}
                    type="file"
                    accept="image/jpeg,image/png"
                    onChange={handleAvatarUpload}
                    style={{ display: "none" }}
                  />
                  <div
                    className={`avatar-upload-zone${useAiAvatar ? " ai-avatar" : ""}${
                      avatarPreview ? " has-image" : ""
                    }`}
                    onClick={triggerFileUpload}
                    title="点击上传头像"
                  >
                    {uploadingAvatar ? (
                      <div className="avatar-uploading">
                        <span className="avatar-uploading-icon" aria-hidden>
                          ⏳
                        </span>
                        <span className="avatar-uploading-text">上传中...</span>
                      </div>
                    ) : avatarPreview ? (
                      <div className="avatar-image-preview">
                        <img src={avatarPreview} alt="宠物头像" />
                        <button
                          type="button"
                          className="avatar-remove-btn"
                          onClick={(e) => {
                            e.stopPropagation();
                            removeAvatar();
                          }}
                          title="移除头像"
                        >
                          ✕
                        </button>
                      </div>
                    ) : useAiAvatar ? (
                      <div className="ai-avatar-preview">
                        <span className="ai-avatar-icon" aria-hidden>
                          🤖
                        </span>
                        <span className="ai-avatar-label">AI生成</span>
                      </div>
                    ) : (
                      <div className="preview-emoji">{selectedEmoji}</div>
                    )}
                    {!avatarPreview && !uploadingAvatar && (
                      <div className="avatar-upload-hint">📷 点击上传</div>
                    )}
                  </div>
                  <div className="avatar-info">
                    <h3>宠物头像</h3>
                    <p>上传照片、选择表情或AI生成</p>
                    <div className="avatar-emoji-row">
                      {currentEmojiOptions.map((emoji) => (
                        <div
                          key={emoji}
                          className={`emoji-opt${
                            !useAiAvatar && !avatarPreview && selectedEmoji === emoji
                              ? " selected"
                              : ""
                          }`}
                          onClick={() => handleSelectEmoji(emoji)}
                        >
                          {emoji}
                        </div>
                      ))}
                      <div
                        className={`emoji-opt ai-opt${
                          useAiAvatar && !avatarPreview ? " selected" : ""
                        }`}
                        onClick={handleSelectAiAvatar}
                        title="AI生成头像"
                      >
                        ✨
                      </div>
                    </div>
                  </div>
                </div>

                <div className="form-grid">
                  <div className="form-field form-col-full">
                    <label className="form-label">
                      宠物昵称 <span className="required">*</span>
                    </label>
                    <Input
                      type="text"
                      placeholder="给它取个好名字"
                      maxLength={12}
                      value={editingPet.name}
                      onChange={(e) => updateField("name", e.target.value)}
                    />
                  </div>
                  <div className="form-field">
                    <label className="form-label">
                      宠物类型 <span className="required">*</span>
                    </label>
                    <Select
                      value={editingPet.type}
                      onChange={handleTypeChange}
                      options={petTypeOptions}
                      placeholder="请选择类型"
                    />
                  </div>
                  <div className="form-field">
                    <label className="form-label">品种</label>
                    {isCustomBreed ? (
                      <div className="custom-breed-input-wrap">
                        <Input
                          type="text"
                          placeholder="请输入宠物品种"
                          value={customBreedInput}
                          onChange={(e) => setCustomBreedInput(e.target.value)}
                          autoFocus
                          allowClear
                          onClear={() => setCustomBreedInput("")}
                          suffix={
                            <button
                              type="button"
                              className="custom-breed-back-btn"
                              onClick={() => {
                                setIsCustomBreed(false);
                                setCustomBreedInput("");
                              }}
                              title="返回选择"
                            >
                              ←
                            </button>
                          }
                        />
                      </div>
                    ) : (
                      <Select
                        value={editingPet.breed}
                        onChange={handleBreedChange}
                        options={currentBreedOptions.map((b) => ({ key: b, label: b }))}
                        placeholder="请选择品种"
                      />
                    )}
                  </div>
                  <div className="form-field">
                    <label className="form-label">
                      生日 <span className="required">*</span>
                    </label>
                    <input
                      className="form-input"
                      type="date"
                      value={editingPet.birthday}
                      onChange={(e) => updateField("birthday", e.target.value)}
                    />
                  </div>
                  <div className="form-field">
                    <label className="form-label">当前体重 (kg)</label>
                    <Input
                      type="number"
                      step="0.1"
                      placeholder="5.2"
                      value={editingPet.weight || ""}
                      onChange={(e) => updateField("weight", parseFloat(e.target.value) || 0)}
                    />
                  </div>
                </div>

                <div className="divider" />

                <div className="form-grid cols-3">
                  <div className="form-field">
                    <label className="form-label">性别</label>
                    <div className="radio-group">
                      <div
                        className={`radio-opt${editingPet.gender === "male" ? " selected" : ""}`}
                        onClick={() => updateField("gender", "male")}
                      >
                        <div className="radio-dot">
                          <div className="radio-dot-inner" />
                        </div>
                        公
                      </div>
                      <div
                        className={`radio-opt${editingPet.gender === "female" ? " selected" : ""}`}
                        onClick={() => updateField("gender", "female")}
                      >
                        <div className="radio-dot">
                          <div className="radio-dot-inner" />
                        </div>
                        母
                      </div>
                    </div>
                  </div>
                  <div className="form-field">
                    <label className="form-label">是否绝育</label>
                    <div className="radio-group">
                      <div
                        className={`radio-opt${editingPet.sterilized ? " selected" : ""}`}
                        onClick={() => updateField("sterilized", true)}
                      >
                        <div className="radio-dot">
                          <div className="radio-dot-inner" />
                        </div>
                        是
                      </div>
                      <div
                        className={`radio-opt${!editingPet.sterilized ? " selected" : ""}`}
                        onClick={() => updateField("sterilized", false)}
                      >
                        <div className="radio-dot">
                          <div className="radio-dot-inner" />
                        </div>
                        否
                      </div>
                    </div>
                  </div>
                  <div className="form-field">
                    <label className="form-label">是否已打芯片</label>
                    <div className="radio-group">
                      <div
                        className={`radio-opt${editingPet.chipped ? " selected" : ""}`}
                        onClick={() => updateField("chipped", true)}
                      >
                        <div className="radio-dot">
                          <div className="radio-dot-inner" />
                        </div>
                        是
                      </div>
                      <div
                        className={`radio-opt${!editingPet.chipped ? " selected" : ""}`}
                        onClick={() => updateField("chipped", false)}
                      >
                        <div className="radio-dot">
                          <div className="radio-dot-inner" />
                        </div>
                        否
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </Card>

            <Card>
              <div className="form-card-header">
                <span className="fch-icon" aria-hidden>
                  ❤️
                </span>
                <span className="fch-title">健康信息</span>
              </div>
              <div className="form-body">
                <div className="form-grid">
                  <div className="form-field">
                    <label className="form-label">过敏史</label>
                    <Input
                      type="text"
                      placeholder="无 / 对某食物过敏"
                      value={editingPet.allergies}
                      onChange={(e) => updateField("allergies", e.target.value)}
                    />
                  </div>
                  <div className="form-field">
                    <label className="form-label">慢性疾病</label>
                    <Input
                      type="text"
                      placeholder="无 / 填写病情"
                      value={editingPet.diseases}
                      onChange={(e) => updateField("diseases", e.target.value)}
                    />
                  </div>
                  <div className="form-field">
                    <label className="form-label">主食品牌</label>
                    <Input
                      type="text"
                      placeholder="如：皇家 / 爱肯拿"
                      value={editingPet.foodBrand}
                      onChange={(e) => updateField("foodBrand", e.target.value)}
                    />
                  </div>
                  <div className="form-field">
                    <label className="form-label">宠物医院</label>
                    <Input
                      type="text"
                      placeholder="常去的宠物医院"
                      value={editingPet.hospital}
                      onChange={(e) => updateField("hospital", e.target.value)}
                    />
                  </div>
                  <div className="form-field form-col-full">
                    <label className="form-label">备注信息</label>
                    <textarea
                      className="form-textarea"
                      placeholder="其他需要 AI 了解的信息…"
                      value={editingPet.notes}
                      onChange={(e) => updateField("notes", e.target.value)}
                    />
                  </div>
                </div>
              </div>
            </Card>
          </div>
        </Modal>
      )}

      {showConfirmDialog && (
        <Modal
          open={showConfirmDialog}
          onClose={cancelCloseModal}
          title="⚠️ 确定要放弃编辑吗？"
          width={420}
          maskClosable={false}
          typewriter={false}
          footer={
            <div className="confirm-dialog-actions">
              <Button type="default" onClick={cancelCloseModal}>
                继续编辑
              </Button>
              <Button type="primary" danger onClick={confirmCloseModal}>
                放弃更改
              </Button>
            </div>
          }
        >
          <div className="confirm-dialog-message">
            您有未保存的更改，关闭后将丢失这些内容。
          </div>
        </Modal>
      )}
    </>
  );
});
