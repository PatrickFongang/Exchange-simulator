"use client";

import { useState } from "react";
import Link from "next/link";
import { useAuth } from "@/providers/auth-provider";
import {
  useGetPortfolio,
  useGetUserOrders,
  useCancelOrder,
  getGetUserOrdersQueryKey,
  getGetPortfolioQueryKey,
} from "@/api/generated";
import type {
  SpotPositionResponseDto,
  OrderResponseDto,
} from "@/api/generated";
import { usePrices } from "@/providers/price-provider";
import { TOKEN_MAP, SUPPORTED_TOKENS } from "@/lib/constants";
import { useQueryClient } from "@tanstack/react-query";
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
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";

function fmt(n: number, decimals = 2) {
  return n.toLocaleString(undefined, {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });
}

function formatDate(dateString?: string) {
  if (!dateString) return "—";
  return new Date(dateString).toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

function UserInfoSection() {
  const { user } = useAuth();

  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <Card>
        <CardHeader className="pb-2">
          <CardDescription>Username</CardDescription>
        </CardHeader>
        <CardContent>
          <p className="text-2xl font-bold">{user?.username ?? "—"}</p>
        </CardContent>
      </Card>
      <Card>
        <CardHeader className="pb-2">
          <CardDescription>Email</CardDescription>
        </CardHeader>
        <CardContent>
          <p className="text-lg font-medium truncate">{user?.email ?? "—"}</p>
        </CardContent>
      </Card>
      <Card>
        <CardHeader className="pb-2">
          <CardDescription>Available Funds</CardDescription>
        </CardHeader>
        <CardContent>
          <p className="text-2xl font-bold tabular-nums text-green-500">
            ${user?.funds != null ? fmt(user.funds) : "—"}
          </p>
        </CardContent>
      </Card>
      <Card>
        <CardHeader className="pb-2">
          <CardDescription>Member Since</CardDescription>
        </CardHeader>
        <CardContent>
          <p className="text-lg font-medium">
            {user?.createdAt
              ? new Date(user.createdAt).toLocaleDateString(undefined, {
                  year: "numeric",
                  month: "long",
                  day: "numeric",
                })
              : "—"}
          </p>
        </CardContent>
      </Card>
    </div>
  );
}

