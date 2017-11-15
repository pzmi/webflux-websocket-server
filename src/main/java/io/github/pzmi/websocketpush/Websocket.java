package io.github.pzmi.websocketpush;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class Websocket {
    private static final Logger LOGGER = LoggerFactory.getLogger(Websocket.class);
    private static final String MESSAGE_HELLO = "{\"messages\": \"Hello\"}";
    private final EmitterProcessor<String> out = EmitterProcessor.create();
    private final EmitterProcessor<String> in = EmitterProcessor.create();

    @Bean
    public HandlerMapping handlerMapping() {
        Map<String, WebSocketHandler> mappings = new HashMap<>(2);
        mappings.put("/in", session -> {
            session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .log()
                .subscribe(out::onNext);

            return session.send(Mono.just(session.textMessage(MESSAGE_HELLO))).then(session.send(in.map(session::textMessage)));
        });

        mappings.put("/out", session -> {
            session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .log()
                .subscribe();

            return session.send(Mono.just(session.textMessage(MESSAGE_HELLO))).then(session.send(out.map(session::textMessage)));
        });

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(mappings);
        mapping.setOrder(10);
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

}
