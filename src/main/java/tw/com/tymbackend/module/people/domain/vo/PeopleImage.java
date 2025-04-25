package tw.com.tymbackend.module.people.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "people_image")
@Data
@NoArgsConstructor
public class PeopleImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version", nullable = true)
    private Long version = 0L;
    
    @Column(name = "codeName")
    private String codeName;
    
    @Column(name = "image", columnDefinition = "TEXT")
    private String image;
    
    // Constructor with parameters
    public PeopleImage(String codeName, String image) {
        this.codeName = codeName;
        this.image = image;
    }
    
    // Getters and Setters
    public String getCodeName() {
        return codeName;
    }
    
    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }
    
    public String getImage() {
        return image;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
}
