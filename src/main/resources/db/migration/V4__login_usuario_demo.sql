-- Transforma o usuário de demonstração num login real, para quem visitar o projeto
-- conseguir testar pelo Swagger sem se cadastrar.
-- E-mail: demo@financas.local | Senha: demo1234 (hash BCrypt)
UPDATE usuario
SET senha_hash = '$2a$10$NMsMw1nrxsIDDzPRKl2vXOV9.nzeht779CdNuMxLdwmm1Ew/Id8Sm'
WHERE id = 1;
