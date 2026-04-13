-- 1. Criar a tabela de carteiras (Postgres usa BIGSERIAL para auto incremento)
CREATE TABLE carteiras (
                           id BIGSERIAL PRIMARY KEY,
                           saldo DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
                           usuario_id BIGINT NOT NULL UNIQUE,
                           CONSTRAINT fk_carteira_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- 2. MIGRAR OS DADOS: Pega o saldo que estava no usuário e joga para a carteira
INSERT INTO carteiras (saldo, usuario_id)
SELECT saldo, id FROM usuarios;

-- 3. Preparar a tabela de transações para a nova estrutura
ALTER TABLE transacoes ADD COLUMN carteira_id BIGINT;

-- 4. VINCULAR TRANSAÇÕES: Aponta cada transação para a carteira correta
UPDATE transacoes
SET carteira_id = carteiras.id
FROM carteiras
WHERE transacoes.usuario_id = carteiras.usuario_id;

-- 5. Adicionar a restrição de NOT NULL e a Foreign Key na carteira_id
-- (No Postgres usamos ALTER COLUMN ... SET NOT NULL)
ALTER TABLE transacoes ALTER COLUMN carteira_id SET NOT NULL;
ALTER TABLE transacoes ADD CONSTRAINT fk_transacao_carteira FOREIGN KEY (carteira_id) REFERENCES carteiras(id);

-- 6. LIMPEZA: Removemos as colunas que não são mais usadas com segurança
-- O CASCADE já cuida de remover as Constraints (FKs) vinculadas a essas colunas.
ALTER TABLE transacoes DROP COLUMN IF EXISTS usuario_id CASCADE;
ALTER TABLE usuarios DROP COLUMN IF EXISTS saldo CASCADE;
