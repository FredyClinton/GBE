package gov.cmr.minfi.db.gbe.app.agent;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AgentRepository extends JpaRepository<Agent, String> {
    boolean existsByMatriculeIgnoreCase(String matricule);

    boolean existsByNuiIgnoreCase(String nui);

    boolean existsByNumeroCniIgnoreCase(String cniNumber);

    boolean existsByPhoneNumberIgnoreCase(String phoneNumber);

    Optional<Agent> findByMatriculeIgnoreCase(String matricule);
}
