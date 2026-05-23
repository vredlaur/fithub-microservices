FROM node:22-alpine AS build
WORKDIR /app
COPY frontend/fithub-client/package*.json ./
RUN npm ci
COPY frontend/fithub-client ./
RUN npm run build

FROM nginx:1.27-alpine
COPY frontend/fithub-client/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
