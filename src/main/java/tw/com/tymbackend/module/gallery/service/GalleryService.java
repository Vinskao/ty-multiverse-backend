package tw.com.tymbackend.module.gallery.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import tw.com.tymbackend.core.factory.RepositoryFactory;
import tw.com.tymbackend.core.service.BaseService;
import tw.com.tymbackend.module.gallery.dao.GalleryRepository;
import tw.com.tymbackend.module.gallery.domain.vo.Gallery;

@Service
public class GalleryService extends BaseService {

    private final RepositoryFactory repositoryFactory;
    private final GalleryRepository galleryRepository;

    public GalleryService(RepositoryFactory repositoryFactory, GalleryRepository galleryRepository) {
        this.repositoryFactory = repositoryFactory;
        this.galleryRepository = galleryRepository;
    }

    public List<Gallery> getAllImages() {
        return repositoryFactory.findAll(Gallery.class);
    }

    public Optional<Gallery> getImageById(Integer id) {
        return repositoryFactory.findById(Gallery.class, id);
    }

    public Gallery saveImage(Gallery gallery) {
        return repositoryFactory.save(gallery);
    }

    public void deleteImage(Integer id) {
        repositoryFactory.deleteById(Gallery.class, id);
    }

    public Gallery updateImage(Integer id, String newBase64Image) {
        Optional<Gallery> optionalGallery = repositoryFactory.findById(Gallery.class, id);
        if (optionalGallery.isPresent()) {
            Gallery gallery = optionalGallery.get();
            gallery.setImageBase64(newBase64Image);
            try {
                return repositoryFactory.save(gallery);
            } catch (ObjectOptimisticLockingFailureException e) {
                throw new RuntimeException("圖片已被其他使用者修改，請重新整理後再試。", e);
            }
        } else {
            throw new RuntimeException("Image not found with ID: " + id);
        }
    }
}

