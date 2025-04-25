package tw.com.tymbackend.module.livestock.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tw.com.tymbackend.module.livestock.domain.vo.Livestock;
import tw.com.tymbackend.module.livestock.service.LivestockService;

import java.util.List;

@RestController
@RequestMapping("/livestock")
public class LivestockController {

    @Autowired
    private LivestockService livestockService;

    @Autowired
    private LivestockWSController livestockWebSocketController;

    @PostMapping("/all")
    public ResponseEntity<List<Livestock>> getAllLivestock() {
        return ResponseEntity.ok(livestockService.getAllLivestock());
    }

    @PostMapping("/{id}")
    public ResponseEntity<Livestock> getLivestockById(@PathVariable Long id) {
        return livestockService.getLivestockById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/by-livestock")
    public ResponseEntity<Livestock> getLivestockByName(@RequestBody String livestock) {
        return livestockService.getLivestockByName(livestock)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Livestock> createLivestock(@RequestBody Livestock livestock) {
        Livestock saved = livestockService.saveLivestock(livestock);
        livestockWebSocketController.broadcastLivestockUpdate(saved);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<Livestock> updateLivestock(@PathVariable Long id, @RequestBody Livestock livestock) {
        livestock.setId(id);
        Livestock updated = livestockService.updateLivestock(livestock);
        livestockWebSocketController.broadcastLivestockUpdate(updated);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<Void> deleteLivestock(@PathVariable Long id) {
        livestockService.deleteLivestock(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/buyer/{buyer}")
    public ResponseEntity<List<Livestock>> getLivestockByBuyer(@PathVariable String buyer) {
        return ResponseEntity.ok(livestockService.getLivestockByBuyer(buyer));
    }
} 