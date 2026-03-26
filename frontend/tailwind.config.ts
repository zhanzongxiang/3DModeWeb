import type { Config } from "tailwindcss";

export default {
  content: ["./index.html", "./src/**/*.{vue,ts,js}"],
  theme: {
    extend: {
      colors: {
        canvas: "#f4f5f7",
        brand: "#00AE42",
        "brand-dark": "#0b8e3b",
        "primary-text": "#1c1e21",
        muted: "#8a8d93",
        "active-pill": "#252525",
        "soft-border": "#eceef1",
        primary: "#00AE42",
        accent: "#00AE42",
        ink: "#1c1e21",
      },
      boxShadow: {
        soft: "0 1px 2px rgba(17, 24, 39, 0.06), 0 12px 30px rgba(17, 24, 39, 0.08)",
        "soft-hover": "0 8px 24px rgba(17, 24, 39, 0.13)",
        header: "0 1px 0 rgba(0,0,0,0.04)",
      },
      borderRadius: {
        card: "16px",
      },
    },
  },
  plugins: [],
} satisfies Config;
