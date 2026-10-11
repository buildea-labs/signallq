import { defineConfig, globalIgnores } from "eslint/config";
import nextVitals from "eslint-config-next/core-web-vitals";
import nextTs from "eslint-config-next/typescript";

const eslintConfig = defineConfig([
  ...nextVitals,
  ...nextTs,
  // Override default ignores of eslint-config-next.
  globalIgnores([
    // Default ignores of eslint-config-next:
    ".next/**",
    "out/**",
    "build/**",
    "next-env.d.ts",
    // Worktrees git locais (ver `.gitignore` e o `exclude` do `tsconfig.json`):
    // são cópias de outra revisão do próprio repositório, com `.next` próprio.
    // Sem esta linha, qualquer worktree local quebra `npm run lint`.
    "worktree/**",
  ]),
  {
    // Preserva o comportamento atual sem exigir refatoração funcional.
    // Estas regras do React Compiler exigiriam refatorações funcionais que não
    // fazem parte da migração para um repositório autônomo.
    rules: {
      "@typescript-eslint/ban-ts-comment": "off",
      "@typescript-eslint/no-explicit-any": "off",
      "react-hooks/purity": "off",
      "react-hooks/set-state-in-effect": "off",
    },
  },
  {
    // Massas de teste nunca podem entrar no grafo de módulos da aplicação.
    // Foi exatamente assim que as fixtures de `src/test/fixtures/` vazaram
    // para `.next/static/chunks`: um harness sob `src/app/` as importava, e o
    // `notFound()` de runtime não impede o bundler de emitir o chunk. Código
    // de aplicação importa fixture => erro de lint, não descoberta no build.
    files: ["src/app/**/*.{ts,tsx}", "src/components/**/*.{ts,tsx}", "src/hooks/**/*.{ts,tsx}", "src/lib/**/*.{ts,tsx}"],
    ignores: ["**/*.test.{ts,tsx}", "**/*.integration.test.{ts,tsx}"],
    rules: {
      "no-restricted-imports": [
        "error",
        {
          patterns: [
            {
              group: ["@/test/*", "@/test/**", "**/test/fixtures/*", "../test/**", "../../test/**"],
              message:
                "Fixtures de teste não podem ser importadas por código de aplicação: elas seriam emitidas no bundle público. Use-as apenas em testes (vitest) ou em e2e-visual/.",
            },
          ],
        },
      ],
    },
  },
]);

export default eslintConfig;
