INSERT INTO perfil (nome, descricao) VALUES
('ADMIN', 'Perfil administrativo com acesso total ao sistema'),
('GERENTE', 'Perfil de gestão e acompanhamento de crises'),
('ANALISTA', 'Perfil especializado em análise e documentação');

INSERT INTO usuario (nome, email, senha, perfil_id, ativo)
SELECT 'Administrador', 'admin@empresa.com', '$2a$10$eT4uVbA6XVGB7dFGqinqTOqW0sSBWBhnAoyLAtcaQzTHxFx57yfjq', p.id, true
FROM perfil p
WHERE p.nome = 'ADMIN';
