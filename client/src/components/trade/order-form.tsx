"use client";

import { useState, useEffect, useMemo } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import {
  useBuy,
  useSell,
  useBuy1,
  useSell1,
  useGetSpotPosition,
  getGetUserOrdersQueryKey,
  getGetPortfolioQueryKey,
  getGetMeQueryKey,
  getGetSpotPositionQueryKey,
} from "@/api/generated";
import type { SpotPositionResponseDto } from "@/api/generated";
import { usePrice } from "@/providers/price-provider";
import type { SupportedToken } from "@/lib/constants";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Slider } from "@/components/ui/slider";

interface OrderFormProps {
  token: SupportedToken;
}

const PRESETS = [0, 25, 50, 75, 100] as const;

function snapToPreset(value: number): number {
  const threshold = 5;
  for (const preset of PRESETS) {
    if (Math.abs(value - preset) <= threshold) {
      return preset;
    }
  }
  return value;
}

export function OrderForm({ token }: OrderFormProps) {
  const [side, setSide] = useState<"buy" | "sell">("buy");
  const [orderType, setOrderType] = useState<"market" | "limit">("market");
  const [quantity, setQuantity] = useState("");
  const [limitPrice, setLimitPrice] = useState("");
  const [sliderValue, setSliderValue] = useState(0);
  const livePrice = usePrice(token);
  const queryClient = useQueryClient();

  // Only fetch position when in sell mode, not constantly
  const { data: positionData, refetch: refetchPosition } = useGetSpotPosition(
    token,
    {
      query: {
        enabled: side === "sell",
        staleTime: Infinity, // Don't auto-refetch, only refetch when triggered
        refetchOnWindowFocus: false,
      },
    }
  );
  const position = positionData as SpotPositionResponseDto | undefined;
  const positionQuantity = position?.quantity ?? 0;

  const marketBuy = useBuy();
  const marketSell = useSell();
  const limitBuy = useBuy1();
  const limitSell = useSell1();

  // Sync slider value to quantity when selling
  useEffect(() => {
    if (side === "sell" && positionQuantity > 0) {
      const pct = sliderValue / 100;
      const qty = positionQuantity * pct;
      setQuantity(qty > 0 ? qty.toFixed(8).replace(/\.?0+$/, "") : "");
    }
  }, [sliderValue, side, positionQuantity]);

  // Reset slider when switching sides, refetch position when switching to sell
  useEffect(() => {
    if (side === "buy") {
      setSliderValue(0);
    } else {
      // Refetch position when switching to sell mode
      refetchPosition();
    }
  }, [side, refetchPosition]);

  const handleSliderChange = (values: number[]) => {
    const snapped = snapToPreset(values[0]);
    setSliderValue(snapped);
  };

  const handlePresetClick = (preset: number) => {
    setSliderValue(preset);
  };

  const isPending =
    marketBuy.isPending ||
    marketSell.isPending ||
    limitBuy.isPending ||
    limitSell.isPending;

  const invalidateAll = () => {
    queryClient.invalidateQueries({ queryKey: getGetUserOrdersQueryKey() });
    queryClient.invalidateQueries({ queryKey: getGetPortfolioQueryKey() });
    queryClient.invalidateQueries({ queryKey: getGetMeQueryKey() });
    queryClient.invalidateQueries({ queryKey: getGetSpotPositionQueryKey(token) });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    const qty = parseFloat(quantity);
    if (isNaN(qty) || qty <= 0) {
      toast.error("Enter a valid quantity");
      return;
    }

    const payload = {
      data: {
        token,
        quantity: qty,
        ...(orderType === "limit" ? { limit: parseFloat(limitPrice) } : {}),
      },
    };

    const onSuccess = () => {
      toast.success(
        `${side === "buy" ? "Buy" : "Sell"} ${orderType} order placed`
      );
      setQuantity("");
      setLimitPrice("");
      setSliderValue(0);
      invalidateAll();
      // Refetch position after order to update available quantity
      if (side === "sell") {
        refetchPosition();
      }
    };

    const onError = () => {
      toast.error("Order failed. Check your funds or quantity.");
    };

    if (orderType === "market") {
      if (side === "buy") {
        marketBuy.mutate(payload, { onSuccess, onError });
      } else {
        marketSell.mutate(payload, { onSuccess, onError });
      }
    } else {
      const limit = parseFloat(limitPrice);
      if (isNaN(limit) || limit <= 0) {
        toast.error("Enter a valid limit price");
        return;
      }
      if (side === "buy") {
        limitBuy.mutate(payload, { onSuccess, onError });
      } else {
        limitSell.mutate(payload, { onSuccess, onError });
      }
    }
  };

  const estimatedValue =
    livePrice != null && quantity
      ? (livePrice * parseFloat(quantity || "0")).toFixed(2)
      : null;

  return (
    <Card>
      <CardHeader className="pb-3">
        <CardTitle className="text-base">Place Order</CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Buy / Sell toggle */}
          <Tabs
            value={side}
            onValueChange={(v) => setSide(v as "buy" | "sell")}
          >
            <TabsList className="w-full">
              <TabsTrigger value="buy" className="flex-1">
                Buy
              </TabsTrigger>
              <TabsTrigger value="sell" className="flex-1">
                Sell
              </TabsTrigger>
            </TabsList>
          </Tabs>

          {/* Market / Limit toggle */}
          <Tabs
            value={orderType}
            onValueChange={(v) => setOrderType(v as "market" | "limit")}
          >
            <TabsList className="w-full">
              <TabsTrigger value="market" className="flex-1">
                Market
              </TabsTrigger>
              <TabsTrigger value="limit" className="flex-1">
                Limit
              </TabsTrigger>
            </TabsList>
          </Tabs>

          {orderType === "limit" && (
            <div className="space-y-2">
              <Label htmlFor="limit-price">Limit Price (USD)</Label>
              <Input
                id="limit-price"
                type="number"
                step="any"
                min="0"
                placeholder="0.00"
                value={limitPrice}
                onChange={(e) => setLimitPrice(e.target.value)}
              />
            </div>
          )}

          <div className="space-y-2">
            <div className="flex items-center justify-between">
              <Label htmlFor="quantity">Quantity ({token.toUpperCase()})</Label>
              {side === "sell" && positionQuantity > 0 && (
                <span className="text-xs text-muted-foreground">
                  Available: {positionQuantity.toFixed(6).replace(/\.?0+$/, "")}
                </span>
              )}
            </div>
            <Input
              id="quantity"
              type="number"
              step="any"
              min="0"
              placeholder="0.00"
              value={quantity}
              onChange={(e) => {
                setQuantity(e.target.value);
                // Update slider when manually entering quantity (sell mode)
                if (side === "sell" && positionQuantity > 0) {
                  const pct = (parseFloat(e.target.value) / positionQuantity) * 100;
                  setSliderValue(Math.min(100, Math.max(0, pct)));
                }
              }}
            />
          </div>

          {/* Quantity slider for selling */}
          {side === "sell" && positionQuantity > 0 && (
            <div className="space-y-3">
              <Slider
                value={[sliderValue]}
                onValueChange={handleSliderChange}
                max={100}
                step={1}
                className="w-full"
              />
              <div className="flex justify-between gap-2">
                {PRESETS.slice(1).map((preset) => (
                  <Button
                    key={preset}
                    type="button"
                    variant={sliderValue === preset ? "default" : "outline"}
                    size="sm"
                    className="flex-1 text-xs"
                    onClick={() => handlePresetClick(preset)}
                  >
                    {preset}%
                  </Button>
                ))}
              </div>
            </div>
          )}

          {side === "sell" && positionQuantity === 0 && (
            <p className="text-sm text-muted-foreground text-center py-2">
              No {token.toUpperCase()} to sell
            </p>
          )}

          {orderType === "market" && estimatedValue && (
            <p className="text-sm text-muted-foreground">
              Estimated value: ${estimatedValue}
            </p>
          )}

          <Button
            type="submit"
            className={`w-full ${
              side === "buy"
                ? "bg-green-600 hover:bg-green-700"
                : "bg-red-600 hover:bg-red-700"
            }`}
            disabled={isPending}
          >
            {isPending
              ? "Placing..."
              : `${side === "buy" ? "Buy" : "Sell"} ${token}`}
          </Button>
        </form>
      </CardContent>
    </Card>
  );
}
