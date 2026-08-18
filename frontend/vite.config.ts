import { defineConfig } from "vite";
import handlebars from "vite-plugin-handlebars";
import { resolve } from "path";

export default defineConfig({
  appType: "mpa",

  server: {
    port: 80,
    strictPort: true,
  },

  plugins: [
    handlebars({
      partialDirectory: "src/hbs",
    }),
  ],

  build: {
    rollupOptions: {
      input: {
        index: resolve(import.meta.dirname, "index.html"),
        main: resolve(import.meta.dirname, "src/pages/main.html"),
      },
    },
  },
});