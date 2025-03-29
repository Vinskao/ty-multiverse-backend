package tw.com.tymbackend.module.people.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tw.com.tymbackend.module.people.dao.PeopleImageRepository;
import tw.com.tymbackend.module.people.domain.vo.PeopleImage;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class PeopleImageService {
    
    @Autowired
    private PeopleImageRepository peopleImageRepository;
    
    /**
     * Get all people images
     * 
     * @return list of all people images
     */
    public List<PeopleImage> getAllPeopleImages() {
        return peopleImageRepository.findAll();
    }
    
    /**
     * Get people image by code name
     * 
     * @param codeName the code name of the person
     * @return the people image
     * @throws NoSuchElementException if no image is found with the given code name
     */
    public PeopleImage getPeopleImageByCodeName(String codeName) {
        Optional<PeopleImage> peopleImage = Optional.ofNullable(peopleImageRepository.findByCodeName(codeName));
        return peopleImage.orElseThrow(() -> new NoSuchElementException("No image found for code name: " + codeName));
    }
    
    /**
     * Save or update a people image
     * 
     * @param peopleImage the people image to save or update
     * @return the saved people image
     */
    @Transactional
    public PeopleImage savePeopleImage(PeopleImage peopleImage) {
        return peopleImageRepository.save(peopleImage);
    }
    
    /**
     * Delete a people image by code name
     * 
     * @param codeName the code name of the person whose image is to be deleted
     * @throws NoSuchElementException if no image is found with the given code name
     */
    @Transactional
    public void deletePeopleImage(String codeName) {
        if (!peopleImageRepository.existsByCodeName(codeName)) {
            throw new NoSuchElementException("No image found for code name: " + codeName);
        }
        peopleImageRepository.deleteById(codeName);
    }
    
    /**
     * Check if a people image exists
     * 
     * @param codeName the code name to check
     * @return true if image exists, false otherwise
     */
    public boolean peopleImageExists(String codeName) {
        return peopleImageRepository.existsByCodeName(codeName);
    }
}
