import chess.*;
import dataaccess.*;
import dataaccess.*;
import server.Server;

public class Main {
    public static void main(String[] args) {
        // var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        // System.out.println("♕ 240 Chess Server: " + piece);
        try {
            var port = 8080;
            if(args.length >= 1) {
                port = Integer.parseInt(args[0]);
            }


            var server = new Server();
            port = server.run(port);
            System.out.println("Server started on port: " + port);
        } catch (Throwable e) {
            System.out.println("Unable to start server: " + e.getMessage());
        }
    }
}
