package dev.m2g2.simao.enums;

import java.util.ArrayList;
import java.util.List;

public enum ChatType {
    MENU("@menu", "Exibir este menu", false),
    CANCEL("@cancel", "Cancelar interação em andamento", false),
    CREATE_TASK("@ctask", "Criar nova tarefa", true),
    LIST_TASKS("@ltask", "Listar tarefas", false),
    DELETE_TASK("@dtask", "Remover tarefa pelo ID", false),
    EXECUTE_TASK("@etask", "Marcar tarefa como concluída", false),
    CREATE_AUTOMATION("@cauto", "Criar nova automação", true),
    LIST_AUTOMATIONS("@lauto", "Listar automações", false),
    DELETE_AUTOMATION("@dauto", "Remover automação pelo ID", false),
    CREATE_NOTE("@cnote", "Criar nova nota", true),
    LIST_NOTES("@lnote", "Listar notas", false),
    DELETE_NOTE("@dnote", "Remover nota pelo ID", false),
    CREATE_PURCHASE("@cbuy", "Registrar nova compra", true),
    CREATE_PURCHASE_INLINE_TEMPLATE("@cbuy_inline_t", "Modelo para cadastrar várias compras", false),
    LIST_PURCHASES("@lbuy", "Listar compras", false),
    UPDATE_PURCHASE("@ubuy", "Atualizar compra pelo ID", true),
    DELETE_PURCHASE("@dbuy", "Remover compra pelo ID", false);

    private final String value;
    private final String description;
    private final Boolean requireInteraction;

    ChatType(String value, String description, Boolean requireInteraction) {
        this.value = value;
        this.description = description;
        this.requireInteraction = requireInteraction;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getRequireInteraction() {
        return requireInteraction;
    }

    public static String showMenuIf(String message) {
        if (!message.equalsIgnoreCase(ChatType.MENU.getValue()))
            return null;

        return """
                *Simão Bot*

                📋 *Tarefas*
                *@ctask* — Criar nova tarefa
                *@ltask* — Listar tarefas
                *@etask <id>* — Concluir tarefa
                *@dtask <id>* — Remover tarefa

                ⚙️ *Automações*
                *@cauto* — Criar nova automação
                *@lauto* — Listar automações
                *@dauto <id>* — Remover automação

                📝 *Notas*
                *@cnote* — Criar nova nota
                *@lnote* — Listar notas
                *@dnote <id>* — Remover nota

                🔧 *Geral*
                *@menu* — Exibir este menu
                *@cancel* — Cancelar interação em andamento
                """;
    }

    public static String showPurchaseMenuIf(String message) {
        if (!message.equalsIgnoreCase(ChatType.MENU.getValue()))
            return null;

        return """
                *Simão Bot — Compras 3D*

                🛒 *Compras de insumos*
                *@cbuy* — Registrar nova compra
                *@cbuy_inline_t* — Modelo p/ cadastrar várias de uma vez
                *@lbuy* — Listar compras
                *@ubuy <id>* — Atualizar compra
                *@dbuy <id>* — Remover compra

                🔧 *Geral*
                *@menu* — Exibir este menu
                *@cancel* — Cancelar interação em andamento
                """;
    }

    public static String showPurchaseInlineTemplateIf(String message) {
        if (!message.equalsIgnoreCase(ChatType.CREATE_PURCHASE_INLINE_TEMPLATE.getValue()))
            return null;

        return """
                Você pode cadastrar várias compras ao mesmo tempo, uma por linha, na estrutura:

                # <descrição> | <quantidade> | <preço unitário> | <fonte> | <data dd/mm/aaaa> | <observações>

                Exemplo:
                # Filamento PLA | 2 | 89,90 | Loja X | 04/08/2026 | cor preta
                # Resina | 1 | 120,00 | Loja Y | 05/08/2026 | -

                As observações são opcionais (use - ou omita o último campo).
                """;
    }

    public static List<ChatType> allByInteractionRequirement(boolean requireInteraction) {
        List<ChatType> chatTypes = new ArrayList<>();
        for (ChatType chatType : ChatType.values()) {
            if (chatType.getRequireInteraction() == requireInteraction) {
                chatTypes.add(chatType);
            }
        }
        return chatTypes;
    }
}
