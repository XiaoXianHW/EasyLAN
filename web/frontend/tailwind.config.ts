import type { Config } from "tailwindcss";

const config: Config = {
  content: ["./src/**/*.{ts,tsx,js,jsx,mdx}"],
  theme: {
    extend: {
      colors: {
        canvas: "#1f2228",
        ink: "#ffffff",
        hairline: "rgba(255,255,255,0.1)",
        "hairline-strong": "rgba(255,255,255,0.2)",
        "surface-elevated": "rgba(255,255,255,0.05)",
        "surface-hover": "rgba(255,255,255,0.08)",
        muted: "rgba(255,255,255,0.7)",
        subtle: "rgba(255,255,255,0.5)",
        faint: "rgba(255,255,255,0.3)",
        accent: "#10b981"
      },
      fontFamily: {
        mono: ["var(--font-mono)", "ui-monospace", "Menlo", "Monaco", "monospace"],
        sans: ["var(--font-sans)", "ui-sans-serif", "system-ui", "sans-serif"]
      },
      letterSpacing: {
        button: "0.1em",
        display: "0.02em"
      },
      borderRadius: {
        pill: "9999px"
      }
    }
  },
  plugins: []
};

export default config;
