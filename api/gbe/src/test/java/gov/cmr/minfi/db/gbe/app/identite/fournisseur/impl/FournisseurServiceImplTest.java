package gov.cmr.minfi.db.gbe.app.identite.fournisseur.impl;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import gov.cmr.minfi.db.gbe.app.identite.auth.tfa.TwoFactorAuthenticationService;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.Fournisseur;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.FournisseurMapper;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.FournisseurRepository;
import gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto.CreateFournisseurRequest;
import gov.cmr.minfi.db.gbe.app.identite.iam.role.Role;
import gov.cmr.minfi.db.gbe.app.identite.iam.role.RoleRepository;
import gov.cmr.minfi.db.gbe.app.identite.user.User;
import gov.cmr.minfi.db.gbe.app.identite.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FournisseurServiceImplTest {

    @Mock private FournisseurRepository fournisseurRepository;
    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private TwoFactorAuthenticationService tfaService;
    @Mock private FournisseurMapper fournisseurMapper;
    @InjectMocks private FournisseurServiceImpl service;

    private final Role roleFournisseur = Role.builder().id("R").name("ROLE_FOURNISSEUR").build();

    private static CreateFournisseurRequest requete(List<String> ribs) {
        return new CreateFournisseurRequest("SARL Exemple", " M012345678901A ", "RC/YAO/2020/B/1", "Yaoundé",
                "+237222000000", "Mbarga", "Paul", "contact@exemple.cm", "+237699000000", "Temp@1234", ribs);
    }

    private static ErrorCode code(Runnable r) {
        try {
            r.run();
        } catch (BusinessException e) {
            return e.getErrorCode();
        }
        throw new AssertionError("BusinessException attendue");
    }

    @BeforeEach
    void init() {
        when(roleRepository.findByName("ROLE_FOURNISSEUR")).thenReturn(Optional.of(roleFournisseur));
        when(passwordEncoder.encode("Temp@1234")).thenReturn("{hash}");
        when(tfaService.generateNewSecret()).thenReturn("SECRET");
    }

    @Test
    void creation_creeUnCompteFournisseurSansMandat() {
        service.createFournisseur(requete(List.of("10005 00001 01234567890 12")));

        final ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(user.capture());
        assertThat(user.getValue().getRole()).isSameAs(roleFournisseur);
        assertThat(user.getValue().getPassword()).isEqualTo("{hash}");
        assertThat(user.getValue().isFirstLogin()).isTrue();
        assertThat(user.getValue().isEnabled()).isTrue();
        assertThat(user.getValue().getAgent()).isNull();
        assertThat(user.getValue().getLastName()).isEqualTo("Mbarga");
        assertThat(user.getValue().getPhoneNumber()).isEqualTo("+237699000000");

        final ArgumentCaptor<Fournisseur> f = ArgumentCaptor.forClass(Fournisseur.class);
        verify(fournisseurRepository).save(f.capture());
        assertThat(f.getValue().getNiu()).isEqualTo("M012345678901A");
        assertThat(f.getValue().getTelephone()).isEqualTo("+237222000000");
        assertThat(f.getValue().getUser()).isSameAs(user.getValue());
        assertThat(f.getValue().getCoordonneesBancaires())
                .extracting(c -> c.getRib()).containsExactly("10005000010123456789012");
    }

    @Test
    void creation_ribInvalide_refusee() {
        assertThat(code(() -> service.createFournisseur(requete(List.of("123"))))).isEqualTo(ErrorCode.RIB_INVALIDE);
        verify(fournisseurRepository, never()).save(any());
    }

    @Test
    void creation_niuDejaPris_refusee() {
        when(fournisseurRepository.existsByNiu("M012345678901A")).thenReturn(true);
        assertThat(code(() -> service.createFournisseur(requete(null)))).isEqualTo(ErrorCode.NUI_ALREADY_EXISTS);
        verify(userRepository, never()).save(any());
    }

    @Test
    void creation_emailDejaPris_refusee() {
        when(userRepository.existsByEmailIgnoreCase("contact@exemple.cm")).thenReturn(true);
        assertThat(code(() -> service.createFournisseur(requete(null)))).isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);
    }

    @Test
    void ajoutRib_fournisseurInconnu_refuse() {
        when(fournisseurRepository.findById("X")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.ajouterRib("X", new gov.cmr.minfi.db.gbe.app.identite.fournisseur.dto.RibRequest("10005000010123456789012")))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.FOURNISSEUR_NOT_FOUND);
    }
}
