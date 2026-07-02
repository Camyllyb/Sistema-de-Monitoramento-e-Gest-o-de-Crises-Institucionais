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
 * Testes de integração do módulo de relatórios de crise.
 *
 * Banco: H2 in-memory (perfil "test") com Flyway.
 *
 * Executar com Java 21:
 *   docker run --rm -v "$PWD":/workspace -w /workspace maven:3.9.9-eclipse-temurin-21 mvn clean test
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RelatorioCriseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static Long criseResolvidaId;
    private static Long criseResolvidaId2;
    private static Long criseEncerradaId;
    private static Long criseAbertaId;
    private static Long criseEmAndamentoId;
    private static Long relatorioId;

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
                "descricao", "Descrição para relatório",
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

    private void garantirCrises() throws Exception {
        if (criseResolvidaId != null) return;
        String token = obterTokenAdmin();

        criseResolvidaId = criarCrise(token, "Crise RESOLVIDA para relatório (admin)");
        alterarStatus(token, criseResolvidaId, "RESOLVIDA");

        criseResolvidaId2 = criarCrise(token, "Crise RESOLVIDA para relatório (gerente)");
        alterarStatus(token, criseResolvidaId2, "RESOLVIDA");

        criseEncerradaId = criarCrise(token, "Crise ENCERRADA para relatório");
        alterarStatus(token, criseEncerradaId, "ENCERRADA");

        criseAbertaId = criarCrise(token, "Crise ABERTA para relatório");

        criseEmAndamentoId = criarCrise(token, "Crise EM_ANDAMENTO para relatório");
        alterarStatus(token, criseEmAndamentoId, "EM_ANDAMENTO");
    }

    private String corpoRelatorioValido(Long criseId) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "criseId", criseId,
                "titulo", "Relatório Final",
                "conteudo", "Conteúdo detalhado da análise da crise."));
    }

    // ─────────────────────────────────────────────
    // 1. ADMIN gera relatório para crise RESOLVIDA → 201
    // ─────────────────────────────────────────────

    @Test
    @Order(1)
    void adminGeraRelatorioParaCriseResolvida_deveRetornar201() throws Exception {
        garantirCrises();
        String token = obterTokenAdmin();
        String resposta = mockMvc.perform(post("/api/relatorios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoRelatorioValido(criseResolvidaId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.dados.id").isNumber())
                .andExpect(jsonPath("$.dados.criseId").value(criseResolvidaId))
                .andExpect(jsonPath("$.dados.titulo").value("Relatório Final"))
                .andReturn().getResponse().getContentAsString();

        relatorioId = objectMapper.readTree(resposta).path("dados").path("id").asLong();
    }

    // ─────────────────────────────────────────────
    // 2. GERENTE gera relatório para crise RESOLVIDA → 201
    // ─────────────────────────────────────────────

    @Test
    @Order(2)
    @WithMockUser(username = "admin@empresa.com", roles = "GERENTE")
    void gerenteGeraRelatorioParaCriseResolvida_deveRetornar201() throws Exception {
        garantirCrises();
        mockMvc.perform(post("/api/relatorios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoRelatorioValido(criseResolvidaId2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.dados.criseId").value(criseResolvidaId2));
    }

    // ─────────────────────────────────────────────
    // 3. Gera relatório para crise ENCERRADA → 201
    // ─────────────────────────────────────────────

    @Test
    @Order(3)
    void geraRelatorioParaCriseEncerrada_deveRetornar201() throws Exception {
        garantirCrises();
        String token = obterTokenAdmin();
        mockMvc.perform(post("/api/relatorios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoRelatorioValido(criseEncerradaId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.dados.criseId").value(criseEncerradaId));
    }

    // ─────────────────────────────────────────────
    // 4. Nega relatório para crise ABERTA → 400
    // ─────────────────────────────────────────────

    @Test
    @Order(4)
    void negaRelatorioParaCriseAberta_deveRetornar400() throws Exception {
        garantirCrises();
        String token = obterTokenAdmin();
        mockMvc.perform(post("/api/relatorios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoRelatorioValido(criseAbertaId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ─────────────────────────────────────────────
    // 5. Nega relatório para crise EM_ANDAMENTO → 400
    // ─────────────────────────────────────────────

    @Test
    @Order(5)
    void negaRelatorioParaCriseEmAndamento_deveRetornar400() throws Exception {
        garantirCrises();
        String token = obterTokenAdmin();
        mockMvc.perform(post("/api/relatorios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoRelatorioValido(criseEmAndamentoId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ─────────────────────────────────────────────
    // 6. Nega relatório duplicado → 400
    // ─────────────────────────────────────────────

    @Test
    @Order(6)
    void negaRelatorioDuplicado_deveRetornar400() throws Exception {
        garantirCrises();
        String token = obterTokenAdmin();
        // criseResolvidaId já tem relatório criado no teste 1
        mockMvc.perform(post("/api/relatorios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoRelatorioValido(criseResolvidaId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ─────────────────────────────────────────────
    // 7. ANALISTA não pode gerar relatório → 403
    // ─────────────────────────────────────────────

    @Test
    @Order(7)
    @WithMockUser(username = "admin@empresa.com", roles = "ANALISTA")
    void analistaNaoPodeGerarRelatorio_deveRetornar403() throws Exception {
        garantirCrises();
        mockMvc.perform(post("/api/relatorios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoRelatorioValido(criseResolvidaId)))
                .andExpect(status().isForbidden());
    }

    // ─────────────────────────────────────────────
    // 8. Sem token → 401
    // ─────────────────────────────────────────────

    @Test
    @Order(8)
    void semToken_deveRetornar401() throws Exception {
        garantirCrises();
        mockMvc.perform(post("/api/relatorios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoRelatorioValido(criseResolvidaId)))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/relatorios"))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────
    // 9. Lista relatórios → 200 com array
    // ─────────────────────────────────────────────

    @Test
    @Order(9)
    void listarRelatorios_deveRetornar200() throws Exception {
        String token = obterTokenAdmin();
        mockMvc.perform(get("/api/relatorios")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.dados").isArray());
    }

    // ─────────────────────────────────────────────
    // 10. Busca relatório por id → 200
    // ─────────────────────────────────────────────

    @Test
    @Order(10)
    void buscarPorId_deveRetornar200() throws Exception {
        String token = obterTokenAdmin();
        mockMvc.perform(get("/api/relatorios/" + relatorioId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.dados.id").value(relatorioId))
                .andExpect(jsonPath("$.dados.titulo").value("Relatório Final"));
    }

    // ─────────────────────────────────────────────
    // 11. Relatório inexistente → 404
    // ─────────────────────────────────────────────

    @Test
    @Order(11)
    void relatorioInexistente_deveRetornar404() throws Exception {
        String token = obterTokenAdmin();
        mockMvc.perform(get("/api/relatorios/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ─────────────────────────────────────────────
    // 12. Vincula relatório à crise e ao usuário gerador
    // ─────────────────────────────────────────────

    @Test
    @Order(12)
    void relatorioVinculadoAoCriseEGerador() throws Exception {
        String token = obterTokenAdmin();
        mockMvc.perform(get("/api/relatorios/" + relatorioId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.criseId").value(criseResolvidaId))
                .andExpect(jsonPath("$.dados.geradorId").value(1))
                .andExpect(jsonPath("$.dados.geradorNome").value("Administrador"))
                .andExpect(jsonPath("$.dados.dataGeracao").isNotEmpty());
    }
}
