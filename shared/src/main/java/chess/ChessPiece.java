package chess;

import java.util.ArrayList;
// import java.util.lombok
import java.util.Collection;
import java.util.Objects;

// import javax.swing.plaf.basic.BasicBorders;
// import chess.ChessPosition;
// import jdk.jfr.Timespan;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
// @EqualsAndHashCode
public class ChessPiece {
    public ChessGame.TeamColor pieceColor;
    public ChessPiece.PieceType type;
    
    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "pieceColor=" + pieceColor +
                ", type=" + type +
                '}';
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(pieceColor);
        result = 31 * result + Objects.hashCode(type);
        return result;
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
            case KNIGHT -> addKnightMoves(board, myPosition, moves);
            case KING -> addKingMoves(board, myPosition, moves);
            case PAWN -> addPawnMoves(board, myPosition, moves);
            default -> throw new IllegalArgumentException("Unexpected value: " + this.type);
        }

        return moves;
    }

    /**
     * Helps calculate all of the possible moves a bishop can make, passes given information
     * on to a function that calculates the moves in each diagonal direction
     *
     * @param board The chess board we are working with
     * @param myPosition The current position of piece
     * @param moves The collection of possible moves for this piece
     */
    private void addBishopMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        addDiagonalMoves(board, myPosition, moves, -1, -1);
        addDiagonalMoves(board, myPosition, moves, 1, -1);
        addDiagonalMoves(board, myPosition, moves, -1, 1);
        addDiagonalMoves(board, myPosition, moves, 1, 1);
    }

    /**
     * Helps calculate all of the possible moves a rook can make, passes given information
     * on to a function that calculates the moves in each straight direction
     *
     * @param board The chess board we are working with
     * @param myPosition The current position of piece
     * @param moves The collection of possible moves for this piece
     */
    private void addRookMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        addStraightMoves(board, myPosition, moves, 0, -1);
        addStraightMoves(board, myPosition, moves, 0, 1);
        addStraightMoves(board, myPosition, moves, -1, 0);
        addStraightMoves(board, myPosition, moves, 1, 0);
    }

    /**
     * Helps calculate all of the moves a queen can make, since a queen combines the moves from a 
     * bishop and a rook we will use the addBishopMoves function and the addRookMoves functions 
     * to create the Collection of moves for the queen. 
     *
     * @param board The chess board we are working with
     * @param myPosition The current position of piece
     * @param moves The collection of possible moves for this piece
     */
    private void addQueenMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        addBishopMoves(board, myPosition, moves);
        addRookMoves(board, myPosition, moves);
    }

    /**
     * Helps calculate all of the possible moves for a knight at a specific position.  
     *
     * @param board The chess board we are working with
     * @param myPosition The current position of piece
     * @param moves The collection of possible moves for this piece
     */
    private void addKnightMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int[][] knightMoves = {
            {-2, -1}, {-2, +1}, // 2 up, 1 left/right
            {+2, -1}, {+2, +1}, // 2 down, 1 left/right
            {-1, -2}, {+1, -2}, // 1 up/down, 2 left
            {-1, +2}, {+1, +2}  // 1 up/down, 2 right
        };

        for (int[] move : knightMoves) {
            int newRow = myPosition.getRow() + move[0];
            int newCol = myPosition.getColumn() + move[1];

            if (isValidPosition(newRow, newCol)) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece pieceAtNewPos = board.getPiece(newPosition);

                if (pieceAtNewPos == null || pieceAtNewPos.getTeamColor() != this.pieceColor) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }
    }

    /**
     * Helps calculate all of the possible moves for a king at a specific position.  
     *
     * NOTE: This function does not include logic to figure out if the king is in check or checkmate. 
     * 
     * @param board The chess board we are working with
     * @param myPosition The current position of piece
     * @param moves The collection of possible moves for this piece
     */
    private void addKingMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int[][] kingMoves = {
            {-1, 0},  {1, 0},
            {0, -1},  {0, 1},
            {-1, -1}, {-1, 1},
            {1, -1}, {1, 1}
        };

        for (int[] move : kingMoves) {
            int newRow = myPosition.getRow() + move[0];
            int newCol = myPosition.getColumn() + move[1];

            if(isValidPosition(newRow, newCol)) {
                ChessPosition newPosition = new ChessPosition(newRow, newCol);
                ChessPiece pieceAtNewPos = board.getPiece(newPosition);

                if(pieceAtNewPos == null || pieceAtNewPos.getTeamColor() != this.pieceColor) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                }
            }
        }
    }

    /**
     * Helps calculate all of the possible moves for a pawn at a specific position not including captures.
     * 
     * @param board The chess board we are working with
     * @param myPosition The current position of piece
     * @param moves The collection of possible moves for this piece
     */
    private void addPawnMoves(ChessBoard board, ChessPosition myPosition, Collection<ChessMove> moves) {
        int row = myPosition.getRow();
        int col = myPosition.getColumn();
        int direction = (this.pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1);

        ChessPosition forwardOne = new ChessPosition((row + direction), col);
        if (isValidPosition(forwardOne.getRow(), forwardOne.getColumn()) && board.getPiece(forwardOne) == null) {
            // Check for promotion
            if(forwardOne.getRow() == 1 || forwardOne.getRow() == 8) {
                moves.add(new ChessMove(myPosition, forwardOne, ChessPiece.PieceType.QUEEN));
                moves.add(new ChessMove(myPosition, forwardOne, ChessPiece.PieceType.BISHOP));
                moves.add(new ChessMove(myPosition, forwardOne, ChessPiece.PieceType.KNIGHT));
                moves.add(new ChessMove(myPosition, forwardOne, ChessPiece.PieceType.ROOK));
            } else {
                moves.add(new ChessMove(myPosition, forwardOne, null));
            }
        }

        if((row == 2 && this.pieceColor == ChessGame.TeamColor.WHITE) || (row == 7 && this.pieceColor == ChessGame.TeamColor.BLACK)) {
            ChessPosition forwardTwo = new ChessPosition(row + 2 * direction, col);
            if (board.getPiece(forwardTwo) == null && board.getPiece(forwardOne) == null) {
                moves.add(new ChessMove(myPosition, forwardTwo, null));
            }
        }

        addPawnCapture(board, myPosition, row+direction, col - 1, moves);
        addPawnCapture(board, myPosition, row+direction, col + 1, moves);
    }

    /**
     * Helps calculate all of the diagonal moves in a specific direction for bishops and queens
     * 
     * @param board The chess board we are working with
     * @param myPosition The current position of piece
     * @param moves The collection of possible moves for this piece
     * @param rowIncrement The increment with which we are moving the column value to find a new move
     * @param colIncrement The increment with which we are moving the column value to find a new move
     */
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
                break;
            } else {
                break;
            }
        }
    }

    /**
     * Helps calculate all of the straight moves in a specific direction for rooks and queens.
     * 
     * @param board The chess board we are working with
     * @param myPosition The current position of piece
     * @param moves The collection of possible moves for this piece
     * @param rowIncrement The increment with which we are moving the column value to find a new move
     * @param colIncrement The increment with which we are moving the column value to find a new move
     */
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
                break;
            } else {
                break;
            }
        }
    }

    /**
     * Helps calculate if there are any possible captures for a specific pawn to make and adds to moves. 
     * 
     * @param board The chess board we are working with
     * @param myPosition The current position of piece
     * @param newRow The row of the possible new position
     * @param newCol The column of the possible new position
     * @param moves The collection of possible moves for this piece
     */
    private void addPawnCapture(ChessBoard board, ChessPosition myPosition, int newRow, int newCol, Collection<ChessMove> moves) {
        if(isValidPosition(newRow, newCol)) {
            ChessPosition capturePosition = new ChessPosition(newRow, newCol);
            ChessPiece targetPiece = board.getPiece(capturePosition);
            if(targetPiece != null && targetPiece.getTeamColor() != this.pieceColor) {
                if(newRow == 1 || newRow == 8) {
                    moves.add(new ChessMove(myPosition, capturePosition, ChessPiece.PieceType.QUEEN));
                    moves.add(new ChessMove(myPosition, capturePosition, ChessPiece.PieceType.BISHOP));
                    moves.add(new ChessMove(myPosition, capturePosition, ChessPiece.PieceType.KNIGHT));
                    moves.add(new ChessMove(myPosition, capturePosition, ChessPiece.PieceType.ROOK));
                } else {
                    moves.add(new ChessMove(myPosition, capturePosition, null));
                }
            }
        }
    }

    /**
     * Takes a row and a column value for a position and returns if it is a valid position. 
     * 
     * @param row Row value of a certain position. 
     * @param col Column value of a certain position. 
     * @return Bool if valid position or not.
     */
    private boolean  isValidPosition(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

}
