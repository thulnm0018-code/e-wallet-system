# Stage 1: Cài đặt dependency và build ứng dụng
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Stage 2: Cấu hình Nginx để serve ứng dụng và xử lý Routing
FROM nginx:stable-alpine

# Copy kết quả build tĩnh vào thư mục mặc định của Nginx
COPY --from=build /app/dist /usr/share/nginx/html

# Ghi đè cấu hình mặc định của Nginx để hỗ trợ React Router/Vite Router (Tránh lỗi 404 khi F5)
RUN echo 'server { \
    listen 80; \
    location / { \
        root /usr/share/nginx/html; \
        index index.html index.htm; \
        try_files $uri $uri/ /index.html; \
    } \
}' > /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]