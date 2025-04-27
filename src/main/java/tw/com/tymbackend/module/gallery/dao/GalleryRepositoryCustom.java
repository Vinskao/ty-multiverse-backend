package tw.com.tymbackend.module.gallery.dao;

import tw.com.tymbackend.module.gallery.domain.vo.Gallery;

import java.util.List;

/**
 * Custom repository interface for Gallery
 */
public interface GalleryRepositoryCustom {
    
    /**
     * Find Gallery by owner
     * 
     * @param owner the owner of the gallery
     * @return the Gallery with the given owner
     */
    Gallery findByOwner(String owner);
    
    /**
     * Find Gallery by buyer
     * 
     * @param buyer the buyer of the gallery
     * @return the Gallery with the given buyer
     */
    Gallery findByBuyer(String buyer);
    
    /**
     * Find Gallery by livestock
     * 
     * @param livestock the livestock of the gallery
     * @return the Gallery with the given livestock
     */
    Gallery findByLivestock(String livestock);
    
    /**
     * Save all galleries
     * 
     * @param galleries the galleries to save
     * @return the saved galleries
     */
    List<Gallery> saveAllGalleries(List<Gallery> galleries);
} 