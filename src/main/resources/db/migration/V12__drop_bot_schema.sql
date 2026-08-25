-- Remove o esquema do bot de WhatsApp: esta branch mantém apenas a loja e o
-- admin (catálogo, clientes, pedidos e compras). As migrations V1–V9 que
-- criaram estas tabelas continuam intactas — já foram aplicadas em produção e
-- reescrevê-las quebraria o checksum do Flyway.
--
-- O que fica: category, cost_parameters, product, product_photo, media,
-- store_config, customer, print_order, purchase.

-- tracker_entry referencia tracker, então cai primeiro.
DROP TABLE IF EXISTS tracker_entry;
DROP TABLE IF EXISTS tracker;

-- Máquina de estados das interações de chat.
DROP TABLE IF EXISTS chat_record;

-- Agenda e execução de automações do bot.
DROP TABLE IF EXISTS automation;

-- Tarefas e notas: uso pessoal, sem relação com a loja.
-- task_scheduler nunca teve migration de criação (a entidade foi adicionada
-- sem uma), então o IF EXISTS cobre tanto o banco que a tem quanto o que não.
DROP TABLE IF EXISTS task_scheduler;
DROP TABLE IF EXISTS task;
DROP TABLE IF EXISTS note;
