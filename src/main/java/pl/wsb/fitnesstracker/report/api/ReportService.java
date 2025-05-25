package pl.wsb.fitnesstracker.report.api;

import java.util.Date;
import java.util.List;

public interface ReportService {
    /**
     * Generates monthly reports for users active after the given date.
     */
    List<MonthlyReport> generateMonthlyReports(Date afterTime);
}
