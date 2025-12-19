# Build the Angular/Vite client
FROM node:24-bookworm-slim AS builder
WORKDIR /app

COPY src/main/webapp/package*.json ./
RUN npm ci

COPY src/main/webapp .

ENV NODE_ENV=production

RUN npm run build

# Serve the production build with Caddy
FROM caddy:2-alpine AS runner

COPY --from=builder /app/dist /srv
COPY docker/docker-entrypoint.sh /docker-entrypoint.sh

RUN chmod +x /docker-entrypoint.sh && \
    echo ':80 { root * /srv; file_server; try_files {path} /index.html; encode gzip }' > /etc/caddy/Caddyfile

EXPOSE 80

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["caddy", "run", "--config", "/etc/caddy/Caddyfile"]
