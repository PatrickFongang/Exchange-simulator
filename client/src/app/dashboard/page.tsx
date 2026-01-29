"use client";

import Link from "next/link";
import { useAuth } from "@/providers/auth-provider";
import { useGetPortfolio } from "@/api/generated";
import type { SpotPositionResponseDto } from "@/api/generated";
import { usePrices } from "@/providers/price-provider";
import { SUPPORTED_TOKENS } from "@/lib/constants";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";

function fmt(n: number, decimals = 2) {
  return n.toLocaleString(undefined, {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });
}

function PortfolioTable() {
  const { data, isLoading } = useGetPortfolio();
  const prices = usePrices();

  const positions = (data as SpotPositionResponseDto[] | undefined) ?? [];

  if (isLoading) {
    return <p className="text-sm text-muted-foreground">Loading positions...</p>;
  }

  if (positions.length === 0) {
    return (
      <p className="text-sm text-muted-foreground">
        No positions yet. Start trading to build your portfolio.
      </p>
    );
  }

  return (
    <Table>
      <TableHeader>
        <TableRow>
          <TableHead>Token</TableHead>
          <TableHead className="text-right">Quantity</TableHead>
          <TableHead className="text-right">Avg Buy Price</TableHead>
          <TableHead className="text-right">Live Price</TableHead>
          <TableHead className="text-right">Value</TableHead>
          <TableHead className="text-right">P&L</TableHead>
          <TableHead></TableHead>
        </TableRow>
      </TableHeader>
      <TableBody>
        {positions.map((pos) => {
          const livePrice = pos.token ? prices[pos.token]?.current : undefined;
          const liveValue =
            livePrice != null && pos.quantity != null
              ? livePrice * pos.quantity
              : undefined;
          const costBasis =
            pos.avgBuyPrice != null && pos.quantity != null
              ? pos.avgBuyPrice * pos.quantity
              : undefined;
          const pnl =
            liveValue != null && costBasis != null
              ? liveValue - costBasis
              : undefined;

          return (
            <TableRow key={pos.positionId}>
              <TableCell className="font-medium">{pos.token?.toUpperCase()}</TableCell>
              <TableCell className="text-right">
                {pos.quantity != null ? fmt(pos.quantity, 6) : "—"}
              </TableCell>
              <TableCell className="text-right">
                {pos.avgBuyPrice != null ? `$${fmt(pos.avgBuyPrice)}` : "—"}
              </TableCell>
              <TableCell className="text-right">
                {livePrice != null ? `$${fmt(livePrice)}` : "..."}
              </TableCell>
              <TableCell className="text-right">
                {liveValue != null ? `$${fmt(liveValue)}` : "—"}
              </TableCell>
              <TableCell
                className={`text-right font-medium ${
                  pnl != null
                    ? pnl >= 0
                      ? "text-green-500"
                      : "text-red-500"
                    : ""
                }`}
              >
                {pnl != null
                  ? `${pnl >= 0 ? "+" : ""}$${fmt(pnl)}`
                  : "—"}
              </TableCell>
              <TableCell className="text-right">
                {pos.token && (
                  <Button variant="ghost" size="sm" asChild>
                    <Link href={`/trade/${pos.token}`}>Trade</Link>
                  </Button>
                )}
              </TableCell>
            </TableRow>
          );
        })}
      </TableBody>
    </Table>
  );
}

function MarketOverview() {
  const prices = usePrices();

  return (
    <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-5">
      {SUPPORTED_TOKENS.map((t) => (
        <Link key={t.token} href={`/trade/${t.token}`}>
          <Card className="transition-colors hover:bg-accent">
            <CardContent className="p-4">
              <div className="flex items-center gap-2">
                <img
                  src={t.icon}
                  alt={t.label}
                  className="h-8 w-8"
                  loading="lazy"
                />
                <div>
                  <p className="text-sm font-medium">{t.displaySymbol}</p>
                  <p className="text-xs text-muted-foreground">{t.label}</p>
                </div>
              </div>
              <p className="mt-2 text-lg font-bold tabular-nums">
                {prices[t.token] != null
                  ? `$${fmt(prices[t.token].current)}`
                  : "..."}
              </p>
            </CardContent>
          </Card>
        </Link>
      ))}
    </div>
  );
}

export default function DashboardPage() {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="mx-auto max-w-7xl px-4 py-8">
        <p className="text-muted-foreground">Loading...</p>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-7xl space-y-8 px-4 py-8">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Dashboard</h1>
          <p className="text-muted-foreground">
            Welcome back, {user?.username ?? "trader"}
          </p>
        </div>
        {user?.funds != null && (
          <Card>
            <CardContent className="p-4">
              <p className="text-sm text-muted-foreground">Available Funds</p>
              <p className="text-2xl font-bold">${fmt(user.funds)}</p>
            </CardContent>
          </Card>
        )}
      </div>

      <section>
        <h2 className="mb-4 text-lg font-semibold">Markets</h2>
        <MarketOverview />
      </section>

      <section>
        <h2 className="mb-4 text-lg font-semibold">Portfolio</h2>
        <Card>
          <CardContent className="p-0">
            <PortfolioTable />
          </CardContent>
        </Card>
      </section>
    </div>
  );
}
