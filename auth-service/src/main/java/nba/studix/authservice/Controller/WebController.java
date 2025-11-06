package nba.studix.authservice.Controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RestController
public class WebController implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/login.html");
        registry.addViewController("/login").setViewName("forward:/login.html");
        registry.addViewController("/admin/dashboard").setViewName("forward:/admin-dashboard.html");
    }

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public Resource serveLogin() {
        return new ClassPathResource("static/login.html");
    }

    @GetMapping(value = "/login", produces = MediaType.TEXT_HTML_VALUE)
    public Resource serveLoginPage() {
        return new ClassPathResource("static/login.html");
    }

    @GetMapping(value = "/admin/dashboard", produces = MediaType.TEXT_HTML_VALUE)
    public Resource serveAdminDashboard() {
        return new ClassPathResource("static/admin-dashboard.html");
    }
}