package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

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
        this.currentBoard.resetBoard();
        this.teamTurn = ChessGame.TeamColor.WHITE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(currentBoard, chessGame.currentBoard) && teamTurn == chessGame.teamTurn;
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(currentBoard);
        result = 31 * result + Objects.hashCode(teamTurn);
        return result;
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

    public void setChessBoard(ChessBoard board) {
        this.currentBoard = board;
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
            ChessPiece[][] tempBoard = copyBoard(currentBoard);
            try {
                currentBoard.makeMove(move, piece.getTeamColor());
                if(!isInCheck(piece.getTeamColor())) {
                    validMoves.add(move);
                }
            } catch (Exception e) {
                throw e;
            }
            currentBoard = new ChessBoard(tempBoard);
        }

        return validMoves;
    }

    public ChessPiece[][] copyBoard(ChessBoard currentBoard) {
        ChessPiece[][] newBoard = new ChessPiece[9][9];
    
        for(int i = 1; i <= 8; i++) {
            for(int j = 1; j <= 8; j++) {
                ChessPosition newPosition = new ChessPosition(i, j);
                newBoard[i][j] = this.currentBoard.getPiece(newPosition);
            }
        }
    
        return newBoard;
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
            throw new InvalidMoveException("Error: Its not this team's turn.");
        }

        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());
        System.out.println(move);
        System.out.println(validMoves);

        if (!validMoves.contains(move)) {
            System.out.println("Not in valid moves");
            throw new InvalidMoveException("Error: Invalid move!");
        } else {
            System.out.println("Somehow made it to valid moves");
            currentBoard.makeMove(move, this.teamTurn);
            this.teamTurn = (this.teamTurn == ChessGame.TeamColor.WHITE) ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = currentBoard.getKingPosition(teamColor);

        if(kingPosition == null) {
            return false;
        }
        
        for (PiecePositionPair pair : currentBoard.getBoardPieces()) {
            ChessPiece piece = pair.getPiece();
            ChessPosition position = pair.getPosition();
            if (piece.getTeamColor() != teamColor) {
                Collection<ChessMove> opponentMoves = piece.pieceMoves(currentBoard, position);
                for (ChessMove move : opponentMoves) {
                    if (move.getEndPosition().equals(kingPosition)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }

        for (PiecePositionPair pair : currentBoard.getBoardPieces(teamColor)) {
            ChessPosition position = pair.getPosition();

            Collection<ChessMove> validMoves = validMoves(position);
            for (ChessMove move : validMoves) {
                ChessPiece[][] tempBoard = copyBoard(currentBoard);
                try {
                    currentBoard.makeMove(move, teamColor);
                    if (!isInCheck(teamColor)) {
                        return false;
                    }
                } catch (Exception e) {
                }
                currentBoard = new ChessBoard(tempBoard);
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }

        for (PiecePositionPair pair : currentBoard.getBoardPieces(teamColor)) {
            ChessPosition position = pair.getPosition();
            if (!validMoves(position).isEmpty()) {
                return false;
            }
        }
        return true;
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
