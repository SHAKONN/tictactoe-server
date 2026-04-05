import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final RoomManager roomManager;

    private BufferedReader in;
    private PrintWriter out;

    private String username;
    private Room room;
    private char symbol; // 'X' or 'O'
    private volatile boolean running = true;

    public ClientHandler(Socket socket, RoomManager roomManager) {
        this.socket = socket;
        this.roomManager = roomManager;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            send("INFO|Connected to Tic-Tac-Toe server");

            String line;
            while (running && (line = in.readLine()) != null) {
                handleCommand(line.trim());
            }
        } catch (IOException e) {
            System.out.println("Client connection error: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void handleCommand(String commandLine) {
        if (commandLine.isEmpty()) {
            send("ERROR|Empty command");
            return;
        }

        String[] parts = commandLine.split("\\|");
        String command = parts[0];

        switch (command) {
            case "CREATE_ROOM":
                // CREATE_ROOM|username
                if (parts.length < 2) {
                    send("ERROR|Usage: CREATE_ROOM|username");
                    return;
                }
                if (room != null) {
                    send("ERROR|You are already in a room");
                    return;
                }

                username = parts[1];
                room = roomManager.createRoom(this, username);
                symbol = 'X';

                send("ROOM_CREATED|" + room.getCode() + "|X");
                break;

            case "JOIN_ROOM":
                // JOIN_ROOM|roomCode|username
                if (parts.length < 3) {
                    send("ERROR|Usage: JOIN_ROOM|roomCode|username");
                    return;
                }
                if (room != null) {
                    send("ERROR|You are already in a room");
                    return;
                }

                String roomCode = parts[1];
                String joinUsername = parts[2];

                Room joinedRoom = roomManager.joinRoom(roomCode, this, joinUsername);
                if (joinedRoom == null) {
                    send("ERROR|Room not found or full");
                    return;
                }

                this.room = joinedRoom;
                this.username = joinUsername;
                this.symbol = 'O';

                send("JOIN_SUCCESS|O");
                joinedRoom.startGameIfReady();
                break;

            case "MOVE":
                // MOVE|index (0..8)
                if (parts.length < 2) {
                    send("ERROR|Usage: MOVE|index");
                    return;
                }
                if (room == null) {
                    send("ERROR|You are not in a room");
                    return;
                }

                try {
                    int index = Integer.parseInt(parts[1]);
                    room.handleMove(this, index);
                } catch (NumberFormatException e) {
                    send("ERROR|Invalid move index");
                }
                break;

            case "QUIT":
                send("INFO|Goodbye");
                running = false;
                disconnect();
                break;

            default:
                send("ERROR|Unknown command");
        }
    }

    public synchronized void send(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public synchronized void disconnect() {
        if (!running && socket.isClosed()) {
            return;
        }

        running = false;

        // If this client is in a room, notify the room.
        if (room != null) {
            Room currentRoom = room;
            room = null;
            currentRoom.handleDisconnect(this);
        }

        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    public String getUsername() {
        return username;
    }

    public char getSymbol() {
        return symbol;
    }
}
