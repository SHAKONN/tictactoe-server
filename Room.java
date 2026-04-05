public class Room {
    private final String code;
    private final RoomManager roomManager;

    private ClientHandler playerX;
    private ClientHandler playerO;

    private String playerXName;
    private String playerOName;

    private final GameLogic gameLogic;
    private boolean gameStarted = false;
    private boolean gameFinished = false;

    public Room(String code, RoomManager roomManager, ClientHandler playerX, String playerXName) {
        this.code = code;
        this.roomManager = roomManager;
        this.playerX = playerX;
        this.playerXName = playerXName;
        this.gameLogic = new GameLogic();
    }

    public synchronized String getCode() {
        return code;
    }

    public synchronized boolean hasFreeSlot() {
        return playerO == null;
    }

    public synchronized boolean addPlayerO(ClientHandler playerO, String playerOName) {
        if (this.playerO != null) {
            return false;
        }
        this.playerO = playerO;
        this.playerOName = playerOName;
        return true;
    }

    public synchronized void startGameIfReady() {
        if (gameStarted || playerX == null || playerO == null) {
            return;
        }

        gameStarted = true;

        playerX.send("START_GAME|" + playerXName + "|" + playerOName);
        playerO.send("START_GAME|" + playerXName + "|" + playerOName);

        // X always moves first.
        playerX.send("YOUR_TURN");
        playerO.send("OPPONENT_TURN");
    }

    public synchronized void handleMove(ClientHandler player, int index) {
        if (!gameStarted) {
            player.send("ERROR|Game has not started yet");
            return;
        }
        if (gameFinished) {
            player.send("ERROR|Game already finished");
            return;
        }
        if (index < 0 || index > 8) {
            player.send("ERROR|Move index must be from 0 to 8");
            return;
        }

        char playerSymbol = player.getSymbol();
        if (playerSymbol != gameLogic.getCurrentTurn()) {
            player.send("ERROR|Not your turn");
            return;
        }

        boolean moveOk = gameLogic.makeMove(playerSymbol, index);
        if (!moveOk) {
            player.send("ERROR|Invalid move");
            return;
        }

        ClientHandler opponent = getOpponent(player);
        if (opponent == null) {
            player.send("ERROR|Opponent not connected");
            return;
        }

        player.send("MOVE_ACCEPTED|" + index);
        opponent.send("OPPONENT_MOVED|" + index);

        char winner = gameLogic.checkWinner();
        if (winner == 'X' || winner == 'O') {
            gameFinished = true;

            if (playerSymbol == winner) {
                player.send("WIN");
                opponent.send("LOSE");
            } else {
                player.send("LOSE");
                opponent.send("WIN");
            }

            roomManager.removeRoom(code);
            return;
        }

        if (gameLogic.isDraw()) {
            gameFinished = true;
            player.send("DRAW");
            opponent.send("DRAW");

            roomManager.removeRoom(code);
            return;
        }

        // Game continues: switch turn.
        player.send("OPPONENT_TURN");
        opponent.send("YOUR_TURN");
    }

    public synchronized void handleDisconnect(ClientHandler disconnectedPlayer) {
        if (gameFinished) {
            return;
        }

        gameFinished = true;

        ClientHandler opponent = getOpponent(disconnectedPlayer);
        if (opponent != null) {
            opponent.send("ERROR|Opponent disconnected");
            opponent.send("WIN");
        }

        roomManager.removeRoom(code);
    }

    private ClientHandler getOpponent(ClientHandler player) {
        if (player == playerX) {
            return playerO;
        } else if (player == playerO) {
            return playerX;
        }
        return null;
    }
}
