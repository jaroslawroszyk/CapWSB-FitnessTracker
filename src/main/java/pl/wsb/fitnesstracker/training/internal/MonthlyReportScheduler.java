package pl.wsb.fitnesstracker.training.internal;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.wsb.fitnesstracker.mail.api.EmailDto;
import pl.wsb.fitnesstracker.mail.api.EmailSender;

@Component
public class MonthlyReportScheduler {
    private final TrainingReportService reportService;
    private final EmailSender emailSender;


    public MonthlyReportScheduler(TrainingReportService reportService, EmailSender emailSender) {
        this.reportService = reportService;
        this.emailSender = emailSender;
    }

    @Scheduled(cron = "0 0 8 1 * ?")
    public void sendReports() {
        var reports = reportService.generateReports();
        reports.forEach(report -> {
            var email = new EmailDto(
                    report.getUserEmail(),
                    "Miesięczne podsumowanie treningów",
                    """
                        Cześć!
        
                        Twoje podsumowanie treningowe:
                        - Treningi: %d
                        - Dystans: %.2f km
                        - Średnia prędkość: %.2f km/h
        
                        Do zobaczenia na kolejnych treningach!
                        """.formatted(report.getTrainingCount(), report.getTotalDistance(), report.getAverageSpeed())
            );
            emailSender.send(email);
        });
    }
}
