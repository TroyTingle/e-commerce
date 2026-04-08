package uk.co.ttingle.commonlib.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenUtil jwtTokenUtil;

  public JwtAuthenticationFilter(JwtTokenUtil jwtTokenUtil) {
    this.jwtTokenUtil = jwtTokenUtil;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    final String authHeader = request.getHeader(JwtConstants.AUTH_HEADER);

    if (authHeader == null || !authHeader.startsWith(JwtConstants.BEARER_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    String jwt = authHeader.substring(JwtConstants.BEARER_PREFIX.length());

    if (!jwtTokenUtil.isTokenValid(jwt)) {
      filterChain.doFilter(request, response);
      return;
    }

    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      AbstractAuthenticationToken authToken = buildAuthentication(jwt);
      if (authToken != null) {
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
      }
    }

    filterChain.doFilter(request, response);
  }

  protected AbstractAuthenticationToken buildAuthentication(String jwt) {
    UUID userId = jwtTokenUtil.extractUserId(jwt);
    if (userId == null) {
      return null;
    }
    return new UsernamePasswordAuthenticationToken(
        userId,
        null,
        jwtTokenUtil.extractRoles(jwt).stream().map(SimpleGrantedAuthority::new).toList());
  }
}
