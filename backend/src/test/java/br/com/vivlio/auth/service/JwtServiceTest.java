package br.com.vivlio.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SEGREDO_VALIDO = "segredo-de-teste-com-mais-de-32-caracteres";

    @Test
    @DisplayName("DT-03 - sem JWT_SECRET a aplicacao nao inicializa")
    void deveRecusarSegredoVazio() {
        assertThatThrownBy(() -> new JwtService("", 60_000))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT_SECRET não definido");
        assertThatThrownBy(() -> new JwtService(null, 60_000))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("DT-03 - segredo com menos de 32 caracteres e recusado")
    void deveRecusarSegredoCurto() {
        assertThatThrownBy(() -> new JwtService("curto-demais", 60_000))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("32 caracteres");
    }

    @Test
    @DisplayName("Token gerado com segredo valido devolve o e-mail")
    void deveGerarELerToken() {
        JwtService service = new JwtService(SEGREDO_VALIDO, 60_000);

        assertThat(service.extrairEmail(service.gerarToken("ana@vivlio.com"))).isEqualTo("ana@vivlio.com");
    }

    @Test
    @DisplayName("Token assinado com outro segredo e rejeitado")
    void deveRejeitarTokenDeOutraChave() {
        String token = new JwtService("outro-segredo-tambem-com-mais-de-32-caracteres", 60_000)
                .gerarToken("ana@vivlio.com");

        assertThat(new JwtService(SEGREDO_VALIDO, 60_000).extrairEmail(token)).isNull();
    }

    @Test
    @DisplayName("Token expirado e rejeitado")
    void deveRejeitarTokenExpirado() {
        JwtService service = new JwtService(SEGREDO_VALIDO, -1_000);

        assertThat(service.extrairEmail(service.gerarToken("ana@vivlio.com"))).isNull();
    }
}
