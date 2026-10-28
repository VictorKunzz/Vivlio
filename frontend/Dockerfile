# Ambiente de desenvolvimento do frontend.
#
# Node 22 porque o Vite 8 exige 20.19+ ou 22.12+. Mesma versao usada no CI.

FROM node:22-alpine

WORKDIR /app

# npm ci, nao npm install: respeita o lockfile e falha se ele estiver fora de
# sincronia com o package.json, que e justamente o erro que se quer pegar.
COPY package.json package-lock.json ./
RUN npm ci

COPY . .

EXPOSE 5173

# --host 0.0.0.0 e obrigatorio: sem isso o Vite escuta so em localhost dentro do
# container e a porta publicada nao responde no host.
CMD ["npm", "run", "dev", "--", "--host", "0.0.0.0"]
