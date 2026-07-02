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
 * Testes de integração do módulo de dashboard.
 *
 * Executar com Java 21:
 *   docker run --rm -v "$PWD":/workspace -w /workspace maven:3.9.9-eclipse-temurin-21 mvn clean test
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    // ─────────────────────────────────────────────
    // 1. ADMIN consulta dashboard → 200
    // ─────────────────────────────────────────────

    @Test
    @Order(1)
    void adminConsultaDashboard_deveRetornar200() throws Exception {
        String token = obterTokenAdmin();
        mockMvc.perform(get("/api/dashboard/resumo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.dados.totalCrises").isNumber())
                .andExpect(jsonPath("$.dados.crisesPorStatus").isMap())
                .andExpect(jsonPath("$.dados.crisesPorNivel").isMap())
                .andExpect(jsonPath("$.dados.totalCrisesCriticas").isNumber());
    }

    // ─────────────────────────────────────────────
    // 2. GERENTE consulta dashboard → 200
    // ─────────────────────────────────────────────

    @Test
    @Order(2)
    @WithMockUser(username = "admin@empresa.com", roles = "GERENTE")
    void gerenteConsultaDashboard_deveRetornar200() throws Exception {
        mockMvc.perform(get("/api/dashboard/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.dados.totalCrises").isNumber());
    }

    // ─────────────────────────────────────────────
    // 3. ANALISTA consulta dashboard → 200
    // ─────────────────────────────────────────────

    @Test
    @Order(3)
    @WithMockUser(username = "admin@empresa.com", roles = "ANALISTA")
    void analistaConsultaDashboard_deveRetornar200() throws Exception {
        mockMvc.perform(get("/api/dashboard/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.dados.totalCrises").isNumber());
    }

    // ─────────────────────────────────────────────
    // 4. Sem token → 401
    // ─────────────────────────────────────────────

    @Test
    @Order(4)
    void semToken_deveRetornar401() throws Exception {
        mockMvc.perform(get("/api/dashboard/resumo"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    // ─────────────────────────────────────────────
    // 5. Dashboard retorna dados reais do banco
    // ─────────────────────────────────────────────

    @Test
    @Order(5)
    void dashboardDeveRetornarDadosReais() throws Exception {
        String token = obterTokenAdmin();
        mockMvc.perform(get("/api/dashboard/resumo")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados.crisesPorStatus.ABERTA").isNumber())
                .andExpect(jsonPath("$.dados.crisesPorStatus.EM_ANDAMENTO").isNumber())
                .andExpect(jsonPath("$.dados.crisesPorStatus.RESOLVIDA").isNumber())
                .andExpect(jsonPath("$.dados.crisesPorStatus.ENCERRADA").isNumber())
                .andExpect(jsonPath("$.dados.crisesPorNivel.BAIXO").isNumber())
                .andExpect(jsonPath("$.dados.crisesPorNivel.MEDIO").isNumber())
                .andExpect(jsonPath("$.dados.crisesPorNivel.ALTO").isNumber())
                .andExpect(jsonPath("$.dados.crisesPorNivel.CRITICO").isNumber());
    }
}
