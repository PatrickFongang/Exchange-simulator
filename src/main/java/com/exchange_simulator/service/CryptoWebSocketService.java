package com.exchange_simulator.service;

import io.reactivex.rxjava3.disposables.Disposable;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

@Service
public class CryptoWebSocketService implements Disposable {
    HttpClient client = HttpClient.newHttpClient();
    public Map<String, WebSocket> openedSockets = new HashMap<>();
    public boolean isDisposedFlag = false;

    public CryptoWebSocketService(){
        System.out.println("Start WebSocket service!");
        CreateTokenWebSocket("BTCUSDT");
        CreateTokenWebSocket("ETHUSDT");
    }

    public void CreateTokenWebSocket(String symbol){
        var url = "wss://fstream.binance.com/ws/"+symbol.toLowerCase()+"@trade";
        client.newWebSocketBuilder()
                .buildAsync(URI.create(url), new CryptoWebSocketListener(symbol))
                .join();

    }

    public void RemoveTokenWebSocket(String symbol){
        if(openedSockets.containsKey(symbol)){
            openedSockets.get(symbol).sendClose(500, "Internal request");
        }
    }

    public boolean isDisposed(){
        return isDisposedFlag;
    }

    public void dispose(){
        for(var symbol : openedSockets.keySet()){
            RemoveTokenWebSocket(symbol);
        }
        isDisposedFlag = true;
    }

    class CryptoWebSocketListener implements WebSocket.Listener{
        String symbol;

        public CryptoWebSocketListener(String symbol){
            this.symbol = symbol;
        }

        @Override
        public void onOpen(WebSocket webSocket){
            System.out.println("WebSocket successfully opened!");
            openedSockets.put(symbol, webSocket);
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last){
            System.out.println(data);
            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error){
            System.out.println("Error in WebSocket:");
            System.out.println(error.getMessage());
            WebSocket.Listener.super.onError(webSocket, error);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket,
                                           int statusCode,
                                           String reason) {
            openedSockets.remove(symbol);
            System.out.println("WebSocket for " + symbol + " closed (" + statusCode + ") because of " + reason);
            return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
        }
    }
}
