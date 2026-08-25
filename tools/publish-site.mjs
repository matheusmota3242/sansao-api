#!/usr/bin/env node
/**
 * Gera a loja estática da ARGILA LAB em site/, pronta para o Cloudflare Pages.
 *
 * O /api/catalog já devolve exatamente o formato que a vitrine lê (e só o que
 * ela precisa — nada de custo ou margem), então aqui só sobra trocar as URLs
 * das fotos por arquivos locais.
 *
 * O catálogo é EMBUTIDO no index.html e as fotos viram arquivos locais: a loja
 * publicada não faz uma única chamada à API, então continua no ar mesmo com o
 * servidor desligado, em manutenção ou quebrado. Esse era o ponto do plano.
 *
 * Precisa rodar de uma máquina que enxergue a API (tailnet, localhost), porque
 * é de lá que sai o catálogo. Por isso não dá para publicar pelo GitHub Actions
 * enquanto o servidor estiver atrás de NAT.
 *
 * Uso:
 *   node tools/publish-site.mjs [--api http://localhost:8080] [--out site]
 */

import { mkdir, writeFile, rm, readFile } from "node:fs/promises";
import { join, dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const raiz = join(dirname(fileURLToPath(import.meta.url)), "..");

function arg(nome, padrao) {
  const i = process.argv.indexOf("--" + nome);
  return i >= 0 && process.argv[i + 1] ? process.argv[i + 1] : padrao;
}

const API = (arg("api", process.env.API || "http://localhost:8080")).replace(/\/+$/, "");
const OUT = resolve(raiz, arg("out", "site"));
const ADMIN = join(raiz, "src/main/resources/static/argilalabapp.html");

const EXTENSOES = {
  "image/jpeg": "jpg", "image/jpg": "jpg", "image/png": "png",
  "image/webp": "webp", "image/gif": "gif", "image/avif": "avif",
  "image/svg+xml": "svg",
};

/** O template da loja mora dentro do admin, num <script type="text/template">. */
async function lerTemplate() {
  const html = await readFile(ADMIN, "utf8");
  const m = html.match(/<script[^>]*id="tplLoja"[^>]*>([\s\S]*?)<\/script>/);
  if (!m) throw new Error("Não achei o template tplLoja em " + ADMIN);
  // O admin escapa o fechamento para não encerrar o próprio <script>.
  return m[1].split("<\\/script>").join("</" + "script>");
}

async function buscarCatalogo() {
  const res = await fetch(API + "/api/catalog");
  if (!res.ok) throw new Error("GET /api/catalog respondeu " + res.status);
  return res.json();
}

/**
 * Baixa cada foto servida pela API e devolve o caminho local. Fotos que já são
 * URL externa ficam como estão — não são nossas para hospedar.
 */
async function baixarFotos(catalogo) {
  const mapa = new Map();
  let baixadas = 0;

  for (const produto of catalogo.produtos || []) {
    const fotos = produto.fotos || [];
    for (let i = 0; i < fotos.length; i++) {
      const url = fotos[i];
      if (!url || !url.startsWith("/api/media/")) continue;

      if (!mapa.has(url)) {
        const res = await fetch(API + url);
        if (!res.ok) {
          console.warn("  ! foto ausente (%s) em %s — pulando", res.status, produto.sku);
          mapa.set(url, null);
        } else {
          const tipo = (res.headers.get("content-type") || "image/jpeg").split(";")[0].trim();
          const hash = url.slice("/api/media/".length);
          const nome = hash + "." + (EXTENSOES[tipo] || "bin");
          await mkdir(join(OUT, "img"), { recursive: true });
          await writeFile(join(OUT, "img", nome), Buffer.from(await res.arrayBuffer()));
          mapa.set(url, "img/" + nome);
          baixadas++;
        }
      }
      fotos[i] = mapa.get(url);
    }
    // Uma foto que falhou vira null; tira do array para a loja não renderizar buraco.
    produto.fotos = fotos.filter(Boolean);
  }
  return baixadas;
}

async function main() {
  console.log("Lendo catálogo de %s ...", API);
  const catalogo = await buscarCatalogo();

  const publicados = (catalogo.produtos || []).length;
  if (!publicados) {
    console.error("\nNenhum produto publicado — a loja sairia vazia.");
    console.error("Marque produtos como 'publicado' no admin e rode de novo.");
    process.exit(1);
  }

  await rm(OUT, { recursive: true, force: true });
  await mkdir(OUT, { recursive: true });

  console.log("Baixando fotos ...");
  const fotos = await baixarFotos(catalogo);

  const template = await lerTemplate();
  const fonte = "const FONTE = null; var CAT = " + JSON.stringify(catalogo) + "; const PREVIA = false;";
  await writeFile(join(OUT, "index.html"), template.replace("/*__FONTE__*/", fonte));

  // As imagens são endereçadas por conteúdo (o nome É o hash), então nunca
  // mudam: pode cachear para sempre. O index muda a cada publicação.
  await writeFile(join(OUT, "_headers"),
    "/img/*\n  Cache-Control: public, max-age=31536000, immutable\n" +
    "/index.html\n  Cache-Control: public, max-age=0, must-revalidate\n");

  console.log("\nsite/ gerado: %d produto(s), %d foto(s).", publicados, fotos);
  console.log("\nPara publicar:");
  console.log("  npx wrangler pages deploy site --project-name=argilalab");
}

main().catch((e) => {
  console.error("\nFalhou:", e.message);
  process.exit(1);
});
