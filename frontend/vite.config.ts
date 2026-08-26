import { defineConfig } from "vite";
import handlebars from "vite-plugin-handlebars";
import { resolve } from "path";

export default defineConfig({
  appType: "mpa",

  server: {
    port: 8080,
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
        bespannungen: resolve(import.meta.dirname, "src/pages/bespannungen.html"),
        dashboard: resolve(import.meta.dirname, "src/pages/dashboard.html"),
        loginRegister: resolve(import.meta.dirname, "src/pages/loginRegister.html"),
        trainerstunden: resolve(import.meta.dirname, "src/pages/trainerstunden.html"),
        aboutme: resolve(import.meta.dirname, "src/pages/aboutme.html"),
        datenschutz: resolve(import.meta.dirname, "src/pages/datenschutz.html"),
        impressum: resolve(import.meta.dirname, "src/pages/impressum.html"),
        infos: resolve(import.meta.dirname, "src/pages/infos.html")
      },
    },
  },
});