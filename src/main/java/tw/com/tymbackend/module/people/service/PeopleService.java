package tw.com.tymbackend.module.people.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ConcurrentModificationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;

import tw.com.tymbackend.core.service.BaseService;
import tw.com.tymbackend.module.people.dao.PeopleRepository;
import tw.com.tymbackend.module.people.domain.vo.People;

@Service
public class PeopleService extends BaseService {

    @Autowired
    private PeopleRepository peopleRepository;

    public List<People> getAllPeople() {
        return peopleRepository.findAll();
    }

    public Optional<People> getPersonBy(String name) {
        return peopleRepository.findByName(name);
    }

    public Optional<People> getPeopleByName(String name) {
        return peopleRepository.findByName(name);
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

    public void deletePerson(String name) {
        peopleRepository.deleteByName(name);
    }

    @Transactional
    public void deleteAllPeople() {
        try {
            peopleRepository.deleteAllPeople();
        } catch (DataAccessException e) {
            throw new RuntimeException("Failed to delete all people records.", e);
        }
    }

    public People insertPerson(People person) {
        Optional<People> existingPerson = peopleRepository.findByName(person.getName());
        if (!existingPerson.isPresent()) {
            return peopleRepository.save(person);
        }
        throw new IllegalArgumentException("Person with name " + person.getName() + " already exists");
    }

    @Retryable(
        value = ObjectOptimisticLockingFailureException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    public People updatePerson(People person) {
        Optional<People> existingPerson = peopleRepository.findByName(person.getName());
        if (existingPerson.isPresent()) {
            People current = existingPerson.get();
            // Copy all fields except version
            person.setVersion(current.getVersion());
            try {
                return peopleRepository.save(person);
            } catch (ObjectOptimisticLockingFailureException e) {
                throw new ConcurrentModificationException("This record was modified by another user. Please refresh and try again.");
            }
        }
        throw new IllegalArgumentException("Person with name " + person.getName() + " does not exist");
    }
}