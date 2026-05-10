package api

import (
	"net/http"
	"sort"

	"github.com/xiaoxianhw/easylan/web/internal/version"
)

// LoaderEntry is a download-page row.
type LoaderEntry struct {
	Loader   string          `json:"loader"`
	Versions []version.Entry `json:"versions"`
}

// Downloads handles GET /api/downloads.
func (h *Handler) Downloads(w http.ResponseWriter, r *http.Request) {
	groups := []LoaderEntry{
		{Loader: "forge", Versions: version.ByLoader(version.LoaderForge)},
		{Loader: "fabric", Versions: version.ByLoader(version.LoaderFabric)},
		{Loader: "neoforge", Versions: version.ByLoader(version.LoaderNeoForge)},
	}
	for i := range groups {
		sort.SliceStable(groups[i].Versions, func(a, b int) bool {
			return semverGreater(groups[i].Versions[a].Version, groups[i].Versions[b].Version)
		})
	}
	writeJSON(w, http.StatusOK, map[string]any{
		"groups": groups,
	})
}

// semverGreater compares two MC version strings ("1.21.11" > "1.20.6")
// using a simple component-wise integer compare.  Returns true when a > b.
func semverGreater(a, b string) bool {
	pa := splitVer(a)
	pb := splitVer(b)
	for i := 0; i < len(pa) && i < len(pb); i++ {
		if pa[i] == pb[i] {
			continue
		}
		return pa[i] > pb[i]
	}
	return len(pa) > len(pb)
}

func splitVer(v string) []int {
	out := []int{}
	cur := 0
	hasDigit := false
	for i := 0; i < len(v); i++ {
		c := v[i]
		if c >= '0' && c <= '9' {
			cur = cur*10 + int(c-'0')
			hasDigit = true
			continue
		}
		if hasDigit {
			out = append(out, cur)
			cur = 0
			hasDigit = false
		}
	}
	if hasDigit {
		out = append(out, cur)
	}
	return out
}
