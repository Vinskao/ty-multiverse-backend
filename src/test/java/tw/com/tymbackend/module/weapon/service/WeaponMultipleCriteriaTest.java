package tw.com.tymbackend.module.weapon.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import tw.com.tymbackend.module.weapon.dao.WeaponRepository;
import tw.com.tymbackend.module.weapon.domain.vo.Weapon;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pins the query built by findByMultipleCriteria so the specification-combining
 * implementation can change without changing the query.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class WeaponMultipleCriteriaTest {

    @Mock
    private WeaponRepository weaponRepository;

    @InjectMocks
    private WeaponService weaponService;

    @Mock
    private Root<Weapon> root;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path damagePath;

    @Mock
    private Path attributesPath;

    @Mock
    private Predicate between;

    @Mock
    private Predicate like;

    @Mock
    private Predicate combined;

    private Specification<Weapon> captureSpec() {
        ArgumentCaptor<Specification<Weapon>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(weaponRepository).findAll(captor.capture());
        return captor.getValue();
    }

    @Test
    void findByMultipleCriteria_Should_ApplyNoRestriction_When_AllCriteriaNull() {
        weaponService.findByMultipleCriteria(null, null, null);

        assertNull(captureSpec().toPredicate(root, query, cb));
        verifyNoInteractions(cb);
    }

    @Test
    void findByMultipleCriteria_Should_FilterByAttributeOnly_When_DamageRangeIncomplete() {
        when(root.get("attributes")).thenReturn(attributesPath);
        when(cb.like(attributesPath, "%fire%")).thenReturn(like);

        weaponService.findByMultipleCriteria(10, null, "fire");

        assertSame(like, captureSpec().toPredicate(root, query, cb));
    }

    @Test
    void findByMultipleCriteria_Should_AndRangeAndAttribute_When_AllCriteriaGiven() {
        when(root.get("baseDamage")).thenReturn(damagePath);
        when(root.get("attributes")).thenReturn(attributesPath);
        when(cb.between(damagePath, 10, 20)).thenReturn(between);
        when(cb.like(attributesPath, "%fire%")).thenReturn(like);
        when(cb.and(between, like)).thenReturn(combined);

        weaponService.findByMultipleCriteria(10, 20, "fire");

        assertSame(combined, captureSpec().toPredicate(root, query, cb));
    }
}
