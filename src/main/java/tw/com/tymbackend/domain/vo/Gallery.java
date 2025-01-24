package tw.com.tymbackend.domain.vo;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "gallery")
public class Gallery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // 對應資料表中的 id 欄位
    private Integer id;

    @Column(name = "image_base64", columnDefinition = "TEXT")
    private String imageBase64;

    @Column(name = "upload_time", nullable = false, updatable = false) // 對應資料表中的 upload_time 欄位
    private LocalDateTime uploadTime;


    public Gallery() {
    }

    public Gallery(Integer id, String imageBase64, LocalDateTime uploadTime) {
        this.id = id;
        this.imageBase64 = imageBase64;
        this.uploadTime = uploadTime;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getImageBase64() {
        return this.imageBase64;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }

    public LocalDateTime getUploadTime() {
        return this.uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public Gallery id(Integer id) {
        setId(id);
        return this;
    }

    public Gallery imageBase64(String imageBase64) {
        setImageBase64(imageBase64);
        return this;
    }

    public Gallery uploadTime(LocalDateTime uploadTime) {
        setUploadTime(uploadTime);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof Gallery)) {
            return false;
        }
        Gallery gallery = (Gallery) o;
        return Objects.equals(id, gallery.id) && Objects.equals(imageBase64, gallery.imageBase64) && Objects.equals(uploadTime, gallery.uploadTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, imageBase64, uploadTime);
    }

    @Override
    public String toString() {
        return "{" +
            " id='" + getId() + "'" +
            ", imageBase64='" + getImageBase64() + "'" +
            ", uploadTime='" + getUploadTime() + "'" +
            "}";
    }

    
}
