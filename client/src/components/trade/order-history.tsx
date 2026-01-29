"use client";

import { useGetUserOrders } from "@/api/generated";
import type { OrderResponseDto } from "@/api/generated";
import type { SupportedToken } from "@/lib/constants";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useCancelOrder, getGetUserOrdersQueryKey } from "@/api/generated";
import { useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import { toast } from "sonner";

interface OrderHistoryProps {
  token: SupportedToken;
}

function fmt(n: number, decimals = 2) {
  return n.toLocaleString(undefined, {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });
}

export function OrderHistory({ token }: OrderHistoryProps) {
  const { data, isLoading } = useGetUserOrders({
    query: { refetchInterval: 5000 },
  });
  const cancelOrder = useCancelOrder();
  const queryClient = useQueryClient();

  const allOrders = (data as OrderResponseDto[] | undefined) ?? [];
  const orders = allOrders
    .filter((o) => o.token === token)
    .sort(
      (a, b) =>
        new Date(b.createdAt ?? 0).getTime() -
        new Date(a.createdAt ?? 0).getTime()
    );

  const handleCancel = (orderId: number) => {
    cancelOrder.mutate(
      { orderId },
      {
        onSuccess: () => {
          toast.success("Order cancelled");
          queryClient.invalidateQueries({
            queryKey: getGetUserOrdersQueryKey(),
          });
        },
        onError: () => {
          toast.error("Failed to cancel order");
        },
      }
    );
  };

  if (isLoading) {
    return (
      <Card>
        <CardContent className="p-4">
          <p className="text-sm text-muted-foreground">Loading orders...</p>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader className="pb-2">
        <CardTitle className="text-base">Order History</CardTitle>
      </CardHeader>
      <CardContent className="p-0">
        {orders.length === 0 ? (
          <p className="p-4 text-sm text-muted-foreground">
            No orders for {token} yet.
          </p>
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Type</TableHead>
                <TableHead>Side</TableHead>
                <TableHead className="text-right">Qty</TableHead>
                <TableHead className="text-right">Price</TableHead>
                <TableHead className="text-right">Value</TableHead>
                <TableHead className="text-right">Date</TableHead>
                <TableHead></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {orders.map((order) => {
                const isOpen =
                  order.orderType === "LIMIT" && order.closedAt == null;

                return (
                  <TableRow key={order.orderId}>
                    <TableCell>
                      <span
                        className={`text-xs font-medium ${
                          order.orderType === "LIMIT"
                            ? "text-yellow-500"
                            : "text-blue-500"
                        }`}
                      >
                        {order.orderType}
                      </span>
                    </TableCell>
                    <TableCell>
                      <span
                        className={
                          order.transactionType === "BUY"
                            ? "text-green-500"
                            : "text-red-500"
                        }
                      >
                        {order.transactionType}
                      </span>
                    </TableCell>
                    <TableCell className="text-right">
                      {order.quantity != null ? fmt(order.quantity, 6) : "—"}
                    </TableCell>
                    <TableCell className="text-right">
                      {order.entry != null ? `$${fmt(order.entry)}` : "—"}
                    </TableCell>
                    <TableCell className="text-right">
                      {order.orderValue != null
                        ? `$${fmt(order.orderValue)}`
                        : "—"}
                    </TableCell>
                    <TableCell className="text-right text-xs text-muted-foreground">
                      {order.createdAt
                        ? new Date(order.createdAt).toLocaleDateString()
                        : "—"}
                    </TableCell>
                    <TableCell className="text-right">
                      {isOpen && order.orderId != null && (
                        <Button
                          variant="ghost"
                          size="sm"
                          className="text-xs text-destructive hover:text-destructive"
                          onClick={() => handleCancel(order.orderId!)}
                          disabled={cancelOrder.isPending}
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
