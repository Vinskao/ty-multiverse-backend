package tw.com.tymbackend.module.people.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tw.com.tymbackend.core.factory.QueryConditionFactory;
import tw.com.tymbackend.core.factory.RepositoryFactory;
import tw.com.tymbackend.module.people.dao.PeopleRepository;
import tw.com.tymbackend.module.people.domain.vo.People;

import java.util.List;
import java.util.Optional;

@Service
public class PeopleService {

    private final RepositoryFactory repositoryFactory;
    private final QueryConditionFactory queryConditionFactory;
    private final PeopleRepository peopleRepository;

    public PeopleService(RepositoryFactory repositoryFactory,
                         QueryConditionFactory queryConditionFactory,
                         PeopleRepository peopleRepository) {
        this.repositoryFactory = repositoryFactory;
        this.queryConditionFactory = queryConditionFactory;
        this.peopleRepository = peopleRepository;
    }

    public List<People> getAllPeople() {
        return repositoryFactory.findAll(People.class);
    }

    public Optional<People> getPersonBy(String name) {
        return repositoryFactory.findById(People.class, name);
    }

    public Optional<People> getPeopleByName(String name) {
        return repositoryFactory.findById(People.class, name);
    }

    public People savePerson(People person) {
        return repositoryFactory.save(person);
    }

    @Transactional
    public List<People> saveAllPeople(List<People> peopleList) {
        return repositoryFactory.saveAll(peopleList);
    }

    public void deletePerson(String name) {
        repositoryFactory.deleteById(People.class, name);
    }

    @Transactional
    public void deleteAllPeople() {
        repositoryFactory.deleteAll(People.class);
    }

    public People insertPerson(People person) {
        Optional<People> existingPerson = repositoryFactory.findById(People.class, person.getName());
        if (!existingPerson.isPresent()) {
            return repositoryFactory.save(person);
        }
        throw new IllegalArgumentException("Person with name " + person.getName() + " already exists");
    }

    public People updatePerson(People person) {
        return repositoryFactory.updateById(People.class, person.getName(), person);
    }
    
    /**
     * Find people by race
     * 
     * @param race the race to search for
     * @return list of people with the specified race
     */
    public List<People> findByRace(String race) {
        Specification<People> spec = queryConditionFactory.createEqualsCondition("race", race);
        return peopleRepository.findAll(spec);
    }
    
    /**
     * Find people by age range
     * 
     * @param minAge minimum age
     * @param maxAge maximum age
     * @return list of people within the age range
     */
    public List<People> findByAgeRange(int minAge, int maxAge) {
        Specification<People> spec = queryConditionFactory.createRangeCondition("age", minAge, maxAge);
        return peopleRepository.findAll(spec);
    }
    
    /**
     * Find people by name containing (case insensitive)
     * 
     * @param namePart part of the name to search for
     * @return list of people with names containing the specified part
     */
    public List<People> findByNameContaining(String namePart) {
        Specification<People> spec = queryConditionFactory.createLikeCondition("name", namePart);
        return peopleRepository.findAll(spec);
    }
    
    /**
     * Find people by multiple criteria
     * 
     * @param race the race to search for
     * @param minAge minimum age
     * @param maxAge maximum age
     * @return list of people matching all criteria
     */
    public List<People> findByMultipleCriteria(String race, int minAge, int maxAge) {
        Specification<People> raceSpec = queryConditionFactory.createEqualsCondition("race", race);
        Specification<People> ageSpec = queryConditionFactory.createRangeCondition("age", minAge, maxAge);
        Specification<People> combinedSpec = queryConditionFactory.createCompositeCondition(raceSpec, ageSpec);
        return peopleRepository.findAll(combinedSpec);
    }
    
    /**
     * Find all people with pagination
     * 
     * @param pageable the pageable
     * @return the page of people
     */
    public Page<People> findAll(Pageable pageable) {
        return repositoryFactory.findAll(People.class, pageable);
    }
}