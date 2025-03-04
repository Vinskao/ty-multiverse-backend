package tw.com.tymbackend.module.gallery.domain.dto;
import java.util.Objects;

public class GalleryUpdateRequestDTO {
    private Integer id;
    private String imageBase64;

    public GalleryUpdateRequestDTO() {
    }

    public GalleryUpdateRequestDTO(Integer id, String imageBase64) {
        this.id = id;
        this.imageBase64 = imageBase64;
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

    public GalleryUpdateRequestDTO id(Integer id) {
        setId(id);
        return this;
    }

    public GalleryUpdateRequestDTO imageBase64(String imageBase64) {
        setImageBase64(imageBase64);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof GalleryUpdateRequestDTO)) {
            return false;
        }
        GalleryUpdateRequestDTO galleryUpdateRequestDTO = (GalleryUpdateRequestDTO) o;
        return Objects.equals(id, galleryUpdateRequestDTO.id) && Objects.equals(imageBase64, galleryUpdateRequestDTO.imageBase64);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, imageBase64);
    }

    @Override
    public String toString() {
        return "{" +
            " id='" + getId() + "'" +
            ", imageBase64='" + getImageBase64() + "'" +
            "}";
    }

}
