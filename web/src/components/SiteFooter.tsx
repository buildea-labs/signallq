import Image from "next/image";
import Link from "next/link";

const LINK = "text-[#49454F] no-underline";

export function SiteFooter() {
  return (
    <footer className="leading-[normal] border-t border-[#F3EEFA] px-6 py-8">
      <div className="mx-auto flex max-w-[1120px] flex-wrap items-center gap-x-8 gap-y-4 text-[14px] text-[#49454F]">
        <span className="flex items-center gap-2 font-bold text-[#1C1B1F]">
          <Image src="/assets/signallq-symbol-512.png" alt="" width={24} height={24} className="h-6 w-6 object-contain" />
          SignallQ
        </span>
        <Link href="/privacidade" className={LINK}>Privacidade</Link>
        <Link href="/termos" className={LINK}>Termos</Link>
        <a href="mailto:suporte@signallq.com" className={LINK}>suporte@signallq.com</a>
        <span className="ml-auto">© 2026 SignallQ · 7Agents Tecnologia</span>
      </div>
    </footer>
  );
}
