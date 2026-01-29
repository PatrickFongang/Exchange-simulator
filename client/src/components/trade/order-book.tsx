"use client";

import { useBuyOrderBook, useSellOrderBook } from "@/api/generated";
import type { OrderResponseDto } from "@/api/generated";
import type { SupportedToken } from "@/lib/constants";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

interface OrderBookProps {
  token: SupportedToken;
}

function fmt(n: number, decimals = 2) {
  return n.toLocaleString(undefined, {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });
}

function BookSide({
  orders,
  side,
  isLoading,
}: {
  orders: OrderResponseDto[];
  side: "buy" | "sell";
  isLoading: boolean;
}) {
  if (isLoading) {
    return <p className="p-2 text-xs text-muted-foreground">Loading...</p>;
  }

  if (orders.length === 0) {
    return <p className="p-2 text-xs text-muted-foreground">No {side} orders</p>;
  }

  return (
    <div className="space-y-0.5">
      {orders.slice(0, 10).map((order, i) => (
        <div
          key={order.orderId ?? i}
          className="flex items-center justify-between px-2 py-0.5 text-xs"
        >
          <span
            className={
              side === "buy" ? "text-green-500" : "text-red-500"
            }
          >
            ${order.entry != null ? fmt(order.entry) : "—"}
          </span>
          <span className="text-muted-foreground">
            {order.quantity != null ? fmt(order.quantity, 6) : "—"}
          </span>
          <span className="text-muted-foreground">
            {order.orderValue != null ? `$${fmt(order.orderValue)}` : "—"}
          </span>
        </div>
      ))}
    </div>
  );
}

export function OrderBook({ token }: OrderBookProps) {
  const { data: buyData, isLoading: buyLoading } = useBuyOrderBook(token, {
    query: { refetchInterval: 5000 },
  });
  const { data: sellData, isLoading: sellLoading } = useSellOrderBook(token, {
    query: { refetchInterval: 5000 },
  });

  const buyOrders = (buyData as OrderResponseDto[] | undefined) ?? [];
  const sellOrders = (sellData as OrderResponseDto[] | undefined) ?? [];

  return (
    <Card>
      <CardHeader className="pb-2">
        <CardTitle className="text-base">Order Book</CardTitle>
      </CardHeader>
      <CardContent className="space-y-2 p-2">
        {/* Column headers */}
        <div className="flex items-center justify-between px-2 text-xs font-medium text-muted-foreground">
          <span>Price</span>
          <span>Qty</span>
          <span>Total</span>
        </div>

        {/* Sell side (asks) — reversed so lowest ask is at bottom */}
        <div>
          <p className="px-2 text-xs font-medium text-red-500">Asks</p>
          <BookSide
            orders={[...sellOrders].reverse()}
            side="sell"
            isLoading={sellLoading}
          />
        </div>

        <div className="border-t border-border" />

        {/* Buy side (bids) */}
        <div>
          <p className="px-2 text-xs font-medium text-green-500">Bids</p>
          <BookSide orders={buyOrders} side="buy" isLoading={buyLoading} />
        </div>
      </CardContent>
    </Card>
  );
}
