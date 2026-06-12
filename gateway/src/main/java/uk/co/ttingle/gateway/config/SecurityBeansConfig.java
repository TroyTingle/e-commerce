package uk.co.ttingle.gateway.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import uk.co.ttingle.commonlib.security.JwtTokenUtil;

@Configuration
@EnableConfigurationProperties(GatewayProperties.class)
@Import(JwtTokenUtil.class)
public class SecurityBeansConfig {}
