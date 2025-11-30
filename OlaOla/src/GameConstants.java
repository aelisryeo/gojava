public interface GameConstants {
    int GAME_WIDTH = 900;
    int GAME_HEIGHT = 700;

    int STAIR_WIDTH = 120;
    int STAIR_HEIGHT = 30;
    int STAIR_GAP = 70;
    int INITIAL_STAIR_COUNT = 10;

    int PLAYER_Y_POSITION = GAME_HEIGHT - 175;
    int LIFE = 3;

    int PLAYER_HEIGHT = 120;
    int PLAYER_WIDTH = 120;

    int GAME_TICK_MS = 16;
    double INITIAL_STAIR_TIME = 2000.0;
    double MIN_TIME_PER_STAIR = 500.0;
    double TIME_REDUCTION = 200.0;
    int DIFFICULTY = 20;

    int ITEM_HEIGHT = (STAIR_WIDTH / 4) * 4;

    String[] DIRECTION_KEYS = {"Q", "W", "E", "R", "T", "Y", "U", "I",
            "O", "P", "A", "S", "D", "F", "H", "J", "K", "L", "Z", "X", "C", "V", "B", "N", "M"};

    String[] LEFT_HAND_KEYS = {"Q", "W", "E", "R", "T", "A", "S", "D", "F", "Z", "X", "C", "V", "B"};
    String[] RIGHT_HAND_KEYS = {"Y", "U", "I", "O", "P", "H", "J", "K", "L", "N", "M"};

    static final double BASE_OBSTACLE_SPAWN_CHANCE = 0.3;
}