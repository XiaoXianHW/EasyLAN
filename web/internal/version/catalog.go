// Package version exposes the curated Minecraft version catalogue used
// on the download page.
//
// Each entry is the canonical string we display on the site plus the
// CurseForge / Modrinth links the user can click to fetch the matching
// EasyLAN release.  The links point to the project page filtered by
// game-version in the canonical fashion supported by both sites.
package version

import "fmt"

// Loader names match the room.Loader values.
type Loader string

const (
	LoaderForge    Loader = "forge"
	LoaderFabric   Loader = "fabric"
	LoaderNeoForge Loader = "neoforge"
)

// Entry is a single supported MC version for a given loader.
type Entry struct {
	Loader      Loader   `json:"loader"`
	Version     string   `json:"version"`
	BranchGroup string   `json:"branchGroup"` // matches branch like forge/1.19.2-1.21.11
	CurseForge  string   `json:"curseforge"`
	Modrinth    string   `json:"modrinth"`
	Notes       string   `json:"notes,omitempty"`
	Tags        []string `json:"tags,omitempty"`
}

// curseforge / modrinth project slugs for EasyLAN.  Both pages
// auto-filter by the URL query parameter, so we just append it.
const (
	curseforgeBase = "https://www.curseforge.com/minecraft/mc-mods/easylan/files/all?gameVersionTypeId=1&filter-game-version="
	modrinthBase   = "https://modrinth.com/mod/easylan/versions?g="
)

func cf(loader, version string) string {
	return curseforgeBase + version + "&filter-loader=" + loader
}
func mr(loader, version string) string {
	return fmt.Sprintf("%s%s&l=%s", modrinthBase, version, loader)
}

