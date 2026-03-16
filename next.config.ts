// ============================================================
// FICHIER  : next.config.ts
// RÔLE     : Configuration Next.js.
//            - Autorise les images PNG, JPG, SVG via next/image
//            - reactStrictMode activé
// ============================================================

import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  reactStrictMode: true,
  images: {
    // ✅ Autorise les SVG via next/image
    dangerouslyAllowSVG:    true,
    contentDispositionType: 'attachment',
    contentSecurityPolicy:  "default-src 'self'; script-src 'none'; sandbox;",
    // Formats modernes pour PNG/JPG
    formats: ['image/avif', 'image/webp'],
    // Ajouter des domaines distants ici si nécessaire :
    // remotePatterns: [{ protocol: 'https', hostname: 'cdn.minfi.cm' }],
  },
};

export default nextConfig;