-- Dados de exemplo do usuário demo (id 1), carregados SÓ no perfil prod (ver application-prod.yml).
-- Quem visita o projeto pelo Swagger já encontra contas, transações e um relatório preenchido.
-- As datas são relativas ao mês do deploy: mes 0 = mês atual, -1 = mês anterior, -2 = dois meses atrás.

-- O CTE guarda só as contas criadas aqui, para as transações nunca caírem em outra conta de mesmo nome
WITH contas_demo AS (
    INSERT INTO conta (usuario_id, nome, tipo, saldo_inicial) VALUES
        (1, 'Nubank',   'CORRENTE', 2500.00),
        (1, 'Carteira', 'CARTEIRA',  400.00),
        (1, 'Poupança', 'POUPANCA', 10000.00)
    RETURNING id, nome
)
INSERT INTO transacao (conta_id, categoria_id, descricao, valor, tipo, data)
SELECT co.id,
       ca.id,
       v.descricao,
       v.valor,
       v.tipo,
       (date_trunc('month', CURRENT_DATE) + make_interval(months => v.mes, days => v.dia - 1))::date
FROM (VALUES
    -- Dois meses atrás
    ('Nubank',   'Salário',       'Salário',                  6500.00, 'RECEITA', -2,  5),
    ('Nubank',   'Moradia',       'Aluguel',                  1800.00, 'DESPESA', -2, 10),
    ('Nubank',   'Alimentação',   'Mercado do mês',            820.40, 'DESPESA', -2, 12),
    ('Carteira', 'Alimentação',   'Feira',                      95.00, 'DESPESA', -2, 16),
    ('Nubank',   'Transporte',    'Combustível',               310.00, 'DESPESA', -2, 18),
    ('Nubank',   'Lazer',         'Cinema',                     64.00, 'DESPESA', -2, 22),
    ('Poupança', 'Investimentos', 'Rendimento da poupança',     58.30, 'RECEITA', -2, 28),
    -- Mês anterior
    ('Nubank',   'Salário',       'Salário',                  6500.00, 'RECEITA', -1,  5),
    ('Nubank',   'Freelance',     'Projeto freelance',        1200.00, 'RECEITA', -1,  8),
    ('Nubank',   'Moradia',       'Aluguel',                  1800.00, 'DESPESA', -1, 10),
    ('Nubank',   'Alimentação',   'Mercado do mês',            764.90, 'DESPESA', -1, 11),
    ('Carteira', 'Alimentação',   'Padaria',                    48.50, 'DESPESA', -1, 14),
    ('Nubank',   'Saúde',         'Farmácia',                  132.75, 'DESPESA', -1, 17),
    ('Nubank',   'Educação',      'Curso online',              199.00, 'DESPESA', -1, 20),
    ('Nubank',   'Transporte',    'Combustível',               285.00, 'DESPESA', -1, 23),
    ('Poupança', 'Investimentos', 'Rendimento da poupança',     59.10, 'RECEITA', -1, 28),
    -- Mês atual (primeiros dias, para não ficar com datas no futuro)
    ('Nubank',   'Salário',       'Salário',                  6500.00, 'RECEITA',  0,  1),
    ('Nubank',   'Moradia',       'Aluguel',                  1800.00, 'DESPESA',  0,  1),
    ('Nubank',   'Alimentação',   'Mercado do mês',            698.20, 'DESPESA',  0,  2),
    ('Carteira', 'Alimentação',   'Feira',                      72.00, 'DESPESA',  0,  2),
    ('Nubank',   'Transporte',    'Aplicativo de transporte',   43.90, 'DESPESA',  0,  3)
) AS v(conta, categoria, descricao, valor, tipo, mes, dia)
JOIN contas_demo co ON co.nome = v.conta
JOIN categoria ca ON ca.usuario_id IS NULL AND ca.nome = v.categoria;
