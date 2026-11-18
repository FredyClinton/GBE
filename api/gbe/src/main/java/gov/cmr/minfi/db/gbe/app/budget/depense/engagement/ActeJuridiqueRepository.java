package gov.cmr.minfi.db.gbe.app.budget.depense.engagement;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface ActeJuridiqueRepository extends JpaRepository<ActeJuridique, String> {

    boolean existsByExpressionBesoinId(String expressionBesoinId);

    /** Espace fournisseur : ses actes qui lui ont été notifiés (y compris annulés ensuite). */
    @Query("""
            SELECT a FROM ActeJuridique a JOIN a.soumission s
            WHERE s.fournisseur.id = :fournisseurId AND a.dateNotification IS NOT NULL
            """)
    Page<ActeJuridique> findNotifiesAuFournisseur(@Param("fournisseurId") String fournisseurId, Pageable pageable);

    @Query("""
            SELECT a FROM ActeJuridique a JOIN a.soumission s
            WHERE a.id = :acteId AND s.fournisseur.id = :fournisseurId AND a.dateNotification IS NOT NULL
            """)
    java.util.Optional<ActeJuridique> findNotifieAuFournisseur(@Param("acteId") String acteId,
                                                               @Param("fournisseurId") String fournisseurId);

    /** Actes du périmètre (sections entières ou programmes), filtrés en base. */
    @Query("""
            SELECT a FROM ActeJuridique a JOIN a.expressionBesoin b
            WHERE (b.section.id IN :sectionsEntieres OR b.programme.id IN :programmes)
              AND (:exerciceId IS NULL OR b.exercice.id = :exerciceId)
              AND (:statut IS NULL OR a.statut = :statut)
            """)
    Page<ActeJuridique> rechercherDansPerimetre(@Param("sectionsEntieres") Collection<String> sectionsEntieres,
                                                @Param("programmes") Collection<String> programmes,
                                                @Param("exerciceId") String exerciceId,
                                                @Param("statut") StatutActe statut,
                                                Pageable pageable);
}
