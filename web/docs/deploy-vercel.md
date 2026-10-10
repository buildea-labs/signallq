# Deploy manual na Vercel

A CI é automática e valida cada `push` e pull request destinado a `main`; ela nunca publica o site. A publicação acontece somente pelo workflow **Deploy manual na Vercel**, iniciado em **Actions**. Não há integração Git nativa entre este repositório e a Vercel.

As referências públicas canônicas do aplicativo usam `https://signallq.com`: Metadata API, Open Graph, compartilhamento, `robots.txt` e sitemap. Mantenha esse domínio apontado para a produção antes de publicar uma versão que contenha essas referências; a alteração de DNS não publica código.

## Preparação inicial

Na raiz do repositório, execute os gates antes do primeiro deploy:

```powershell
npm ci
npm run lint
npm run typecheck
npm test
npm run build
```

Depois, autentique e vincule o projeto exclusivamente pela CLI:

```powershell
npx vercel login
npx vercel link
```

Use a conta pessoal gratuita do Luiz, crie ou selecione `signallq-web` na raiz deste repositório e confirme Next.js. Não conecte o projeto à integração Git da Vercel. A pasta local `.vercel/` continua ignorada e não deve ser versionada.

Faça primeiro um preview e valide a URL exibida; só então publique em produção:

```powershell
npx vercel deploy
npx vercel deploy --prod
```

Registre na issue #19 as URLs, o commit, o projeto/escopo vinculados e a confirmação no painel de que não há auto-deploy por Git. A autenticação, seleção da conta/projeto e eventual aprovação de produção são ações manuais do Luiz.

## Configuração do GitHub Actions

Após o `vercel link`, obtenha `VERCEL_ORG_ID` e `VERCEL_PROJECT_ID` na configuração local criada pela CLI e gere um `VERCEL_TOKEN` na Vercel. Cadastre os três valores como **Actions secrets** do repositório:

- `VERCEL_TOKEN`
- `VERCEL_ORG_ID`
- `VERCEL_PROJECT_ID`

Não copie seus valores para arquivos versionados, README, issue, pull request, logs ou artefatos. O workflow falha antes do deploy e informa qual secret falta quando essa configuração ainda não existir.

Opcionalmente, crie o GitHub Environment `production` e configure revisores obrigatórios. O job de produção já referencia esse Environment; sem proteção adicional, ele ainda é manual porque o único gatilho do workflow é `workflow_dispatch`.

## Publicar ou redeployar

Em **Actions**, abra **Deploy manual na Vercel** e clique em **Run workflow**. Escolha:

- `preview` para uma URL de validação;
- `production` apenas para publicação explícita;
- uma branch, tag ou SHA já validado em `ref`.

O workflow primeiro resolve a ref para um SHA imutável e o exibe antes de solicitar a aprovação do Environment de produção. Em seguida, faz checkout desse SHA, repete `npm ci`, lint, typecheck, testes e build, executa `vercel pull`, `vercel build` e publica o artefato pré-construído com a Vercel CLI 58.4.4. Em produção, somente então acrescenta `--prod`. A execução resume ambiente, ref, SHA efetivamente publicado, resultado e URL no GitHub Actions Summary.

Para redeployar uma versão anterior, informe o SHA daquela versão no campo `ref` e escolha o ambiente conscientemente. Não há rollback automático.

## Falhas comuns

- **Secret ausente:** cadastre os três secrets acima sem registrar valores em qualquer artefato público.
- **Falha nos gates:** corrija o commit indicado; o workflow não publica uma build reprovada.
- **Falha no `vercel pull` ou `build`:** confirme que os IDs pertencem ao projeto vinculado e que o token tem acesso a ele.
- **Deploy inesperado após push/merge:** investigue a configuração do projeto na Vercel; este repositório não tem gatilho automático de deploy e a integração Git deve permanecer desativada.
