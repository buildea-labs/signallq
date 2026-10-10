import os
import re

files_to_process = [
    {
        "client": "src/app/dns/DnsClient.tsx",
        "page": "src/app/dns/page.tsx",
        "route": "/dns",
        "func": "DnsClient"
    },
    {
        "client": "src/app/historico/HistoricoClient.tsx",
        "page": "src/app/historico/page.tsx",
        "route": "/historico",
        "func": "HistoricoClient"
    },
    {
        "client": "src/app/jogos/JogosClient.tsx",
        "page": "src/app/jogos/page.tsx",
        "route": "/jogos",
        "func": "JogosClient"
    },
    {
        "client": "src/app/NotFoundClient.tsx",
        "page": "src/app/not-found.tsx",
        "route": "/404", # WAIT, not-found is special
        "func": "NotFoundClient",
        "not_found": True
    },
    {
        "client": "src/app/internet-para-jogos/InternetParaJogosClient.tsx",
        "page": "src/app/internet-para-jogos/page.tsx",
        "route": "/internet-para-jogos",
        "func": "InternetParaJogosClient"
    },
    {
        "client": "src/app/lag-em-jogos-online/LagEmJogosOnlineClient.tsx",
        "page": "src/app/lag-em-jogos-online/page.tsx",
        "route": "/lag-em-jogos-online",
        "func": "LagEmJogosOnlineClient"
    },
    {
        "client": "src/app/internet-boa-mas-travando/InternetBoaMasTravandoClient.tsx",
        "page": "src/app/internet-boa-mas-travando/page.tsx",
        "route": "/internet-boa-mas-travando",
        "func": "InternetBoaMasTravandoClient"
    }
]

for item in files_to_process:
    with open(item["client"], "r", encoding="utf-8") as f:
        content = f.read()
    
    # Remove useDocumentMeta import
    content = re.sub(r'import\s+{\s*useDocumentMeta\s*}\s+from\s+[\'"].*?useDocumentMeta[\'"]\s*\n?', '', content)
    # Remove PAGE_META or NOT_FOUND_META import if it's the only thing imported, but simpler:
    # Actually, we can just remove the specific call.
    content = re.sub(r'\s*useDocumentMeta\([^)]+\);?', '', content)
    
    # Replace export default function Something() with export function Func()
    content = re.sub(r'export\s+default\s+function\s+[A-Za-z0-9_]+\s*\(', f'export function {item["func"]}(', content)
    
    with open(item["client"], "w", encoding="utf-8") as f:
        f.write(content)
        
    # Write page.tsx
    if item.get("not_found"):
        page_content = f"""import type {{ Metadata }} from 'next'
import {{ NOT_FOUND_META }} from "@/lib/pageMetaCatalog"
import {{ routeMetadata }} from "@/lib/routeMetadata"
import {{ NotFoundClient }} from "./NotFoundClient"

export const metadata: Metadata = routeMetadata(NOT_FOUND_META)

export default function NotFound() {{
  return <NotFoundClient />
}}
"""
    else:
        page_content = f"""import type {{ Metadata }} from 'next'
import {{ PAGE_META }} from "@/lib/pageMetaCatalog"
import {{ routeMetadata }} from "@/lib/routeMetadata"
import {{ {item["func"]} }} from "./{os.path.basename(item["client"]).replace('.tsx', '')}"

export const metadata: Metadata = routeMetadata(PAGE_META["{item['route']}"])

export default function Page() {{
  return <{item["func"]} />
}}
"""
    with open(item["page"], "w", encoding="utf-8") as f:
        f.write(page_content)

print("Done")
