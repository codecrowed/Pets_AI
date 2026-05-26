package jiangxiaopeng.ai.diet.infrastructure.persistence;

import jiangxiaopeng.ai.shared.domain.BaseEntity;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "pet_water_records")
public class WaterRecordEntity extends BaseEntity {

    @Column(name = "pet_id", nullable = false)
    private Long petId;

    @Column(name = "water_amount", nullable = false)
    private Integer waterAmount;

    @Column(name = "record_time", nullable = false)
    private Instant recordTime;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    // Getters and Setters
    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }
    public Integer getWaterAmount() { return waterAmount; }
    public void setWaterAmount(Integer waterAmount) { this.waterAmount = waterAmount; }
    public Instant getRecordTime() { return recordTime; }
    public void setRecordTime(Instant recordTime) { this.recordTime = recordTime; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
}