function PortfolioSection() {
  const { data, isLoading } = useGetPortfolio();
  const prices = usePrices();

  const positions = (data as SpotPositionResponseDto[] | undefined) ?? [];

  let totalValue = 0;
  let totalPnL = 0;

  positions.forEach((pos) => {
    const livePrice = pos.token ? prices[pos.token]?.current : undefined;
    if (livePrice != null && pos.quantity != null) {
      const value = livePrice * pos.quantity;
      totalValue += value;
      if (pos.avgBuyPrice != null) {
        totalPnL += value - pos.avgBuyPrice * pos.quantity;
      }
    }
  });

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Portfolio</CardTitle>
          <CardDescription>Your current holdings</CardDescription>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-muted-foreground">Loading positions...</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>Portfolio</CardTitle>
            <CardDescription>Your current holdings</CardDescription>
          </div>
          <div className="text-right">
            <p className="text-sm text-muted-foreground">Total Value</p>
            <p className="text-2xl font-bold tabular-nums">${fmt(totalValue)}</p>
            <p
              className={`text-sm font-medium tabular-nums ${
                totalPnL >= 0 ? "text-green-500" : "text-red-500"
              }`}
            >
              {totalPnL >= 0 ? "+" : ""}${fmt(totalPnL)} P&L
            </p>
          </div>
        </div>
      </CardHeader>
      <CardContent>
        {positions.length === 0 ? (
          <p className="text-sm text-muted-foreground">
            No positions yet. Start trading to build your portfolio.
          </p>
        ) : (
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
                const tokenInfo = pos.token
                  ? TOKEN_MAP[pos.token as keyof typeof TOKEN_MAP]
                  : undefined;
                const livePrice = pos.token
                  ? prices[pos.token]?.current
                  : undefined;
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
                    <TableCell>
                      <div className="flex items-center gap-2">
                        {tokenInfo && (
                          <img
                            src={tokenInfo.icon}
                            alt={tokenInfo.label}
                            className="h-6 w-6"
                            loading="lazy"
                          />
                        )}
                        <span className="font-medium">
                          {pos.token?.toUpperCase()}
                        </span>
                      </div>
                    </TableCell>
                    <TableCell className="text-right tabular-nums">
                      {pos.quantity != null ? fmt(pos.quantity, 6) : "—"}
                    </TableCell>
                    <TableCell className="text-right tabular-nums">
                      {pos.avgBuyPrice != null
                        ? `$${fmt(pos.avgBuyPrice)}`
                        : "—"}
                    </TableCell>
                    <TableCell className="text-right tabular-nums">
                      {livePrice != null ? `$${fmt(livePrice)}` : "..."}
                    </TableCell>
                    <TableCell className="text-right tabular-nums">
                      {liveValue != null ? `$${fmt(liveValue)}` : "—"}
                    </TableCell>
                    <TableCell
                      className={`text-right font-medium tabular-nums ${
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
        )}
      </CardContent>
    </Card>
  );
}

function OrdersSection() {
  const { data, isLoading } = useGetUserOrders();
  const queryClient = useQueryClient();
  const cancelMutation = useCancelOrder();

  const [filter, setFilter] = useState<"all" | "open" | "filled">("all");

  const orders = (data as OrderResponseDto[] | undefined) ?? [];

  const filteredOrders = orders.filter((order) => {
    if (filter === "open") return !order.closedAt;
    if (filter === "filled") return !!order.closedAt;
    return true;
  });

  const sortedOrders = [...filteredOrders].sort((a, b) => {
    const dateA = new Date(a.createdAt ?? 0).getTime();
    const dateB = new Date(b.createdAt ?? 0).getTime();
    return dateB - dateA;
  });

  const handleCancel = (orderId: number) => {
    cancelMutation.mutate(
      { orderId },
      {
        onSuccess: () => {
          queryClient.invalidateQueries({
            queryKey: getGetUserOrdersQueryKey(),
          });
          queryClient.invalidateQueries({
            queryKey: getGetPortfolioQueryKey(),
          });
        },
      }
    );
  };

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>Order History</CardTitle>
          <CardDescription>Your trading activity</CardDescription>
        </CardHeader>
        <CardContent>
          <p className="text-sm text-muted-foreground">Loading orders...</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>Order History</CardTitle>
            <CardDescription>Your trading activity</CardDescription>
          </div>
          <Tabs
            value={filter}
            onValueChange={(v) => setFilter(v as typeof filter)}
          >
            <TabsList>
              <TabsTrigger value="all">All ({orders.length})</TabsTrigger>
              <TabsTrigger value="open">
                Open ({orders.filter((o) => !o.closedAt).length})
              </TabsTrigger>
              <TabsTrigger value="filled">
                Filled ({orders.filter((o) => !!o.closedAt).length})
              </TabsTrigger>
            </TabsList>
          </Tabs>
        </div>
      </CardHeader>
      <CardContent>
        {sortedOrders.length === 0 ? (
          <p className="text-sm text-muted-foreground">
            No orders found. Start trading to see your order history.
          </p>
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Date</TableHead>
                <TableHead>Token</TableHead>
                <TableHead>Type</TableHead>
                <TableHead>Side</TableHead>
                <TableHead className="text-right">Quantity</TableHead>
                <TableHead className="text-right">Price</TableHead>
                <TableHead className="text-right">Value</TableHead>
                <TableHead>Status</TableHead>
                <TableHead></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {sortedOrders.map((order) => {
                const tokenInfo = order.token
                  ? TOKEN_MAP[order.token as keyof typeof TOKEN_MAP]
                  : undefined;
                const isOpen = !order.closedAt;
                const isLimitOrder = order.orderType === "LIMIT";

                return (
                  <TableRow key={order.orderId}>
                    <TableCell className="text-sm">
                      {formatDate(order.createdAt)}
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-2">
                        {tokenInfo && (
                          <img
                            src={tokenInfo.icon}
                            alt={tokenInfo.label}
                            className="h-5 w-5"
                            loading="lazy"
                          />
                        )}
                        <span className="font-medium">
                          {order.token?.toUpperCase()}
                        </span>
                      </div>
                    </TableCell>
                    <TableCell>
                      <Badge variant="outline">{order.orderType}</Badge>
                    </TableCell>
                    <TableCell>
                      <Badge
                        variant={
                          order.transactionType === "BUY"
                            ? "default"
                            : "secondary"
                        }
                        className={
                          order.transactionType === "BUY"
                            ? "bg-green-500/20 text-green-500 hover:bg-green-500/30"
                            : "bg-red-500/20 text-red-500 hover:bg-red-500/30"
                        }
                      >
                        {order.transactionType}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right tabular-nums">
                      {order.quantity != null ? fmt(order.quantity, 6) : "—"}
                    </TableCell>
                    <TableCell className="text-right tabular-nums">
                      {order.entry != null ? `$${fmt(order.entry)}` : "—"}
                    </TableCell>
                    <TableCell className="text-right tabular-nums">
                      {order.orderValue != null
                        ? `$${fmt(order.orderValue)}`
                        : "—"}
                    </TableCell>
                    <TableCell>
                      {isOpen ? (
                        <Badge variant="outline" className="text-yellow-500 border-yellow-500">
                          Open
                        </Badge>
                      ) : (
                        <Badge variant="outline" className="text-green-500 border-green-500">
                          Filled
                        </Badge>
                      )}
                    </TableCell>
                    <TableCell>
                      {isOpen && isLimitOrder && (
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() =>
                            order.orderId && handleCancel(order.orderId)
                          }
                          disabled={cancelMutation.isPending}
                        >
                          Cancel
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        )}
      </CardContent>
    </Card>
  );
}

function AccountStats() {
  const { data: ordersData, isLoading: ordersLoading } = useGetUserOrders();
  const { data: portfolioData, isLoading: portfolioLoading } = useGetPortfolio();

  const orders = (ordersData as OrderResponseDto[] | undefined) ?? [];
  const positions = (portfolioData as SpotPositionResponseDto[] | undefined) ?? [];

  const totalTrades = orders.filter((o) => !!o.closedAt).length;
  const buyOrders = orders.filter((o) => o.transactionType === "BUY").length;
  const sellOrders = orders.filter((o) => o.transactionType === "SELL").length;
  const openOrders = orders.filter((o) => !o.closedAt).length;
  const uniqueTokens = new Set(positions.map((p) => p.token)).size;

  if (ordersLoading || portfolioLoading) {
    return null;
  }

  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
      <Card>
        <CardHeader className="pb-2">
          <CardDescription>Total Trades</CardDescription>
        </CardHeader>
        <CardContent>
          <p className="text-2xl font-bold tabular-nums">{totalTrades}</p>
        </CardContent>
      </Card>
      <Card>
        <CardHeader className="pb-2">
          <CardDescription>Buy Orders</CardDescription>
        </CardHeader>
        <CardContent>
          <p className="text-2xl font-bold tabular-nums text-green-500">{buyOrders}</p>
        </CardContent>
      </Card>
      <Card>
        <CardHeader className="pb-2">
          <CardDescription>Sell Orders</CardDescription>
        </CardHeader>
        <CardContent>
          <p className="text-2xl font-bold tabular-nums text-red-500">{sellOrders}</p>
        </CardContent>
      </Card>
      <Card>
        <CardHeader className="pb-2">
          <CardDescription>Open Orders</CardDescription>
        </CardHeader>
        <CardContent>
          <p className="text-2xl font-bold tabular-nums text-yellow-500">{openOrders}</p>
        </CardContent>
      </Card>
      <Card>
        <CardHeader className="pb-2">
          <CardDescription>Tokens Held</CardDescription>
        </CardHeader>
        <CardContent>
          <p className="text-2xl font-bold tabular-nums">{uniqueTokens}</p>
        </CardContent>
      </Card>
    </div>
  );
}

export default function AccountPage() {
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
      <div>
        <h1 className="text-2xl font-bold">Account</h1>
        <p className="text-muted-foreground">
          Manage your account and view activity
        </p>
      </div>

      <UserInfoSection />
      <AccountStats />
      <PortfolioSection />
      <OrdersSection />
    </div>
  );
}
