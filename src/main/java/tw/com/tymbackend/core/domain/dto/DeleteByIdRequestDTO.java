package tw.com.tymbackend.core.domain.dto;
import java.util.Objects;

public class DeleteByIdRequestDTO {
    private Integer id;

    public DeleteByIdRequestDTO() {
    }

    public DeleteByIdRequestDTO(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public DeleteByIdRequestDTO id(Integer id) {
        setId(id);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof DeleteByIdRequestDTO)) {
            return false;
        }
        DeleteByIdRequestDTO deleteByIdRequestDTO = (DeleteByIdRequestDTO) o;
        return Objects.equals(id, deleteByIdRequestDTO.id);
    }

    @Override
    public String toString() {
        return "{" +
            " id='" + getId() + "'" +
            "}";
    }
    
}
