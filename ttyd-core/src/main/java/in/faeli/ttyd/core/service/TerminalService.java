package in.faeli.ttyd.core.service;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import in.faeli.ttyd.core.helper.ThreadHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import in.faeli.ttyd.core.TerminalConfig;
import in.faeli.ttyd.core.helper.IOHelper;

@Component
public class TerminalService {
    @Autowired
    private TerminalConfig config;

    private boolean isReady;
    private PtyProcess process;
    private String[] termCommand;
    private Integer columns = 20;
    private Integer rows = 10;
    private BufferedReader inputReader;
    private BufferedReader errorReader;
    private BufferedWriter outputWriter;
    private WebSocketSession webSocketSession;

    public void setWebSocketSession(WebSocketSession webSocketSession) {
        this.webSocketSession = webSocketSession;
    }

    private LinkedBlockingQueue<String> commandQueue = new LinkedBlockingQueue<>();

    public void onTerminalInit() {
    }

    public void onTerminalReady() throws Exception {
        initializeProcess();
    }

    private void initializeProcess() throws Exception {
        if (isReady)
            return;
        final String osName = System.getProperty("os.name");
        final String userHome = System.getProperty("user.home");
        final Path dataDir = Paths.get(userHome).resolve(".terminalfx");
        IOHelper.copyLibPty(dataDir);
        if (StringUtils.startsWithIgnoreCase(osName, "win")) {
            this.termCommand = "powershell.exe".split("\\s+");
        } else {
            this.termCommand = "/bin/bash -i".split("\\s+");
        }
        // 开始
        if(Objects.nonNull(config.getShell())){
            this.termCommand = config.getShell().split("\\s+");
        }

        Map<String, String> envs = new HashMap<>(System.getenv());
        envs.put("TERM", "xterm");

        System.setProperty("PTY_LIB_FOLDER", dataDir.resolve("libpty").toString());

        this.process = PtyProcess.exec(termCommand, envs, userHome);

        process.setWinSize(new WinSize(columns, rows));
        this.inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        this.errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        this.outputWriter = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        ThreadHelper.start(() -> {
            printReader(inputReader);
        });

        ThreadHelper.start(() -> {
            printReader(errorReader);
        });
        this.isReady = true;
    }

    public void print(String text) throws IOException {

        Map<String, String> map = new HashMap<>();
        map.put("type", "TERMINAL_PRINT");
        map.put("text", text);

        String message = new ObjectMapper().writeValueAsString(map);

        webSocketSession.sendMessage(new TextMessage(message));

    }

    private void printReader(BufferedReader bufferedReader) {
        try {
            int nRead;
            char[] data = new char[1 * 1024];

            while ((nRead = bufferedReader.read(data, 0, data.length)) != -1) {
                StringBuilder builder = new StringBuilder(nRead);
                builder.append(data, 0, nRead);
                print(builder.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onCommand(String command) throws InterruptedException {

        if (Objects.isNull(command)) {
            return;
        }

        commandQueue.put(command);
        ThreadHelper.start(() -> {
            try {
                outputWriter.write(commandQueue.poll());
                outputWriter.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    public void onTerminalResize(String columns, String rows) {
        if (Objects.nonNull(columns) && Objects.nonNull(rows)) {
            this.columns = Integer.valueOf(columns);
            this.rows = Integer.valueOf(rows);

            if (Objects.nonNull(process)) {
                process.setWinSize(new WinSize(this.columns, this.rows));
            }

        }
    }

    public void onTerminalClose(){
        if(null!=process && process.isAlive()){
            process.destroy();
        }
    }
}
