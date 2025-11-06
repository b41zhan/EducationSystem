package nba.studix.userservice.Config;

import nba.studix.userservice.Entity.*;
import nba.studix.userservice.Repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DataInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    @Transactional
    CommandLineRunner initDatabase(UserRepository userRepository,
                                   UserRoleRepository userRoleRepository,
                                   SchoolRepository schoolRepository) {
        return args -> {
            // Проверяем, есть ли уже админ
            if (userRepository.findByEmail("admin@school.kz").isEmpty()) {
                logger.info("Creating default admin user...");

                // Создаем администратора
                User admin = new User();
                admin.setEmail("admin@educationsystem.com");
                admin.setPasswordHash("admin123"); // Временный пароль
                admin.setFirstName("System");
                admin.setLastName("Administrator");
                admin.setPatronymic("");
                admin.setStatus(UserStatus.ACTIVE);
                admin.setProfileVisibility(ProfileVisibility.PRIVATE);

                User savedAdmin = userRepository.save(admin);

                // Назначаем роль ADMIN
                UserRole adminRole = new UserRole(savedAdmin, Role.ADMIN);
                userRoleRepository.save(adminRole);

                logger.info("Default admin created successfully: admin@educationsystem.com / admin123");
            }

            // Создаем демо-школу если нет школ
            if (schoolRepository.count() == 0) {
                logger.info("Creating demo school...");

                School demoSchool = new School();
                demoSchool.setName("Demo Educational School");
                demoSchool.setAddress("123 Education Street, Learning City");

                schoolRepository.save(demoSchool);

                logger.info("Demo school created successfully");
            }
        };
    }
}
