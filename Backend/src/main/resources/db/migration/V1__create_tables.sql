CREATE TABLE perfil (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE,
    descricao VARCHAR(255) NOT NULL
);

CREATE TABLE usuario (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    senha TEXT NOT NULL,
    perfil_id BIGINT NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_usuario_perfil FOREIGN KEY (perfil_id) REFERENCES perfil(id)
);

CREATE TABLE crise (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    nivel VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    data_abertura TIMESTAMP NOT NULL,
    criador_id BIGINT NOT NULL,
    responsavel_id BIGINT,
    CONSTRAINT fk_crise_criador FOREIGN KEY (criador_id) REFERENCES usuario(id),
    CONSTRAINT fk_crise_responsavel FOREIGN KEY (responsavel_id) REFERENCES usuario(id)
);

CREATE TABLE acao_crise (
    id BIGSERIAL PRIMARY KEY,
    descricao TEXT NOT NULL,
    tipo VARCHAR(30) NOT NULL,
    data_acao TIMESTAMP NOT NULL,
    crise_id BIGINT NOT NULL,
    executor_id BIGINT NOT NULL,
    CONSTRAINT fk_acao_crise FOREIGN KEY (crise_id) REFERENCES crise(id),
    CONSTRAINT fk_acao_executor FOREIGN KEY (executor_id) REFERENCES usuario(id)
);

CREATE TABLE relatorio_crise (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    conteudo TEXT NOT NULL,
    data_geracao TIMESTAMP NOT NULL,
    crise_id BIGINT NOT NULL,
    gerador_id BIGINT NOT NULL,
    CONSTRAINT fk_relatorio_crise FOREIGN KEY (crise_id) REFERENCES crise(id),
    CONSTRAINT fk_relatorio_gerador FOREIGN KEY (gerador_id) REFERENCES usuario(id)
);

CREATE TABLE auditoria_log (
    id BIGSERIAL PRIMARY KEY,
    acao VARCHAR(200) NOT NULL,
    entidade VARCHAR(100) NOT NULL,
    detalhes TEXT,
    data_registro TIMESTAMP NOT NULL,
    usuario_id BIGINT,
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);
