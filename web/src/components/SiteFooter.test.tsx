import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it } from "vitest";
import { SiteFooter } from "./SiteFooter";

describe("SiteFooter — landing pública do app Android", () => {
  afterEach(() => cleanup());

  it("expõe Política de Privacidade, Termos de Uso, Contato e o botão de download", () => {
    render(<SiteFooter />);
    expect(screen.getByRole("link", { name: "Política de Privacidade" })).toHaveAttribute("href", "/privacidade");
    expect(screen.getByRole("link", { name: "Termos de Uso" })).toHaveAttribute("href", "/termos");
    expect(screen.getByRole("link", { name: "Contato" })).toHaveAttribute("href", "mailto:suporte@signallq.com");
    expect(screen.getByRole("button", { name: /Baixar/i })).toBeInTheDocument();
  });
});
