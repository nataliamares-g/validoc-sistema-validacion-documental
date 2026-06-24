package mx.unam.validoc.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import mx.unam.validoc.model.DocumentRecord;
public interface DocumentRepository extends JpaRepository<DocumentRecord,Long>{ Optional<DocumentRecord> findByFolio(String folio); }
