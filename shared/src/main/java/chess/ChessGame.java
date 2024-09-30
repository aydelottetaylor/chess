package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard currentBoard;
    private TeamColor teamTurn;
    
    public ChessGame() {
        this.currentBoard = new ChessBoard();
        this.teamTurn = ChessGame.TeamColor.WHITE;
    }

    @Override
    public String toString() {
        return "ChessGame{}";
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return this.teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.teamTurn = team;        
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = currentBoard.getPiece(startPosition);

        if (piece == null) {
            return null;
        }

        Collection<ChessMove> possibleMoves = piece.pieceMoves(this.currentBoard, startPosition);

        Collection<ChessMove> validMoves = new ArrayList<>();
        for (ChessMove move : possibleMoves) {
            ChessBoard simulatedBoard = this.currentBoard;
            try {
                simulatedBoard.makeMove(move, this.teamTurn);
                if(!isInCheck(this.teamTurn)) {
                    validMoves.add(move);
                }
            } catch (Exception e) {

            }
        }

        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = currentBoard.getPiece(move.getStartPosition());
        if(piece == null || piece.getTeamColor() != this.teamTurn) {
            throw new InvalidMoveException("Its not this team's turn.");
        }

        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        if (!validMoves.contains(move)) {
            throw new InvalidMoveException("Invalid move!");
        }

        currentBoard.makeMove(move, this.teamTurn);

        this.teamTurn = (this.teamTurn == ChessGame.TeamColor.WHITE) ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        // ChessPosition kingPosition = currentBoard.getKingPosition(teamColor);

        // for (ChessPiece piece : currentBoard.getPieces()) {
        //     if (piece.getTeamColor() != teamColor) {
        //         Collection<ChessMove> opponentMoves = piece.pieceMoves(currentBoard, currentBoard.getPosition(piece));
        //         for (ChessMove move : opponentMoves) {
        //             if (move.getEndPosition().equals(kingPosition)) {
        //                 return true;
        //             }
        //         }
        //     }
        // }

        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.currentBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return this.currentBoard;
    }
}
