package tw.com.tymbackend.module.ai_usage.domain.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiTokenUsageOverviewDTO {

    private Long thisMonth;
    private Long lastMonth;
    private Long thisYear;
    private Long observableTotal;
    private Long dailyAverage;
    private Long monthlyAverage;
    private Long yearlyAverage;
    private Double wowPercent;
    private Double momPercent;
    private Double yoyPercent;
    private LocalDate dataSince;
    private String timezone;
}
