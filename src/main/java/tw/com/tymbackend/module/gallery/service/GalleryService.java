package tw.com.tymbackend.module.gallery.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

import tw.com.tymbackend.module.gallery.dao.GalleryRepository;
import tw.com.tymbackend.module.gallery.domain.vo.Gallery;

@Service
public class GalleryService {

    private final GalleryRepository galleryRepository;

    public GalleryService(GalleryRepository galleryRepository) {
        this.galleryRepository = galleryRepository;
    }

    public List<Gallery> getAllImages() {
        return galleryRepository.findAll();
    }

    public Optional<Gallery> getImageById(Integer id) {
        return galleryRepository.findById(id);
    }

    public Gallery saveImage(Gallery gallery) {
        return galleryRepository.save(gallery);
    }

    public void deleteImage(Integer id) {
        galleryRepository.deleteById(id);
    }

    public Gallery updateImage(Integer id, String newBase64Image) {
        Optional<Gallery> optionalGallery = galleryRepository.findById(id);
        if (optionalGallery.isPresent()) {
            Gallery gallery = optionalGallery.get();
            gallery.setImageBase64(newBase64Image);
            try {
                return galleryRepository.save(gallery);
            } catch (ObjectOptimisticLockingFailureException e) {
                throw new RuntimeException("圖片已被其他使用者修改，請重新整理後再試。", e);
            }
        } else {
            throw new RuntimeException("Image not found with ID: " + id);
        }
    }
}

