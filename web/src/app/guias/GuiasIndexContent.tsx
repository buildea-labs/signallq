import Link from 'next/link'
import { GUIDES } from './guides-data'

export default function GuiasIndexContent() {
  return (
    <section className="mx-auto max-w-[768px] bg-white px-6 pb-[120px] pt-[88px] font-sans leading-[normal] text-[#1C1B1F]">
      <div className="mb-5 text-[14px] font-medium text-[#5B21D6]">Guias</div>
      <h1 className="m-0 mb-8 text-balance text-[clamp(36px,4.6vw,52px)] font-bold leading-[1.05] tracking-[-1.2px]">
        Entenda sua internet, sem jargão.
      </h1>
      {GUIDES.map((guide) => (
        <Link
          key={guide.slug}
          href={`/guias/${guide.slug}`}
          className="block border-t border-[#E7E0EC] py-7 text-[#1C1B1F] no-underline"
        >
          <div className="text-[24px] font-bold leading-[30px] tracking-[-0.3px]">{guide.title}</div>
          <div className="mt-2 max-w-[36em] text-[16px] leading-6 text-[#49454F]">{guide.summary}</div>
        </Link>
      ))}
    </section>
  )
}
