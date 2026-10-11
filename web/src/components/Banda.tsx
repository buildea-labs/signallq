import type { ReactNode } from "react";

export function Banda({
  children,
  tint,
  className = "",
}: {
  children: ReactNode;
  tint?: "secondary";
  className?: string;
}) {
  return (
    <div className="w-full" style={tint === "secondary" ? { background: "var(--bg-secondary)" } : undefined}>
      <div className={`mx-auto w-full max-w-[1200px] px-5 lg:px-10 ${className}`}>
        {children}
      </div>
    </div>
  );
}
