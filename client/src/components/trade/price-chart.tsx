"use client";

import { useEffect, useRef, useCallback, useState } from "react";
import {
  createChart,
  ColorType,
  CandlestickSeries,
  type CandlestickData,
  type Time,
  type ISeriesApi,
  type IChartApi,
} from "lightweight-charts";
import { TOKEN_MAP, type SupportedToken } from "@/lib/constants";
import { usePrice } from "@/providers/price-provider";
import { Button } from "@/components/ui/button";

interface PriceChartProps {
  token: SupportedToken;
}

// Binance interval options with their duration in seconds
const INTERVALS = [
  { label: "1m", value: "1m", seconds: 60 },
  { label: "5m", value: "5m", seconds: 300 },
  { label: "15m", value: "15m", seconds: 900 },
  { label: "30m", value: "30m", seconds: 1800 },
  { label: "1h", value: "1h", seconds: 3600 },
  { label: "4h", value: "4h", seconds: 14400 },
] as const;

type IntervalValue = (typeof INTERVALS)[number]["value"];

async function fetchKlines(
  symbol: string,
  interval: IntervalValue,
  endTime?: number
): Promise<CandlestickData<Time>[]> {
  const params = new URLSearchParams({
    symbol: symbol.toUpperCase(),
    interval,
    limit: "500",
  });
  if (endTime) {
    params.set("endTime", String(endTime));
  }

  const res = await fetch(`https://api.binance.com/api/v3/klines?${params}`);
  const data: unknown[][] = await res.json();

  return data.map((k) => ({
    time: Math.floor((k[0] as number) / 1000) as Time,
    open: parseFloat(k[1] as string),
    high: parseFloat(k[2] as string),
    low: parseFloat(k[3] as string),
    close: parseFloat(k[4] as string),
  }));
}

export function PriceChart({ token }: PriceChartProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const chartRef = useRef<IChartApi | null>(null);
  const seriesRef = useRef<ISeriesApi<"Candlestick"> | null>(null);
  const candlesRef = useRef<CandlestickData<Time>[]>([]);
  const isLoadingRef = useRef(false);
  const oldestTimestampRef = useRef<number | null>(null);
  const intervalRef = useRef<IntervalValue>("1h");
  const liveCandleRef = useRef<CandlestickData<Time> | null>(null);
  const livePrice = usePrice(token);

  const [interval, setInterval] = useState<IntervalValue>("1h");

  // Keep ref in sync with state for use in callbacks
  useEffect(() => {
    intervalRef.current = interval;
  }, [interval]);

  const loadMoreHistory = useCallback(async () => {
    if (isLoadingRef.current || !oldestTimestampRef.current) return;

    const info = TOKEN_MAP[token];
    if (!info) return;

    isLoadingRef.current = true;

    try {
      // Fetch candles before our oldest timestamp
      const endTime = oldestTimestampRef.current * 1000 - 1;
      const olderCandles = await fetchKlines(info.binanceSymbol, intervalRef.current, endTime);

      if (olderCandles.length > 0 && seriesRef.current) {
        // Prepend older candles
        candlesRef.current = [...olderCandles, ...candlesRef.current];
        seriesRef.current.setData(candlesRef.current);
        oldestTimestampRef.current = olderCandles[0].time as number;
      }
    } finally {
      isLoadingRef.current = false;
    }
  }, [token]);

  // Create chart and load historical data
  useEffect(() => {
    if (!containerRef.current) return;

    const chart = createChart(containerRef.current, {
      layout: {
        background: { type: ColorType.Solid, color: "transparent" },
        textColor: "#a1a1aa",
      },
      grid: {
        vertLines: { color: "rgba(255,255,255,0.05)" },
        horzLines: { color: "rgba(255,255,255,0.05)" },
      },
      width: containerRef.current.clientWidth,
      height: 400,
      timeScale: {
        timeVisible: true,
        secondsVisible: false,
        barSpacing: 8,
        rightOffset: 5,
      },
      crosshair: {
        horzLine: { color: "rgba(255,255,255,0.2)" },
        vertLine: { color: "rgba(255,255,255,0.2)" },
      },
    });

    const series = chart.addSeries(CandlestickSeries, {
      upColor: "#22c55e",
      downColor: "#ef4444",
      borderDownColor: "#ef4444",
      borderUpColor: "#22c55e",
      wickDownColor: "#ef4444",
      wickUpColor: "#22c55e",
    });

    chartRef.current = chart;
    seriesRef.current = series;

    const info = TOKEN_MAP[token];
    if (info) {
      fetchKlines(info.binanceSymbol, interval).then((candles) => {
        candlesRef.current = candles;
        series.setData(candles);
        if (candles.length > 0) {
          oldestTimestampRef.current = candles[0].time as number;
        }
        // Scroll to most recent data, user can scroll left to see history
        chart.timeScale().scrollToRealTime();
      });
    }

    // Load more history when scrolling to the left edge
    const handleVisibleRangeChange = () => {
      const visibleRange = chart.timeScale().getVisibleLogicalRange();
      if (visibleRange && visibleRange.from < 10) {
        loadMoreHistory();
      }
    };

    chart.timeScale().subscribeVisibleLogicalRangeChange(handleVisibleRangeChange);

    const handleResize = () => {
      if (containerRef.current) {
        chart.applyOptions({ width: containerRef.current.clientWidth });
      }
    };
    window.addEventListener("resize", handleResize);

    return () => {
      window.removeEventListener("resize", handleResize);
      chart.timeScale().unsubscribeVisibleLogicalRangeChange(handleVisibleRangeChange);
      chart.remove();
      chartRef.current = null;
      seriesRef.current = null;
      candlesRef.current = [];
      oldestTimestampRef.current = null;
      liveCandleRef.current = null;
    };
  }, [token, interval, loadMoreHistory]);

  // Update current candle or create new one with live price
  useEffect(() => {
    if (!seriesRef.current || livePrice == null) return;

    const now = Math.floor(Date.now() / 1000);
    const intervalConfig = INTERVALS.find((i) => i.value === interval);
    const intervalSeconds = intervalConfig?.seconds ?? 3600;
    const timestamp = (now - (now % intervalSeconds)) as Time;

    const liveCandle = liveCandleRef.current;

    if (liveCandle && liveCandle.time === timestamp) {
      // Same interval period - update high/low/close
      liveCandle.high = Math.max(liveCandle.high, livePrice);
      liveCandle.low = Math.min(liveCandle.low, livePrice);
      liveCandle.close = livePrice;
      seriesRef.current.update({ ...liveCandle });
    } else {
      // New interval period - create a new candle
      const newCandle: CandlestickData<Time> = {
        time: timestamp,
        open: livePrice,
        high: livePrice,
        low: livePrice,
        close: livePrice,
      };
      liveCandleRef.current = newCandle;
      seriesRef.current.update(newCandle);
    }
  }, [livePrice, interval]);

  return (
    <div className="space-y-2">
      <div className="flex gap-1">
        {INTERVALS.map((int) => (
          <Button
            key={int.value}
            variant={interval === int.value ? "default" : "outline"}
            size="sm"
            className="text-xs px-2 py-1 h-7"
            onClick={() => setInterval(int.value)}
          >
            {int.label}
          </Button>
        ))}
      </div>
      <div ref={containerRef} className="w-full" />
    </div>
  );
}
