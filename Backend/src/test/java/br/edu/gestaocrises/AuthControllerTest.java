package br.edu.gestaocrises;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração da autenticação JWT.
 *
 * Banco utilizado nos testes: H2 in-memory (perfil "test"), com Flyway aplicando
 * as mesmas migrations do PostgreSQL. O banco oficial do projeto continua sendo
 * PostgreSQL; o H2 é usado exclusivamente para agilizar os testes locais e de CI.
 *
 * Para rodar com o banco real, usar Docker:
 *   docker run --rm -v "$PWD":/workspace -w /workspace maven:3.9.9-eclipse-temurin-21 mvn clean test
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // ─────────────────────────────────────────────
    // Auxiliar: obtém token do admin
    // ─────────────────────────────────────────────

    private String obterTokenAdmin() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "admin@empresa.com", "senha", "admin123"));

        String resposta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(resposta).path("dados").path("token").asText();
    }

    // ─────────────────────────────────────────────
    // POST /api/auth/login
    // ─────────────────────────────────────────────

    @Test
    void loginValido_deveRetornar200EToken() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "admin@empresa.com", "senha", "admin123"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.mensagem").value("Login realizado com sucesso"))
                .andExpect(jsonPath("$.dados.token").isNotEmpty())
                .andExpect(jsonPath("$.dados.tipo").value("Bearer"))
                .andExpect(jsonPath("$.dados.usuario.email").value("admin@empresa.com"))
                .andExpect(jsonPath("$.dados.usuario.perfil").value("ADMIN"));
    }

    @Test
    void loginSenhaErrada_deveRetornar401() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "admin@empresa.com", "senha", "senhaErrada"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void loginEmailInexistente_deveRetornar401() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "inexistente@empresa.com", "senha", "qualquer"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void loginCamposInvalidos_deveRetornar400() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "nao-e-email", "senha", ""));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ─────────────────────────────────────────────
    // GET /api/auth/me
    // ─────────────────────────────────────────────

    @Test
    void meSemToken_deveRetornar401() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void meComTokenValido_deveRetornar200() throws Exception {
        String token = obterTokenAdmin();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.dados.email").value("admin@empresa.com"))
                .andExpect(jsonPath("$.dados.perfil").value("ADMIN"))
                .andExpect(jsonPath("$.dados.senha").doesNotExist());
    }

    // ─────────────────────────────────────────────
    // GET /api/auth/admin-check
    // ─────────────────────────────────────────────

    @Test
    void adminCheckSemToken_deveRetornar401() throws Exception {
        mockMvc.perform(get("/api/auth/admin-check")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void adminCheckComTokenAdmin_deveRetornar200() throws Exception {
        String token = obterTokenAdmin();

        mockMvc.perform(get("/api/auth/admin-check")
                        .header("Authorization", "Bearer " + token)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.mensagem").value("Acesso administrativo autorizado"));
    }
}
