import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class RoomManager {
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public Room createRoom(ClientHandler playerX, String username) {
        String code = generateUniqueCode();
        Room room = new Room(code, this, playerX, username);
        rooms.put(code, room);

        System.out.println("Room created: " + code + " by " + username);
        return room;
    }

    public Room joinRoom(String code, ClientHandler playerO, String username) {
        Room room = rooms.get(code);
        if (room == null) {
            return null;
        }

        synchronized (room) {
            if (!room.hasFreeSlot()) {
                return null;
            }

            boolean added = room.addPlayerO(playerO, username);
            if (!added) {
                return null;
            }
        }

        System.out.println("Player " + username + " joined room " + code);
        return room;
    }

    public void removeRoom(String code) {
        Room removed = rooms.remove(code);
        if (removed != null) {
            System.out.println("Room removed: " + code);
        }
    }

    private String generateUniqueCode() {
        String code;
        do {
            // 4-digit room code from 1000 to 9999.
            code = String.valueOf(1000 + random.nextInt(9000));
        } while (rooms.containsKey(code));

        return code;
    }
}
