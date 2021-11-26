package in.faeli.ttyd;

import in.faeli.ttyd.websocket.TerminalSocket;
import org.springframework.beans.factory.annotation.Autowired;
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
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired
    private Config config;
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(terminalSocket(),getHandlerPath());
    }

    private String getHandlerPath() {
        if (Objects.nonNull(config) && Objects.nonNull(config.getSocket()) && Objects.nonNull(config.getSocket().getPath())) {
            return config.getSocket().getPath();
        }
        return "/terminal";
    }

    @Bean
    public WebSocketHandler terminalSocket()
    {
        return new PerConnectionWebSocketHandler(TerminalSocket.class);
    }
}
