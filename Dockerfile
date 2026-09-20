FROM node:20-slim

WORKDIR /app

COPY server/package*.json ./
RUN npm install --production

COPY server/ ./

EXPOSE 3000
ENV NODE_ENV=production

CMD ["node", "src/index.js"]
