FROM node:22-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

ARG VITE_ENTRA_TENANT_ID
ARG VITE_ENTRA_SPA_CLIENT_ID
ARG VITE_ENTRA_API_SCOPE
ARG VITE_API_BASE_URL=/api

ENV VITE_ENTRA_TENANT_ID=${VITE_ENTRA_TENANT_ID}
ENV VITE_ENTRA_SPA_CLIENT_ID=${VITE_ENTRA_SPA_CLIENT_ID}
ENV VITE_ENTRA_API_SCOPE=${VITE_ENTRA_API_SCOPE}
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}

RUN npm run build

FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html
COPY default.conf.template /etc/nginx/templates/default.conf.template

ENV API_UPSTREAM=http://api-gateway:8080

EXPOSE 80