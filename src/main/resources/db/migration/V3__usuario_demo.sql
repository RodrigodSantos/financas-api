-- Usuário de demonstração usado enquanto a autenticação (JWT) não existe.
-- Ver br.com.financas.shared.usuario.UsuarioLogado.
INSERT INTO usuario (id, nome, email, senha_hash)
VALUES (1, 'Usuário Demo', 'demo@financas.local', 'sem-login');

SELECT setval('usuario_id_seq', (SELECT MAX(id) FROM usuario));
