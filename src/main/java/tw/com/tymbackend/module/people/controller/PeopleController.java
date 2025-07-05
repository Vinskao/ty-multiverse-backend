package tw.com.tymbackend.module.people.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tw.com.tymbackend.module.people.domain.dto.PeopleNameRequestDTO;
import tw.com.tymbackend.module.people.domain.vo.People;
import tw.com.tymbackend.module.people.service.PeopleService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/people")
public class PeopleController {

    @Autowired
    private PeopleService peopleService;

    // 插入 1 個 (接收 JSON)
    @PostMapping("/insert")
    public ResponseEntity<?> insertPeople(@RequestBody People people) {
        try {
            People savedPeople = peopleService.insertPerson(people);
            return new ResponseEntity<>(savedPeople, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Invalid input: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Internal server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Unexpected error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 更新 1 個 (接收 JSON)
    @PostMapping("/update")
    public ResponseEntity<?> updatePeople(@RequestBody People people) {
        try {
            // 驗證輸入
            if (people == null || people.getName() == null || people.getName().trim().isEmpty()) {
                return new ResponseEntity<>("Invalid input: name is required", HttpStatus.BAD_REQUEST);
            }
            
            // 嘗試更新
            People updatedPeople = peopleService.updatePerson(people);
            return new ResponseEntity<>(updatedPeople, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Person not found: " + e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (org.hibernate.StaleObjectStateException e) {
            // 樂觀鎖定衝突，返回衝突狀態
            return new ResponseEntity<>("Concurrent update detected: " + e.getMessage(), HttpStatus.CONFLICT);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // 數據完整性違規，返回錯誤請求狀態
            return new ResponseEntity<>("Data integrity violation: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Internal server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Unexpected error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 插入多個 (接收 JSON)
    @PostMapping("/insert-multiple")
    public ResponseEntity<?> insertMultiplePeople(@RequestBody List<People> peopleList) {
        try {
            List<People> savedPeople = peopleService.saveAllPeople(peopleList);
            return new ResponseEntity<>(savedPeople, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Invalid input: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Internal server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Unexpected error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 搜尋 1 個 (接收 id 傳出 JSON)
    @PostMapping("/get")
    public ResponseEntity<?> getPeopleById(@RequestBody PeopleNameRequestDTO request) {
        try {
            Optional<People> people = peopleService.getPeopleByName(request.getName());
            if (people.isPresent()) {
                return new ResponseEntity<>(people.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Person not found", HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Internal server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Unexpected error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 搜尋所有 (傳出 JSON)
    @PostMapping("/get-all")
    public ResponseEntity<?> getAllPeople() {
        try {
            List<People> people = peopleService.getAllPeople();
            return new ResponseEntity<>(people, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Internal server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Unexpected error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 搜尋 name (接收 name 傳出 JSON)
    @PostMapping("/get-by-name")
    public ResponseEntity<?> getPeopleByName(@RequestBody PeopleNameRequestDTO request) {
        try {
            Optional<People> people = peopleService.getPeopleByName(request.getName());
            if (people.isPresent()) {
                return new ResponseEntity<>(people.get(), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Person not found", HttpStatus.NOT_FOUND);
            }
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Internal server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Unexpected error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 刪除所有
    @PostMapping("/delete-all")
    public ResponseEntity<?> deleteAllPeople() {
        try {
            peopleService.deleteAllPeople();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Internal server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Unexpected error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 取得所有人的名字
    @GetMapping("/names")
    public ResponseEntity<?> getAllPeopleNames() {
        try {
            List<String> names = peopleService.getAllPeopleNames();
            return new ResponseEntity<>(names, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Internal server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            return new ResponseEntity<>("Unexpected error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
