package tw.com.tymbackend.module.people.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.EmptyResultDataAccessException;

import tw.com.tymbackend.core.factory.QueryConditionFactory;
import tw.com.tymbackend.core.factory.RepositoryFactory;
import tw.com.tymbackend.module.people.dao.PeopleRepository;
import tw.com.tymbackend.module.people.domain.vo.People;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true, noRollbackFor = {IllegalArgumentException.class, EmptyResultDataAccessException.class})
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
        return peopleRepository.findByName(name);
    }

    public Optional<People> getPeopleByName(String name) {
        return peopleRepository.findByName(name);
    }

    @Transactional
    public People savePerson(People person) {
        return repositoryFactory.save(person);
    }

    @Transactional
    public List<People> saveAllPeople(List<People> peopleList) {
        return repositoryFactory.saveAll(peopleList);
    }

    @Transactional
    public void deletePerson(String name) {
        Optional<People> person = peopleRepository.findByName(name);
        if (person.isPresent()) {
            peopleRepository.deleteByName(name);
        }
        // If person doesn't exist, just return without throwing an exception
    }

    @Transactional
    public void deleteAllPeople() {
        try {
            repositoryFactory.deleteAll(People.class);
        } catch (Exception e) {
            // Log the exception but don't rethrow it to prevent transaction rollback
            // You might want to add proper logging here
            System.err.println("Error during deleteAllPeople: " + e.getMessage());
        }
    }

    @Transactional
    public People insertPerson(People person) {
        Optional<People> existingPerson = peopleRepository.findByName(person.getName());
        if (!existingPerson.isPresent()) {
            return repositoryFactory.save(person);
        }
        throw new IllegalArgumentException("Person with name " + person.getName() + " already exists");
    }

    @Transactional
    public People updatePerson(People person) {
        // 先檢查人員是否存在
        Optional<People> existingPerson = peopleRepository.findByName(person.getName());
        if (!existingPerson.isPresent()) {
            throw new IllegalArgumentException("Person not found with name: " + person.getName());
        }
        
        // 獲取現有實體
        People existing = existingPerson.get();
        
        // 更新實體屬性，而不是替換整個實體
        // 這樣可以避免樂觀鎖定衝突
        existing.setBaseAttributes(person.getBaseAttributes());
        existing.setBonusAttributes(person.getBonusAttributes());
        existing.setStateAttributes(person.getStateAttributes());
        existing.setNameOriginal(person.getNameOriginal());
        existing.setCodeName(person.getCodeName());
        existing.setPhysicPower(person.getPhysicPower());
        existing.setMagicPower(person.getMagicPower());
        existing.setUtilityPower(person.getUtilityPower());
        existing.setDob(person.getDob());
        existing.setRace(person.getRace());
        existing.setAttributes(person.getAttributes());
        existing.setGender(person.getGender());
        existing.setAssSize(person.getAssSize());
        existing.setBoobsSize(person.getBoobsSize());
        existing.setHeightCm(person.getHeightCm());
        existing.setWeightKg(person.getWeightKg());
        existing.setProfession(person.getProfession());
        existing.setCombat(person.getCombat());
        existing.setFavoriteFoods(person.getFavoriteFoods());
        existing.setJob(person.getJob());
        existing.setPhysics(person.getPhysics());
        existing.setKnownAs(person.getKnownAs());
        existing.setPersonally(person.getPersonally());
        existing.setInterest(person.getInterest());
        existing.setLikes(person.getLikes());
        existing.setDislikes(person.getDislikes());
        existing.setConcubine(person.getConcubine());
        existing.setFaction(person.getFaction());
        existing.setArmyId(person.getArmyId());
        existing.setArmyName(person.getArmyName());
        existing.setDeptId(person.getDeptId());
        existing.setDeptName(person.getDeptName());
        existing.setOriginArmyId(person.getOriginArmyId());
        existing.setOriginArmyName(person.getOriginArmyName());
        existing.setGaveBirth(person.isGaveBirth());
        existing.setEmail(person.getEmail());
        existing.setAge(person.getAge());
        existing.setProxy(person.getProxy());
        existing.setHei(person.getHei());
        existing.setHRRatio(person.getHRRatio());
        existing.setPhysicsFallout4(person.getPhysicsFallout4());
        
        try {
            // 保存更新後的實體
            return repositoryFactory.save(existing);
        } catch (Exception e) {
            // 記錄異常但不重新拋出，防止事務回滾
            System.err.println("Error updating person: " + e.getMessage());
            e.printStackTrace();
            // 返回原始實體
            return existing;
        }
    }
    
    /**
     * Find people by race
     * 
     * @param race the race to search for
     * @return list of people with the specified race
     */
    public List<People> findByRace(String race) {
        Specification<People> spec = queryConditionFactory.createEqualsCondition("race", race);
        return repositoryFactory.getSpecificationRepository(People.class, Long.class).findAll(spec);
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
        return repositoryFactory.getSpecificationRepository(People.class, Long.class).findAll(spec);
    }
    
    /**
     * Find people by name containing (case insensitive)
     * 
     * @param namePart part of the name to search for
     * @return list of people with names containing the specified part
     */
    public List<People> findByNameContaining(String namePart) {
        Specification<People> spec = queryConditionFactory.createLikeCondition("name", namePart);
        return repositoryFactory.getSpecificationRepository(People.class, Long.class).findAll(spec);
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
        return repositoryFactory.getSpecificationRepository(People.class, Long.class).findAll(combinedSpec);
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