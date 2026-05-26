package jiangxiaopeng.ai.diet.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public class WaterRecord {

    private Long id;
    private Long petId;
    private Integer waterAmount;
    private Instant recordTime;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    public WaterRecord() {}

    public static WaterRecord create(Long petId, Integer waterAmount, Instant recordTime) {
        WaterRecord record = new WaterRecord();
        record.petId = petId;
        record.waterAmount = waterAmount;
        record.recordTime = recordTime;
        record.createdAt = Instant.now();
        record.updatedAt = Instant.now();
        return record;
    }

    public void update(Integer waterAmount, Instant recordTime) {
        if (waterAmount != null) this.waterAmount = waterAmount;
        if (recordTime != null) this.recordTime = recordTime;
        this.updatedAt = Instant.now();
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public LocalDate getRecordDate() {
        return recordTime.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public Integer getWaterAmount() { return waterAmount; }
    public void setWaterAmount(Integer waterAmount) { this.waterAmount = waterAmount; }
    public Instant getRecordTime() { return recordTime; }
    public void setRecordTime(Instant recordTime) { this.recordTime = recordTime; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
}