// Catalogue is the curated list shown on the download page.
//
// The order matches the EasyLAN repository's branch layout
// (forge / fabric / neoforge → version groups).
var Catalogue = []Entry{
	// --- Forge -----------------------------------------------------------
	{LoaderForge, "1.7.2", "forge/1.7.2-1.11.2", cf("forge", "1.7.2"), mr("forge", "1.7.2"), "Legacy", []string{"legacy"}},
	{LoaderForge, "1.7.10", "forge/1.7.2-1.11.2", cf("forge", "1.7.10"), mr("forge", "1.7.10"), "", []string{"legacy"}},
	{LoaderForge, "1.8", "forge/1.7.2-1.11.2", cf("forge", "1.8"), mr("forge", "1.8"), "", []string{"legacy"}},
	{LoaderForge, "1.8.9", "forge/1.7.2-1.11.2", cf("forge", "1.8.9"), mr("forge", "1.8.9"), "", []string{"legacy"}},
	{LoaderForge, "1.9.4", "forge/1.7.2-1.11.2", cf("forge", "1.9.4"), mr("forge", "1.9.4"), "", []string{"legacy"}},
	{LoaderForge, "1.10.2", "forge/1.7.2-1.11.2", cf("forge", "1.10.2"), mr("forge", "1.10.2"), "", []string{"legacy"}},
	{LoaderForge, "1.11.2", "forge/1.7.2-1.11.2", cf("forge", "1.11.2"), mr("forge", "1.11.2"), "", []string{"legacy"}},

	{LoaderForge, "1.12.2", "forge/1.12.2", cf("forge", "1.12.2"), mr("forge", "1.12.2"), "Most popular legacy version", []string{"legacy", "popular"}},

	{LoaderForge, "1.13.2", "forge/1.13.2-1.15.2", cf("forge", "1.13.2"), mr("forge", "1.13.2"), "", nil},
	{LoaderForge, "1.14.4", "forge/1.13.2-1.15.2", cf("forge", "1.14.4"), mr("forge", "1.14.4"), "", nil},
	{LoaderForge, "1.15.2", "forge/1.13.2-1.15.2", cf("forge", "1.15.2"), mr("forge", "1.15.2"), "", nil},

	{LoaderForge, "1.16.4", "forge/1.16.4-1.18.2", cf("forge", "1.16.4"), mr("forge", "1.16.4"), "", nil},
	{LoaderForge, "1.16.5", "forge/1.16.4-1.18.2", cf("forge", "1.16.5"), mr("forge", "1.16.5"), "", []string{"popular"}},
	{LoaderForge, "1.17.1", "forge/1.16.4-1.18.2", cf("forge", "1.17.1"), mr("forge", "1.17.1"), "", nil},
	{LoaderForge, "1.18.2", "forge/1.16.4-1.18.2", cf("forge", "1.18.2"), mr("forge", "1.18.2"), "", []string{"popular"}},

	{LoaderForge, "1.19.2", "forge/1.19.2-1.21.11", cf("forge", "1.19.2"), mr("forge", "1.19.2"), "", []string{"popular"}},
	{LoaderForge, "1.19.4", "forge/1.19.2-1.21.11", cf("forge", "1.19.4"), mr("forge", "1.19.4"), "", nil},
	{LoaderForge, "1.20.1", "forge/1.19.2-1.21.11", cf("forge", "1.20.1"), mr("forge", "1.20.1"), "", []string{"popular"}},
	{LoaderForge, "1.20.6", "forge/1.19.2-1.21.11", cf("forge", "1.20.6"), mr("forge", "1.20.6"), "", nil},
	{LoaderForge, "1.21.1", "forge/1.19.2-1.21.11", cf("forge", "1.21.1"), mr("forge", "1.21.1"), "", []string{"popular"}},
	{LoaderForge, "1.21.5", "forge/1.19.2-1.21.11", cf("forge", "1.21.5"), mr("forge", "1.21.5"), "", nil},
	{LoaderForge, "1.21.11", "forge/1.19.2-1.21.11", cf("forge", "1.21.11"), mr("forge", "1.21.11"), "Latest", []string{"latest"}},

	// --- Fabric ----------------------------------------------------------
	{LoaderFabric, "1.14.4", "fabric/1.14.4-1.15.2", cf("fabric", "1.14.4"), mr("fabric", "1.14.4"), "", nil},
	{LoaderFabric, "1.15.2", "fabric/1.14.4-1.15.2", cf("fabric", "1.15.2"), mr("fabric", "1.15.2"), "", nil},

	{LoaderFabric, "1.16.4", "fabric/1.16.4-1.16.5", cf("fabric", "1.16.4"), mr("fabric", "1.16.4"), "", nil},
	{LoaderFabric, "1.16.5", "fabric/1.16.4-1.16.5", cf("fabric", "1.16.5"), mr("fabric", "1.16.5"), "", []string{"popular"}},

	{LoaderFabric, "1.17.1", "fabric/1.17.1-1.20.1", cf("fabric", "1.17.1"), mr("fabric", "1.17.1"), "", nil},
	{LoaderFabric, "1.18.2", "fabric/1.17.1-1.20.1", cf("fabric", "1.18.2"), mr("fabric", "1.18.2"), "", []string{"popular"}},
	{LoaderFabric, "1.19.2", "fabric/1.17.1-1.20.1", cf("fabric", "1.19.2"), mr("fabric", "1.19.2"), "", []string{"popular"}},
	{LoaderFabric, "1.19.4", "fabric/1.17.1-1.20.1", cf("fabric", "1.19.4"), mr("fabric", "1.19.4"), "", nil},
	{LoaderFabric, "1.20.1", "fabric/1.17.1-1.20.1", cf("fabric", "1.20.1"), mr("fabric", "1.20.1"), "", []string{"popular"}},

	{LoaderFabric, "1.20.6", "fabric/1.20.6-1.21.11", cf("fabric", "1.20.6"), mr("fabric", "1.20.6"), "", nil},
	{LoaderFabric, "1.21.1", "fabric/1.20.6-1.21.11", cf("fabric", "1.21.1"), mr("fabric", "1.21.1"), "", []string{"popular"}},
	{LoaderFabric, "1.21.5", "fabric/1.20.6-1.21.11", cf("fabric", "1.21.5"), mr("fabric", "1.21.5"), "", nil},
	{LoaderFabric, "1.21.11", "fabric/1.20.6-1.21.11", cf("fabric", "1.21.11"), mr("fabric", "1.21.11"), "Latest", []string{"latest"}},

	// --- NeoForge --------------------------------------------------------
	{LoaderNeoForge, "1.20.1", "neoforge/1.20.1-1.21.11", cf("neoforge", "1.20.1"), mr("neoforge", "1.20.1"), "", []string{"popular"}},
	{LoaderNeoForge, "1.20.6", "neoforge/1.20.1-1.21.11", cf("neoforge", "1.20.6"), mr("neoforge", "1.20.6"), "", nil},
	{LoaderNeoForge, "1.21.1", "neoforge/1.20.1-1.21.11", cf("neoforge", "1.21.1"), mr("neoforge", "1.21.1"), "", []string{"popular"}},
	{LoaderNeoForge, "1.21.5", "neoforge/1.20.1-1.21.11", cf("neoforge", "1.21.5"), mr("neoforge", "1.21.5"), "", nil},
	{LoaderNeoForge, "1.21.11", "neoforge/1.20.1-1.21.11", cf("neoforge", "1.21.11"), mr("neoforge", "1.21.11"), "Latest", []string{"latest"}},
}

// ByLoader groups Catalogue entries by loader.
func ByLoader(loader Loader) []Entry {
	out := make([]Entry, 0, len(Catalogue))
	for _, e := range Catalogue {
		if e.Loader == loader {
			out = append(out, e)
		}
	}
	return out
}
