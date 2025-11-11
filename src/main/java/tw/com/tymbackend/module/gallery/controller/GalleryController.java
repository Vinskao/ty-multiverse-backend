package tw.com.tymbackend.module.gallery.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tw.com.tymbackend.module.gallery.domain.dto.DeleteByIdRequestDTO;
import tw.com.tymbackend.module.gallery.domain.dto.GalleryUpdateRequestDTO;
import tw.com.tymbackend.module.gallery.domain.vo.Gallery;
import tw.com.tymbackend.module.gallery.service.GalleryService;
import tw.com.ty.common.response.BackendApiResponse;

@RestController
@RequestMapping("/gallery")
public class GalleryController {

    @Autowired
    private GalleryService galleryService;

    @PostMapping("/getAll")
    public ResponseEntity<BackendApiResponse<List<Gallery>>> getAllImages() {
        try {
            List<Gallery> galleries = galleryService.getAllImages();
            return ResponseEntity.ok(BackendApiResponse.success("获取所有图片成功", galleries));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(BackendApiResponse.internalError("获取图片失败", e.getMessage()));
        }
    }

    @PostMapping("/getById")
    public ResponseEntity<Gallery> getImageById(@RequestBody Integer id) {
        try {
            Optional<Gallery> gallery = galleryService.getImageById(id);
            return gallery.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/save")
    public ResponseEntity<Gallery> saveImage(@RequestBody Gallery gallery) {
        try {
            Gallery savedGallery = galleryService.saveImage(gallery);
            return ResponseEntity.ok(savedGallery);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/update")
    public ResponseEntity<Gallery> updateImage(@RequestBody GalleryUpdateRequestDTO updateRequest) {
        try {
            Gallery updatedGallery = galleryService.updateImage(updateRequest.getId(), updateRequest.getImageBase64());
            return ResponseEntity.ok(updatedGallery);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> deleteImage(@RequestBody DeleteByIdRequestDTO id) {
        try {
            galleryService.deleteImage(id.getId());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }


}
