package kz.report.dev.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class MainController {

    @Value("${spring.profiles.active:prod}")
    private String profile;

    @Autowired
    private Environment environment;


    @GetMapping
    public String main() {
        String innerProfile = environment.getProperty("spring.profiles.active");
        System.out.println(innerProfile);
        return "index";
    }
}
