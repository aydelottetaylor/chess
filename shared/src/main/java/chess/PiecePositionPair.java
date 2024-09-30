package chess;

public class PiecePositionPair {
    private ChessPiece piece;
    private ChessPosition position;

    public PiecePositionPair(ChessPiece piece, ChessPosition position) {
        this.piece = piece;
        this.position = position;
    }

    public ChessPiece getPiece() {
        return this.piece;
    }

    public ChessPosition getPosition() {
        return this.position;
    }
}