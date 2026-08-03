import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Chạy ở cổng 3000 vì backend (OAuth2LoginSuccessHandler) đang cấu hình
// FRONTEND_URL_CALLBACK trỏ về http://localhost:3000/oauth2/callback
export default defineConfig({
  plugins: [react()],
  server: { port: 3000 },
});
