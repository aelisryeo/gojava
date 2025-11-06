import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OlaOla extends JPanel implements ActionListener, KeyListener, GameConstants {

    // 게임 객체
    private Character player;
    private List<StairInfo> stairs = new ArrayList<>(); // 분리된 StairInfo 클래스 사용

    private boolean isGameOver = false;
    private int score = 0;

    private Timer loopTimer;
    private double timePerStair;
    private double remainTime;

    private String currentDirectionKey;
    private Random random = new Random();

    private boolean requiresDirectionChange = false;

    private boolean isPlayerFacingLeft = false;


    public OlaOla() {
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setBackground(Color.BLACK);

        // 키 입력 리스너 설정
        setFocusable(true);
        addKeyListener(this);

        this.timePerStair = INITIAL_STAIR_TIME;
        this.remainTime = this.timePerStair;

        loopTimer = new Timer(GAME_TICK_MS, this);
        loopTimer.start();

        player = new Character(
                (GAME_WIDTH / 2) - 32,
                PLAYER_Y_POSITION,
                "character.png"
        );

        // 초기 계단 생성
        initializeStairs();
        updateDirectionKey();
    }


    private void initializeStairs() {
        int currentX = player.getX() - (STAIR_WIDTH / 2) + (player.getWidth() / 2);
        int currentY = GAME_HEIGHT - 40;

        stairs.add(new StairInfo(currentX, currentY, STAIR_WIDTH, STAIR_HEIGHT, false, false, ObstacleType.NONE, ItemType.NONE));

        for (int i = 0; i < INITIAL_STAIR_COUNT + 5; i++) {
            generateNewStair();
        }
    }

    private void generateNewStair() {
        if (stairs.isEmpty()) return;

        StairInfo lastStair = stairs.get(stairs.size() - 1);
        int newY = lastStair.bounds.y - STAIR_GAP;

        int expectedX = lastStair.isLeftDirection ? lastStair.bounds.x - STAIR_WIDTH : lastStair.bounds.x + STAIR_WIDTH;

        boolean cannotGoStraight = (expectedX < 0) || (expectedX + STAIR_WIDTH > GAME_WIDTH);
        boolean isTurn = cannotGoStraight || (random.nextDouble() < 0.4);

        int newX;
        boolean nextIsLeft = lastStair.isLeftDirection;

        if (isTurn) {
            int targetLeftX = lastStair.bounds.x - STAIR_WIDTH;
            int targetRightX = lastStair.bounds.x + STAIR_WIDTH;

            if (targetLeftX <= 50) {
                nextIsLeft = false;
            } else if (targetRightX + STAIR_WIDTH >= GAME_WIDTH - 50) {
                nextIsLeft = true;
            } else {
                nextIsLeft = random.nextBoolean();
            }
            newX = nextIsLeft ? targetLeftX : targetRightX;

        } else {
            newX = expectedX;
            nextIsLeft = lastStair.isLeftDirection;
        }

        if (newX < 0) newX = 50;
        if (newX + STAIR_WIDTH > GAME_WIDTH) newX = GAME_WIDTH - STAIR_WIDTH - 50;

        ObstacleType newObstacle = ObstacleType.NONE;
        ItemType newItem = ItemType.NONE;

        stairs.add(new StairInfo(newX, newY, STAIR_WIDTH, STAIR_HEIGHT, nextIsLeft, isTurn, newObstacle, newItem));

        if (stairs.size() > INITIAL_STAIR_COUNT + 10) {
            stairs.remove(0);
        }
    }

    private void updateGameLogic() {
        if (isGameOver) {
            if (loopTimer.isRunning()) {
                loopTimer.stop();
            }
            return;
        }

        remainTime -= GAME_TICK_MS; // 상수 사용

        if (remainTime <= 0) {
            isGameOver = true;
            System.out.println("Game Over");
        }
    }

    // --- 7. 캐릭터 이동 및 계단 체크 로직 ---
    private void playerClimb() {
        if (isGameOver || stairs.size() < 2) return;

        StairInfo currentStair = stairs.get(0);
        StairInfo nextStair = stairs.get(1);
        StairInfo nnextStair = stairs.get(2);

        if (nnextStair.isLeftDirection != nextStair.isLeftDirection) {
            requiresDirectionChange = true;
            updateDirectionKey();
        } else {
            requiresDirectionChange = false;
        }

        int moveDistance = STAIR_WIDTH;
        int nextPlayerX;
        if (isPlayerFacingLeft) {
            nextPlayerX = player.getX() - moveDistance;
        } else {
            nextPlayerX = player.getX() + moveDistance;
        }

        int nextPlayerCenterX = nextPlayerX + (player.getWidth() / 2);
        if (
                nextPlayerCenterX < nextStair.bounds.x ||
                        nextPlayerCenterX > nextStair.bounds.x + nextStair.bounds.width
        ) {
            System.out.println("게임 오버! 계단 경로를 벗어났습니다.");
            isGameOver = true;
            return;
        }
        if (nextStair.isLeftDirection != isPlayerFacingLeft) {
            System.out.println("게임 오버! 잘못된 방향으로 올랐습니다.");
            isGameOver = true;
            return;
        }

        player.setX(nextPlayerX);
        stairs.remove(0);

        for (StairInfo stair : stairs) {
            stair.bounds.y += STAIR_GAP;
        }

        generateNewStair();
        score++;
        updateDifficulty();
        this.remainTime = this.timePerStair;
    }


    private void updateDifficulty() {
        if (score > 0 && score % DIFFICULTY == 0) {
            double newTime = timePerStair - TIME_REDUCTION;

            timePerStair = Math.max(newTime, MIN_TIME_PER_STAIR);
        }
    }

    private void updateDirectionKey() {
        int randomIndex = random.nextInt(DIRECTION_KEYS.length);
        currentDirectionKey = DIRECTION_KEYS[randomIndex];
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (isGameOver) {
            return;
        }

        String pressedKey = KeyEvent.getKeyText(e.getKeyCode()).toUpperCase();

        if (pressedKey.equals(currentDirectionKey)) {
            isPlayerFacingLeft = !isPlayerFacingLeft;
            requiresDirectionChange = false;
            System.out.println("방향 전환 성공: "+ currentDirectionKey);
            playerClimb();
        }

        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            playerClimb();
        }

        repaint();
    }

    // --- 9. 그리기 (랜더링) ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        //남은 시간 바
        if (!isGameOver) {
            double timePercent = Math.max(0, remainTime / timePerStair);

            if (timePercent > 0.5) {
                g.setColor(Color.green);
            } else if (timePercent > 0.25) {
                g.setColor(Color.yellow);
            } else {
                g.setColor(Color.red);
            }

            int barWidth = (int) (GAME_WIDTH * timePercent);
            g.fillRect(0, 0, barWidth, 15);
        }

        // 1. 계단 그리기
        for (StairInfo stair : stairs) {
            if (stair == stairs.get(0)) {
                g.setColor(Color.ORANGE);
            } else {
                g.setColor(Color.WHITE);
            }
            g.fillRect(stair.bounds.x, stair.bounds.y, stair.bounds.width, stair.bounds.height);
        }

        Graphics2D g2d = (Graphics2D)g.create();
        if (player.getImage() != null) {
            if (isPlayerFacingLeft) {
                g2d.scale(-1, 1);
                int flippedX = -(player.getX() + player.getWidth());
                g2d.drawImage(
                        player.getImage(),
                        flippedX,
                        player.getY(),
                        player.getWidth(),
                        player.getHeight(),
                        this
                );
            } else {
                g2d.drawImage(
                        player.getImage(),
                        player.getX(),
                        player.getY(),
                        player.getWidth(),
                        player.getHeight(),
                        this
                );
            }
        }
        g2d.dispose();

        // 3. 점수 및 정보 표시
        g.setColor(Color.YELLOW);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Direction: " + (isPlayerFacingLeft ? "LEFT" : "RIGHT"), 10, 40);

        if(requiresDirectionChange) {
            g.setColor(Color.CYAN);
            g.setFont(new Font("SansSerif", Font.BOLD, 24));
            g.drawString("TURN KEY : [" + currentDirectionKey + "]", GAME_WIDTH - 200, 400);
        }

        // 4. 게임 오버 메시지
        if (isGameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("SansSerif", Font.BOLD, 40));
            g.drawString("GAME OVER", GAME_WIDTH / 2 - 120, GAME_HEIGHT / 2);
        }
    }

    // Timer 이벤트 처리 (게임 루프)
    @Override
    public void actionPerformed(ActionEvent e) {
        updateGameLogic();
        repaint();
    }

    // 사용하지 않는 KeyListener 메소드
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

}