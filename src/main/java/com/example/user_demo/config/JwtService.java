package com.example.user_demo.config;


import com.example.user_demo.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {
    @Value("${jwt.secret}")
    private String signerKey;
    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey secretKey;
    @PostConstruct
    public void init(){
        secretKey = new SecretKeySpec(signerKey.getBytes(), "HmacSHA256");
    }
    // tao token
    public String generateToken(User user){
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("email" , user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .claim("role" , user.getRoleUser())
                .compact();
    }
    public String getUserFromToken(String token){
        return extractClaim(token,Claims::getSubject);
    }
    // valid token
    boolean isValid(String token , UserDetails userDetails){
        final String username = getUserFromToken(token);
        return ( username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    public <T> T extractClaim(String token , Function<Claims, T> claimsTFunction){
        return claimsTFunction.apply(Jwts
                .parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
        );
    }
    public boolean isTokenExpired(String token){
        return extractClaim(token , Claims::getExpiration).before(new Date());
    }
    public long getExpirationSeconds() {
        return expiration / 1000;
    }

}
