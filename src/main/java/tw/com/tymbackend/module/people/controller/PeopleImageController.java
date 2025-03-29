package tw.com.tymbackend.module.people.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.com.tymbackend.module.people.domain.vo.PeopleImage;
import tw.com.tymbackend.module.people.service.PeopleImageService;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/people-images")
public class PeopleImageController {
    
    @Autowired
    private PeopleImageService peopleImageService;
    
    /**
     * Get all people images
     * 
     * @return list of all people images
     */
    @GetMapping
    public ResponseEntity<List<PeopleImage>> getAllPeopleImages() {
        List<PeopleImage> peopleImages = peopleImageService.getAllPeopleImages();
        return ResponseEntity.ok(peopleImages);
    }
    
    /**
     * Get people image by code name
     * 
     * @param codeName the code name of the person
     * @return the people image if found
     */
    @GetMapping("/{codeName}")
    public ResponseEntity<?> getPeopleImageByCodeName(@PathVariable String codeName) {
        try {
            PeopleImage peopleImage = peopleImageService.getPeopleImageByCodeName(codeName);
            return ResponseEntity.ok(peopleImage);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    
    /**
     * Create a new people image
     * 
     * @param peopleImage the people image to create
     * @return the created people image
     */
    @PostMapping
    public ResponseEntity<?> createPeopleImage(@RequestBody PeopleImage peopleImage) {
        try {
            boolean exists = peopleImageService.peopleImageExists(peopleImage.getCodeName());
            if (exists) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Image already exists for code name: " + peopleImage.getCodeName());
            }
            PeopleImage savedPeopleImage = peopleImageService.savePeopleImage(peopleImage);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPeopleImage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating image: " + e.getMessage());
        }
    }
    
    /**
     * Update an existing people image
     * 
     * @param codeName the code name of the person
     * @param peopleImage the updated people image
     * @return the updated people image
     */
    @PutMapping("/{codeName}")
    public ResponseEntity<?> updatePeopleImage(@PathVariable String codeName, @RequestBody PeopleImage peopleImage) {
        try {
            if (!peopleImageService.peopleImageExists(codeName)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("No image found for code name: " + codeName);
            }
            
            // Ensure the code name in the path matches the one in the body
            peopleImage.setCodeName(codeName);
            
            PeopleImage updatedPeopleImage = peopleImageService.savePeopleImage(peopleImage);
            return ResponseEntity.ok(updatedPeopleImage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating image: " + e.getMessage());
        }
    }
    
    /**
     * Delete a people image
     * 
     * @param codeName the code name of the person
     * @return success or error message
     */
    @DeleteMapping("/{codeName}")
    public ResponseEntity<?> deletePeopleImage(@PathVariable String codeName) {
        try {
            peopleImageService.deletePeopleImage(codeName);
            return ResponseEntity.ok("Image deleted successfully for code name: " + codeName);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting image: " + e.getMessage());
        }
    }
}
