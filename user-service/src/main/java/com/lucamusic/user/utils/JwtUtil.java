/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lucamusic.user.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.util.Arrays;
import java.util.List;
import lombok.Data;
import org.springframework.security.authentication.BadCredentialsException;

/**
 *
 * @author miso
 */
@Data
@Service
public class JwtUtil {
    private String secret;
    private int jwtExpirationInMs;

    @Value("${jwt.secret}")
    public void setSecret(String secret) {
            this.secret = secret;
    }

    @Value("${jwt.jwtExpirationInMs}")
    public void setJwtExpirationInMs(int jwtExpirationInMs) {
            this.jwtExpirationInMs = jwtExpirationInMs;
    }

    // generate token for user
    public String generateToken(UserDetails userDetails) {
            Map<String, Object> claims = new HashMap<>();
            Collection<? extends GrantedAuthority> roles = userDetails.getAuthorities();
            if (roles.contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                    claims.put("isAdmin", true);
            }
            if (roles.contains(new SimpleGrantedAuthority("ROLE_USER"))) {
                    claims.put("isUser", true);
            }
            return doGenerateToken(claims, userDetails.getUsername());
    }

    private String doGenerateToken(Map<String, Object> claims, String subject) {

            return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                            .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs)).signWith(SignatureAlgorithm.HS512, secret).compact();
    }
    
    public boolean validateToken(String authToken) {
		try {
			Jws<Claims> claiJwsms = Jwts.parser().setSigningKey(secret).parseClaimsJws(authToken);
			return true;
		} catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex) {
			throw new BadCredentialsException("INVALID_CREDENTIALS", ex);
		} catch (ExpiredJwtException ex) {
			throw ex;
		}
	}

    public String getUsernameFromToken(String token) {
            Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();

            return claims.getSubject();
    }

    public List<SimpleGrantedAuthority> getRolesFromToken(String authToken) {
            List<SimpleGrantedAuthority> roles = null;
            Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(authToken).getBody();
            Boolean isAdmin = claims.get("isAdmin", Boolean.class);
            Boolean isUser = claims.get("isUser", Boolean.class);
            if (isAdmin != null && isAdmin == true) {
                    roles = Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"));
            }
            if (isUser != null && isUser == true) {
                    roles = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
            }
            return roles;
    }
    
}
