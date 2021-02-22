package kz.report.dev;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.report.dev.reportpackage.annotation.Report;
import kz.report.dev.reportpackage.ReportContainer;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.annotation.PostConstruct;
import java.util.Objects;

@SpringBootApplication
public class DevApplication {

	@Value("%{synergy.login}")
	private String synLogin;

	@Value("%{synergy.password}")
	private String synPassword;

    @Autowired
    private ReportContainer reportContainer;

	public static void main(String[] args) {
		SpringApplication.run(DevApplication.class, args);
	}

	@Bean
	public ObjectMapper getObjectMapper() {
		return new ObjectMapper();
	}


	@PostConstruct
	public void init() {
		System.out.println("Scanning report package...");

		Reflections ref = new Reflections("kz.report.dev.reportpackage.report");
		for (Class<?> cl : ref.getTypesAnnotatedWith(Report.class)) {
			Report report = cl.getAnnotation(Report.class);
			if (Objects.isNull(report)) continue;
			reportContainer.putReportClass(report.reportName(), cl.getName());
		}
	}
}
