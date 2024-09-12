package chess;

import java.util.Collection;

import lombok.EqualsAndHashCode;

import java.util.ArrayList;

import javax.swing.plaf.basic.BasicBorders;

import chess.ChessPosition;
import jdk.jfr.Timespan;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
@EqualsAndHashCode
public class ChessPiece {
    public ChessGame.TeamColor pieceColor;
    public ChessPiece.PieceType type;
    
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> moves = new ArrayList<>();

        switch(this.type) {
            case BISHOP -> addBishopMoves(board, myPosition, moves);
            case ROOK -> addRookMoves(board, myPosition, moves);
            case QUEEN -> addQueenMoves(board, myPosition, moves);
            default -> throw new IllegalArgumentException("Unexpected value: " + this.type);
        }

        return moves;
    }

    private void addBishopMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        addDiagonalMoves(board, myPosition, moves, -1, -1);
        addDiagonalMoves(board, myPosition, moves, 1, -1);
        addDiagonalMoves(board, myPosition, moves, -1, 1);
        addDiagonalMoves(board, myPosition, moves, 1, 1);
    }

    private void addRookMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        addStraightMoves(board, myPosition, moves, 0, -1);
        addStraightMoves(board, myPosition, moves, 0, 1);
        addStraightMoves(board, myPosition, moves, -1, 0);
        addStraightMoves(board, myPosition, moves, 1, 0);
    }

    private void addQueenMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        addBishopMoves(board, myPosition, moves);
        addRookMoves(board, myPosition, moves);
    }

    private void addDiagonalMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves, int rowIncrement, int colIncrement) {
        int currentRow = myPosition.getRow();
        int currentCol = myPosition.getColumn();

        while(true) {
            currentRow += rowIncrement;
            currentCol += colIncrement;

            if(!isValidPosition(currentRow, currentCol)) {
                break;
            }

            ChessPosition newPosition = new ChessPosition(currentRow, currentCol);
            ChessPiece pieceAtNewPos = board.getPiece(newPosition);

            if(pieceAtNewPos == null) {
                moves.add(new ChessMove(myPosition, newPosition, null));
            } else if (pieceAtNewPos.getTeamColor() != this.pieceColor) {
                moves.add(new ChessMove(myPosition, newPosition, null));
            } else {
                break;
            }
        }
    }

    private void addStraightMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves, int rowIncrement, int colIncrement) {
        int currentRow = myPosition.getRow();
        int currentCol = myPosition.getColumn();

        while(true) {
            currentRow += rowIncrement;
            currentCol += colIncrement;

            if(!isValidPosition(currentRow, currentCol)) {
                break;
            }

            ChessPosition newPosition = new ChessPosition(currentRow, currentCol);
            ChessPiece pieceAtNewPos = board.getPiece(newPosition);

            if(pieceAtNewPos == null) {
                moves.add(new ChessMove(myPosition, newPosition, null));
            } else if (pieceAtNewPos.getTeamColor() != this.pieceColor) {
                moves.add(new ChessMove(myPosition, newPosition, null));
            } else {
                break;
            }
        }
    }

    private boolean  isValidPosition(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

}
