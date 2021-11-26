package in.faeli.ttyd.core;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "terminal")
@ConditionalOnProperty(value = "terminal.enabled", havingValue = "true",matchIfMissing = false)
public class TerminalConfig {
    private Boolean enabled;
    private SocketConfig socket;
    private String shell;

    public String getShell() {
        return shell;
    }
    public void setShell(String shell) {
        this.shell = shell;
    }

    public SocketConfig getSocket() {
        return socket;
    }

    public void setSocket(SocketConfig socket) {
        this.socket = socket;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    static class SocketConfig {
        private String path;

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}
