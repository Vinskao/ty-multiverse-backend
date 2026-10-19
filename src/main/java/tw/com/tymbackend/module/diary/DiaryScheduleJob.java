package tw.com.tymbackend.module.diary;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Queues overdue weekly reviews; delivery and the next seven-day interval begin at login. */
@Component
public class DiaryScheduleJob {
    private final DiaryService service;

    public DiaryScheduleJob(DiaryService service) {
        this.service = service;
    }

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Taipei")
    public void queueDueReviews() {
        service.queueDueWorkflows();
    }
}
