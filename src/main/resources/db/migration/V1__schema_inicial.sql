CREATE TABLE usuario (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(120) NOT NULL,
    email       VARCHAR(160) NOT NULL UNIQUE,
    senha_hash  VARCHAR(255) NOT NULL,
    criado_em   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE conta (
    id            BIGSERIAL PRIMARY KEY,
    usuario_id    BIGINT         NOT NULL REFERENCES usuario (id),
    nome          VARCHAR(80)    NOT NULL,
    tipo          VARCHAR(20)    NOT NULL, -- CORRENTE, POUPANCA, CARTEIRA, INVESTIMENTO
    saldo_inicial NUMERIC(15, 2) NOT NULL DEFAULT 0,
    criado_em     TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- usuario_id nulo = categoria global (disponível para todos)
CREATE TABLE categoria (
    id          BIGSERIAL PRIMARY KEY,
    usuario_id  BIGINT      REFERENCES usuario (id),
    nome        VARCHAR(60) NOT NULL,
    tipo        VARCHAR(10) NOT NULL, -- RECEITA, DESPESA
    CONSTRAINT uk_categoria_usuario_nome_tipo UNIQUE (usuario_id, nome, tipo)
);

CREATE TABLE transacao (
    id            BIGSERIAL PRIMARY KEY,
    conta_id      BIGINT         NOT NULL REFERENCES conta (id),
    categoria_id  BIGINT         NOT NULL REFERENCES categoria (id),
    descricao     VARCHAR(160)   NOT NULL,
    valor         NUMERIC(15, 2) NOT NULL CHECK (valor > 0),
    tipo          VARCHAR(10)    NOT NULL, -- RECEITA, DESPESA
    data          DATE           NOT NULL,
    criado_em     TIMESTAMP      NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_conta_usuario ON conta (usuario_id);
CREATE INDEX idx_transacao_conta_data ON transacao (conta_id, data);
CREATE INDEX idx_transacao_categoria ON transacao (categoria_id);
