package tw.com.tymbackend.module.people.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import tw.com.tymbackend.module.people.dao.PeopleImageRepository;
import tw.com.tymbackend.module.people.dao.PeopleRepository;
import tw.com.tymbackend.module.people.domain.vo.People;
import tw.com.tymbackend.module.people.domain.vo.PeopleImage;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Pins how findByMultipleSpecifications combines specifications, so the combining
 * implementation can change (e.g. deprecated Specification.where) without changing the query.
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class PeopleSpecificationCombineTest {

    @Mock
    private PeopleRepository peopleRepository;

    @Mock
    private PeopleImageRepository peopleImageRepository;

    @InjectMocks
    private PeopleService peopleService;

    @InjectMocks
    private PeopleImageService peopleImageService;

    @Mock
    private Root<People> root;

    @Mock
    private Root<PeopleImage> imageRoot;

    @Mock
    private CriteriaQuery<?> query;

    @Mock
    private CriteriaBuilder cb;

    private Specification<People> capturePeopleSpec() {
        ArgumentCaptor<Specification<People>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(peopleRepository).findAll(captor.capture());
        return captor.getValue();
    }

    private Specification<PeopleImage> captureImageSpec() {
        ArgumentCaptor<Specification<PeopleImage>> captor = ArgumentCaptor.forClass(Specification.class);
        verify(peopleImageRepository).findAll(captor.capture());
        return captor.getValue();
    }

    @Test
    void findByMultipleSpecifications_Should_ApplyNoRestriction_When_SpecListIsEmpty() {
        peopleService.findByMultipleSpecifications(List.of());

        assertNull(capturePeopleSpec().toPredicate(root, query, cb));
        verifyNoInteractions(cb);
    }

    @Test
    void findByMultipleSpecifications_Should_UseSinglePredicate_When_OneSpecGiven() {
        Predicate p1 = mock(Predicate.class);
        Specification<People> s1 = (r, q, c) -> p1;

        peopleService.findByMultipleSpecifications(List.of(s1));

        assertSame(p1, capturePeopleSpec().toPredicate(root, query, cb));
        verifyNoInteractions(cb);
    }

    @Test
    void findByMultipleSpecifications_Should_AndAllPredicates_When_TwoSpecsGiven() {
        Predicate p1 = mock(Predicate.class);
        Predicate p2 = mock(Predicate.class);
        Predicate combined = mock(Predicate.class);
        when(cb.and(p1, p2)).thenReturn(combined);

        peopleService.findByMultipleSpecifications(List.of((r, q, c) -> p1, (r, q, c) -> p2));

        assertSame(combined, capturePeopleSpec().toPredicate(root, query, cb));
    }

    @Test
    void peopleImageFindByMultipleSpecifications_Should_ApplyNoRestriction_When_SpecListIsEmpty() {
        peopleImageService.findByMultipleSpecifications(List.of());

        assertNull(captureImageSpec().toPredicate(imageRoot, query, cb));
        verifyNoInteractions(cb);
    }

    @Test
    void peopleImageFindByMultipleSpecifications_Should_AndAllPredicates_When_TwoSpecsGiven() {
        Predicate p1 = mock(Predicate.class);
        Predicate p2 = mock(Predicate.class);
        Predicate combined = mock(Predicate.class);
        when(cb.and(p1, p2)).thenReturn(combined);

        peopleImageService.findByMultipleSpecifications(List.of((r, q, c) -> p1, (r, q, c) -> p2));

        assertSame(combined, captureImageSpec().toPredicate(imageRoot, query, cb));
    }
}
