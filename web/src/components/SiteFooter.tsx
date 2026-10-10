import Image from 'next/image'
import Link from 'next/link'
import { PlayStoreBadge } from './PlayStoreBadge'

export function SiteFooter() {
  const links = [
    { label: "Política de Privacidade", href: "/privacidade" },
    { label: "Termos de Uso", href: "/termos" },
    { label: "Contato", href: "mailto:suporte@signallq.com" },
  ];

  return (
    <div className="relative z-[2] w-full box-border bg-transparent border-t border-[color-mix(in_srgb,_var(--border)_14%,_transparent)]">
      <div className="max-w-[1280px] mx-auto flex flex-wrap items-center justify-between gap-6 py-[28px] px-5 box-border">
        <div className="flex flex-col gap-3">
          <Image
            className="sq-logo-light object-contain block"
            src="/assets/signallq-lockup-light-bg-v5.png"
            alt=""
            aria-hidden="true"
            width={128}
            height={32}
          />
          <Image
            className="sq-logo-dark object-contain hidden"
            src="/assets/signallq-lockup-dark-bg-v5.png"
            alt=""
            aria-hidden="true"
            width={128}
            height={32}
          />
          <div className="flex flex-wrap gap-x-4 gap-y-1">
            {links.map((l, i) => (
              <Link key={i} href={l.href} className="font-normal text-[13px] leading-[1.4] text-[color:var(--text-primary)] no-underline hover:underline">
                {l.label}
              </Link>
            ))}
          </div>
          <div className="font-normal text-[12px] leading-[1.33] text-[color:var(--text-tertiary)]">
            © 2026 SignallQ · by Buildea.
          </div>
        </div>

        <PlayStoreBadge height={48} source="site_footer" />
      </div>
    </div>
  );
}
