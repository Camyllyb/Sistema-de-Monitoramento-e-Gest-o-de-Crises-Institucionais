package br.edu.gestaocrises;

import br.edu.gestaocrises.auditoria.AuditoriaLog;
import br.edu.gestaocrises.auditoria.AuditoriaLogRepository;
import br.edu.gestaocrises.usuarios.Usuario;
import br.edu.gestaocrises.usuarios.UsuarioRepository;
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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração do módulo de auditoria — etapa 1 (consulta).
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
class AuditoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuditoriaLogRepository auditoriaLogRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static boolean logsSeeded = false;

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

    private void garantirLogs() {
        if (logsSeeded) return;
        Usuario admin = usuarioRepository.findByEmail("admin@empresa.com").orElseThrow();

        AuditoriaLog logCrise = AuditoriaLog.builder()
                .acao("CRIACAO")
                .entidade("CRISE")
                .entidadeId(10L)
                .detalhes("Crise criada pelo administrador")
                .dataRegistro(OffsetDateTime.of(2026, 1, 15, 10, 0, 0, 0, ZoneOffset.UTC))
                .usuario(admin)
                .build();

        AuditoriaLog logRelatorio = AuditoriaLog.builder()
                .acao("GERACAO_RELATORIO")
                .entidade("RELATORIO")
                .entidadeId(20L)
                .detalhes("Relatório gerado")
                .dataRegistro(OffsetDateTime.of(2026, 4, 20, 14, 0, 0, 0, ZoneOffset.UTC))
                .usuario(admin)
                .build();

        AuditoriaLog logSistema = AuditoriaLog.builder()
                .acao("ATUALIZACAO")
                .entidade("USUARIO")
                .entidadeId(null)
                .detalhes("Atualização de perfil")
                .dataRegistro(OffsetDateTime.of(2026, 2, 28, 9, 0, 0, 0, ZoneOffset.UTC))
                .usuario(null)
                .build();

        auditoriaLogRepository.saveAll(List.of(logCrise, logRelatorio, logSistema));
        logsSeeded = true;
    }

    // ─────────────────────────────────────────────
    // 1. ADMIN consulta auditoria com sucesso → 200
    // ─────────────────────────────────────────────

    @Test
    @Order(1)
    void adminConsultaAuditoria_deveRetornar200() throws Exception {
        garantirLogs();
        String token = obterTokenAdmin();
        mockMvc.perform(get("/api/auditoria")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.dados").isArray());
    }

    // ─────────────────────────────────────────────
    // 2. GERENTE consulta auditoria com sucesso → 200
    // ─────────────────────────────────────────────

    @Test
    @Order(2)
    @WithMockUser(username = "admin@empresa.com", roles = "GERENTE")
    void gerenteConsultaAuditoria_deveRetornar200() throws Exception {
        garantirLogs();
        mockMvc.perform(get("/api/auditoria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.dados").isArray());
    }

    // ─────────────────────────────────────────────
    // 3. ANALISTA recebe 403
    // ─────────────────────────────────────────────

    @Test
    @Order(3)
    @WithMockUser(username = "admin@empresa.com", roles = "ANALISTA")
    void analistaConsultaAuditoria_deveRetornar403() throws Exception {
        mockMvc.perform(get("/api/auditoria"))
                .andExpect(status().isForbidden());
    }

    // ─────────────────────────────────────────────
    // 4. Sem token → 401
    // ─────────────────────────────────────────────

    @Test
    @Order(4)
    void semToken_deveRetornar401() throws Exception {
        mockMvc.perform(get("/api/auditoria"))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────
    // 5. Filtro por entidade
    // ─────────────────────────────────────────────

    @Test
    @Order(5)
    void filtroPorEntidade_deveRetornarApenasCrise() throws Exception {
        garantirLogs();
        String token = obterTokenAdmin();
        mockMvc.perform(get("/api/auditoria")
                        .param("entidade", "CRISE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados", not(empty())))
                .andExpect(jsonPath("$.dados[*].entidade",
                        everyItem(equalToIgnoringCase("CRISE"))));
    }

    // ─────────────────────────────────────────────
    // 6. Filtro por ação
    // ─────────────────────────────────────────────

    @Test
    @Order(6)
    void filtroPorAcao_deveRetornarRelatorio() throws Exception {
        garantirLogs();
        String token = obterTokenAdmin();
        mockMvc.perform(get("/api/auditoria")
                        .param("acao", "GERACAO_RELATORIO")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados", not(empty())))
                .andExpect(jsonPath("$.dados[0].acao").value("GERACAO_RELATORIO"));
    }

    // ─────────────────────────────────────────────
    // 7. Filtro por período
    // ─────────────────────────────────────────────

    @Test
    @Order(7)
    void filtroPorPeriodo_deveRetornarApenasLogsNoPeriodo() throws Exception {
        garantirLogs();
        String token = obterTokenAdmin();
        // Período: 2026-03-01 a 2026-05-01 — inclui apenas logRelatorio (abr/2026)
        mockMvc.perform(get("/api/auditoria")
                        .param("dataInicio", "2026-03-01T00:00:00+00:00")
                        .param("dataFim", "2026-05-01T00:00:00+00:00")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados", not(empty())))
                .andExpect(jsonPath("$.dados[0].acao").value("GERACAO_RELATORIO"));
    }

    // ─────────────────────────────────────────────
    // 8. Resposta não contém senha, token nem dado sensível
    // ─────────────────────────────────────────────

    @Test
    @Order(8)
    void respostaNaoContemDadosSensiveis() throws Exception {
        garantirLogs();
        String token = obterTokenAdmin();
        String resposta = mockMvc.perform(get("/api/auditoria")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        org.assertj.core.api.Assertions.assertThat(resposta)
                .doesNotContainIgnoringCase("senha")
                .doesNotContainIgnoringCase("password")
                .doesNotContainIgnoringCase("secret")
                .doesNotContainIgnoringCase("token")
                .doesNotContainIgnoringCase("jwt");
    }
}
