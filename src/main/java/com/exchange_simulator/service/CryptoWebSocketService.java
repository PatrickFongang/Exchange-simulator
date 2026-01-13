package com.exchange_simulator.service;

import com.exchange_simulator.dto.binance.MarkPriceStreamEvent;
import io.reactivex.rxjava3.disposables.Disposable;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

@Service
public class CryptoWebSocketService implements Disposable {
    HttpClient client = HttpClient.newHttpClient();

    public Map<String, WebSocket> openedSockets = new HashMap<>();
    public Map<String, List<Consumer<MarkPriceStreamEvent>>> listeners= new HashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    private boolean isDisposedFlag = false;

    public CryptoWebSocketService(){
        System.out.println("Start WebSocket service!");
    }

    private void CreateTokenWebSocket(String symbol){
        var url = "wss://fstream.binance.com/ws/"+symbol.toLowerCase()+"@markPrice@1s";
        client.newWebSocketBuilder()
                .buildAsync(URI.create(url), new CryptoWebSocketListener(symbol))
                .join();
    }

    private void RemoveTokenWebSocket(String symbol){
        if(openedSockets.containsKey(symbol)){
            openedSockets.get(symbol).sendClose(500, "Internal request");
        }
    }

    public void AddTokenListener(String symbol, Consumer<MarkPriceStreamEvent> consumer){
        var arr = listeners.getOrDefault(symbol, new ArrayList<>());
        if(!arr.contains(consumer)){
            arr.add(consumer);
        }

        if(!openedSockets.containsKey(symbol)){
            CreateTokenWebSocket(symbol);
        }
    }

    public void RemoveTokenListener(String symbol, Consumer<MarkPriceStreamEvent> consumer){
        listeners.get(symbol).remove(consumer);

        if(listeners.get(symbol).isEmpty()){
            RemoveTokenWebSocket(symbol);
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

    private void HandleMarkPriceMessage(CharSequence message) {
        var markPriceStreamEvent
                = objectMapper.readValue(message.toString(), MarkPriceStreamEvent.class);

        System.out.println("Parsed event = (" +
                " Symbol = " + markPriceStreamEvent.symbol() +
                " Index price = " + markPriceStreamEvent.indexPrice() +
                " )");

        var symbol = markPriceStreamEvent.symbol();
        if (!listeners.containsKey(symbol)) return;

        for (var listener : listeners.get(symbol)) {
            listener.accept(markPriceStreamEvent);
        }
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
            HandleMarkPriceMessage(data);
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
