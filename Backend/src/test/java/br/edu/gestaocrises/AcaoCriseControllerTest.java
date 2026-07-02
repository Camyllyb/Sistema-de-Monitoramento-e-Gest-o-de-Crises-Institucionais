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
 * Testes de integração do módulo de ações de crise.
 *
 * Banco: H2 in-memory (perfil "test") com Flyway.
 * Banco oficial: PostgreSQL.
 *
 * Executar com Java 21:
 *   docker run --rm -v "$PWD":/workspace -w /workspace maven:3.9.9-eclipse-temurin-21 mvn clean test
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AcaoCriseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static Long criseAbertaId;
    private static Long criseResolvidaId;
    private static Long criseEncerradaId;

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

    private Long criarCrise(String token, String titulo) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "titulo", titulo,
                "descricao", "Descrição de teste",
                "tipo", "TECNOLOGIA",
                "nivel", "ALTO",
                "responsavelId", 1));
        String resposta = mockMvc.perform(post("/api/crises")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).path("dados").path("id").asLong();
    }

    private void alterarStatus(String token, Long criseId, String status) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("status", status));
        mockMvc.perform(patch("/api/crises/" + criseId + "/status")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private String corpoAcaoValido() throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "tipo", "CONTENCAO",
                "descricao", "Ação de contenção iniciada"));
    }

    // ─────────────────────────────────────────────
    // Setup: cria crises nos estados necessários
    // ─────────────────────────────────────────────

    private void garantirCrises() throws Exception {
        if (criseAbertaId != null) return;
        String token = obterTokenAdmin();
        criseAbertaId = criarCrise(token, "Crise ABERTA para ações");
        criseResolvidaId = criarCrise(token, "Crise RESOLVIDA para ações");
        alterarStatus(token, criseResolvidaId, "RESOLVIDA");
        criseEncerradaId = criarCrise(token, "Crise ENCERRADA para ações");
        alterarStatus(token, criseEncerradaId, "ENCERRADA");
    }

    // ─────────────────────────────────────────────
    // 1. Listar ações com token válido → 200
    // ─────────────────────────────────────────────

    @Test
    @Order(1)
    void listarAcoesComToken_deveRetornar200() throws Exception {
        garantirCrises();
        String token = obterTokenAdmin();
        mockMvc.perform(get("/api/crises/" + criseAbertaId + "/acoes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.dados").isArray());
    }

    // ─────────────────────────────────────────────
    // 2. Registrar ação com token válido → 201
    // ─────────────────────────────────────────────

    @Test
    @Order(2)
    void registrarAcaoComToken_deveRetornar201() throws Exception {
        garantirCrises();
        String token = obterTokenAdmin();
        mockMvc.perform(post("/api/crises/" + criseAbertaId + "/acoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAcaoValido()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.dados.tipo").value("CONTENCAO"))
                .andExpect(jsonPath("$.dados.descricao").value("Ação de contenção iniciada"))
                .andExpect(jsonPath("$.dados.criseId").value(criseAbertaId))
                .andExpect(jsonPath("$.dados.executorNome").isNotEmpty());
    }

    // ─────────────────────────────────────────────
    // 3. ADMIN registra ação → 201
    // ─────────────────────────────────────────────

    @Test
    @Order(3)
    @WithMockUser(username = "admin@empresa.com", roles = "ADMIN")
    void adminRegistraAcao_deveRetornar201() throws Exception {
        garantirCrises();
        mockMvc.perform(post("/api/crises/" + criseAbertaId + "/acoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAcaoValido()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201));
    }

    // ─────────────────────────────────────────────
    // 4. GERENTE registra ação → 201
    // ─────────────────────────────────────────────

    @Test
    @Order(4)
    @WithMockUser(username = "admin@empresa.com", roles = "GERENTE")
    void gerenteRegistraAcao_deveRetornar201() throws Exception {
        garantirCrises();
        mockMvc.perform(post("/api/crises/" + criseAbertaId + "/acoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAcaoValido()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201));
    }

    // ─────────────────────────────────────────────
    // 5. ANALISTA registra ação → 201
    // ─────────────────────────────────────────────

    @Test
    @Order(5)
    @WithMockUser(username = "admin@empresa.com", roles = "ANALISTA")
    void analistaRegistraAcao_deveRetornar201() throws Exception {
        garantirCrises();
        mockMvc.perform(post("/api/crises/" + criseAbertaId + "/acoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAcaoValido()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201));
    }

    // ─────────────────────────────────────────────
    // 6. Sem token → 401
    // ─────────────────────────────────────────────

    @Test
    @Order(6)
    void semToken_deveRetornar401() throws Exception {
        garantirCrises();
        mockMvc.perform(get("/api/crises/" + criseAbertaId + "/acoes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        mockMvc.perform(post("/api/crises/" + criseAbertaId + "/acoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAcaoValido()))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────
    // 7. Ação em crise RESOLVIDA → 400
    // ─────────────────────────────────────────────

    @Test
    @Order(7)
    void acaoEmCriseResolvida_deveRetornar400() throws Exception {
        garantirCrises();
        String token = obterTokenAdmin();
        mockMvc.perform(post("/api/crises/" + criseResolvidaId + "/acoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAcaoValido()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ─────────────────────────────────────────────
    // 8. Ação em crise ENCERRADA → 400
    // ─────────────────────────────────────────────

    @Test
    @Order(8)
    void acaoEmCriseEncerrada_deveRetornar400() throws Exception {
        garantirCrises();
        String token = obterTokenAdmin();
        mockMvc.perform(post("/api/crises/" + criseEncerradaId + "/acoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAcaoValido()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ─────────────────────────────────────────────
    // 9. Tipo inválido → 400
    // ─────────────────────────────────────────────

    @Test
    @Order(9)
    void tipoInvalido_deveRetornar400() throws Exception {
        garantirCrises();
        String token = obterTokenAdmin();
        String body = objectMapper.writeValueAsString(Map.of(
                "tipo", "TIPO_INEXISTENTE",
                "descricao", "Descrição qualquer"));
        mockMvc.perform(post("/api/crises/" + criseAbertaId + "/acoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    // ─────────────────────────────────────────────
    // 10. Crise inexistente → 404
    // ─────────────────────────────────────────────

    @Test
    @Order(10)
    void criseInexistente_deveRetornar404() throws Exception {
        String token = obterTokenAdmin();
        mockMvc.perform(get("/api/crises/999999/acoes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        mockMvc.perform(post("/api/crises/999999/acoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAcaoValido()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ─────────────────────────────────────────────
    // 11. Executor é o usuário autenticado
    // ─────────────────────────────────────────────

    @Test
    @Order(11)
    void executorDeveSerUsuarioAutenticado() throws Exception {
        garantirCrises();
        String token = obterTokenAdmin();
        mockMvc.perform(post("/api/crises/" + criseAbertaId + "/acoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoAcaoValido()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dados.executorId").value(1))
                .andExpect(jsonPath("$.dados.executorNome").value("Administrador"));
    }

    // ─────────────────────────────────────────────
    // 12. Listagem ordenada por dataAcao desc
    // ─────────────────────────────────────────────

    @Test
    @Order(12)
    void listagem_deveEstarOrdenadaPorDataAcaoDesc() throws Exception {
        garantirCrises();
        String token = obterTokenAdmin();

        String acaoAntiga = objectMapper.writeValueAsString(Map.of(
                "tipo", "MONITORAMENTO",
                "descricao", "Ação mais antiga",
                "dataAcao", "2020-01-01T08:00:00+00:00"));

        String acaoRecente = objectMapper.writeValueAsString(Map.of(
                "tipo", "RESOLUCAO",
                "descricao", "Ação mais recente",
                "dataAcao", "2099-12-31T23:59:59+00:00"));

        mockMvc.perform(post("/api/crises/" + criseAbertaId + "/acoes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(acaoAntiga));

        mockMvc.perform(post("/api/crises/" + criseAbertaId + "/acoes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(acaoRecente));

        mockMvc.perform(get("/api/crises/" + criseAbertaId + "/acoes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados[0].tipo").value("RESOLUCAO"))
                .andExpect(jsonPath("$.dados[0].descricao").value("Ação mais recente"));
    }
}
