package kz.report.dev.reportpackage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Scope("singleton")
public class ReportContainer {
    private Map<String, String> reports = new HashMap<>();

    public String getReportClass(String reportName) {
        return reports.get(reportName);
    }

    public void putReportClass(String reportName, String className) {
        reports.put(reportName, className);
    }
}
