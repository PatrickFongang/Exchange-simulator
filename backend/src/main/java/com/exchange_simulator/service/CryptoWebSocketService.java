package com.exchange_simulator.service;

import com.exchange_simulator.dto.binance.MarkPriceStreamEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Consumer;

@Service
public class CryptoWebSocketService {
    HttpClient client = HttpClient.newHttpClient();

    public Map<String, WebSocket> openedSockets = new ConcurrentHashMap<>();

    public Map<String, CopyOnWriteArrayList<Consumer<MarkPriceStreamEvent>>> listeners = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public CryptoWebSocketService(){
        System.out.println("Start WebSocket service!");
    }

    private void CreateTokenWebSocket(String symbol){
        var url = "wss://fstream.binance.com/ws/"+symbol.toLowerCase()+"usdt@markPrice@1s";
        client.newWebSocketBuilder()
                .buildAsync(URI.create(url), new CryptoWebSocketListener(symbol))
                .join();
    }

    private void RemoveTokenWebSocket(String symbol){
        if(openedSockets.containsKey(symbol)){
            openedSockets.get(symbol).sendClose(WebSocket.NORMAL_CLOSURE, "Internal request")
                    .thenAccept(_ -> System.out.println("Send close to socket " + symbol))
                    .exceptionally(ex -> {
                        System.out.println("Error sending close: " + ex);
                        return null;
                    });
        }
    }

    public Consumer<Runnable> AddTokenListener(String symbol, Consumer<MarkPriceStreamEvent> consumer){
        if(!listeners.containsKey(symbol)){
            listeners.put(symbol, new CopyOnWriteArrayList<>(List.of(consumer)));
        }
        else{
            var arr = listeners.get(symbol);
            if(!arr.contains(consumer)){
                arr.add(consumer);
            }
        }

        if(!openedSockets.containsKey(symbol)){
            CreateTokenWebSocket(symbol);
        }

        return (cb) -> this.RemoveTokenListener(symbol, consumer, Optional.of(cb));
    }

    public void RemoveTokenListener(String symbol, Consumer<MarkPriceStreamEvent> consumer, Optional<Runnable> cb){
        listeners.get(symbol).remove(consumer);

        if(listeners.get(symbol).isEmpty()){
            RemoveTokenWebSocket(symbol);
        }

        cb.ifPresent(Runnable::run);
    }

    public void dispose(){
        for(var symbol : openedSockets.keySet()){
            RemoveTokenWebSocket(symbol);
        }
    }

    private void HandleMarkPriceMessage(CharSequence message) {
        var markPriceStreamEvent
                = objectMapper.readValue(message.toString(), MarkPriceStreamEvent.class);

        var symbol = markPriceStreamEvent.getSymbol().toLowerCase();
        markPriceStreamEvent.setSymbol(symbol.substring(0, symbol.length()-4));

        eventPublisher.publishEvent(markPriceStreamEvent);
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
            error.printStackTrace(System.out);
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