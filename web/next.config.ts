import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  turbopack: {},
  async redirects() {
    return [
      // Mantém um único host público indexável. O destino absoluto preserva
      // caminho e query string; o redirecionamento permanente é aplicado
      // somente ao host www, sem interferir nas demais rotas ou hosts.
      {
        source: '/:path*',
        has: [{ type: 'host', value: 'www.signallq.com' }],
        destination: 'https://signallq.com/:path*',
        permanent: true,
      },
    ];
  },
};

export default nextConfig;
