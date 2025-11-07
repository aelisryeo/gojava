import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 게임 메인 클래스: JFrame, JPanel 설정, 게임 루프(Timer), 키 입력, 그리기 담당
 */
public class TestIsComing extends JPanel implements ActionListener, KeyListener {

    // 게임 상수
    private final int GAME_WIDTH = 800;
    private final int GAME_HEIGHT = 600;

    private final int STAIR_WIDTH = 100;  // 계단 너비
    private final int STAIR_HEIGHT = 20;  // 계단 높이
    private final int STAIR_GAP = 50;     // 계단 간 수직 간격 (캐릭터 한 칸 이동 거리)
    private final int INITIAL_STAIR_COUNT = 10; // 초기 생성 계단 개수

    private final int PLAYER_Y_POSITION = GAME_HEIGHT - 300;
    private int LIFE = 3;
    // 게임 객체
    private Character player;
    private int playerStairIndex = 0;

    private Character chaser;
    private final int CHASER_MOVE_INTERVAL = 700;
    private int chaserMoveTimer = 0;
    private int chaserStairIndex = 0;
    //private final double CHASER_SPEED = 0.05;
    private final int CHASER_OFFSET_X = 20;
    private double chaserY;

    private List<StairInfo> stairs = new ArrayList<>(); // 계단을 관리하는 리스트

    private boolean isGameOver = false;
    private int score = 0;

    private Timer loopTimer;
    private final int GAME_TICK_MS = 16;
    private double timePerStair;
    private final double INITIAL_STAIR_TIME = 3000.0;
    private final double MIN_TIME_PER_STAIR = 250.0;
    private final double TIME_REDUCTION = 100.0;
    private final int DIFFICULTY = 20;
    private double remainTime;
    //;인식잘안되길래 일단 h로 넣어둠
    private final String[] DIRECTION_KEYS = {"A", "S", "D", "F", "H", "J", "K", "L"};
    private String currentDirectionKey;
    private Random random = new Random();

    private boolean requiresDirectionChange = false;

    private class StairInfo {
        Rectangle bounds;
        boolean isLeftDirection;
        boolean isTurnPoint;
        ObstacleType obstacle;
        ItemType item;

        public StairInfo(int x, int y, boolean isLeft, boolean isTurn, ObstacleType obstacle, ItemType item) {
            this.bounds = new Rectangle(x, y, STAIR_WIDTH, STAIR_HEIGHT);
            this.isLeftDirection = isLeft;
            this.isTurnPoint = isTurn;
            this.obstacle = obstacle;
            this.item = item;
        }
    }

    public enum ObstacleType {
        PROFESSOR,
        MUSHROOM,
        STUDENT,
        BED,
        NONE
    }

    public enum ItemType {
        TEST,
        CLOCK,
        HEART,
        NONE
    }

    private boolean isPlayerFacingLeft = false;
    private boolean isChaserFacingLeft = false;

    // 생성자: 게임의 모든 것을 초기화합니다.
    public TestIsComing() {

        // JPanel 기본 설정
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setBackground(Color.BLACK);

        // 키 입력 리스너 설정
        setFocusable(true);
        addKeyListener(this);

        this.timePerStair = INITIAL_STAIR_TIME;
        this.remainTime = this.timePerStair;

        loopTimer = new Timer(GAME_TICK_MS, this);
        loopTimer.start();

        int initialPlayerY = GAME_HEIGHT - 300;

// Character 객체 생성 시, 파일명만 전달합니다.
        player = new Character(
                (GAME_WIDTH / 2) - 32,
                initialPlayerY,
                "character.png" // "res/" 접두어를 제거하고 파일명만 남깁니다.
        );

        this.chaserY = initialPlayerY + 150;
        chaser = new Character(
                player.getX() + CHASER_OFFSET_X,
                (int)this.chaserY,
                "chaser.png"
        );
        // 초기 계단 생성
        initializeStairs();

        StairInfo initialStair = stairs.get(chaserStairIndex);

        updateDirectionKey();

    }


