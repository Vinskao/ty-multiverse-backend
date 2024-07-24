package tw.com.tymbackend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tw.com.tymbackend.dao.PeopleRepository;
import tw.com.tymbackend.domain.People;

@Service
public class PeopleService {

    @Autowired
    private PeopleRepository peopleRepository;

    public List<People> getAllPeople() {
        return peopleRepository.findAll();
    }

    public People getPersonById(Long id) {
        return peopleRepository.findById(id).orElse(null);
    }

    public People savePerson(People person) {
        return peopleRepository.save(person);
    }

    @Transactional
    public List<People> saveAllPeople(List<People> peopleList) {
        // 获取已有的 name_original 列表
        List<String> existingNames = peopleRepository.findAll().stream()
                .map(People::getNameOriginal)
                .collect(Collectors.toList());

        // 过滤掉已有的 name_original
        List<People> newPeople = peopleList.stream()
                .filter(person -> !existingNames.contains(person.getNameOriginal()))
                .collect(Collectors.toList());

        // 保存新的记录
        return peopleRepository.saveAll(newPeople);
    }

    public void deletePerson(Long id) {
        peopleRepository.deleteById(id);
    }

}