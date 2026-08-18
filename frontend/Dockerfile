# Build stage
FROM node:22-alpine AS builder

WORKDIR /app

# Install dependencies
COPY package.json package-lock.json ./
RUN npm ci

# Copy source
COPY . .

# Build Vite application
RUN npm run build


# Production stage
FROM nginx:alpine

# Copy Vite's production output to nginx
COPY --from=builder /app/dist /usr/share/nginx/html

# nginx listens on port 80
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]