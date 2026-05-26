export type Pet = {
  id: number;
  name: string;
  type: string;
  breed: string;
  emoji: string;
  avatarUrl?: string;
  gender: "male" | "female";
  birthday: string;
  weight: number;
  sterilized: boolean;
  chipped: boolean;
  allergies: string;
  diseases: string;
  foodBrand: string;
  hospital: string;
  notes: string;
  isActive?: boolean;
};

export const defaultPets: Pet[] = [
  {
    id: 0,
    name: "小橘子",
    type: "dog",
    breed: "柴犬",
    emoji: "🐕",
    gender: "male",
    birthday: "2024-01-15",
    weight: 5.2,
    sterilized: true,
    chipped: true,
    allergies: "",
    diseases: "",
    foodBrand: "皇家柴犬专用粮",
    hospital: "博爱动物医院",
    notes: "小橘子比较活泼，喜欢追球，不喜欢洗澡。每天早晚各散步一次。",
    isActive: true,
  },
  {
    id: 1,
    name: "豆豆",
    type: "cat",
    breed: "英短蓝猫",
    emoji: "🐱",
    gender: "female",
    birthday: "2025-04-01",
    weight: 3.8,
    sterilized: false,
    chipped: false,
    allergies: "",
    diseases: "",
    foodBrand: "",
    hospital: "",
    notes: "",
    isActive: false,
  },
];

export function createEmptyPet(): Pet {
  return {
    id: Date.now(),
    name: "",
    type: "dog",
    breed: "",
    emoji: "🐕",
    gender: "male",
    birthday: "",
    weight: 0,
    sterilized: false,
    chipped: false,
    allergies: "",
    diseases: "",
    foodBrand: "",
    hospital: "",
    notes: "",
  };
}

export function showToast(msg: string, type: "success" | "warning" = "success") {
  window.dispatchEvent(new CustomEvent("pawpal-toast", { detail: { msg, type } }));
}
