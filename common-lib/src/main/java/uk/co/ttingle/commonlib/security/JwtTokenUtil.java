package uk.co.ttingle.commonlib.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenUtil {

  @Value("${security.jwt.secret}")
  private String secretKey;

  @Value("${security.jwt.expiration}")
  private long jwtExpirationMs;

  public String generateToken(String subject) {
    return generateToken(subject, Map.of());
  }

  public String generateToken(String subject, Map<String, Object> claims) {
    Instant now = Instant.now();

    return Jwts.builder()
        .claims(claims)
        .subject(subject)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusMillis(jwtExpirationMs)))
        .signWith(getSecretKey())
        .compact();
  }

  public String generateUserToken(UUID userId, String email, Collection<String> roles) {
    return generateToken(
        userId.toString(),
        Map.of(JwtConstants.EMAIL_CLAIM, email, JwtConstants.ROLES_CLAIM, List.copyOf(roles)));
  }

  public String extractSubject(String token) {
    return parseClaims(token).getSubject();
  }

  public UUID extractUserId(String token) {
    String subject = extractSubject(token);
    if (subject == null || subject.isBlank()) {
      return null;
    }
    return UUID.fromString(subject);
  }

  public String extractClaim(String token, String claimName) {
    return parseClaims(token).get(claimName, String.class);
  }

  public String extractEmail(String token) {
    return extractClaim(token, JwtConstants.EMAIL_CLAIM);
  }

  public List<String> extractRoles(String token) {
    Object roles = parseClaims(token).get(JwtConstants.ROLES_CLAIM);
    if (!(roles instanceof List<?> roleList)) {
      return List.of();
    }
    return roleList.stream().map(String::valueOf).toList();
  }

  public Date extractExpiration(String token) {
    return parseClaims(token).getExpiration();
  }

  public boolean isTokenExpired(String token) {
    return extractExpiration(token).before(Date.from(Instant.now()));
  }

  public boolean isTokenValid(String token) {
    try {
      parseClaims(token);
      return !isTokenExpired(token);
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  private Claims parseClaims(String token) {
    return Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token).getPayload();
  }

  private SecretKey getSecretKey() {
    return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
  }
}
