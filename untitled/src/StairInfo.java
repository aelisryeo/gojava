import java.awt.Rectangle;

public class StairInfo {
    public Rectangle bounds;
    public boolean isLeftDirection;
    public boolean isTurnPoint;
    public ObstacleType obstacle;
    public ItemType item;

    // 생성자 수정: GameConstants 대신 너비와 높이를 직접 받음
    public StairInfo(int x, int y, int width, int height, boolean isLeft, boolean isTurn, ObstacleType obstacle, ItemType item) {
        this.bounds = new Rectangle(x, y, width, height);
        this.isLeftDirection = isLeft;
        this.isTurnPoint = isTurn;
        this.obstacle = obstacle;
        this.item = item;
    }
}