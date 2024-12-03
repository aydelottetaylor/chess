package websocket.messages;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * 
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    ServerMessageType serverMessageType;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage)) {
            return false;
        }
        ServerMessage that = (ServerMessage) o;
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }

    public static class ServerMessageDeserializer implements JsonDeserializer<ServerMessage> {
        @Override
        public ServerMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            ServerMessageType type = ServerMessageType.valueOf(jsonObject.get("serverMessageType").getAsString());

            switch (type) {
                case LOAD_GAME:
                    return context.deserialize(json, LoadGameMessage.class);
                case ERROR:
                    return context.deserialize(json, ErrorMessage.class);
                case NOTIFICATION:
                    return context.deserialize(json, NotificationMessage.class);
                default:
                    throw new JsonParseException("Unknown server message type: " + type);
            }
        }
    }
}
