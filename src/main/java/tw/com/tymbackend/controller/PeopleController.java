package tw.com.tymbackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tw.com.tymbackend.domain.People;
import tw.com.tymbackend.service.PeopleService;

import java.util.List;

@RestController
@RequestMapping("/people")
public class PeopleController {

    @Autowired
    private PeopleService peopleService;

    // 插入 1 個 (接收 JSON)
    @PostMapping("/insert")
    public ResponseEntity<People> insertPeople(@RequestBody People people) {
        People savedPeople = peopleService.savePerson(people);
        return new ResponseEntity<>(savedPeople, HttpStatus.CREATED);
    }

    // 插入 n 個 (接收 JSON)
    @PostMapping("/insert-multiple")
    public ResponseEntity<List<People>> insertMultiplePeople(@RequestBody List<People> peopleList) {
        List<People> savedPeople = peopleService.saveAllPeople(peopleList);
        return new ResponseEntity<>(savedPeople, HttpStatus.CREATED);
    }

    // 更新 1 個 (接收 JSON)
    @PostMapping("/update")
    public ResponseEntity<People> updatePeople(@RequestParam Long id, @RequestBody People people) {
        people.setId(id);
        People updatedPeople = peopleService.savePerson(people);
        return new ResponseEntity<>(updatedPeople, HttpStatus.OK);
    }

    // 搜尋 1 個 (接收 id 傳出 JSON)
    @PostMapping("/get")
    public ResponseEntity<People> getPeopleById(@RequestParam Long id) {
        People people = peopleService.getPersonById(id);
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
}
