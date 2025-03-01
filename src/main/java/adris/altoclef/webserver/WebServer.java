package adris.altoclef.webserver;

import adris.altoclef.AltoClef;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.websocket.WsContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebServer {
    private final AltoClef mod;
    private final WebConfig config;
    private final Map<String, WsContext> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private Javalin app;

    public WebServer(AltoClef mod) {
        this.mod = mod;
        this.config = WebConfig.load();
    }

    public void start() {
        app = Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.directory = "/web";
                staticFiles.location = Location.CLASSPATH;
            });
        }).start(config.getPort());

        app.get("/api/status", ctx -> {
            ObjectNode status = mapper.createObjectNode()
                .put("connected", true)
                .put("running", mod.getTaskRunner().getCurrentTaskChain() != null && 
                              mod.getTaskRunner().getCurrentTaskChain().getTasks().size() > 0)
                .put("username", mod.getPlayer().getGameProfile().getName());
            ctx.json(status);
        });

        app.post("/api/command", ctx -> {
            JsonNode body = mapper.readTree(ctx.body());
            String command = body.get("command").asText();
            mod.getCommandExecutor().execute(command);
            ctx.status(200);
        });

        app.ws("/api/ws", ws -> {
            ws.onConnect(ctx -> {
                sessions.put(ctx.getSessionId(), ctx);
            });

            ws.onClose(ctx -> {
                sessions.remove(ctx.getSessionId());
            });

            ws.onMessage(ctx -> {
                try {
                    ObjectNode response = mapper.createObjectNode()
                        .put("type", "ack")
                        .put("message", ctx.message());
                    ctx.send(mapper.writeValueAsString(response));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    public void stop() {
        if (app != null) {
            app.stop();
        }
    }
} 