package jiangxiaopeng.ai.pet.application;

import jiangxiaopeng.ai.pet.application.dto.PetAvatarPresignedUrlResponse;
import jiangxiaopeng.ai.pet.application.dto.PetAvatarUploadResponse;
import jiangxiaopeng.ai.pet.application.dto.PetCreateCommand;
import jiangxiaopeng.ai.pet.application.dto.PetDetailDto;
import jiangxiaopeng.ai.pet.application.dto.PetSummaryDto;
import jiangxiaopeng.ai.pet.application.dto.PetUpdateCommand;
import jiangxiaopeng.ai.pet.domain.model.Gender;
import jiangxiaopeng.ai.pet.domain.model.Pet;
import jiangxiaopeng.ai.pet.domain.model.PetSpecies;
import jiangxiaopeng.ai.pet.domain.repository.PetRepository;
import jiangxiaopeng.ai.shared.exception.BusinessException;
import jiangxiaopeng.ai.shared.exception.ErrorCode;
import jiangxiaopeng.ai.storage.domain.service.StorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.List;
import java.util.UUID;

@Service
public class PetApplicationService {

    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024;
    private static final String AVATAR_PATH_PREFIX = "ai-chat/images/pet_avatar/";
    private static final long PRESIGNED_URL_EXPIRATION_SECONDS = 600;

    private final PetRepository petRepository;
    private final StorageService storageService;

    public PetApplicationService(PetRepository petRepository,
                                  StorageService storageService) {
        this.petRepository = petRepository;
        this.storageService = storageService;
    }

    @Transactional(readOnly = true)
    public List<PetSummaryDto> listPets(Long userId) {
        return petRepository.findByUserId(userId).stream()
                .map(PetSummaryDto::fromDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public PetDetailDto getPetDetail(Long petId, Long userId) {
        Pet pet = petRepository.findByIdAndUserId(petId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PET_001));
        return PetDetailDto.fromDomain(pet);
    }

    @Transactional
    public PetDetailDto createPet(PetCreateCommand command, Long userId) {
        Pet pet = Pet.create(userId, command.name(), PetSpecies.fromValue(command.species()));
        applyCommandToPet(pet, command);
        pet = petRepository.save(pet);
        return PetDetailDto.fromDomain(pet);
    }

    @Transactional
    public PetDetailDto updatePet(Long petId, PetUpdateCommand command, Long userId) {
        Pet pet = petRepository.findByIdAndUserId(petId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PET_001));

        pet.setName(command.name());
        pet.setSpecies(PetSpecies.fromValue(command.species()));
        applyUpdateCommandToPet(pet, command);

        pet = petRepository.save(pet);
        return PetDetailDto.fromDomain(pet);
    }

    @Transactional
    public void deletePet(Long petId, Long userId) {
        Pet pet = petRepository.findByIdAndUserId(petId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PET_001));
        petRepository.softDelete(pet.getId());
    }

    @Transactional
    public PetAvatarUploadResponse uploadAvatar(Long petId, Long userId, MultipartFile file) {
        Pet pet = petRepository.findByIdAndUserId(petId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PET_001));

        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "请选择要上传的图片");
        }

        String contentType = file.getContentType();
        if (!isAllowedImageType(contentType)) {
            throw new BusinessException(ErrorCode.FILE_002, "仅支持 JPG 或 PNG 格式图片");
        }

        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BusinessException(ErrorCode.FILE_001, "图片大小不能超过5MB");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = petId + "_" + UUID.randomUUID().toString().replace("-", "") + extension;
        String storageKey = AVATAR_PATH_PREFIX + fileName;

        try {
            storageService.upload(storageKey, file.getInputStream(), contentType, file.getSize());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.MSG_001, "头像上传失败: " + e.getMessage());
        }

        String avatarUrl = storageService.getPublicUrl(storageKey);
        pet.setAvatarUrl(avatarUrl);
        petRepository.save(pet);

        return new PetAvatarUploadResponse(petId, avatarUrl);
    }

    public PetAvatarPresignedUrlResponse getAvatarUploadUrl(Long petId, Long userId, String contentType) {
        petRepository.findByIdAndUserId(petId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PET_001));

        if (!isAllowedImageType(contentType)) {
            throw new BusinessException(ErrorCode.FILE_002, "仅支持 JPG 或 PNG 格式图片");
        }

        String extension = getExtensionFromContentType(contentType);
        String fileName = petId + "_" + UUID.randomUUID().toString().replace("-", "") + extension;
        String avatarKey = AVATAR_PATH_PREFIX + fileName;

        URL uploadUrl = storageService.generatePresignedUploadUrl(avatarKey, contentType, PRESIGNED_URL_EXPIRATION_SECONDS);
        String publicUrl = storageService.getPublicUrl(avatarKey);

        return new PetAvatarPresignedUrlResponse(
                uploadUrl.toString(),
                avatarKey,
                publicUrl,
                PRESIGNED_URL_EXPIRATION_SECONDS
        );
    }

    @Transactional
    public PetAvatarUploadResponse confirmAvatarUpload(Long petId, Long userId, String avatarKey) {
        Pet pet = petRepository.findByIdAndUserId(petId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PET_001));

        if (avatarKey == null || avatarKey.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "avatarKey 不能为空");
        }

        if (!avatarKey.startsWith(AVATAR_PATH_PREFIX)) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "无效的 avatarKey");
        }

        if (!storageService.exists(avatarKey)) {
            throw new BusinessException(ErrorCode.FILE_003, "图片未上传成功，请重试");
        }

        pet.setAvatarUrl(avatarKey);
        petRepository.save(pet);

        String publicUrl = storageService.getPublicUrl(avatarKey);
        return new PetAvatarUploadResponse(petId, publicUrl);
    }

    private boolean isAllowedImageType(String contentType) {
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png")
        );
    }

    private String getExtensionFromContentType(String contentType) {
        return switch (contentType) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            default -> ".jpg";
        };
    }

    private void applyCommandToPet(Pet pet, PetCreateCommand command) {
        pet.setBreed(command.breed());
        pet.setBirthday(command.birthday());
        pet.setWeightKg(command.weightKg());
        pet.setGender(command.gender() != null ? Gender.fromValue(command.gender()) : null);
        pet.setNeutered(command.neutered() != null ? command.neutered() : false);
        pet.setMicrochipped(command.microchipped() != null ? command.microchipped() : false);
        pet.setAvatarUrl(command.avatarUrl());
        pet.setAvatarEmoji(command.avatarEmoji());
        pet.setAllergies(command.allergies());
        pet.setChronicConditions(command.chronicConditions());
        pet.setMainFoodBrand(command.mainFoodBrand());
        pet.setVetHospital(command.vetHospital());
        pet.setNotes(command.notes());
    }

    private void applyUpdateCommandToPet(Pet pet, PetUpdateCommand command) {
        pet.setBreed(command.breed());
        pet.setBirthday(command.birthday());
        pet.setWeightKg(command.weightKg());
        pet.setGender(command.gender() != null ? Gender.fromValue(command.gender()) : null);
        pet.setNeutered(command.neutered() != null ? command.neutered() : false);
        pet.setMicrochipped(command.microchipped() != null ? command.microchipped() : false);
        pet.setAvatarUrl(command.avatarUrl());
        pet.setAvatarEmoji(command.avatarEmoji());
        pet.setAllergies(command.allergies());
        pet.setChronicConditions(command.chronicConditions());
        pet.setMainFoodBrand(command.mainFoodBrand());
        pet.setVetHospital(command.vetHospital());
        pet.setNotes(command.notes());
    }
}
