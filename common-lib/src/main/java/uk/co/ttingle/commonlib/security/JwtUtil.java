package uk.co.ttingle.commonlib.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

public class JwtUtil {

  private final SecretKey secretKey;
  private final long jwtExpirationMs;

  public JwtUtil(String secret, long jwtExpirationMs) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    this.jwtExpirationMs = jwtExpirationMs;
  }

  public String generateToken(String email) {
    return generateToken(email, Map.of());
  }

  public String generateToken(String email, Map<String, Object> claims) {
    Instant now = Instant.now();

    return Jwts.builder()
        .claims(claims)
        .subject(email)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusMillis(jwtExpirationMs)))
        .signWith(secretKey)
        .compact();
  }

  public String extractEmail(String token) {
    return parseClaims(token).getSubject();
  }

  public Date extractExpiration(String token) {
    return parseClaims(token).getExpiration();
  }

  public boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  public boolean isTokenValid(String token) {
    try {
      parseClaims(token);
      return isTokenExpired(token);
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  private Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
