package mx.unam.validoc.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import mx.unam.validoc.model.AppUser;
public interface AppUserRepository extends JpaRepository<AppUser,Long>{ Optional<AppUser> findByEmail(String email); boolean existsByEmail(String email); }
