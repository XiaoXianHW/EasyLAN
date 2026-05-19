// Package frontend embeds the compiled Next.js static export so the
// platform ships as a single self-contained Go binary.
//
// Build pipeline (run from web/):
//
//	cd frontend && npm install && npm run build  # produces frontend/out
//	cp -R frontend/out internal/frontend/dist
//	cd .. && go build -o easylan-web ./
//
// At runtime Handler() returns an http.Handler that serves the embedded
// site, falling back to index.html for unknown routes (so client-side
// routes inside Next.js work).
package frontend

import (
	"embed"
	"errors"
	"io/fs"
	"net/http"
	"path"
	"strings"
)

//go:embed all:dist
var dist embed.FS

// Handler returns the embedded static site as an http.Handler.  When
// the dist directory is empty (eg. the developer hasn't built the
// frontend yet) the handler responds with a friendly placeholder.
func Handler() (http.Handler, error) {
	sub, err := fs.Sub(dist, "dist")
	if err != nil {
		return nil, err
	}
	if !hasIndex(sub) {
		return placeholder{}, nil
	}
	fileServer := http.FileServer(http.FS(sub))
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		upath := r.URL.Path
		if upath == "/" {
			fileServer.ServeHTTP(w, r)
			return
		}
		clean := strings.TrimPrefix(path.Clean(upath), "/")
		if _, err := fs.Stat(sub, clean); err == nil {
			fileServer.ServeHTTP(w, r)
			return
		}
		// Try to serve <path>.html for Next.js static export shape.
		if _, err := fs.Stat(sub, clean+".html"); err == nil {
			r2 := r.Clone(r.Context())
			r2.URL.Path = "/" + clean + ".html"
			fileServer.ServeHTTP(w, r2)
			return
		}
		// Fallback to index.html for client-side routing.
		r2 := r.Clone(r.Context())
		r2.URL.Path = "/index.html"
		fileServer.ServeHTTP(w, r2)
	}), nil
}

func hasIndex(fsys fs.FS) bool {
	_, err := fs.Stat(fsys, "index.html")
	return err == nil
}

type placeholder struct{}

func (placeholder) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	if strings.HasPrefix(r.URL.Path, "/api/") {
		http.NotFound(w, r)
		return
	}
	w.Header().Set("Content-Type", "text/html; charset=utf-8")
	w.WriteHeader(http.StatusOK)
	_, _ = w.Write([]byte(`<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8" />
<title>EasyLAN Web</title>
<style>
:root { color-scheme: dark; }
body{font-family:ui-sans-serif,system-ui,sans-serif;background:#1f2228;color:#fff;padding:48px;max-width:760px;margin:0 auto;line-height:1.5}
h1{font-family:ui-monospace,monospace;font-size:32px;letter-spacing:1px;margin-bottom:24px}
code{background:rgba(255,255,255,0.08);padding:2px 6px;border-radius:4px;font-family:ui-monospace,monospace}
pre{background:rgba(255,255,255,0.05);padding:16px;border-radius:8px;border:1px solid rgba(255,255,255,0.1);overflow-x:auto}
a{color:#fff;text-decoration:underline}
</style>
</head>
<body>
<h1>EASYLAN WEB</h1>
<p>The Go server is running but no compiled frontend was found at
<code>web/internal/frontend/dist/</code>.</p>
<p>Build the frontend first:</p>
<pre>cd web/frontend
npm install
npm run build
cp -R out ../internal/frontend/dist
cd .. &amp;&amp; go build -o easylan-web ./</pre>
<p>Or run the dev frontend separately:</p>
<pre>cd web/frontend &amp;&amp; npm run dev</pre>
<p>The API surface is reachable at <a href="/api/health">/api/health</a>.</p>
</body>
</html>`))
}

// ErrNoIndex is returned by Handler when the embedded site is empty.
var ErrNoIndex = errors.New("frontend: dist/index.html not found")
