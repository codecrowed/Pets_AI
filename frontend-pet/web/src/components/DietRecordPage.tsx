import { useState, useCallback, useMemo, useEffect } from "react";
import { Button, Card, Input, Tabs, type TabItem } from "animal-island-ui";
import { TopBar } from "./TopBar";
import type { Pet } from "../lib/pet-types";
import {
  type DailyDietStats,
  type WeeklyCalories,
  type DailyWaterStats,
  type ContinuousDays,
  type Food,
  type AiDietAnalysis,
  getDailyDietStats,
  getWeeklyCalories,
  getDailyWaterStats,
  getContinuousDays,
  addDietRecord,
  deleteDietRecord,
  searchFoods,
  getFrequentFoods,
  analyzeDiet,
  addWaterRecord,
  deleteWaterRecord,
} from "../lib/diet-api";

type PendingFoodItem = {
  id: string;
  food: Food | null;
  foodName: string;
  weight: string;
  time: string;
};

type MealItem = {
  id: number;
  name: string;
  icon: string;
  amount: number;
  time: string;
  calories: number;
};

type MealGroup = {
  type: "breakfast" | "lunch" | "dinner" | "snack" | "supplement";
  label: string;
  icon: string;
  items: MealItem[];
};

type DietRecordPageProps = {
  onMenuClick: () => void;
  pets: Pet[];
  activePet: Pet | null;
  onSwitchPet: (petId: number) => void;
  onAddPet: () => void;
};

const mealTypeOptions = [
  { type: "breakfast" as const, label: "早餐", icon: "🌅" },
  { type: "lunch" as const, label: "午餐", icon: "☀️" },
  { type: "dinner" as const, label: "晚餐", icon: "🌙" },
  { type: "snack" as const, label: "零食", icon: "🍬" },
  { type: "supplement" as const, label: "营养品", icon: "💊" },
];

function showToast(msg: string, type: "success" | "warning" = "success") {
  window.dispatchEvent(new CustomEvent("pawpal-toast", { detail: { msg, type } }));
}

