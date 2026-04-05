public class GameLogic {
    private final char[] board = new char[9];
    private char currentTurn = 'X';

    public GameLogic() {
        for (int i = 0; i < board.length; i++) {
            board[i] = ' ';
        }
    }

    public synchronized char getCurrentTurn() {
        return currentTurn;
    }

    public synchronized boolean makeMove(char symbol, int index) {
        if (index < 0 || index > 8) {
            return false;
        }
        if (symbol != currentTurn) {
            return false;
        }
        if (board[index] != ' ') {
            return false;
        }

        board[index] = symbol;
        currentTurn = (currentTurn == 'X') ? 'O' : 'X';
        return true;
    }

    public synchronized char checkWinner() {
        int[][] lines = {
            {0, 1, 2}, // rows
            {3, 4, 5},
            {6, 7, 8},
            {0, 3, 6}, // columns
            {1, 4, 7},
            {2, 5, 8},
            {0, 4, 8}, // diagonals
            {2, 4, 6}
        };

        for (int[] line : lines) {
            char a = board[line[0]];
            char b = board[line[1]];
            char c = board[line[2]];

            if (a != ' ' && a == b && b == c) {
                return a;
            }
        }

        return ' ';
    }

    public synchronized boolean isDraw() {
        if (checkWinner() != ' ') {
            return false;
        }

        for (char cell : board) {
            if (cell == ' ') {
                return false;
            }
        }

        return true;
    }

    // Helpful for debugging or logging board state.
    public synchronized String boardAsString() {
        return new String(board);
    }
}
