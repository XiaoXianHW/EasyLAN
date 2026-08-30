// Thin wrapper around the platform's REST API.  All endpoints live at
// /api/* and are served by the same Go binary that serves this frontend.

export type IPInfo = {
  ip: string;
  ipv6: boolean;
  country: string;
  region: string;
  province: string;
  city: string;
  isp: string;
  display: string;
  isLocal: boolean;
  stunServers: string[];
  serverTime: string;
};

export type NATResult = {
  type: string;
  clientIp: string;
  clientPort: number;
  serverObservedIp: string;
  serverObservedPort: number;
  isSymmetric: boolean;
  isIpv6: boolean;
  description: string;
  recommendation: string;
};

export type Room = {
  code: string;
  name: string;
  ownerName: string;
  loader: string;
  version: string;
  modes: string[];
  motd: string;
  maxPlayers: number;
  onlinePlayer: number;
  players: string[];
  region: string;
  natType: string;
  hostPort: number;
  hasPassword: boolean;
  isPublic: boolean;
  tags: string[];
  createdAt: string;
  updatedAt: string;
  expiresAt: string;
};

export type DownloadGroup = {
  loader: string;
  versions: {
    loader: string;
    version: string;
    branchGroup: string;
    curseforge: string;
    modrinth: string;
    notes?: string;
    tags?: string[];
  }[];
};

const API_BASE =
  typeof window === "undefined" ? "" : window.location.origin;

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(API_BASE + path, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(init?.headers || {})
    }
  });
  if (!res.ok) {
    let detail = res.statusText;
    try {
      const body = (await res.json()) as { message?: string };
      if (body && typeof body.message === "string") detail = body.message;
    } catch {
      /* ignore */
    }
    throw new Error(`${res.status} ${detail}`);
  }
  if (res.status === 204) return undefined as unknown as T;
  return (await res.json()) as T;
}

export const api = {
  ip: () => request<IPInfo>("/api/ip"),
  natProbe: (payload: Record<string, unknown>) =>
    request<NATResult>("/api/nat/probe", {
      method: "POST",
      body: JSON.stringify(payload)
    }),
  listRooms: (params: Record<string, string | number | boolean> = {}) => {
    const search = new URLSearchParams();
    for (const [k, v] of Object.entries(params)) {
      if (v === undefined || v === null || v === "") continue;
      search.set(k, String(v));
    }
    const qs = search.toString();
    return request<{ items: Room[]; total: number; limit: number; offset: number }>(
      `/api/rooms${qs ? `?${qs}` : ""}`
    );
  },
  getRoom: (code: string) => request<Room>(`/api/rooms/${code}`),
  joinRoom: (code: string, password?: string) =>
    request<Room>(`/api/rooms/${code}/join`, {
      method: "POST",
      body: JSON.stringify({ password: password ?? "" })
    }),
  createRoom: (payload: Record<string, unknown>) =>
    request<{ code: string; ownerToken: string; room: Room }>(`/api/rooms`, {
      method: "POST",
      body: JSON.stringify(payload)
    }),
  downloads: () => request<{ groups: DownloadGroup[] }>("/api/downloads")
};
