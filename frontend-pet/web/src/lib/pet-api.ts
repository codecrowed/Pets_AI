import { apiJson } from "./api-client";
import type { Pet } from "./pet-types";

export type PetSummary = {
  id: number;
  name: string;
  species: string;
  breed: string | null;
  avatarEmoji: string | null;
  avatarUrl: string | null;
  gender: string | null;
  birthday: string | null;
  neutered: boolean | null;
  microchipped: boolean | null;
};

export type PetDetail = {
  id: number;
  name: string;
  species: string;
  breed: string | null;
  birthday: string | null;
  weightKg: number | null;
  gender: string | null;
  neutered: boolean | null;
  microchipped: boolean | null;
  avatarUrl: string | null;
  avatarEmoji: string | null;
  allergies: string | null;
  chronicConditions: string | null;
  mainFoodBrand: string | null;
  vetHospital: string | null;
  notes: string | null;
  createdAt: string;
  updatedAt: string;
};

export type PetCreateRequest = {
  name: string;
  species: string;
  breed?: string;
  birthday?: string;
  weightKg?: number;
  gender?: string;
  neutered?: boolean;
  microchipped?: boolean;
  avatarUrl?: string;
  avatarEmoji?: string;
  allergies?: string;
  chronicConditions?: string;
  mainFoodBrand?: string;
  vetHospital?: string;
  notes?: string;
};

export type PetUpdateRequest = PetCreateRequest;

export async function listPets(): Promise<PetSummary[]> {
  return apiJson<PetSummary[]>("/api/v1/pets");
}

export async function getPetDetail(petId: number): Promise<PetDetail> {
  return apiJson<PetDetail>(`/api/v1/pets/${petId}`);
}

export async function createPet(data: PetCreateRequest): Promise<PetDetail> {
  return apiJson<PetDetail>("/api/v1/pets", {
    method: "POST",
    body: JSON.stringify(data),
  });
}

export async function updatePet(petId: number, data: PetUpdateRequest): Promise<PetDetail> {
  return apiJson<PetDetail>(`/api/v1/pets/${petId}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });
}

export async function deletePet(petId: number): Promise<void> {
  await apiJson<Record<string, unknown>>(`/api/v1/pets/${petId}`, {
    method: "DELETE",
  });
}

export type PetAvatarUploadResponse = {
  petId: number;
  avatarUrl: string;
};

export type PetAvatarPresignedUrlResponse = {
  uploadUrl: string;
  avatarKey: string;
  publicUrl: string;
  expiresInSeconds: number;
};

export async function getAvatarPresignedUrl(petId: number, contentType: string): Promise<PetAvatarPresignedUrlResponse> {
  return apiJson<PetAvatarPresignedUrlResponse>(
    `/api/v1/pets/${petId}/avatar/presigned-url?contentType=${encodeURIComponent(contentType)}`
  );
}

export async function uploadToOss(uploadUrl: string, file: File): Promise<void> {
  const response = await fetch(uploadUrl, {
    method: "PUT",
    headers: {
      "Content-Type": file.type,
    },
    body: file,
  });

  if (!response.ok) {
    throw new Error("OSS 上传失败");
  }
}

export async function confirmAvatarUpload(petId: number, avatarKey: string): Promise<PetAvatarUploadResponse> {
  return apiJson<PetAvatarUploadResponse>(
    `/api/v1/pets/${petId}/avatar/confirm?avatarKey=${encodeURIComponent(avatarKey)}`,
    { method: "POST" }
  );
}

export async function uploadPetAvatar(petId: number, file: File): Promise<PetAvatarUploadResponse> {
  const presignedInfo = await getAvatarPresignedUrl(petId, file.type);
  
  await uploadToOss(presignedInfo.uploadUrl, file);
  
  const result = await confirmAvatarUpload(petId, presignedInfo.avatarKey);
  
  return result;
}

export function petSummaryToFrontend(s: PetSummary, isActive = false): Pet {
  return {
    id: s.id,
    name: s.name,
    type: s.species,
    breed: s.breed ?? "",
    emoji: s.avatarEmoji ?? (s.species === "cat" ? "🐈" : "🐕"),
    avatarUrl: s.avatarUrl ?? undefined,
    gender: (s.gender as "male" | "female") ?? "male",
    birthday: s.birthday ?? "",
    weight: 0,
    sterilized: s.neutered ?? false,
    chipped: s.microchipped ?? false,
    allergies: "",
    diseases: "",
    foodBrand: "",
    hospital: "",
    notes: "",
    isActive,
  };
}

export function petDetailToFrontend(d: PetDetail, isActive = false): Pet {
  return {
    id: d.id,
    name: d.name,
    type: d.species,
    breed: d.breed ?? "",
    emoji: d.avatarEmoji ?? (d.species === "cat" ? "🐈" : "🐕"),
    avatarUrl: d.avatarUrl ?? undefined,
    gender: (d.gender as "male" | "female") ?? "male",
    birthday: d.birthday ?? "",
    weight: d.weightKg ?? 0,
    sterilized: d.neutered ?? false,
    chipped: d.microchipped ?? false,
    allergies: d.allergies ?? "",
    diseases: d.chronicConditions ?? "",
    foodBrand: d.mainFoodBrand ?? "",
    hospital: d.vetHospital ?? "",
    notes: d.notes ?? "",
    isActive,
  };
}

export function frontendPetToCreateRequest(pet: Pet): PetCreateRequest {
  return {
    name: pet.name,
    species: pet.type,
    breed: pet.breed || undefined,
    birthday: pet.birthday || undefined,
    weightKg: pet.weight || undefined,
    gender: pet.gender,
    neutered: pet.sterilized,
    microchipped: pet.chipped,
    avatarEmoji: pet.emoji === "AI" ? undefined : pet.emoji,
    allergies: pet.allergies || undefined,
    chronicConditions: pet.diseases || undefined,
    mainFoodBrand: pet.foodBrand || undefined,
    vetHospital: pet.hospital || undefined,
    notes: pet.notes || undefined,
  };
}

export function frontendPetToUpdateRequest(pet: Pet): PetUpdateRequest {
  return frontendPetToCreateRequest(pet);
}
