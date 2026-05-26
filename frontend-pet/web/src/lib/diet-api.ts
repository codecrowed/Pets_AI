import { apiJson } from "./api-client";

// ========== 类型定义 ==========

export type DietRecord = {
  id: number;
  petId: number;
  foodId: number | null;
  foodName: string;
  foodIcon: string;
  weight: number;
  mealType: string;
  mealTypeLabel: string;
  mealTime: string;
  estimatedKcal: number | null;
  proteinG: number | null;
  fatG: number | null;
  carbG: number | null;
  createdAt: string;
};

export type MealGroup = {
  mealType: string;
  mealTypeLabel: string;
  mealIcon: string;
  totalKcal: number;
  items: DietRecord[];
};

export type DailyDietStats = {
  date: string;
  totalKcal: number;
  proteinG: number;
  fatG: number;
  carbG: number;
  targetKcal: number;
  waterMl: number;
  progressPercent: number;
  mealGroups: MealGroup[];
};

export type DailyCalories = {
  date: string;
  dayLabel: string;
  calories: number;
  targetCalories: number;
};

export type WeeklyCalories = {
  dailyCalories: DailyCalories[];
  avgCalories: number;
  targetCalories: number;
};

export type ContinuousDays = {
  continuousDays: number;
  message: string;
};

export type WaterRecord = {
  id: number;
  petId: number;
  waterAmount: number;
  recordTime: string;
  createdAt: string;
};

export type DailyWaterStats = {
  date: string;
  totalWaterMl: number;
  targetWaterMl: number;
  progressPercent: number;
  records: WaterRecord[];
};

export type StapleFood = {
  id: number;
  name: string;
  brand: string;
  foodType: string;
  foodTypeLabel: string;
  targetSpecies: string;
  targetSpeciesLabel: string;
  targetBreed: string | null;
  targetAge: string | null;
  targetAgeLabel: string | null;
  targetSize: string | null;
  targetSizeLabel: string | null;
  crudeProteinPct: number;
  crudeFatPct: number;
  crudeAshPct: number | null;
  crudeFiberPct: number | null;
  moisturePct: number | null;
  imageUrl: string | null;
  description: string | null;
  ingredients: string | null;
  feedingGuide: string | null;
};

export type Food = {
  id: number;
  name: string;
  icon: string | null;
  category: string;
  categoryLabel: string;
  isStapleFood: boolean;
  stapleFoodId: number | null;
  stapleFood: StapleFood | null;
  kcalPer100g: number | null;
  proteinPer100g: number | null;
  fatPer100g: number | null;
  carbPer100g: number | null;
  description: string | null;
};

export type NutritionAnalysis = {
  proteinStatus: string;
  fatStatus: string;
  carbStatus: string;
  calorieStatus: string;
  overallStatus: string;
};

export type AiDietAnalysis = {
  date: string;
  summary: string;
  suggestions: string[];
  nutritionAnalysis: NutritionAnalysis | null;
};

// ========== 饮食记录 API ==========

export async function getDietRecordsByDate(petId: number, date: string): Promise<DietRecord[]> {
  return apiJson<DietRecord[]>(`/api/v1/diet-records/by-date?petId=${petId}&date=${date}`);
}

export async function addDietRecord(data: {
  petId: number;
  foodId?: number;
  foodName: string;
  weight: number;
  mealType: string;
  mealTime: string;
}): Promise<DietRecord> {
  return apiJson<DietRecord>("/api/v1/diet-records", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function updateDietRecord(
  recordId: number,
  data: {
    foodName?: string;
    weight?: number;
    mealType?: string;
    mealTime?: string;
  }
): Promise<DietRecord> {
  return apiJson<DietRecord>(`/api/v1/diet-records/${recordId}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

export async function deleteDietRecord(recordId: number): Promise<void> {
  await apiJson<null>(`/api/v1/diet-records/${recordId}`, {
    method: "DELETE",
  });
}

// ========== 饮食统计 API ==========

export async function getDailyDietStats(petId: number, date: string): Promise<DailyDietStats> {
  return apiJson<DailyDietStats>(`/api/v1/diet-stats/daily?petId=${petId}&date=${date}`);
}

export async function getContinuousDays(petId: number): Promise<ContinuousDays> {
  return apiJson<ContinuousDays>(`/api/v1/diet-stats/continuous-days?petId=${petId}`);
}

export async function getWeeklyCalories(petId: number, endDate: string): Promise<WeeklyCalories> {
  return apiJson<WeeklyCalories>(`/api/v1/diet-stats/weekly-calories?petId=${petId}&endDate=${endDate}`);
}

// ========== 饮水记录 API ==========

export async function getDailyWaterStats(petId: number, date: string): Promise<DailyWaterStats> {
  return apiJson<DailyWaterStats>(`/api/v1/water-records/daily?petId=${petId}&date=${date}`);
}

export async function addWaterRecord(data: {
  petId: number;
  waterAmount: number;
  recordTime: string;
}): Promise<WaterRecord> {
  return apiJson<WaterRecord>("/api/v1/water-records", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function updateWaterRecord(
  recordId: number,
  data: {
    waterAmount?: number;
    recordTime?: string;
  }
): Promise<WaterRecord> {
  return apiJson<WaterRecord>(`/api/v1/water-records/${recordId}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

export async function deleteWaterRecord(recordId: number): Promise<void> {
  await apiJson<null>(`/api/v1/water-records/${recordId}`, {
    method: "DELETE",
  });
}

// ========== 食物库 API ==========

export async function searchFoods(
  keyword: string,
  pageNum?: number,
  pageSize?: number
): Promise<Food[]> {
  const params = new URLSearchParams({ keyword });
  if (pageNum !== undefined) params.set("pageNum", String(pageNum));
  if (pageSize !== undefined) params.set("pageSize", String(pageSize));
  return apiJson<Food[]>(`/api/v1/foods/search?${params.toString()}`);
}

export async function getFrequentFoods(): Promise<Food[]> {
  return apiJson<Food[]>("/api/v1/foods/frequent");
}

// ========== AI 营养分析 API ==========

export async function analyzeDiet(petId: number, date: string): Promise<AiDietAnalysis> {
  return apiJson<AiDietAnalysis>("/api/v1/ai/diet-analysis", {
    method: "POST",
    body: JSON.stringify({ petId, date }),
  });
}
