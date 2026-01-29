"use client";

import {
  createContext,
  useContext,
  useEffect,
  useRef,
  useState,
  useCallback,
} from "react";
import { SUPPORTED_TOKENS } from "@/lib/constants";

type Prices = Record<string, { current: number; previous: number }>;

const PriceContext = createContext<Prices>({});

export function usePrices() {
  return useContext(PriceContext);
}

export function usePrice(token: string): number | undefined {
  const prices = usePrices();
  return prices[token]?.current;
}

export function PriceProvider({ children }: { children: React.ReactNode }) {
  const [prices, setPrices] = useState<Prices>({});
  const [prevPrices, setPrevPrices] = useState<Prices>({});
  const wsRef = useRef<WebSocket | null>(null);

  const connect = useCallback(() => {
    const streams = SUPPORTED_TOKENS.map(
      (t) => `${t.binanceSymbol}@trade`
    ).join("/");
    const ws = new WebSocket(
      `wss://stream.binance.com:9443/stream?streams=${streams}`
    );

    ws.onmessage = (event) => {
      const message = JSON.parse(event.data);
      if (message.data?.s && message.data?.p) {
        const symbol = (message.data.s as string).toLowerCase();
        const price = parseFloat(message.data.p);
        const token = SUPPORTED_TOKENS.find(
          (t) => t.binanceSymbol === symbol
        )?.token;
        if (token) {
          setPrices((prev) => ({ ...prev, 
            [token]: { 
              current: price, 
              previous: prev[token]?.current ?? price } 
            }));
        }
      }
    };

    ws.onclose = () => {
      setTimeout(connect, 3000);
    };

    wsRef.current = ws;
  }, []);

  useEffect(() => {
    connect();
    return () => {
      wsRef.current?.close();
    };
  }, [connect]);

  return (
    <PriceContext.Provider value={prices}>{children}</PriceContext.Provider>
  );
}
