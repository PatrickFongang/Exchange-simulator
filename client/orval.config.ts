import { defineConfig } from "orval";

export default defineConfig({
  api: {
    input: "./openapi.yml",
    output: {
      target: "./src/api/generated.ts",
      client: "react-query",
      mode: "single",
      override: {
        mutator: {
          path: "./src/api/axios-instance.ts",
          name: "customInstance",
        },
      },
    },
  },
});
