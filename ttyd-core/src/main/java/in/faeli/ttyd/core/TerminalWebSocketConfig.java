package in.faeli.ttyd.core;

import in.faeli.ttyd.core.websocket.TerminalSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.PerConnectionWebSocketHandler;

import java.util.Objects;

@Configuration
@EnableWebSocket
@ConditionalOnProperty(value = "terminal.enabled", havingValue = "true")
public class TerminalWebSocketConfig implements WebSocketConfigurer {
    @Autowired
    private TerminalConfig config;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        final String handlerPath = getHandlerPath();
        System.out.println("REGISTER TERMINAL WEB SOCKET: " + handlerPath);
        registry.addHandler(terminalSocket(), handlerPath);
    }

    private String getHandlerPath() {
        if (Objects.nonNull(config) && Objects.nonNull(config.getSocket()) && Objects.nonNull(config.getSocket().getPath())) {
            return config.getSocket().getPath();
        }
        return "/terminal";
    }

    @Bean
    public WebSocketHandler terminalSocket() {
        return new PerConnectionWebSocketHandler(TerminalSocket.class);
    }
}
