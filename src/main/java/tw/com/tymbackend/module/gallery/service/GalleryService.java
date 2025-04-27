package tw.com.tymbackend.module.gallery.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import tw.com.tymbackend.core.repository.DataAccessor;
import tw.com.tymbackend.module.gallery.dao.GalleryRepository;
import tw.com.tymbackend.module.gallery.domain.vo.Gallery;

@Service
public class GalleryService {

    private final DataAccessor<Gallery, Integer> galleryDataAccessor;
    private final GalleryRepository galleryRepository;

    public GalleryService(DataAccessor<Gallery, Integer> galleryDataAccessor, GalleryRepository galleryRepository) {
        this.galleryDataAccessor = galleryDataAccessor;
        this.galleryRepository = galleryRepository;
    }

    public List<Gallery> getAllImages() {
        return galleryDataAccessor.findAll();
    }

    public Optional<Gallery> getImageById(Integer id) {
        return galleryDataAccessor.findById(id);
    }

    public Gallery saveImage(Gallery gallery) {
        return galleryDataAccessor.save(gallery);
    }

    public void deleteImage(Integer id) {
        galleryDataAccessor.deleteById(id);
    }

    public Gallery updateImage(Integer id, String newBase64Image) {
        Optional<Gallery> optionalGallery = galleryDataAccessor.findById(id);
        if (optionalGallery.isPresent()) {
            Gallery gallery = optionalGallery.get();
            gallery.setImageBase64(newBase64Image);
            try {
                return galleryDataAccessor.save(gallery);
            } catch (ObjectOptimisticLockingFailureException e) {
                throw new RuntimeException("圖片已被其他使用者修改，請重新整理後再試。", e);
            }
        } else {
            throw new RuntimeException("Image not found with ID: " + id);
        }
    }
}

