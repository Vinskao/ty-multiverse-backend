package tw.com.tymbackend.module.people.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "people_image")
public class PeopleImage {
    
    @Id
    @Column(name = "codeName")
    private String codeName;
    
    @Column(name = "image", columnDefinition = "TEXT")
    private String image;
    
    // Default constructor
    public PeopleImage() {
    }
    
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
