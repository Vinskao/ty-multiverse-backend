package tw.com.tymbackend.module.palais;

import org.junit.jupiter.api.Test;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PalaisReviewPolicyTest {
    private final PalaisReviewPolicy policy = new PalaisReviewPolicy("chiaki-id");

    @Test
    void initialPolicyRequiresChiakiAndRejectsSelfReview() {
        assertTrue(policy.canReview("maker", "chiaki-id", true, 1, Set.of()));
        assertFalse(policy.canReview("maker", "other-checker", true, 1, Set.of()));
        assertFalse(policy.canReview("chiaki-id", "chiaki-id", true, 1, Set.of()));
        assertFalse(policy.canReview("maker", "chiaki-id", false, 1, Set.of()));
    }

    @Test
    void dualReviewRequiresDistinctActiveCheckers() {
        assertTrue(policy.canReview("maker", "other-checker", true, 2, Set.of("chiaki-id")));
        assertFalse(policy.canReview("maker", "chiaki-id", true, 2, Set.of("chiaki-id")));
        assertFalse(policy.canReview("maker", "other-checker", false, 2, Set.of("chiaki-id")));
        assertFalse(policy.hasEnoughApprovals(2, Set.of("chiaki-id")));
        assertTrue(policy.hasEnoughApprovals(2, Set.of("chiaki-id", "other-checker")));
    }

    @Test
    void unsupportedQuorumsFailClosed() {
        assertFalse(policy.canReview("maker", "chiaki-id", true, 0, Set.of()));
        assertFalse(policy.hasEnoughApprovals(0, Set.of()));
    }
}
