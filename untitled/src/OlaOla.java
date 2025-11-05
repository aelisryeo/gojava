import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 게임 메인 클래스: JFrame, JPanel 설정, 게임 루프(Timer), 키 입력, 그리기 담당
 */
public class OlaOla extends JPanel implements ActionListener, KeyListener {

    // 게임 상수
    private final int GAME_WIDTH = 800;
    private final int GAME_HEIGHT = 600;

    private final int STAIR_WIDTH = 100;  // 계단 너비
    private final int STAIR_HEIGHT = 20;  // 계단 높이
    private final int STAIR_GAP = 50;     // 계단 간 수직 간격 (캐릭터 한 칸 이동 거리)
    private final int INITIAL_STAIR_COUNT = 10; // 초기 생성 계단 개수

    private final int PLAYER_Y_POSITION = GAME_HEIGHT - 100;
    private int LIFE = 3;
    // 게임 객체
    private Character player;
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
    //private final int CHANGE_FREQUENCY = 5; //키를 강제로 전환해야 하는 빈도

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

    // 생성자: 게임의 모든 것을 초기화합니다.
    public OlaOla() {
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
        // 캐릭터 객체 생성
        // 초기 위치는 화면 중앙 하단에 가깝게 설정

// Character 객체 생성 시, 파일명만 전달합니다. (경로는 Character.java에서 처리)
        player = new Character(
                (GAME_WIDTH / 2) - 32,
                PLAYER_Y_POSITION,
                "character.png" // "res/" 접두어를 제거하고 파일명만 남깁니다.
        );

        // 초기 계단 생성
        initializeStairs();
        updateDirectionKey();

    }


    private void initializeStairs() {
        int currentX = player.getX() - (STAIR_WIDTH / 2) + (player.getWidth() / 2);
        int currentY = GAME_HEIGHT - 40;

        stairs.add(new StairInfo(currentX, currentY, false, false, ObstacleType.NONE, ItemType.NONE));

        for (int i = 0; i < INITIAL_STAIR_COUNT + 5; i++) {
            generateNewStair();
        }
    }

    private void generateNewStair() {
        if (stairs.isEmpty()) return;

        StairInfo lastStair = stairs.get(stairs.size() - 1);
        int newY = lastStair.bounds.y - STAIR_GAP;

        int expectedX = lastStair.isLeftDirection? lastStair.bounds.x - STAIR_WIDTH : lastStair.bounds.x + STAIR_WIDTH;

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
        if (newX + STAIR_WIDTH > GAME_WIDTH) newX = GAME_WIDTH - STAIR_WIDTH - 50 ;

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

        remainTime -= GAME_TICK_MS;

        if (remainTime <= 0) {
            isGameOver = true;
            System.out.println("Game Over");
        }

    }

    // --- 7. 캐릭터 이동 및 계단 체크 로직 ---
    private void playerClimb() {
        if (isGameOver || stairs.size() < 2) return;

        // 1. 다음 목표 계단 (현재 캐릭터가 밟고 있는 계단은 0번이 아닐 수 있음. 가장 아래 계단이 목표)
        StairInfo currentStair = stairs.get(0);
        StairInfo nextStair = stairs.get(1);
        StairInfo nnextStair = stairs.get(2);


        // ⭐ 1. 방향 전환 요구사항 체크 ⭐
        // 현재 계단에서 다음 계단으로 갈 때 방향이 바뀌는지 확인
        // 즉, stairs[0]의 isLeftDirection과 stairs[1]의 isLeftDirection이 다르면 꺾이는 지점
        if (nnextStair.isLeftDirection != nextStair.isLeftDirection) {
            // 방향이 바뀔 때만 키 전환을 요구합니다.
            requiresDirectionChange = true;
            // 방향 전환이 요구될 때 새로운 키를 할당합니다.
            // (이 키를 눌러야 다음 계단으로 이동하며, isPlayerFacingLeft도 반전됩니다.)
            updateDirectionKey();
        } else {
            // 방향이 바뀌지 않으면 전환 요구를 해제합니다.
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
            // 다음 위치의 중심이 목표 계단의 시작점보다 작거나
                nextPlayerCenterX < nextStair.bounds.x ||
                        // 다음 위치의 중심이 목표 계단의 끝점보다 크다면, 즉 계단 밖이라면
                        nextPlayerCenterX > nextStair.bounds.x + nextStair.bounds.width
        ) {
            System.out.println("게임 오버! 계단 경로를 벗어났습니다.");
            isGameOver = true;
            return;
        }
        // 2. 충돌(게임 오버) 체크
        // ⭐ 3. 방향 오류 체크 (다음 계단의 방향과 캐릭터의 방향이 일치하는지 확인) ⭐
        if (nextStair.isLeftDirection != isPlayerFacingLeft) {
            System.out.println("게임 오버! 잘못된 방향으로 올랐습니다.");
            isGameOver = true;
            return;
        }

        player.setX(nextPlayerX);

        stairs.remove(0);

        // 3. 게임 월드 스크롤 (캐릭터를 이동시키는 대신 계단 전체를 아래로 내립니다.)
        for (StairInfo stair : stairs) {
            stair.bounds.y += STAIR_GAP; // 모든 계단을 STAIR_GAP만큼 내립니다.
        }

        // 4. 새로운 계단 생성
        generateNewStair();

        // 5. 점수 증가
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
            // 게임 오버 상태에서 재시작 로직 등을 추가할 수 있습니다.
            return;
        }

        String pressedKey = KeyEvent.getKeyText(e.getKeyCode()).toUpperCase();

        if (pressedKey.equals(currentDirectionKey)) {
                isPlayerFacingLeft = !isPlayerFacingLeft;
                requiresDirectionChange = false;
                System.out.println("방향 전환 성공: "+ currentDirectionKey);

        }

        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            playerClimb();
        }

        // 캐릭터의 방향에 따라 x 위치를 조정하여 방향 전환을 시각적으로 보여줄 수 있습니다.
        // 여기서는 단순함을 위해 생략하고, 다음 단계에서 시각적 처리를 추가할 수 있습니다.

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
        Graphics2D g2d = (Graphics2D)g.create();
        if (player.getImage() != null) {

            // 반전이 필요한 경우
            if (isPlayerFacingLeft) {
                // (1) X축 기준으로 반전 변환을 적용합니다. (scale(-1, 1))
                g2d.scale(-1, 1);

                // (2) 반전된 후의 좌표계에서 캐릭터를 그립니다.
                //     - X 좌표를 음수로 설정하여 원래 위치로 되돌립니다.
                //     - X 좌표 = -(플레이어 실제 X 좌표 + 플레이어 너비)
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
                // 반전이 필요 없는 경우: 일반 그리기
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

        // g2d 사용을 마쳤으므로 dispose() 호출 (필수)
        g2d.dispose();
        // 2. 캐릭터 그리기
        //player.draw(g, this);

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
        // 타이머 이벤트 시 로직 업데이트 및 화면 재그리기
        updateGameLogic();
        repaint();
    }

    // 사용하지 않는 KeyListener 메소드
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

    // --- 10. 메인 메소드: 프로그램 실행 ---
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("올라올라");
            OlaOla gamePanel = new OlaOla();

            frame.add(gamePanel);
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

            // 키 입력이 작동하도록 게임 패널에 포커스를 줍니다.
            gamePanel.requestFocusInWindow();
        });
    }
} 