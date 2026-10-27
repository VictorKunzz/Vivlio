-- N2: avaliacao do estado do livro na doacao (PR #13).
-- As colunas aceitam nulo porque os livros doados antes da avaliacao nao tem essas respostas.
-- IF NOT EXISTS: bancos que ja receberam essas colunas pelo ddl-auto=update tambem migram.
-- Os codigos das opcoes ficam sem CHECK de proposito: incluir uma opcao nova na ficha so
-- exige mudar o enum Java, sem migracao.

ALTER TABLE livros ADD COLUMN IF NOT EXISTS estado_capa VARCHAR(255);
ALTER TABLE livros ADD COLUMN IF NOT EXISTS estado_lombada VARCHAR(255);
ALTER TABLE livros ADD COLUMN IF NOT EXISTS estado_paginas VARCHAR(255);
ALTER TABLE livros ADD COLUMN IF NOT EXISTS estado_marcacoes VARCHAR(255);
ALTER TABLE livros ADD COLUMN IF NOT EXISTS estado_manchas VARCHAR(255);
ALTER TABLE livros ADD COLUMN IF NOT EXISTS estado_odor VARCHAR(255);
ALTER TABLE livros ADD COLUMN IF NOT EXISTS didatico_com_exercicios_respondidos BOOLEAN;
ALTER TABLE livros ADD COLUMN IF NOT EXISTS sobrecapa_ausente BOOLEAN;
ALTER TABLE livros ADD COLUMN IF NOT EXISTS nota_estado INTEGER;
