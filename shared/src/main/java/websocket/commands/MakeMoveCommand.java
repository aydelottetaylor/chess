package websocket.commands;

 // Assuming ChessMove is in the model package
import chess.ChessMove;

import java.util.Objects;

public class MakeMoveCommand extends UserGameCommand {

    private final ChessMove move;

    public MakeMoveCommand(String authToken, Integer gameID, ChessMove move) {
        super(CommandType.MAKE_MOVE, authToken, gameID);
        this.move = move;
    }

    public ChessMove getMove() {
        return move;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MakeMoveCommand)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        MakeMoveCommand that = (MakeMoveCommand) o;
        return Objects.equals(move, that.move);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), move);
    }

    @Override
    public String toString() {
        return "MakeMoveCommand{" +
                "commandType=" + getCommandType() +
                ", authToken='" + getAuthToken() + '\'' +
                ", gameID=" + getGameID() +
                ", move=" + move +
                '}';
    }
}