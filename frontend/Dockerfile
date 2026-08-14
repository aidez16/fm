# Multi-stage build: compile the Angular app with Node, serve the static
# output with nginx (which also reverse-proxies /api to the backend service).
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm install --legacy-peer-deps
COPY . .
RUN npm run build:prod

FROM nginx:1.27-alpine
COPY --from=build /app/dist/future-movement-summary-ui/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
