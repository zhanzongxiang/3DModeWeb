const WEBP_QUERY = (import.meta.env.VITE_WEBP_QUERY as string | undefined)?.trim() ?? "";

type OptimizeOptions = {
  appendWebpQuery?: boolean;
};

export function optimizeImageUrl(rawUrl: string, options: OptimizeOptions = {}) {
  const url = rawUrl.trim();
  if (!url) {
    return "";
  }
  if (!options.appendWebpQuery || !WEBP_QUERY) {
    return url;
  }
  if (url.includes(WEBP_QUERY)) {
    return url;
  }

  const hashIndex = url.indexOf("#");
  const hashPart = hashIndex >= 0 ? url.slice(hashIndex) : "";
  const pureUrl = hashIndex >= 0 ? url.slice(0, hashIndex) : url;
  const joiner = pureUrl.includes("?") ? "&" : "?";
  return `${pureUrl}${joiner}${WEBP_QUERY}${hashPart}`;
}
