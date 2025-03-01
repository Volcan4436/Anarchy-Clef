package adris.altoclef.webserver;

import io.javalin.websocket.WsContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class WebSocketSession {
    private final WsContext ctx;
    private final ObjectMapper mapper = new ObjectMapper();

    public WebSocketSession(WsContext ctx) {
        this.ctx = ctx;
    }

    public void handleMessage(String message) {
        try {
            ObjectNode response = mapper.createObjectNode()
                .put("type", "ack")
                .put("message", message);
            sendMessage(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(Object message) {
        try {
            if (ctx.session.isOpen()) {
                ctx.send(mapper.writeValueAsString(message));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getSessionId() {
        return ctx.getSessionId();
    }
} 