function formatDate(offset: number): string {
  const d = new Date();
  d.setDate(d.getDate() + offset);
  const weekDays = ["日", "一", "二", "三", "四", "五", "六"];
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日（周${weekDays[d.getDay()]}）`;
}

export function DietRecordPage({ onMenuClick, pets, activePet, onSwitchPet, onAddPet }: DietRecordPageProps) {
  const petName = activePet?.name || "宠物";
  const petEmoji = activePet?.emoji === "AI" ? "🤖" : activePet?.emoji || "🐕";
  const petId = activePet?.id;

  const [dateOffset, setDateOffset] = useState(0);
  const [loading, setLoading] = useState(false);
  const [mealGroups, setMealGroups] = useState<MealGroup[]>([]);
  const [dailyStats, setDailyStats] = useState<DailyDietStats | null>(null);
  const [weeklyData, setWeeklyData] = useState<WeeklyCalories | null>(null);
  const [waterStats, setWaterStats] = useState<DailyWaterStats | null>(null);
  const [continuousDays, setContinuousDays] = useState<ContinuousDays | null>(null);
  const [searchResults, setSearchResults] = useState<Food[]>([]);
  const [frequentFoods, setFrequentFoods] = useState<Food[]>([]);
  const [aiAnalysis, setAiAnalysis] = useState<AiDietAnalysis | null>(null);
  const [analyzing, setAnalyzing] = useState(false);

  const [selectedMealType, setSelectedMealType] = useState<MealGroup["type"]>("dinner");
  const [foodName, setFoodName] = useState("");
  const [foodAmount, setFoodAmount] = useState("80");
  const [foodTime, setFoodTime] = useState("18:00");
  const [selectedFood, setSelectedFood] = useState<Food | null>(null);
  const [activeMealItem, setActiveMealItem] = useState<number | null>(null);
  const [searchKeyword, setSearchKeyword] = useState("");
  const [hasSearched, setHasSearched] = useState(false);

  const [pendingFoods, setPendingFoods] = useState<PendingFoodItem[]>([]);

  const [waterAmount, setWaterAmount] = useState("100");
  const [waterTime, setWaterTime] = useState("12:00");
  const [showWaterForm, setShowWaterForm] = useState(false);

  const currentDate = useMemo(() => {
    const d = new Date();
    d.setDate(d.getDate() + dateOffset);
    return d.toISOString().split("T")[0];
  }, [dateOffset]);

  const targetCalories = dailyStats?.targetKcal ?? 480;
  const totalCalories = dailyStats?.totalKcal ?? 0;
  const progressPercent = Math.min(100, Math.round((totalCalories / targetCalories) * 100));

  const calculatedCalories = useMemo(() => {
    const amount = parseInt(foodAmount) || 0;
    if (selectedFood?.kcalPer100g) {
      return Math.round((selectedFood.kcalPer100g * amount) / 100);
    }
    return Math.round((120 * amount) / 100);
  }, [selectedFood, foodAmount]);

  const pendingTotalCalories = useMemo(() => {
    return pendingFoods.reduce((sum, item) => {
      const amount = parseInt(item.weight) || 0;
      const kcal = item.food?.kcalPer100g ?? 120;
      return sum + Math.round((kcal * amount) / 100);
    }, 0);
  }, [pendingFoods]);

  const loadData = useCallback(async () => {
    if (!petId) return;
    setLoading(true);
    try {
      const [stats, weekly, water, days] = await Promise.all([
        getDailyDietStats(petId, currentDate),
        getWeeklyCalories(petId, currentDate),
        getDailyWaterStats(petId, currentDate),
        getContinuousDays(petId),
      ]);
      setDailyStats(stats);
      setWeeklyData(weekly);
      setWaterStats(water);
      setContinuousDays(days);

      const groups: MealGroup[] = stats.mealGroups.map((g) => ({
        type: g.mealType.toLowerCase() as MealGroup["type"],
        label: g.mealTypeLabel,
        icon: g.mealIcon,
        items: g.items.map((item) => ({
          id: item.id,
          name: item.foodName,
          icon: item.foodIcon || "🍽️",
          amount: item.weight,
          time: new Date(item.mealTime).toLocaleTimeString("zh-CN", { hour: "2-digit", minute: "2-digit" }),
          calories: item.estimatedKcal ?? 0,
        })),
      }));

      const defaultTypes: MealGroup["type"][] = ["breakfast", "lunch", "dinner"];
      for (const type of defaultTypes) {
        if (!groups.find((g) => g.type === type)) {
          const opt = mealTypeOptions.find((m) => m.type === type);
          groups.push({ type, label: opt?.label || type, icon: opt?.icon || "🍽️", items: [] });
        }
      }
      groups.sort((a, b) => {
        const order = ["breakfast", "lunch", "dinner", "snack", "supplement"];
        return order.indexOf(a.type) - order.indexOf(b.type);
      });
      setMealGroups(groups);
    } catch (err) {
      console.error("Failed to load diet data", err);
      showToast("加载数据失败", "warning");
    } finally {
      setLoading(false);
    }
  }, [petId, currentDate]);

  const loadFrequentFoods = useCallback(async () => {
    try {
      const foods = await getFrequentFoods();
      setFrequentFoods(foods);
    } catch (err) {
      console.error("Failed to load frequent foods", err);
    }
  }, []);

  useEffect(() => {
    if (!petId) return;
    loadData();
  }, [petId, currentDate, loadData]);

  useEffect(() => {
    loadFrequentFoods();
  }, [loadFrequentFoods]);

  const handleSearchFoods = useCallback(async (keyword: string) => {
    setSearchKeyword(keyword);
    if (keyword.trim().length < 1) {
      setSearchResults([]);
      setHasSearched(false);
      return;
    }
    try {
      const results = await searchFoods(keyword, 1, 10);
      setSearchResults(results);
      setHasSearched(true);
    } catch (err) {
      console.error("Failed to search foods", err);
      setHasSearched(true);
    }
  }, []);

  const changeDate = useCallback((delta: number) => {
    setDateOffset((prev) => prev + delta);
  }, []);

  const goToday = useCallback(() => {
    setDateOffset(0);
  }, []);

  const selectMealType = useCallback((type: MealGroup["type"]) => {
    setSelectedMealType(type);
  }, []);

  const selectFood = useCallback((food: Food) => {
    setSelectedFood(food);
    setFoodName(food.name);
    setSearchResults([]);
    setSearchKeyword("");
    setHasSearched(false);
    showToast(`✅ 已选择：${food.name}`, "success");
  }, []);

  const addToPendingFoods = useCallback(() => {
    if (!foodName.trim()) {
      showToast("请输入食物名称", "warning");
      return;
    }
    const amount = parseInt(foodAmount) || 0;
    if (amount <= 0) {
      showToast("请输入有效的重量", "warning");
      return;
    }
    const newItem: PendingFoodItem = {
      id: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
      food: selectedFood,
      foodName,
      weight: foodAmount,
      time: foodTime,
    };
    setPendingFoods((prev) => [...prev, newItem]);
    setFoodName("");
    setFoodAmount("80");
    setSelectedFood(null);
    showToast(`✅ 已添加到列表：${newItem.foodName}`, "success");
  }, [foodName, foodAmount, foodTime, selectedFood]);

  const removePendingFood = useCallback((id: string) => {
    setPendingFoods((prev) => prev.filter((item) => item.id !== id));
  }, []);

  const updatePendingFoodWeight = useCallback((id: string, weight: string) => {
    setPendingFoods((prev) => prev.map((item) => (item.id === id ? { ...item, weight } : item)));
  }, []);

  const saveFoodRecord = useCallback(async () => {
    if (!petId) {
      showToast("请先选择宠物", "warning");
      return;
    }

    const itemsToSave: PendingFoodItem[] = [...pendingFoods];

    if (foodName.trim() && (parseInt(foodAmount) || 0) > 0) {
      itemsToSave.push({
        id: "current",
        food: selectedFood,
        foodName,
        weight: foodAmount,
        time: foodTime,
      });
    }

    if (itemsToSave.length === 0) {
      showToast("请至少添加一项食物", "warning");
      return;
    }

    try {
      let totalKcal = 0;
      for (const item of itemsToSave) {
        const weight = parseInt(item.weight) || 0;
        const mealDateTime = `${currentDate}T${item.time}:00`;
        await addDietRecord({
          petId,
          foodId: item.food?.id,
          foodName: item.foodName,
          weight,
          mealType: selectedMealType.toUpperCase(),
          mealTime: mealDateTime,
        });
        const kcal = item.food?.kcalPer100g ?? 120;
        totalKcal += Math.round((kcal * weight) / 100);
      }

      showToast(`✅ 已保存 ${itemsToSave.length} 条记录，共 ${totalKcal} kcal`, "success");
      setPendingFoods([]);
      setFoodName("");
      setFoodAmount("80");
      setSelectedFood(null);
      loadData();
      loadFrequentFoods();
    } catch (err) {
      console.error("Failed to save diet record", err);
      showToast("保存失败，请重试", "warning");
    }
  }, [
    petId,
    pendingFoods,
    foodName,
    foodAmount,
    foodTime,
    selectedMealType,
    selectedFood,
    currentDate,
    loadData,
    loadFrequentFoods,
  ]);

  const saveWaterRecord = useCallback(async () => {
    if (!petId) {
      showToast("请先选择宠物", "warning");
      return;
    }
    const amount = parseInt(waterAmount) || 0;
    if (amount <= 0) {
      showToast("请输入有效的饮水量", "warning");
      return;
    }

    const recordDateTime = `${currentDate}T${waterTime}:00`;

    try {
      await addWaterRecord({
        petId,
        waterAmount: amount,
        recordTime: recordDateTime,
      });
      showToast(`💧 已记录：${amount}ml`, "success");
      setWaterAmount("100");
      setShowWaterForm(false);
      loadData();
    } catch (err) {
      console.error("Failed to save water record", err);
      showToast("保存失败，请重试", "warning");
    }
  }, [petId, waterAmount, waterTime, currentDate, loadData]);

  const handleDeleteWaterRecord = useCallback(
    async (recordId: number) => {
      try {
        await deleteWaterRecord(recordId);
        showToast("已删除饮水记录", "success");
        loadData();
      } catch (err) {
        console.error("Failed to delete water record", err);
        showToast("删除失败", "warning");
      }
    },
    [loadData]
  );

  const _handleDeleteRecord = useCallback(
    async (recordId: number) => {
      try {
        await deleteDietRecord(recordId);
        showToast("已删除记录", "success");
        loadData();
      } catch (err) {
        console.error("Failed to delete record", err);
        showToast("删除失败", "warning");
      }
    },
    [loadData]
  );
  void _handleDeleteRecord;

  const handleAiAnalysis = useCallback(async () => {
    if (!petId) {
      showToast("请先选择宠物", "warning");
      return;
    }
    setAnalyzing(true);
    try {
      const analysis = await analyzeDiet(petId, currentDate);
      setAiAnalysis(analysis);
      showToast("AI 分析完成", "success");
    } catch (err) {
      console.error("Failed to analyze diet", err);
      showToast("分析失败，请重试", "warning");
    } finally {
      setAnalyzing(false);
    }
  }, [petId, currentDate]);

  const openAddFood = useCallback((mealType: MealGroup["type"]) => {
    setSelectedMealType(mealType);
    document.getElementById("addFoodCard")?.scrollIntoView({ behavior: "smooth", block: "start" });
  }, []);

  const getMealCalories = useCallback((group: MealGroup) => {
    return group.items.reduce((sum, item) => sum + item.calories, 0);
  }, []);

  const mealTypeTabItems = useMemo<TabItem[]>(
    () =>
      mealTypeOptions.map((opt) => ({
        key: opt.type,
        label: (
          <span>
            {opt.icon} {opt.label}
          </span>
        ),
        children: null,
      })),
    []
  );

  return (
    <>
      <TopBar
        title="饮食记录"
        subtitle="记录每日饮食，AI 营养分析"
        onMenuClick={onMenuClick}
        pets={pets}
        activePet={activePet}
        onSwitchPet={onSwitchPet}
        onAddPet={onAddPet}
      />

      <div className="page-content">
        <div className="diet-layout">
          <div className="diet-sidebar">
            <div className="ds-header">
              <span className="ds-title">饮食记录</span>
              <div className="ds-pet-tag">
                {petEmoji} {petName}
              </div>
            </div>
            <div className="diet-date-nav">
              <Button type="text" size="small" className="ddn-btn" onClick={() => changeDate(-1)} aria-label="前一天">
                ‹
              </Button>
              <div>
                <div className="ddn-date">{formatDate(dateOffset)}</div>
                <Button type="default" size="small" className="ddn-today-btn" onClick={goToday}>
                  今天
                </Button>
              </div>
              <Button type="text" size="small" className="ddn-btn" onClick={() => changeDate(1)} aria-label="后一天">
                ›
              </Button>
            </div>
            <Card className="daily-summary">
              <div className="ds-summary-title">📊 今日营养摘要</div>
              <div className="ds-macro-row">
                <div className="ds-macro">
                  <div className="ds-macro-val">{totalCalories}</div>
                  <div className="ds-macro-lbl">已摄入 kcal</div>
                </div>
                <div className="ds-macro">
                  <div className="ds-macro-val">{Math.round(dailyStats?.proteinG ?? 0)}g</div>
                  <div className="ds-macro-lbl">蛋白质</div>
                </div>
                <div className="ds-macro">
                  <div className="ds-macro-val">{Math.round(dailyStats?.fatG ?? 0)}g</div>
                  <div className="ds-macro-lbl">脂肪</div>
                </div>
                <div className="ds-macro">
                  <div className="ds-macro-val">{Math.round(dailyStats?.carbG ?? 0)}g</div>
                  <div className="ds-macro-lbl">碳水</div>
                </div>
              </div>
              <div className="ds-cal-bar">
                <div className="ds-cal-label">
                  <span>目标 {targetCalories} kcal</span>
                  <span>{progressPercent}%</span>
                </div>
                <div className="ds-bar-track">
                  <div className="ds-bar-fill" style={{ width: `${progressPercent}%` }} />
                </div>
              </div>
            </Card>
            <div className="meal-list">
              {mealGroups.map((group) => (
                <div className="meal-group" key={group.type}>
                  <div className="meal-group-header">
                    <div className="mgh-title">
                      <span aria-hidden>{group.icon}</span>
                      {group.label}
                    </div>
                    <span className="mgh-cal">{getMealCalories(group)} kcal</span>
                  </div>
                  {group.items.map((item) => (
                    <button
                      key={item.id}
                      className={`meal-item${activeMealItem === item.id ? " active" : ""}`}
                      onClick={() => setActiveMealItem(item.id)}
                      type="button"
                    >
                      <span className="mi-icon" aria-hidden>
                        {item.icon}
                      </span>
                      <div className="mi-info">
                        <div className="mi-name">{item.name}</div>
                        <div className="mi-detail">
                          {item.amount}g · {item.time}
                        </div>
                      </div>
                      <span className="mi-cal">{item.calories} kcal</span>
                    </button>
                  ))}
                  <Button
                    type="dashed"
                    size="small"
                    block
                    className={`meal-add-btn${group.items.length === 0 ? " highlight" : ""}`}
                    onClick={() => openAddFood(group.type)}
                  >
                    ＋ 添加{group.label}记录
                  </Button>
                </div>
              ))}
            </div>
          </div>

          <div className="diet-main">
            <div className="diet-main-title">
              📊 本周饮食概览
              <Button type="default" size="small" style={{ marginLeft: "auto" }} onClick={() => openAddFood("dinner")}>
                ＋ 快速添加
              </Button>
            </div>

            <div className="diet-stats-grid">
              <Card className="stat-card" color="app-orange">
                <div className="sc-icon" aria-hidden>
                  🔥
                </div>
                <div>
                  <span className="sc-val">{totalCalories}</span>
                  <span className="sc-unit">kcal</span>
                </div>
                <div className="sc-lbl">今日已摄入</div>
                <div className="sc-trend up">↑ 目标达成{progressPercent}%</div>
              </Card>
              <Card className="stat-card" color="app-pink">
                <div className="sc-icon" aria-hidden>
                  🥩
                </div>
                <div>
                  <span className="sc-val">{Math.round(dailyStats?.proteinG ?? 0)}</span>
                  <span className="sc-unit">g</span>
                </div>
                <div className="sc-lbl">蛋白质</div>
                <div className="sc-trend up">↑ 优质蛋白</div>
              </Card>
              <Card
                className="stat-card stat-card-clickable"
                color="app-blue"
                onClick={() => setShowWaterForm(!showWaterForm)}
              >
                <div className="sc-icon" aria-hidden>
                  💧
                </div>
                <div>
                  <span className="sc-val">{waterStats?.totalWaterMl ?? 0}</span>
                  <span className="sc-unit">ml</span>
                </div>
                <div className="sc-lbl">今日饮水</div>
                <div className={`sc-trend ${(waterStats?.totalWaterMl ?? 0) >= 400 ? "up" : "down"}`}>
                  {(waterStats?.totalWaterMl ?? 0) >= 400 ? "↑ 已达标" : "↓ 点击添加"}
                </div>
              </Card>
              <Card className="stat-card" color="app-green">
                <div className="sc-icon" aria-hidden>
                  📈
                </div>
                <div>
                  <span className="sc-val">{continuousDays?.continuousDays ?? 0}</span>
                  <span className="sc-unit">天</span>
                </div>
                <div className="sc-lbl">连续记录</div>
                <div className="sc-trend up">↑ {continuousDays?.message ?? "坚持记录中"}</div>
              </Card>
            </div>

            {showWaterForm && (
              <Card className="water-form-card" color="app-blue">
                <div className="wfc-header">
                  <span className="wfc-title">💧 添加饮水记录</span>
                  <Button
                    type="text"
                    size="small"
                    className="wfc-close"
                    onClick={() => setShowWaterForm(false)}
                    aria-label="关闭"
                  >
                    ✕
                  </Button>
                </div>
                <div className="wfc-body">
                  <div className="wfc-field">
                    <label className="wfc-label">饮水量 (ml)</label>
                    <div className="wfc-input-group">
                      <Input
                        type="number"
                        value={waterAmount}
                        min={0}
                        onChange={(e) => setWaterAmount(e.target.value)}
                        placeholder="输入饮水量"
                      />
                      <div className="wfc-quick-btns">
                        {[50, 100, 150, 200].map((amt) => (
                          <Button
                            key={amt}
                            type={waterAmount === String(amt) ? "primary" : "default"}
                            size="small"
                            className="wfc-quick-btn"
                            onClick={() => setWaterAmount(String(amt))}
                          >
                            {amt}ml
                          </Button>
                        ))}
                      </div>
                    </div>
                  </div>
                  <div className="wfc-field">
                    <label className="wfc-label">记录时间</label>
                    <input
                      className="wfc-input"
                      type="time"
                      value={waterTime}
                      onChange={(e) => setWaterTime(e.target.value)}
                    />
                  </div>
                  <Button type="primary" block className="wfc-save-btn" onClick={saveWaterRecord}>
                    💧 保存饮水记录
                  </Button>
                </div>
                {waterStats && waterStats.records.length > 0 && (
                  <div className="wfc-records">
                    <div className="wfc-records-title">今日饮水记录</div>
                    {waterStats.records.map((record) => (
                      <div key={record.id} className="wfc-record-item">
                        <span className="wfc-record-amount">{record.waterAmount}ml</span>
                        <span className="wfc-record-time">
                          {new Date(record.recordTime).toLocaleTimeString("zh-CN", {
                            hour: "2-digit",
                            minute: "2-digit",
                          })}
                        </span>
                        <Button
                          type="text"
                          size="small"
                          className="wfc-record-delete"
                          onClick={() => handleDeleteWaterRecord(record.id)}
                          aria-label="删除"
                          title="删除"
                        >
                          ✕
                        </Button>
                      </div>
                    ))}
                  </div>
                )}
              </Card>
            )}

            <Card className="add-food-card" id="addFoodCard">
              <div className="afc-title">
                🍽️ 添加饮食记录 ·{" "}
                <span style={{ color: "var(--c-primary-dark)" }}>
                  {mealTypeOptions.find((m) => m.type === selectedMealType)?.label}
                </span>
              </div>

              <Tabs
                items={mealTypeTabItems}
                activeKey={selectedMealType}
                onChange={(key) => selectMealType(key as MealGroup["type"])}
                className="meal-type-tabs"
              />

              <div className="food-search-row">
                <Input
                  type="text"
                  placeholder="搜索食物名称（主粮、肉类、蔬菜...）"
                  prefix={<span aria-hidden>🔍</span>}
                  allowClear
                  value={searchKeyword}
                  onChange={(e) => handleSearchFoods(e.target.value)}
                  onClear={() => handleSearchFoods("")}
                />
                <Button type="default" size="small" onClick={() => showToast("📷 扫条码功能开发中", "warning")}>
                  📷 扫码
                </Button>
              </div>

              {(searchResults.length > 0 || (hasSearched && searchKeyword.trim())) && (
                <div className="food-search-results">
                  {searchResults.length > 0 ? (
                    searchResults.map((food) => (
                      <button
                        key={food.id}
                        className="food-search-item"
                        onClick={() => selectFood(food)}
                        type="button"
                      >
                        <span className="fsi-icon" aria-hidden>
                          {food.icon || "🍽️"}
                        </span>
                        <div className="fsi-info">
                          <div className="fsi-name">{food.name}</div>
                          <div className="fsi-detail">
                            {food.categoryLabel} · {food.kcalPer100g ?? "?"}kcal/100g
                            {food.isStapleFood && food.stapleFood && (
                              <span className="fsi-brand"> · {food.stapleFood.brand}</span>
                            )}
                          </div>
                        </div>
                      </button>
                    ))
                  ) : (
                    <div className="food-search-empty">
                      <span aria-hidden>🔍</span> 未找到&ldquo;{searchKeyword}&rdquo;相关的食物
                    </div>
                  )}
                </div>
              )}

              <div className="food-quick-chips">
                <div className="fqc-label">常用食物：</div>
                {frequentFoods.slice(0, 8).map((food) => (
                  <Button
                    key={food.id}
                    type={selectedFood?.id === food.id ? "primary" : "default"}
                    size="small"
                    className="fqc-chip"
                    onClick={() => selectFood(food)}
                  >
                    {food.icon || "🍽️"} {food.name}
                  </Button>
                ))}
              </div>

              <div className="food-detail-row">
                <div className="fdr-field">
                  <div className="fdr-label">食物名称</div>
                  <Input
                    type="text"
                    value={foodName}
                    readOnly
                    placeholder="请从上方搜索或选择食物"
                    className="fdr-input-readonly"
                  />
                </div>
                <div className="fdr-field">
                  <div className="fdr-label">重量</div>
                  <Input
                    type="number"
                    value={foodAmount}
                    min={0}
                    onChange={(e) => setFoodAmount(e.target.value)}
                    placeholder="输入重量"
                    suffix="g"
                  />
                  <div className="fdr-unit">单位：克 (g)</div>
                </div>
                <div className="fdr-field">
                  <div className="fdr-label">用餐时间</div>
                  <input
                    className="fdr-input"
                    type="time"
                    value={foodTime}
                    onChange={(e) => setFoodTime(e.target.value)}
                  />
                </div>
                <div className="fdr-cal-total">
                  <span className="fdr-cal-label">🔥 估算热量</span>
                  <span className="fdr-cal-val">{calculatedCalories} kcal</span>
                </div>
              </div>

              {pendingFoods.length > 0 && (
                <Card className="pending-foods-list" color="app-yellow">
                  <div className="pfl-header">
                    <span>📋 待保存的食物列表 ({pendingFoods.length})</span>
                    <span className="pfl-total">共 {pendingTotalCalories} kcal</span>
                  </div>
                  {pendingFoods.map((item) => (
                    <div key={item.id} className="pending-food-item">
                      <span className="pfi-icon" aria-hidden>
                        {item.food?.icon || "🍽️"}
                      </span>
                      <span className="pfi-name">{item.foodName}</span>
                      <input
                        className="pfi-weight"
                        type="number"
                        value={item.weight}
                        min={0}
                        onChange={(e) => updatePendingFoodWeight(item.id, e.target.value)}
                      />
                      <span className="pfi-unit">g</span>
                      <span className="pfi-time">{item.time}</span>
                      <Button
                        type="text"
                        size="small"
                        className="pfi-remove"
                        onClick={() => removePendingFood(item.id)}
                        aria-label="移除"
                        title="移除"
                      >
                        ✕
                      </Button>
                    </div>
                  ))}
                </Card>
              )}

              <div className="form-actions">
                <Button
                  type="dashed"
                  className="btn-add-list"
                  onClick={addToPendingFoods}
                  disabled={!foodName.trim()}
                >
                  ＋ 添加到列表
                </Button>
                <Button
                  type="primary"
                  onClick={saveFoodRecord}
                  disabled={loading || (pendingFoods.length === 0 && !foodName.trim())}
                >
                  ✓ 保存{pendingFoods.length > 0 ? ` ${pendingFoods.length + (foodName.trim() ? 1 : 0)} 条` : ""}记录
                </Button>
                <Button type="default" onClick={handleAiAnalysis} disabled={analyzing} loading={analyzing}>
                  {analyzing ? "分析中…" : "🤖 AI 营养分析"}
                </Button>
              </div>

              {aiAnalysis && (
                <Card className="ai-analysis-result" color="purple">
                  <div className="aar-title">🤖 AI 营养分析结果</div>
                  <div className="aar-summary">{aiAnalysis.summary}</div>
                  {aiAnalysis.suggestions.length > 0 && (
                    <div className="aar-suggestions">
                      <div className="aar-sugg-title">建议：</div>
                      <ul>
                        {aiAnalysis.suggestions.map((s, i) => (
                          <li key={i}>{s}</li>
                        ))}
                      </ul>
                    </div>
                  )}
                  {aiAnalysis.nutritionAnalysis && (
                    <div className="aar-status">
                      <span className="aar-tag">热量: {aiAnalysis.nutritionAnalysis.calorieStatus}</span>
                      <span className="aar-tag">蛋白质: {aiAnalysis.nutritionAnalysis.proteinStatus}</span>
                      <span className="aar-tag">脂肪: {aiAnalysis.nutritionAnalysis.fatStatus}</span>
                      <span className="aar-tag">综合: {aiAnalysis.nutritionAnalysis.overallStatus}</span>
                    </div>
                  )}
                </Card>
              )}
            </Card>

            <Card className="week-chart-card">
              <div className="wcc-header">
                <div className="wcc-title">📅 近7天热量趋势</div>
                <div className="wcc-legend">
                  <div className="wcc-leg-item">
                    <div className="wcc-leg-dot" style={{ background: "var(--c-primary)" }} />
                    实际摄入
                  </div>
                  <div className="wcc-leg-item">
                    <div className="wcc-leg-dot" style={{ background: "var(--c-green)", opacity: 0.5 }} />
                    目标{weeklyData?.targetCalories ?? 480}kcal
                  </div>
                </div>
              </div>
              <div className="chart-bars">
                {(weeklyData?.dailyCalories ?? []).map((d, idx) => {
                  const allCalories = weeklyData?.dailyCalories ?? [];
                  const max = Math.max(...allCalories.map((x) => Math.max(x.calories, x.targetCalories)), 1);
                  const calH = Math.round((d.calories / max) * 74);
                  const tarH = Math.round((d.targetCalories / max) * 74);
                  return (
                    <div className="chart-bar-group" key={idx}>
                      <div className="chart-bar-wrap">
                        <div
                          className="chart-bar target"
                          style={{ height: `${tarH}px` }}
                          title={`目标: ${d.targetCalories}kcal`}
                        />
                        <div
                          className="chart-bar cal"
                          style={{ height: `${calH}px` }}
                          title={`${d.calories}kcal`}
                        />
                      </div>
                      <div className="chart-bar-label">{d.dayLabel}</div>
                      <div className="chart-bar-val">{d.calories}</div>
                    </div>
                  );
                })}
              </div>
            </Card>
          </div>
        </div>
      </div>
    </>
  );
}
