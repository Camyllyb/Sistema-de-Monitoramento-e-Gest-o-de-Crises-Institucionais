package br.edu.gestaocrises;

import br.edu.gestaocrises.auditoria.AuditoriaLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de integração — auditoria automática gerada pelos outros services.
 *
 * Cada operação (criar crise, editar, status, ação, relatório) deve gerar
 * um log de auditoria consultável via GET /api/auditoria.
 *
 * Executar com Java 21:
 *   docker run --rm -v "$PWD":/workspace -w /workspace maven:3.9.9-eclipse-temurin-21 mvn clean test
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuditoriaIntegracaoTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuditoriaLogRepository auditoriaLogRepository;

    private static Long criseId;
    private static Long criseParaRelatorioId;

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
                "descricao", "Descrição auditoria",
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

    private void alterarStatus(String token, Long id, String status) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("status", status));
        mockMvc.perform(patch("/api/crises/" + id + "/status")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private boolean existeLogComAcao(String acao) {
        return auditoriaLogRepository.findAll().stream()
                .anyMatch(l -> acao.equals(l.getAcao()));
    }

    // ─────────────────────────────────────────────
    // 1. Login gera log LOGIN
    // ─────────────────────────────────────────────

    @Test
    @Order(1)
    void login_deveGerarLogLogin() throws Exception {
        String token = obterTokenAdmin();
        assertThat(existeLogComAcao("LOGIN")).isTrue();
    }

    // ─────────────────────────────────────────────
    // 2. Criar crise gera log CRIACAO_CRISE
    // ─────────────────────────────────────────────

    @Test
    @Order(2)
    void criarCrise_deveGerarLogCriacaoCrise() throws Exception {
        String token = obterTokenAdmin();
        criseId = criarCrise(token, "Crise para auditoria");
        criseParaRelatorioId = criarCrise(token, "Crise para relatório auditado");

        assertThat(existeLogComAcao("CRIACAO_CRISE")).isTrue();
    }

    // ─────────────────────────────────────────────
    // 3. Editar crise gera log EDICAO_CRISE
    // ─────────────────────────────────────────────

    @Test
    @Order(3)
    void editarCrise_deveGerarLogEdicaoCrise() throws Exception {
        String token = obterTokenAdmin();
        String body = objectMapper.writeValueAsString(Map.of(
                "titulo", "Crise editada auditoria",
                "descricao", "Descrição atualizada",
                "tipo", "TECNOLOGIA",
                "nivel", "CRITICO",
                "responsavelId", 1));
        mockMvc.perform(put("/api/crises/" + criseId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        assertThat(existeLogComAcao("EDICAO_CRISE")).isTrue();
    }

    // ─────────────────────────────────────────────
    // 4. Alterar status gera log ALTERACAO_STATUS_CRISE
    // ─────────────────────────────────────────────

    @Test
    @Order(4)
    void alterarStatus_deveGerarLogAlteracaoStatus() throws Exception {
        String token = obterTokenAdmin();
        alterarStatus(token, criseId, "EM_ANDAMENTO");

        assertThat(existeLogComAcao("ALTERACAO_STATUS_CRISE")).isTrue();

        // Verifica detalhes do último log de status
        boolean detalhesCorretos = auditoriaLogRepository.findAll().stream()
                .filter(l -> "ALTERACAO_STATUS_CRISE".equals(l.getAcao()))
                .anyMatch(l -> l.getDetalhes() != null
                        && l.getDetalhes().contains("ABERTA")
                        && l.getDetalhes().contains("EM_ANDAMENTO"));
        assertThat(detalhesCorretos).isTrue();
    }

    // ─────────────────────────────────────────────
    // 5. Registrar ação de crise gera log REGISTRO_ACAO_CRISE
    // ─────────────────────────────────────────────

    @Test
    @Order(5)
    void registrarAcaoCrise_deveGerarLogRegistroAcao() throws Exception {
        String token = obterTokenAdmin();
        String body = objectMapper.writeValueAsString(Map.of(
                "tipo", "CONTENCAO",
                "descricao", "Contenção iniciada para auditoria"));
        mockMvc.perform(post("/api/crises/" + criseId + "/acoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        assertThat(existeLogComAcao("REGISTRO_ACAO_CRISE")).isTrue();
    }

    // ─────────────────────────────────────────────
    // 6. Gerar relatório gera log GERACAO_RELATORIO
    // ─────────────────────────────────────────────

    @Test
    @Order(6)
    void gerarRelatorio_deveGerarLogGeracaoRelatorio() throws Exception {
        String token = obterTokenAdmin();
        // Crise precisa ser RESOLVIDA para ter relatório
        alterarStatus(token, criseParaRelatorioId, "RESOLVIDA");

        String body = objectMapper.writeValueAsString(Map.of(
                "criseId", criseParaRelatorioId,
                "titulo", "Relatório auditado",
                "conteudo", "Conteúdo do relatório para auditoria"));
        mockMvc.perform(post("/api/relatorios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        assertThat(existeLogComAcao("GERACAO_RELATORIO")).isTrue();
    }

    // ─────────────────────────────────────────────
    // 7. Logs criados têm usuarioId, acao, entidade, entidadeId, dataRegistro
    // ─────────────────────────────────────────────

    @Test
    @Order(7)
    void logsDevemTerCamposObrigatorios() throws Exception {
        String token = obterTokenAdmin();
        mockMvc.perform(get("/api/auditoria")
                        .param("acao", "CRIACAO_CRISE")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados", not(empty())))
                .andExpect(jsonPath("$.dados[0].id").isNumber())
                .andExpect(jsonPath("$.dados[0].acao").value("CRIACAO_CRISE"))
                .andExpect(jsonPath("$.dados[0].entidade").value("CRISE"))
                .andExpect(jsonPath("$.dados[0].entidadeId").isNumber())
                .andExpect(jsonPath("$.dados[0].usuarioId").isNumber())
                .andExpect(jsonPath("$.dados[0].dataRegistro").isNotEmpty());
    }

    // ─────────────────────────────────────────────
    // 8. Resposta não contém dados sensíveis
    // ─────────────────────────────────────────────

    @Test
    @Order(8)
    void logsNaoContemDadosSensiveis() throws Exception {
        String token = obterTokenAdmin();
        String resposta = mockMvc.perform(get("/api/auditoria")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(resposta)
                .doesNotContainIgnoringCase("senha")
                .doesNotContainIgnoringCase("password")
                .doesNotContainIgnoringCase("secret")
                .doesNotContainIgnoringCase("jwt");
    }

    // ─────────────────────────────────────────────
    // 9. Filtro por ação retorna apenas logs daquela ação
    // ─────────────────────────────────────────────

    @Test
    @Order(9)
    void filtroAcao_deveRetornarApenasLogsCorretos() throws Exception {
        String token = obterTokenAdmin();
        mockMvc.perform(get("/api/auditoria")
                        .param("acao", "GERACAO_RELATORIO")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dados", not(empty())))
                .andExpect(jsonPath("$.dados[0].acao").value("GERACAO_RELATORIO"))
                .andExpect(jsonPath("$.dados[0].entidade").value("RELATORIO_CRISE"));
    }
}
