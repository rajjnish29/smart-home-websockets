package com.example.chatclient;

import com.example.chatclient.model.Message;
import com.example.chatclient.service.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
public class StompSessionHandlerImpl implements StompSessionHandler {

    private WebSocketService webSocketService;
    private AtomicBoolean isConnected;

    public StompSessionHandlerImpl() {
        this.isConnected = new AtomicBoolean();
    }

    @Autowired
    public void setWebSocketService(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    @Override
    public void afterConnected(
            StompSession session, StompHeaders connectedHeaders) {
            this.isConnected.set(true);
            session.subscribe("/topic/messages/outgoing", this);
            session.subscribe("/user/queue/payment", this);
    }

    @Override
    public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
        exception.printStackTrace();
    }

    @Override
    public void handleTransportError(StompSession session, Throwable exception) {
        if (this.isConnected.get()) {
            this.isConnected.set(false);
            webSocketService.initSession();
        }

    }

    @Override
    public Type getPayloadType(StompHeaders headers) {
        return Message.class;
    }

    @Override
    public void handleFrame(StompHeaders headers, Object payload) {
        Message msg = (Message) payload;
        log.info("Received : " + msg.getContent()+ " from : " + msg.getFrom());
    }

}