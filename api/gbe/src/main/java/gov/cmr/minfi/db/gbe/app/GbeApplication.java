package gov.cmr.minfi.db.gbe.app;

import gov.cmr.minfi.db.gbe.app.role.Role;
import gov.cmr.minfi.db.gbe.app.role.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

@SpringBootApplication
public class GbeApplication {

    public static void main(String[] args) {
        SpringApplication.run(GbeApplication.class, args);

    }

    @Bean
    public CommandLineRunner commandLineRunner(final RoleRepository roleRepository) {
        return args -> {
            final Optional<Role> userRole = roleRepository.findByName("ROLE_USER");
            if (userRole.isEmpty()) {
                Role role = new Role();
                role.setName("ROLE_USER");
                role.setCreatedBy("APP");
                roleRepository.save(role);
            }
        };
    }

}
