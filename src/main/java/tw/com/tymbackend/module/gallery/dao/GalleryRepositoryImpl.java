package tw.com.tymbackend.module.gallery.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import tw.com.tymbackend.module.gallery.domain.vo.Gallery;

import java.util.List;

/**
 * Implementation of GalleryRepository
 */
@Repository
public class GalleryRepositoryImpl implements GalleryRepositoryCustom {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * Find Gallery by owner
     * 
     * @param owner the owner of the gallery
     * @return the Gallery with the given owner
     */
    @Override
    public Gallery findByOwner(String owner) {
        // Implementation using EntityManager
        return entityManager.createQuery("SELECT g FROM Gallery g WHERE g.owner = :owner", Gallery.class)
                .setParameter("owner", owner)
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Find Gallery by buyer
     * 
     * @param buyer the buyer of the gallery
     * @return the Gallery with the given buyer
     */
    @Override
    public Gallery findByBuyer(String buyer) {
        // Implementation using EntityManager
        return entityManager.createQuery("SELECT g FROM Gallery g WHERE g.buyer = :buyer", Gallery.class)
                .setParameter("buyer", buyer)
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Find Gallery by livestock
     * 
     * @param livestock the livestock of the gallery
     * @return the Gallery with the given livestock
     */
    @Override
    public Gallery findByLivestock(String livestock) {
        // Implementation using EntityManager
        return entityManager.createQuery("SELECT g FROM Gallery g WHERE g.livestock = :livestock", Gallery.class)
                .setParameter("livestock", livestock)
                .getResultList()
                .stream()
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Save all galleries
     * 
     * @param galleries the galleries to save
     * @return the saved galleries
     */
    @Override
    public List<Gallery> saveAllGalleries(List<Gallery> galleries) {
        // Implementation using EntityManager
        galleries.forEach(gallery -> {
            if (gallery.getId() == null) {
                entityManager.persist(gallery);
            } else {
                entityManager.merge(gallery);
            }
        });
        entityManager.flush();
        return galleries;
    }
} 