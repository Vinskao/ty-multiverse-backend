package tw.com.tymbackend.module.palais;

import java.util.Set;

record PalaisReviewPolicy(String initialCheckerId) {

    boolean canReview(String applicantId, String reviewerId, boolean activeChecker,
                      int requiredApprovals, Set<String> priorReviewers) {
        if (!activeChecker || applicantId.equals(reviewerId) || priorReviewers.contains(reviewerId)) {
            return false;
        }

        return switch (requiredApprovals) {
            case 1 -> reviewerId.equals(initialCheckerId);
            case 2 -> true;
            default -> false;
        };
    }

    boolean hasEnoughApprovals(int requiredApprovals, Set<String> activeApprovers) {
        return (requiredApprovals == 1 || requiredApprovals == 2)
                && activeApprovers.size() >= requiredApprovals;
    }
}
