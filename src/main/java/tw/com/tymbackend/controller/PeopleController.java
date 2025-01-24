package tw.com.tymbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.com.tymbackend.domain.dto.PeopleNameRequestDTO;
import tw.com.tymbackend.domain.vo.People;
import tw.com.tymbackend.service.PeopleService;

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
            People updatedPeople = peopleService.updatePerson(people);
            return new ResponseEntity<>(updatedPeople, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Person not found: " + e.getMessage(), HttpStatus.NOT_FOUND);
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
}
