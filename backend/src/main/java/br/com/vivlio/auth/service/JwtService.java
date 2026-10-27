package br.com.vivlio.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;

@Service
public class JwtService {

    // HMAC-SHA256 exige chave de pelo menos 256 bits.
    static final int TAMANHO_MINIMO_SEGREDO = 32;

    private final SecretKey chave;
    private final long expiracaoMs;

    public JwtService(@Value("${vivlio.jwt.secret}") String secret,
                      @Value("${vivlio.jwt.expiracao-ms}") long expiracaoMs) {
        this.chave = Keys.hmacShaKeyFor(validarSegredo(secret));
        this.expiracaoMs = expiracaoMs;
    }

    private static byte[] validarSegredo(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET não definido. Configure a variável de ambiente (veja .env.example).");
        }
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < TAMANHO_MINIMO_SEGREDO) {
            throw new IllegalStateException("JWT_SECRET precisa ter pelo menos "
                    + TAMANHO_MINIMO_SEGREDO + " caracteres.");
        }
        return bytes;
    }

    public String gerarToken(String email) {
        long agora = System.currentTimeMillis();
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(agora))
                .expiration(new Date(agora + expiracaoMs))
                .signWith(chave)
                .compact();
    }

    public String extrairEmail(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(chave)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
        } catch (Exception ex) {
            return null;
        }
    }
}
