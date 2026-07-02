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
 * Testes de integração do módulo de crises.
 *
 * Banco: H2 in-memory (perfil "test") com Flyway (V1, V2, V3).
 * Banco oficial do projeto: PostgreSQL.
 *
 * Para executar com Java 21:
 *   docker run --rm -v "$PWD":/workspace -w /workspace maven:3.9.9-eclipse-temurin-21 mvn clean test
 *
 * Nota sobre o campo 'tipo':
 *   A entidade Crise possui o campo 'tipo' (NOT NULL no banco).
 *   Ele não está descrito no spec de crises, mas é exigido pelo schema V1.
 *   Os DTOs incluem 'tipo' para garantir que a persistência funcione corretamente.
 *
 * Nota sobre DELETE:
 *   A entidade Crise não possui campo 'ativo'. Por isso, a exclusão física
 *   só é permitida para crises com status ABERTA (sem histórico).
 *   Uma migration futura deve adicionar 'ativo' para soft delete completo.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CriseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static Long criseId;
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

    private String corpoCreatValido() throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "titulo", "Indisponibilidade do sistema acadêmico",
                "descricao", "Sistema acadêmico fora do ar",
                "tipo", "TECNOLOGIA",
                "nivel", "ALTO",
                "responsavelId", 1));
    }

    private Long criarEEncerrarCrise() throws Exception {
        String token = obterTokenAdmin();
        String criar = objectMapper.writeValueAsString(Map.of(
                "titulo", "Crise para encerrar",
                "descricao", "Será encerrada nos testes",
                "tipo", "ADMINISTRATIVA",
                "nivel", "BAIXO",
                "responsavelId", 1));

        String resposta = mockMvc.perform(post("/api/crises")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criar))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(resposta).path("dados").path("id").asLong();

        String encerrar = objectMapper.writeValueAsString(Map.of("status", "ENCERRADA"));
        mockMvc.perform(patch("/api/crises/" + id + "/status")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(encerrar));

        return id;
    }

    // ─────────────────────────────────────────────
    // 1. ADMIN cria crise com dados válidos → 201
    // ─────────────────────────────────────────────

    @Test
    @Order(1)
    void adminCriaCriseValida_deveRetornar201() throws Exception {
        String token = obterTokenAdmin();

        String resposta = mockMvc.perform(post("/api/crises")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoCreatValido()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.dados.titulo").value("Indisponibilidade do sistema acadêmico"))
                .andExpect(jsonPath("$.dados.status").value("ABERTA"))
                .andExpect(jsonPath("$.dados.nivel").value("ALTO"))
                .andExpect(jsonPath("$.dados.criadoPorNome").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        criseId = objectMapper.readTree(resposta).path("dados").path("id").asLong();
    }

    // ─────────────────────────────────────────────
    // 2. GERENTE cria crise → 201
    // ─────────────────────────────────────────────

    @Test
    @Order(2)
    @WithMockUser(username = "admin@empresa.com", roles = "GERENTE")
    void gerenteCriaCrise_deveRetornar201() throws Exception {
        mockMvc.perform(post("/api/crises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoCreatValido()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.dados.status").value("ABERTA"));
    }

    // ─────────────────────────────────────────────
    // 3. ANALISTA não cria crise → 403
    // ─────────────────────────────────────────────

    @Test
    @Order(3)
    @WithMockUser(roles = "ANALISTA")
    void analistaNaoCriaCrise_deveRetornar403() throws Exception {
        mockMvc.perform(post("/api/crises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpoCreatValido()))
                .andExpect(status().isForbidden());
    }

    // ─────────────────────────────────────────────
    // 4. Sem token ao listar → 401
    // ─────────────────────────────────────────────

    @Test
    @Order(4)
    void semTokenAoListar_deveRetornar401() throws Exception {
        mockMvc.perform(get("/api/crises"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    // ─────────────────────────────────────────────
    // 5. ADMIN lista crises → 200
    // ─────────────────────────────────────────────

    @Test
    @Order(5)
    void adminListaCrises_deveRetornar200() throws Exception {
        String token = obterTokenAdmin();
        mockMvc.perform(get("/api/crises")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.dados").isArray());
    }

    // ─────────────────────────────────────────────
    // 6. Buscar crise inexistente → 404
    // ─────────────────────────────────────────────

    @Test
    @Order(6)
    void buscarCriseInexistente_deveRetornar404() throws Exception {
        String token = obterTokenAdmin();
        mockMvc.perform(get("/api/crises/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ─────────────────────────────────────────────
    // 7. Criar sem título → 400
    // ─────────────────────────────────────────────

    @Test
    @Order(7)
    void criarSemTitulo_deveRetornar400() throws Exception {
        String token = obterTokenAdmin();
        String body = objectMapper.writeValueAsString(Map.of(
                "descricao", "Sem título",
                "tipo", "TECNOLOGIA",
                "nivel", "ALTO",
                "responsavelId", 1));

        mockMvc.perform(post("/api/crises")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ─────────────────────────────────────────────
    // 8. Criar com responsável inexistente → 404
    // ─────────────────────────────────────────────

    @Test
    @Order(8)
    void criarComResponsavelInexistente_deveRetornar404() throws Exception {
        String token = obterTokenAdmin();
        String body = objectMapper.writeValueAsString(Map.of(
                "titulo", "Crise teste",
                "descricao", "Responsável que não existe",
                "tipo", "TECNOLOGIA",
                "nivel", "ALTO",
                "responsavelId", 999999));

        mockMvc.perform(post("/api/crises")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // ─────────────────────────────────────────────
    // 9. Atualizar crise → 200
    // ─────────────────────────────────────────────

    @Test
    @Order(9)
    void atualizarCrise_deveRetornar200() throws Exception {
        String token = obterTokenAdmin();
        String body = objectMapper.writeValueAsString(Map.of(
                "titulo", "Título atualizado",
                "descricao", "Descrição atualizada",
                "tipo", "INFRAESTRUTURA",
                "nivel", "CRITICO",
                "responsavelId", 1));

        mockMvc.perform(put("/api/crises/" + criseId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.dados.titulo").value("Título atualizado"))
                .andExpect(jsonPath("$.dados.nivel").value("CRITICO"));
    }

    // ─────────────────────────────────────────────
    // 10. Alterar status ABERTA → EM_ANDAMENTO → 200
    // ─────────────────────────────────────────────

    @Test
    @Order(10)
    void alterarStatusAbertaParaEmAndamento_deveRetornar200() throws Exception {
        String token = obterTokenAdmin();
        String body = objectMapper.writeValueAsString(Map.of("status", "EM_ANDAMENTO"));

        mockMvc.perform(patch("/api/crises/" + criseId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.dados.status").value("EM_ANDAMENTO"));
    }

    // ─────────────────────────────────────────────
    // 11. Alterar status ENCERRADA → ABERTA → 400
    // ─────────────────────────────────────────────

    @Test
    @Order(11)
    void alterarStatusEncerradaParaAberta_deveRetornar400() throws Exception {
        criseEncerradaId = criarEEncerrarCrise();
        String token = obterTokenAdmin();
        String body = objectMapper.writeValueAsString(Map.of("status", "ABERTA"));

        mockMvc.perform(patch("/api/crises/" + criseEncerradaId + "/status")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ─────────────────────────────────────────────
    // 12. Editar crise ENCERRADA → 400
    // ─────────────────────────────────────────────

    @Test
    @Order(12)
    void editarCriseEncerrada_deveRetornar400() throws Exception {
        String token = obterTokenAdmin();
        String body = objectMapper.writeValueAsString(Map.of(
                "titulo", "Tentativa de edição",
                "descricao", "Não deve funcionar",
                "tipo", "TECNOLOGIA",
                "nivel", "BAIXO",
                "responsavelId", 1));

        mockMvc.perform(put("/api/crises/" + criseEncerradaId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
