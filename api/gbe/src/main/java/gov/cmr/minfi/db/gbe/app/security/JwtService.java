package gov.cmr.minfi.db.gbe.app.security;

import gov.cmr.minfi.db.gbe.app.common.exception.BusinessException;
import gov.cmr.minfi.db.gbe.app.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
@Getter
@Setter
public class JwtService {

    private static final String TOKEN_TYPE = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "ACCES_TOKEN";
    private static final String MFA_TOKEN_TYPE = "MFA_TOKEN";
    private static final String REFRESH_TOKEN_TYPE = "REFRESH_TOKEN";
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    @Value("${app.security.jwt.access-token-expiration}")
    private long accesTokenExpiration;

    @Value("${app.security.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;


    @Value("${app.security.jwt.mfa-token-expiration}")
    private long mfaTokenExpiration;


    public JwtService(
            @Value("${app.security.jwt.private-key}") final Resource privateKey,
            @Value("${app.security.jwt.public-key}") final Resource publicKey) throws Exception {
        this.privateKey = KeyUtils.loadPrivateKey(privateKey);
        this.publicKey = KeyUtils.loadPublicKey(publicKey);
    }

    public String generateAccessToken(final String username) {
        final Map<String, Object> claims = Map.of(TOKEN_TYPE, ACCESS_TOKEN_TYPE);
        return buildToken(username, claims, this.accesTokenExpiration);
    }

    public String generateRefreshToken(final String username) {
        final Map<String, Object> claims = Map.of(TOKEN_TYPE, REFRESH_TOKEN_TYPE);
        return buildToken(username, claims, this.refreshTokenExpiration);
    }

    private String buildToken(final String username, final Map<String, Object> claims, long expiration) {
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(this.privateKey)
                .compact();
    }

    /**
     * Identifiant (e-mail) porté par un jeton d'accès valide : signature correcte, non expiré (vérifié par le parseur)
     * et de type accès. Vide pour tout autre jeton, y compris un jeton MFA (obtenu avec le seul mot de passe, il ne
     * doit pas ouvrir l'API sans le code TOTP) ou un jeton de rafraîchissement.
     */
    public Optional<String> extraireSujetAccessToken(final String token) {
        final Claims claims;
        try {
            claims = lireClaims(token);
        } catch (final JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
        if (!ACCESS_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE))) {
            return Optional.empty();
        }
        return Optional.ofNullable(claims.getSubject());
    }

    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    private Claims extractClaims(String token) {
        try {
            return lireClaims(token);
        } catch (final JwtException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    /** Vérifie la signature et l'expiration ; lève {@link JwtException} ou {@link IllegalArgumentException} sinon. */
    private Claims lireClaims(final String token) {
        return Jwts.parser()
                .verifyWith(this.publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Identifiant (e-mail) porté par un jeton de rafraîchissement. Refuse avec {@code REFRESH_TOKEN_INVALIDE} (401)
     * un jeton absent, mal formé, mal signé, expiré (le parseur vérifie l'expiration) ou d'un autre type
     * (jeton d'accès ou jeton MFA).
     */
    public String extraireSujetRefreshToken(final String refreshToken) {
        final Claims claims;
        try {
            claims = lireClaims(refreshToken);
        } catch (final JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALIDE);
        }
        if (!REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE)) || claims.getSubject() == null) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALIDE);
        }
        return claims.getSubject();
    }

    public String generateMfaToken(final String username) {
        final Map<String, Object> claims = Map.of(TOKEN_TYPE, MFA_TOKEN_TYPE);
        return buildToken(username, claims, this.mfaTokenExpiration);
    }

    public void validateMfaToken(final String token) {
        final Claims claims = extractClaims(token);
        if (!MFA_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE))) {
            throw new RuntimeException("Invalid  token type - MFA token required");

        }

        if (isTokenExpired(token)) {
            throw new RuntimeException("MFA Token  expired");
        }
    }

    public String extractUsernameFromMfaToken(final String token) {
        validateMfaToken(token);

        return extractUsername(token);
    }


}






















