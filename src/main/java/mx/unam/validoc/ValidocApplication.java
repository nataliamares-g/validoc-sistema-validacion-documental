package mx.unam.validoc;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import mx.unam.validoc.model.AppUser;
import mx.unam.validoc.repository.AppUserRepository;

@SpringBootApplication
public class ValidocApplication {
  public static void main(String[] args) { SpringApplication.run(ValidocApplication.class, args); }

  @Bean
  CommandLineRunner seedUsers(AppUserRepository users, PasswordEncoder encoder) {
    return args -> {
      if (!users.existsByEmail("admin@validoc.mx")) users.save(new AppUser("Administrador", "admin@validoc.mx", encoder.encode("Admin123*"), "ADMIN"));
      if (!users.existsByEmail("capturista@validoc.mx")) users.save(new AppUser("Capturista", "capturista@validoc.mx", encoder.encode("Captura123*"), "CAPTURISTA"));
    };
  }
}
