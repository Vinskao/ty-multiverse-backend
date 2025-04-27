package tw.com.tymbackend.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 基礎實體類別，提供所有實體共用的基本屬性。
 * 此類別使用 JPA 審計功能來追蹤實體的創建和修改資訊。
 *
 * @author TYM Backend Team
 * @version 1.0
 * @since 2024-01-01
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    /**
     * 實體版本號，用於樂觀鎖定。
     */
    @Version
    @Column(name = "version")
    private Long version;

    /**
     * 實體創建時間。
     * 此欄位在實體創建時自動設置，且不可更新。
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 實體最後更新時間。
     * 此欄位在實體更新時自動更新。
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 實體創建者。
     * 此欄位在實體創建時自動設置，且不可更新。
     */
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    /**
     * 實體最後更新者。
     * 此欄位在實體更新時自動更新。
     */
    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    /**
     * 獲取實體版本號。
     *
     * @return 實體的版本號
     */
    public Long getVersion() {
        return version;
    }

    /**
     * 設置實體版本號。
     *
     * @param version 要設置的版本號
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * 獲取實體創建時間。
     *
     * @return 實體的創建時間
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * 設置實體創建時間。
     *
     * @param createdAt 要設置的創建時間
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * 獲取實體最後更新時間。
     *
     * @return 實體的最後更新時間
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * 設置實體最後更新時間。
     *
     * @param updatedAt 要設置的最後更新時間
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * 獲取實體創建者。
     *
     * @return 實體的創建者
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * 設置實體創建者。
     *
     * @param createdBy 要設置的創建者
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * 獲取實體最後更新者。
     *
     * @return 實體的最後更新者
     */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * 設置實體最後更新者。
     *
     * @param updatedBy 要設置的最後更新者
     */
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
} 