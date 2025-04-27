package tw.com.tymbackend.module.people.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.EmptyResultDataAccessException;

import tw.com.tymbackend.core.repository.DataAccessor;
import tw.com.tymbackend.module.people.dao.PeopleRepository;
import tw.com.tymbackend.module.people.domain.vo.People;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Objects;

@Service
@Transactional(readOnly = true, noRollbackFor = {IllegalArgumentException.class, EmptyResultDataAccessException.class})
public class PeopleService {

    private final DataAccessor<People, Long> peopleDataAccessor;
    private final PeopleRepository peopleRepository;

    public PeopleService(DataAccessor<People, Long> peopleDataAccessor,
                        PeopleRepository peopleRepository) {
        this.peopleDataAccessor = peopleDataAccessor;
        this.peopleRepository = peopleRepository;
    }

    public List<People> getAllPeople() {
        return peopleDataAccessor.findAll();
    }

    public Optional<People> getPersonBy(String name) {
        return peopleRepository.findByName(name);
    }

    public Optional<People> getPeopleByName(String name) {
        return peopleRepository.findByName(name);
    }

    @Transactional
    public People savePerson(People person) {
        return peopleDataAccessor.save(person);
    }

    @Transactional
    public List<People> saveAllPeople(List<People> peopleList) {
        return peopleDataAccessor.saveAll(peopleList);
    }

    @Transactional
    public void deletePerson(String name) {
        Optional<People> person = peopleRepository.findByName(name);
        if (person.isPresent()) {
            peopleRepository.deleteByName(name);
        }
    }

    @Transactional
    public void deleteAllPeople() {
        try {
            peopleDataAccessor.deleteAll();
        } catch (Exception e) {
            System.err.println("Error during deleteAllPeople: " + e.getMessage());
        }
    }

    @Transactional
    public People insertPerson(People person) {
        Optional<People> existingPerson = peopleRepository.findByName(person.getName());
        if (!existingPerson.isPresent()) {
            return peopleDataAccessor.save(person);
        }
        throw new IllegalArgumentException("Person with name " + person.getName() + " already exists");
    }

    @Transactional
    public People updatePerson(People person) {
        Optional<People> existingPerson = peopleRepository.findByName(person.getName());
        if (!existingPerson.isPresent()) {
            throw new IllegalArgumentException("Person not found with name: " + person.getName());
        }
        
        People existing = existingPerson.get();
        updatePersonFields(existing, person);
        
        try {
            return peopleDataAccessor.save(existing);
        } catch (Exception e) {
            System.err.println("Error updating person: " + e.getMessage());
            e.printStackTrace();
            return existing;
        }
    }
    
    private void updatePersonFields(People existingPerson, People updatedPerson) {
        if (Objects.nonNull(updatedPerson.getName())) {
            existingPerson.setName(updatedPerson.getName());
        }
        if (Objects.nonNull(updatedPerson.getAge())) {
            existingPerson.setAge(updatedPerson.getAge());
        }
        if (Objects.nonNull(updatedPerson.getRace())) {
            existingPerson.setRace(updatedPerson.getRace());
        }
        // ... other fields ...
    }
    
    public List<People> findByRace(String race) {
        Specification<People> spec = (root, query, cb) -> 
            cb.equal(root.get("race"), race);
        return peopleDataAccessor.findAll(spec);
    }
    
    public List<People> findByAgeRange(int minAge, int maxAge) {
        Specification<People> spec = (root, query, cb) -> 
            cb.between(root.get("age"), minAge, maxAge);
        return peopleDataAccessor.findAll(spec);
    }
    
    public List<People> findByNameContaining(String namePart) {
        Specification<People> spec = (root, query, cb) -> 
            cb.like(cb.lower(root.get("name")), "%" + namePart.toLowerCase() + "%");
        return peopleDataAccessor.findAll(spec);
    }
    
    public List<People> findByMultipleCriteria(String race, int minAge, int maxAge) {
        List<Specification<People>> specs = new ArrayList<>();
        
        if (race != null) {
            specs.add((root, query, cb) -> cb.equal(root.get("race"), race));
        }
        specs.add((root, query, cb) -> cb.between(root.get("age"), minAge, maxAge));
        
        Specification<People> combinedSpec = specs.stream()
            .reduce(Specification.where(null), Specification::and);
            
        return peopleDataAccessor.findAll(combinedSpec);
    }
    
    public Page<People> findAll(Pageable pageable) {
        return peopleDataAccessor.findAll(pageable);
    }
}