public interface GameContext {
    // 상태 변경 요청
    void changeState(GameState newState);

    // 게임 데이터 접근
    void decreaseLife();
    void decreaseScore(int amount);
    int getGameWidth();
    int getGameHeight();

    // 게임 타이머 제어
    void stopGameTimer();
    void startGameTimer();
}