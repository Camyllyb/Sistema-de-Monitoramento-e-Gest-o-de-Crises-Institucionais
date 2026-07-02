package br.edu.gestaocrises;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseConnectionTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void deveConectarAoBancoEExistirTabelaPerfil() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM perfil", Integer.class);
        assertThat(count).isNotNull();
    }
}
