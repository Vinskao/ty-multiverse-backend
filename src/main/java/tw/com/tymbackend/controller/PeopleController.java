package tw.com.tymbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.com.tymbackend.domain.People;
import tw.com.tymbackend.domain.dto.PeopleNameRequest;
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
    public ResponseEntity<People> insertPeople(@RequestBody People people) {
        try {
            People savedPeople = peopleService.insertPerson(people);
            return new ResponseEntity<>(savedPeople, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/update")
    public ResponseEntity<People> updatePeople(@RequestBody People people) {
        try {
            People updatedPeople = peopleService.updatePerson(people);
            return new ResponseEntity<>(updatedPeople, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 插入 n 個 (接收 JSON)
    @PostMapping("/insert-multiple")
    public ResponseEntity<List<People>> insertMultiplePeople(@RequestBody List<People> peopleList) {
        List<People> savedPeople = peopleService.saveAllPeople(peopleList);
        return new ResponseEntity<>(savedPeople, HttpStatus.CREATED);
    }

    // 搜尋 1 個 (接收 id 傳出 JSON)
    @PostMapping("/get")
    public ResponseEntity<?> getPeopleById(@RequestBody PeopleNameRequest request) {
        Optional<People> people = peopleService.getPeopleByName(request.getName());
        if (people != null) {
            return new ResponseEntity<>(people, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // 搜尋 n 個 (傳出 JSON)
    @PostMapping("/get-all")
    public ResponseEntity<List<People>> getAllPeople() {
        List<People> people = peopleService.getAllPeople();
        return new ResponseEntity<>(people, HttpStatus.OK);
    }

    // 搜尋 name (接收 name 傳出 JSON)
    @PostMapping("/get-by-name")
    public ResponseEntity<?> getPeopleByName(@RequestBody PeopleNameRequest request) {
        Optional<People> people = peopleService.getPeopleByName(request.getName());
        return new ResponseEntity<>(people, HttpStatus.OK);
    }

    @PostMapping("/delete-all")
    public ResponseEntity<Void> deleteAllPeople() {
        try {
            peopleService.deleteAllPeople();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
