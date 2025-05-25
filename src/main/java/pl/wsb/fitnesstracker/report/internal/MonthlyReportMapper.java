package pl.wsb.fitnesstracker.report.internal;

import pl.wsb.fitnesstracker.mail.api.EmailDto;
import pl.wsb.fitnesstracker.report.api.MonthlyReport;

import java.text.SimpleDateFormat;

public class MonthlyReportMapper {

    public static final String EMAIL_TITLE = "Your monthly report is here!";

    private static final String EMAIL_BODY_TEMPLATE = """
            Witaj %s,
            
            Twoje podsumowanie treningów za miesiąc %s:
                - Liczba wykonanych treningów: %d
            
            Kontynuuj dobrą pracę, czekamy na kolejne wyniki!
            
            Pozdrawiamy, \s
                Zespół Fitness Tracker
            """;

    public static EmailDto toEmailDto(MonthlyReport monthlyReport) {
        String toAddress = monthlyReport.user().getEmail();
        String userName = monthlyReport.user().getFirstName();
        String monthName = new SimpleDateFormat("MMMM").format(monthlyReport.afterTime());
        int trainingsCompleted = monthlyReport.trainings().size();
        String emailBody = String.format(EMAIL_BODY_TEMPLATE, userName, monthName, trainingsCompleted);
        return new EmailDto(toAddress, EMAIL_TITLE, emailBody);
    }
}
