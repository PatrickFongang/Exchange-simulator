"use client";

import { createContext, useContext, useCallback } from "react";
import { useRouter } from "next/navigation";
import { useQueryClient } from "@tanstack/react-query";
import { useGetMe, getGetMeQueryKey } from "@/api/generated";
import type { UserResponseDto } from "@/api/generated";

interface AuthContextValue {
  user: UserResponseDto | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  handleLogout: () => void;
  refetchUser: () => void;
}

const AuthContext = createContext<AuthContextValue>({
  user: null,
  isAuthenticated: false,
  isLoading: true,
  handleLogout: () => {},
  refetchUser: () => {},
});

export function useAuth() {
  return useContext(AuthContext);
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const queryClient = useQueryClient();

  const {
    data: user,
    isLoading,
    isError,
    refetch,
  } = useGetMe({
    query: {
      retry: false,
    },
  });

  const handleLogout = useCallback(async () => {
    queryClient.setQueryData(getGetMeQueryKey(), null);
    queryClient.removeQueries({ queryKey: getGetMeQueryKey() });
    router.push("/login");
  }, [queryClient, router]);

  // If session expired / not logged in, the query errors out — treat as null
  const resolvedUser = isError ? null : (user as UserResponseDto | null) ?? null;

  return (
    <AuthContext.Provider
      value={{
        user: resolvedUser,
        isAuthenticated: !!resolvedUser,
        isLoading,
        handleLogout,
        refetchUser: refetch,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}
