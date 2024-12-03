package websocket.messages;

import java.util.Objects;

public class LoadGameMessage extends ServerMessage {
    private final Object game;

    public LoadGameMessage(Object game) {
        super(ServerMessageType.LOAD_GAME);
        this.game = game;
    }

    public Object getGame() {
        return game;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LoadGameMessage)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        LoadGameMessage that = (LoadGameMessage) o;
        return Objects.equals(game, that.game);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), game);
    }
}