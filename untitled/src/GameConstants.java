public interface GameConstants {
    // 게임 상수
    int GAME_WIDTH = 800;
    int GAME_HEIGHT = 600;

    int STAIR_WIDTH = 100;
    int STAIR_HEIGHT = 20;
    int STAIR_GAP = 50;
    int INITIAL_STAIR_COUNT = 10;

    int PLAYER_Y_POSITION = GAME_HEIGHT - 100;
    int LIFE = 3;

    int GAME_TICK_MS = 16;
    double INITIAL_STAIR_TIME = 1000.0;
    double MIN_TIME_PER_STAIR = 500.0;
    double TIME_REDUCTION = 200.0;
    int DIFFICULTY = 20;

    String[] DIRECTION_KEYS = {"A", "S", "D", "F", "H", "J", "K", "L"};

    String[] LEFT_HAND_KEYS = {"A", "S", "D", "F"};
    String[] RIGHT_HAND_KEYS = {"H", "J", "K", "L"};
}