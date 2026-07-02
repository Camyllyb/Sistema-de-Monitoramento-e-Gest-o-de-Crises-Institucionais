package br.edu.gestaocrises;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração do módulo de usuários.
 *
 * Banco utilizado: H2 in-memory (perfil "test") com as mesmas migrations Flyway.
 * O banco oficial do projeto é PostgreSQL — H2 é usado apenas para testes locais/CI.
 *
 * Para executar com Java 21:
 *   docker run --rm -v "$PWD":/workspace -w /workspace maven:3.9.9-eclipse-temurin-21 mvn clean test
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static Long gerenteId;

    // ─────────────────────────────────────────────
    // Auxiliares
    // ─────────────────────────────────────────────

    private String obterTokenAdmin() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "admin@empresa.com", "senha", "admin123"));
        String resposta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).path("dados").path("token").asText();
    }

    private String obterToken(String email, String senha) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", email, "senha", senha));
        String resposta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).path("dados").path("token").asText();
    }

    // ─────────────────────────────────────────────
    // 1. ADMIN lista usuários
    // ─────────────────────────────────────────────

    @Test
    @Order(1)
    void adminDeveListarUsuarios() throws Exception {
        String token = obterTokenAdmin();
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.dados").isArray());
    }

    // ─────────────────────────────────────────────
    // 2. Sem token retorna 401
    // ─────────────────────────────────────────────

    @Test
    @Order(2)
    void semTokenDeveRetornar401() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    // ─────────────────────────────────────────────
    // 3. Criar usuário GERENTE com dados válidos
    // ─────────────────────────────────────────────

    @Test
    @Order(3)
    void criarGerenteValido_deveRetornar201() throws Exception {
        String token = obterTokenAdmin();
        String body = objectMapper.writeValueAsString(Map.of(
                "nome", "Maria Silva",
                "email", "maria@if.edu.br",
                "senha", "senha123",
                "perfil", "GERENTE"));

        String resposta = mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.dados.email").value("maria@if.edu.br"))
                .andExpect(jsonPath("$.dados.perfil").value("GERENTE"))
                .andExpect(jsonPath("$.dados.ativo").value(true))
                .andExpect(jsonPath("$.dados.senha").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        gerenteId = objectMapper.readTree(resposta).path("dados").path("id").asLong();
    }

    // ─────────────────────────────────────────────
    // 4. Email duplicado retorna 400
    // ─────────────────────────────────────────────

    @Test
    @Order(4)
    void criarComEmailDuplicado_deveRetornar400() throws Exception {
        String token = obterTokenAdmin();
        String body = objectMapper.writeValueAsString(Map.of(
                "nome", "Outro Usuário",
                "email", "maria@if.edu.br",
                "senha", "senha123",
                "perfil", "GERENTE"));

        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ─────────────────────────────────────────────
    // 5. Perfil inexistente retorna 400
    // ─────────────────────────────────────────────

    @Test
    @Order(5)
    void criarComPerfilInexistente_deveRetornar400() throws Exception {
        String token = obterTokenAdmin();
        String body = objectMapper.writeValueAsString(Map.of(
                "nome", "João",
                "email", "joao@if.edu.br",
                "senha", "senha123",
                "perfil", "VISUALIZADOR"));

        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ─────────────────────────────────────────────
    // 6. Atualizar usuário retorna 200
    // ─────────────────────────────────────────────

    @Test
    @Order(6)
    void atualizarUsuario_deveRetornar200() throws Exception {
        String token = obterTokenAdmin();
        String body = objectMapper.writeValueAsString(Map.of(
                "nome", "Maria Souza",
                "email", "maria.souza@if.edu.br",
                "perfil", "ANALISTA"));

        mockMvc.perform(put("/api/usuarios/" + gerenteId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.dados.nome").value("Maria Souza"))
                .andExpect(jsonPath("$.dados.perfil").value("ANALISTA"));
    }

    // ─────────────────────────────────────────────
    // 7. Alterar senha retorna 200
    // ─────────────────────────────────────────────

    @Test
    @Order(7)
    void alterarSenha_deveRetornar200() throws Exception {
        String token = obterTokenAdmin();
        String body = objectMapper.writeValueAsString(Map.of("novaSenha", "novaSenha123"));

        mockMvc.perform(patch("/api/usuarios/" + gerenteId + "/senha")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.mensagem").value("Senha atualizada com sucesso"));
    }

    // ─────────────────────────────────────────────
    // 8. Desativar usuário retorna 200
    // ─────────────────────────────────────────────

    @Test
    @Order(8)
    void desativarUsuario_deveRetornar200() throws Exception {
        String token = obterTokenAdmin();

        mockMvc.perform(delete("/api/usuarios/" + gerenteId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.mensagem").value("Usuário desativado com sucesso"));
    }

    // ─────────────────────────────────────────────
    // 9. Login de usuário desativado retorna 401
    // ─────────────────────────────────────────────

    @Test
    @Order(9)
    void loginUsuarioDesativado_deveRetornar401() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "maria.souza@if.edu.br", "senha", "novaSenha123"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    // ─────────────────────────────────────────────
    // 10. ANALISTA não acessa /api/usuarios → 403
    // ─────────────────────────────────────────────

    @Test
    @Order(10)
    @WithMockUser(roles = "ANALISTA")
    void analistaDeveReceberForbiddenAoAcessarUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden());
    }
}
