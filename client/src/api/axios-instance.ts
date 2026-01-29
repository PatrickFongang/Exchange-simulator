import Axios from "axios";

export const AXIOS_INSTANCE = Axios.create({
  baseURL: "http://localhost:8080",
  withCredentials: true,
});

export const customInstance = async <T>(
  url: string,
  options?: RequestInit
): Promise<T> => {
  const { method = "GET", headers, body, signal } = options ?? {};

  const response = await AXIOS_INSTANCE.request<T>({
    url,
    method,
    data: body,
    signal: signal ?? undefined,
    headers: headers as Record<string, string>,
  });

  return response.data;
};

export default customInstance;
