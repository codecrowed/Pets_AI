const PET_ID_KEY = "pawpal_current_pet_id";

export function getCurrentPetId(): number | null {
  if (typeof window === "undefined") return null;
  const stored = localStorage.getItem(PET_ID_KEY);
  if (!stored) return null;
  const id = parseInt(stored, 10);
  return isNaN(id) ? null : id;
}

export function setCurrentPetId(petId: number | null): void {
  if (typeof window === "undefined") return;
  if (petId === null) {
    localStorage.removeItem(PET_ID_KEY);
  } else {
    localStorage.setItem(PET_ID_KEY, String(petId));
  }
}

export function clearCurrentPetId(): void {
  if (typeof window === "undefined") return;
  localStorage.removeItem(PET_ID_KEY);
}