    private void initializeStairs() {
        int currentX = player.getX() - (STAIR_WIDTH / 2) + (player.getWidth() / 2);
        int currentY =  player.getY() + player.getHeight() - STAIR_HEIGHT; //GAME_HEIGHT - 40;

        stairs.add(new StairInfo(currentX, currentY, false, false, ObstacleType.NONE, ItemType.NONE));

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

        // 2. 꺾이는 계단일지 결정 (40% 확률 유지)
        //    만약 직진이 불가능하면, 확률과 상관없이 무조건 꺾이게 만듭니다.
        boolean isTurn = cannotGoStraight || (random.nextDouble() < 0.4);
        // 1. 다음 계단이 '꺾이는' 계단일지 결정 (40% 확률로 설정하셨으므로 유지)

        int newX;
        boolean nextIsLeft = lastStair.isLeftDirection;

        // ⭐ 2. 꺾이는 계단 생성 로직 ⭐
        if (isTurn) {
            // 꺾임 지점은 현재 방향의 반대로 이동을 시도합니다.

            // 꺾임 시 예상되는 X 좌표 계산
            int targetLeftX = lastStair.bounds.x - STAIR_WIDTH;
            int targetRightX = lastStair.bounds.x + STAIR_WIDTH;

            // 경계 확인 후 최종 방향 결정
            if (targetLeftX <= 50) {
                // 왼쪽 경계 초과: 무조건 오른쪽으로 꺾어야 함
                nextIsLeft = false;
            } else if (targetRightX + STAIR_WIDTH >= GAME_WIDTH - 50) {
                // 오른쪽 경계 초과: 무조건 왼쪽으로 꺾어야 함
                nextIsLeft = true;
            } else {
                // 양쪽 모두 가능: 랜덤하게 선택
                nextIsLeft = random.nextBoolean();
            }

            // 최종 X 좌표 계산
            newX = nextIsLeft ? targetLeftX : targetRightX;

        } else {
            // 3. ⭐ 직진 계단 생성 로직 (X좌표 이동) ⭐
            newX = expectedX;
            nextIsLeft = lastStair.isLeftDirection;
            // isTurn은 false로 유지됩니다.
        }

        // 4. 최종 안전 장치 (경계 확인)
        if (newX < 0) newX = 50;
        if (newX + STAIR_WIDTH > GAME_WIDTH) newX = GAME_WIDTH - STAIR_WIDTH - 50;

        ObstacleType newObstacle = ObstacleType.NONE;
        ItemType newItem = ItemType.NONE;

        //TODO: newItem이랑 ObstacleType 랜덤선택 만들기 (가중치 설정해서 랜덤뽑기 ㄱㄱ)

        // 5. StairInfo 생성 및 추가
        stairs.add(new StairInfo(newX, newY, nextIsLeft, isTurn, newObstacle, newItem));

        // (생략: 리스트 관리 로직)
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

        chaserMoveTimer += GAME_TICK_MS;
        while (chaserMoveTimer >= CHASER_MOVE_INTERVAL) {
            if(chaserStairIndex < stairs.size() - 1) {
                chaserClimb();
            }
            chaserMoveTimer -= CHASER_MOVE_INTERVAL;
            if (isGameOver) return;
        }

        remainTime -= GAME_TICK_MS;

        if (remainTime <= 0) {
            isGameOver = true;
            System.out.println("Game Over");
        }

    }
    private void chaserClimb() {
        if (chaserStairIndex >= stairs.size() -1 ) {
            chaser.setY(chaser.getY() + 20);
            return;
        }
        if (stairs.size() <= chaserStairIndex + 1) return;
        // 1. 다음 목표 계단
        StairInfo nextStair = stairs.get(chaserStairIndex + 1);

        // 2. ⭐ 추격자 위치 스냅 ⭐
        int targetX = nextStair.bounds.x + (STAIR_WIDTH / 2) - (chaser.getWidth() / 2);
        int targetY = nextStair.bounds.y - chaser.getHeight();

        chaser.setX(targetX);
        chaser.setY(targetY); // Chaser 객체의 Y 좌표 업데이트
        chaserStairIndex++;

        // 3. ⭐ 플레이어와의 충돌 감지 ⭐
        // 추격자의 Y 위치가 플레이어의 Y 위치보다 위에 있거나 같으면 충돌
        if (chaser.getY() <= player.getY()) {
            isGameOver = true;
            System.out.println("게임 오버! 추격자에게 잡혔습니다.");
            return;
        }
    }

