-- Remove a restrição antiga que está causando o erro 500
ALTER TABLE transacoes DROP CONSTRAINT IF EXISTS transacoes_tipo_check;

-- Opcional: Adiciona a nova restrição com todos os tipos atuais (mais seguro)
ALTER TABLE transacoes ADD CONSTRAINT transacoes_tipo_check
    CHECK (tipo IN ('DEPOSITO', 'SAQUE', 'TRANSFERENCIA_ENVIADA', 'TRANSFERENCIA_RECEBIDA'));