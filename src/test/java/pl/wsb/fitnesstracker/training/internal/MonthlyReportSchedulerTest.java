package pl.wsb.fitnesstracker.training.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.wsb.fitnesstracker.mail.api.EmailSender;
import pl.wsb.fitnesstracker.training.api.MonthlyTrainingReport;
import pl.wsb.fitnesstracker.training.internal.MonthlyReportScheduler;
import pl.wsb.fitnesstracker.training.internal.TrainingReportService;

import java.util.List;

import static org.mockito.Mockito.*;

class MonthlyReportSchedulerTest {

    private TrainingReportService reportService;
    private EmailSender emailSender;
    private MonthlyReportScheduler scheduler;

    @BeforeEach
    void setUp() {
        reportService = mock(TrainingReportService.class);
        emailSender = mock(EmailSender.class);
        scheduler = new MonthlyReportScheduler(reportService, emailSender);
    }

    @Test
    void sendReports_shouldSendEmailsWithCorrectContent() {
        MonthlyTrainingReport report1 = mock(MonthlyTrainingReport.class);
        when(report1.getUserEmail()).thenReturn("user1@example.com");
        when(report1.getTrainingCount()).thenReturn(5);
        when(report1.getTotalDistance()).thenReturn(42.0);
        when(report1.getAverageSpeed()).thenReturn(12.3);

        MonthlyTrainingReport report2 = mock(MonthlyTrainingReport.class);
        when(report2.getUserEmail()).thenReturn("user2@example.com");
        when(report2.getTrainingCount()).thenReturn(3);
        when(report2.getTotalDistance()).thenReturn(30.5);
        when(report2.getAverageSpeed()).thenReturn(10.1);

        when(reportService.generateReports()).thenReturn(List.of(report1, report2));

        scheduler.sendReports();

        verify(emailSender).send(argThat(email ->
                email.getToAddress().equals("user1@example.com") &&
                        email.getSubject().equals("Miesięczne podsumowanie treningów") &&
                        email.getContent().contains("Treningi: 5") &&
                        email.getContent().contains("Dystans: 42.00 km") &&
                        email.getContent().contains("Średnia prędkość: 12.30 km/h")
        ));

        verify(emailSender).send(argThat(email ->
                email.getToAddress().equals("user2@example.com") &&
                        email.getSubject().equals("Miesięczne podsumowanie treningów") &&
                        email.getContent().contains("Treningi: 3") &&
                        email.getContent().contains("Dystans: 30.50 km") &&
                        email.getContent().contains("Średnia prędkość: 10.10 km/h")
        ));

        verifyNoMoreInteractions(emailSender);
    }

    @Test
    void sendReports_withEmptyReports_shouldNotSendAnyEmails() {
        when(reportService.generateReports()).thenReturn(List.of());

        scheduler.sendReports();

        verifyNoInteractions(emailSender);
    }

    @Test
    void sendReports_withZeroValues_shouldSendEmailsWithZeroData() {
        MonthlyTrainingReport zeroReport = mock(MonthlyTrainingReport.class);
        when(zeroReport.getUserEmail()).thenReturn("zero@example.com");
        when(zeroReport.getTrainingCount()).thenReturn(0);
        when(zeroReport.getTotalDistance()).thenReturn(0.0);
        when(zeroReport.getAverageSpeed()).thenReturn(0.0);

        when(reportService.generateReports()).thenReturn(List.of(zeroReport));

        scheduler.sendReports();

        verify(emailSender).send(argThat(email ->
                email.getToAddress().equals("zero@example.com") &&
                        email.getSubject().equals("Miesięczne podsumowanie treningów") &&
                        email.getContent().contains("Treningi: 0") &&
                        email.getContent().contains("Dystans: 0.00 km") &&
                        email.getContent().contains("Średnia prędkość: 0.00 km/h")
        ));

        verifyNoMoreInteractions(emailSender);
    }
}
