FROM node:24-alpine
WORKDIR /workspace

# Instala dependências do sistema para um melhor console interativo
RUN apk add --no-cache tree

# Instala o CLI do Gemini globalmente
RUN npm install -g @google/gemini-cli

# Usa o shell padrão do Alpine
CMD ["sh"]