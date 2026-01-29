"use client";

import { memo, useRef, useEffect } from "react";
import Link from "next/link";
import { useAuth } from "@/providers/auth-provider";
import { useLogout } from "@/api/generated";
import { Button } from "@/components/ui/button";
import { usePrices } from "@/providers/price-provider";
import { SUPPORTED_TOKENS } from "@/lib/constants";

const PriceTickerItem = memo(function PriceTickerItem({
  token,
}: {
  token: (typeof SUPPORTED_TOKENS)[number];
}) {
  const prices = usePrices();
  const priceData = prices[token.token];
  const priceRef = useRef<HTMLSpanElement>(null);
  const prevPriceRef = useRef<number | null>(null);

  useEffect(() => {
    if (!priceRef.current || priceData?.current == null) return;

    const current = priceData.current;
    const previous = prevPriceRef.current;

    // Update text content directly without React re-render
    priceRef.current.textContent = `$${current.toLocaleString(undefined, {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })}`;

    // Update color class
    if (previous != null) {
      priceRef.current.classList.remove("text-green-500", "text-red-500", "text-muted-foreground");
      if (current > previous) {
        priceRef.current.classList.add("text-green-500");
      } else if (current < previous) {
        priceRef.current.classList.add("text-red-500");
      } else {
        priceRef.current.classList.add("text-muted-foreground");
      }
    }

    prevPriceRef.current = current;
  }, [priceData?.current]);

  return (
    <Link
      href={`/trade/${token.token}`}
      className="inline-flex items-center gap-1.5 px-4 whitespace-nowrap hover:opacity-80 transition-opacity"
    >
      <span className="font-medium text-foreground">{token.displaySymbol}</span>
      <span ref={priceRef} className="text-muted-foreground tabular-nums">
        ...
      </span>
    </Link>
  );
});

const PriceTicker = memo(function PriceTicker() {
  return (
    <div className="relative overflow-hidden w-full">
      <div className="animate-marquee flex will-change-transform">
        <div className="flex shrink-0">
          {SUPPORTED_TOKENS.map((t) => (
            <PriceTickerItem key={t.token} token={t} />
          ))}
        </div>
        <div className="flex shrink-0" aria-hidden="true">
          {SUPPORTED_TOKENS.map((t) => (
            <PriceTickerItem key={`dup-${t.token}`} token={t} />
          ))}
        </div>
      </div>
    </div>
  );
});

export function Navbar() {
  const { user, isAuthenticated, handleLogout } = useAuth();
  const logoutMutation = useLogout();

  const onLogout = () => {
    logoutMutation.mutate(undefined, {
      onSettled: () => {
        handleLogout();
      },
    });
  };

  return (
    <header className="border-b border-border bg-card">
      <div className="mx-auto flex h-14 max-w-7xl items-center justify-between px-4">
        <div className="flex items-center gap-6">
          <Link href="/dashboard" className="text-lg font-bold">
            ExchangeSim
          </Link>
          {isAuthenticated && (
            <nav className="hidden md:flex items-center gap-4 text-sm">
              <Link
                href="/dashboard"
                className="text-muted-foreground hover:text-foreground transition-colors"
              >
                Dashboard
              </Link>
              <Link
                href="/account"
                className="text-muted-foreground hover:text-foreground transition-colors"
              >
                Account
              </Link>
            </nav>
          )}
        </div>

        <div className="flex items-center gap-4">
          {isAuthenticated ? (
            <div className="flex items-center gap-3">
              <span className="text-sm text-muted-foreground">
                {user?.username}
                {user?.funds != null && (
                  <span className="ml-2 font-medium text-foreground">
                    ${user.funds.toLocaleString(undefined, { minimumFractionDigits: 2 })}
                  </span>
                )}
              </span>
              <Button variant="ghost" size="sm" onClick={onLogout}>
                Logout
              </Button>
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <Button variant="ghost" size="sm" asChild>
                <Link href="/login">Login</Link>
              </Button>
              <Button size="sm" asChild>
                <Link href="/register">Register</Link>
              </Button>
            </div>
          )}
        </div>
      </div>
      {isAuthenticated && (
        <div className="border-t border-border bg-background/50 py-2 text-sm">
          <PriceTicker />
        </div>
      )}
    </header>
  );
}
