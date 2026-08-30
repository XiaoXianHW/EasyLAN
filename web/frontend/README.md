# EasyLAN frontend

Next.js 14 (App Router, static export) source for the EasyLAN web
platform.  The build output is embedded into the Go binary at
`web/internal/frontend/dist`.

```bash
npm install
npm run dev          # http://localhost:3000  (talks to the Go API at :8080)
npm run build        # produces ./out
npm run export       # build + copy ./out → ../internal/frontend/dist
```

Pages:

* `/` — homepage with NAT detection
* `/rooms` — public room list
* `/rooms/view?code=XXXXX-XXXXX` — room detail / join
* `/download` — Forge / Fabric / NeoForge version matrix
* `/docs` — operator documentation

Styling uses Tailwind CSS with a small design-token layer at the top of
`src/app/globals.css`. Colour palette and typography draw on the Ollama
+ xAI references in
[VoltAgent/awesome-design-md](https://github.com/VoltAgent/awesome-design-md).
