import type { Config } from "tailwindcss";

export default {
  content: ["./index.html", "./src/**/*.{vue,ts,js}"],
  theme: {
    extend: {
      colors: {
        "brand-green": "#00AE42",
        "brand-green-dark": "#0B8E3B",
        "neutral-bg": "#F6F7F8",
        "neutral-border": "#E8EAEC",
        "text-main": "#171717",
        "text-sub": "#666D76",
        primary: "#00AE42",
        accent: "#00AE42",
        ink: "#171717",
      },
      boxShadow: {
        "header-soft": "0 1px 0 rgba(0,0,0,0.03), 0 8px 26px rgba(17,24,39,0.05)",
        "card-soft": "0 1px 2px rgba(15,23,42,0.05), 0 8px 26px rgba(15,23,42,0.08)",
        "card-soft-hover": "0 10px 36px rgba(15,23,42,0.12)",
      },
      borderRadius: {
        "4xl": "1.5rem",
      },
    },
  },
  plugins: [],
} satisfies Config;
