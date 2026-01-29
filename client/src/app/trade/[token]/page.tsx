"use client";

import { use } from "react";
import { usePrices } from "@/providers/price-provider";
import { TOKEN_MAP, type SupportedToken } from "@/lib/constants";
import { PriceChart } from "@/components/trade/price-chart";
import { OrderForm } from "@/components/trade/order-form";
import { OrderBook } from "@/components/trade/order-book";
import { OrderHistory } from "@/components/trade/order-history";
import { Card, CardContent } from "@/components/ui/card";

function fmt(n: number, decimals = 2) {
  return n.toLocaleString(undefined, {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });
}

export default function TradePage({
  params,
}: {
  params: Promise<{ token: string }>;
}) {
  const { token } = use(params);
  const info = TOKEN_MAP[token as SupportedToken];
  const prices = usePrices();
  const priceData = prices[token];

  const priceColorClass =
    priceData?.current > priceData?.previous
      ? "text-green-500"
      : priceData?.current < priceData?.previous
        ? "text-red-500"
        : "";

  if (!info) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8">
        <h1 className="text-2xl font-bold">Unknown token: {token}</h1>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-7xl space-y-6 px-4 py-6">
      {/* Header */}
      <div className="flex items-center gap-4">
        <h1 className="text-2xl font-bold">
          {info.label} ({info.displaySymbol})
        </h1>
        <span className={`text-2xl font-bold tabular-nums transition-colors ${priceColorClass}`}>
          {priceData != null ? `$${fmt(priceData.current)}` : "..."}
        </span>
      </div>

      {/* Main grid: chart + sidebar */}
      <div className="grid gap-6 lg:grid-cols-[1fr_320px]">
        {/* Left column */}
        <div className="space-y-6">
          <Card>
            <CardContent className="p-2">
              <PriceChart token={token as SupportedToken} />
            </CardContent>
          </Card>
          <OrderHistory token={token as SupportedToken} />
        </div>

        {/* Right sidebar */}
        <div className="space-y-6">
          <OrderForm token={token as SupportedToken} />
          <OrderBook token={token as SupportedToken} />
        </div>
      </div>
    </div>
  );
}