    // --- 7. 캐릭터 이동 및 계단 체크 로직 ---
    private void playerClimb() {

        if (isGameOver || stairs.size() < playerStairIndex + 2) return;
        StairInfo targetStair = stairs.get(playerStairIndex + 1);

        if (targetStair.isLeftDirection != isPlayerFacingLeft) {
            System.out.println("게임 오버! 잘못된 방향으로 올랐습니다.");
            isGameOver = true;
            return;
        }

        // 2. 이동 확정: 다음 계단(targetStair)의 중앙에 스냅
        int targetX = targetStair.bounds.x + (STAIR_WIDTH / 2) - (player.getWidth() / 2);
        player.setX(targetX);
        playerStairIndex++;

        // 3. 다음 턴에 방향 전환이 필요한지 판단
        // 새로 밟은 계단(현재 Index)의 '다음' 계단(Index + 1)을 확인합니다.
        if (stairs.size() >= playerStairIndex + 2) {
            StairInfo nnextStair = stairs.get(playerStairIndex + 1);
            if (nnextStair.isTurnPoint) {
                requiresDirectionChange = true;
                updateDirectionKey();
            } else {
                requiresDirectionChange = false;
            }
        } else {
            requiresDirectionChange = false;
        }

        // 4. 스크롤 및 생성
        for (StairInfo stair : stairs) {
            stair.bounds.y += STAIR_GAP;
        }
        chaser.setY(chaser.getY() + STAIR_GAP);

        // 5. 화면 밖으로 벗어난 계단 제거 (동시에 playerStairIndex도 정리해야 합니다.)
        removeOldStairs();

        generateNewStair();
        score++;
        updateDifficulty();
        this.remainTime = this.timePerStair;
    }


    private void removeOldStairs() {
        int removedCount = 0;
        while (!stairs.isEmpty() && stairs.get(0).bounds.y + stairs.get(0).bounds.height > GAME_HEIGHT) {
            stairs.remove(0);
            removedCount++;

            // ⭐⭐⭐ 핵심 수정: 밟고 지나간 계단이 제거되면 플레이어의 인덱스를 1 감소시켜 동기화합니다. ⭐⭐⭐
            /*if (playerStairIndex > 0) {
                playerStairIndex--;
            }
            // playerStairIndex가 0인 상태에서 제거되면, 플레이어는 여전히 새로 0번이 된 계단을 밟고 있는 것으로 간주합니다.
            if (chaserStairIndex > 0) {
                chaserStairIndex--;
            }
             */
            if (removedCount > 0) {
                playerStairIndex = Math.max(0, playerStairIndex - removedCount);
                chaserStairIndex = Math.max(0, chaserStairIndex - removedCount);
            }
        }
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
            // 게임 오버 상태에서 재시작 로직 등을 추가할 수 있습니다.
            return;
        }

        String pressedKey = KeyEvent.getKeyText(e.getKeyCode()).toUpperCase();

        if (pressedKey.equals(currentDirectionKey)) {
            isPlayerFacingLeft = !isPlayerFacingLeft;
            requiresDirectionChange = false;
            System.out.println("방향 전환 성공: " + currentDirectionKey);
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
            // 캐릭터가 밟아야 하는 첫 계단은 눈에 띄게 다른 색으로 칠합니다.
            if (stair == stairs.get(0)) {
                g.setColor(Color.ORANGE);
            } else {
                g.setColor(Color.WHITE);
            }
            g.fillRect(stair.bounds.x, stair.bounds.y, stair.bounds.width, stair.bounds.height);
        }

        drawCharacter(g,chaser, chaser.getX(), chaser.getY(), isChaserFacingLeft);
        drawCharacter(g, player, player.getX(), player.getY(), isPlayerFacingLeft);

        // 3. 점수 및 정보 표시
        g.setColor(Color.YELLOW);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Direction: " + (isPlayerFacingLeft ? "LEFT" : "RIGHT"), 10, 40);

        if (requiresDirectionChange) {
            g.setColor(Color.CYAN);
            g.setFont(new Font("SansSerif", Font.BOLD, 24));
            g.drawString("TURN KEY : [" + currentDirectionKey + "]", GAME_WIDTH - 200, 400);
        }

        g.setColor(Color.PINK);
        g.setFont(new Font("SansSerif", Font.BOLD, 24));
        g.drawString("시험기간이 쫓아온다", 400, 40);

        // 4. 게임 오버 메시지
        if (isGameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("SansSerif", Font.BOLD, 40));
            g.drawString("GAME OVER", GAME_WIDTH / 2 - 120, GAME_HEIGHT / 2);
        }

    }

    private void drawCharacter(Graphics g, Character character, int x, int y, boolean isFacingLeft) {
        if (character == null || character.getImage() == null) return;

        Graphics2D g2d = (Graphics2D) g.create();

        if (isFacingLeft) {
            g2d.scale(-1, 1);
            int flippedX = -(x + character.getWidth());

            g2d.drawImage(character.getImage(), flippedX, y, character.getWidth(), character.getHeight(), this);
        } else {
            g2d.drawImage(character.getImage(), x, y, character.getWidth(), character.getHeight(), this);
        }
        g2d.dispose();
    }

    // Timer 이벤트 처리 (게임 루프)
    @Override
    public void actionPerformed(ActionEvent e) {
        // 타이머 이벤트 시 로직 업데이트 및 화면 재그리기
        updateGameLogic();
        repaint();
    }

    // 사용하지 않는 KeyListener 메소드
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
