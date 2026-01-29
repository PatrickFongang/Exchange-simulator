const ICON_BASE = "https://cdn.jsdelivr.net/gh/atomiclabs/cryptocurrency-icons@1a63530be6e374711a8554f31b17e4cb92c25fa5/svg/color";

export const SUPPORTED_TOKENS = [
  { token: "btc", binanceSymbol: "btcusdt", label: "Bitcoin", displaySymbol: "BTC", icon: `${ICON_BASE}/btc.svg` },
  { token: "eth", binanceSymbol: "ethusdt", label: "Ethereum", displaySymbol: "ETH", icon: `${ICON_BASE}/eth.svg` },
  { token: "sol", binanceSymbol: "solusdt", label: "Solana", displaySymbol: "SOL", icon: `${ICON_BASE}/sol.svg` },
  { token: "xrp", binanceSymbol: "xrpusdt", label: "XRP", displaySymbol: "XRP", icon: `${ICON_BASE}/xrp.svg` },
  { token: "doge", binanceSymbol: "dogeusdt", label: "Dogecoin", displaySymbol: "DOGE", icon: `${ICON_BASE}/doge.svg` },
  { token: "ada", binanceSymbol: "adausdt", label: "Cardano", displaySymbol: "ADA", icon: `${ICON_BASE}/ada.svg` },
  { token: "bnb", binanceSymbol: "bnbusdt", label: "BNB", displaySymbol: "BNB", icon: `${ICON_BASE}/bnb.svg` },
  { token: "trx", binanceSymbol: "trxusdt", label: "TRON", displaySymbol: "TRX", icon: `${ICON_BASE}/trx.svg` },
  { token: "link", binanceSymbol: "linkusdt", label: "Chainlink", displaySymbol: "LINK", icon: `${ICON_BASE}/link.svg` },
  { token: "ltc", binanceSymbol: "ltcusdt", label: "Litecoin", displaySymbol: "LTC", icon: `${ICON_BASE}/ltc.svg` },
] as const;

export type SupportedToken = (typeof SUPPORTED_TOKENS)[number]["token"];

export const TOKEN_MAP = Object.fromEntries(
  SUPPORTED_TOKENS.map((t) => [t.token, t])
) as Record<SupportedToken, (typeof SUPPORTED_TOKENS)[number]>;
