import { defineConfig } from "vite";
import handlebars from "vite-plugin-handlebars";

export default defineConfig({
  server: {
    port: 80,
    strictPort: true,
  },

  plugins: [
    handlebars({
      partialDirectory: "src/hbs",
    }),
  ],
});