import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestIsComing extends JPanel implements ActionListener, KeyListener, GameConstants {

    private final int PLAYER_Y_POSITION = GAME_HEIGHT - 350;

    private Character player;
    private int playerStairIndex = 0;

    private Character chaser;
    private final int CHASER_MOVE_INTERVAL = 800;
    private final int CHASER_INTERVAL_REDUCTION = 70;
    private final int CHASER_MIN_INTERVAL = 20;
    private int currentChaserInterval;
    private int chaserMoveTimer = 0;
    private int chaserStairIndex = 0;
    private final int CHASER_OFFSET_X = 20;

    private List<StairInfo> stairs = new ArrayList<>();
    private boolean isGameOver = false;
    private int score = 0;

    private Timer loopTimer;

    private double timePerStair;
    private final double INITIAL_STAIR_TIME = 3000.0;
    private final double MIN_TIME_PER_STAIR = 250.0;
    private final double TIME_REDUCTION = 100.0;
    private double remainTime;

    private String currentDirectionKey;
    private Random random = new Random();

    private boolean requiresDirectionChange = false;

    private boolean isPlayerFacingLeft = false;
    private boolean isChaserFacingLeft = false; // 추격자 방향
    private BufferedImage TBimage;

    private BufferedImage[] charAnim;
    private int currentCharFrame = 0;

    private int playerX = 368;
    private Timer playerTimer;
    private static final int PLAYER_ANIMATION_FRAMES = 3;
    private static final int ANIMATION_DELAY_MS = 150;

    private GameLauncher launcher;


    public TestIsComing(GameLauncher launcher, CharacterSelect selectedCharacter) {
        this.launcher = launcher;

        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        this.timePerStair = INITIAL_STAIR_TIME;
        this.remainTime = this.timePerStair;

        this.currentChaserInterval = CHASER_MOVE_INTERVAL;

        loopTimer = new Timer(GAME_TICK_MS, this);
        loopTimer.start();

        /*
        player = new Character(
                (GAME_WIDTH / 2) - 32,
                PLAYER_Y_POSITION,
                characterImagePath
        );

         */

        chaser = new Character(
                playerX + CHASER_OFFSET_X,
                PLAYER_Y_POSITION + 150, // 플레이어보다 아래
                "image/chaser.png"
        );

        String[] paths = selectedCharacter.getImagePath();
        charAnim = new BufferedImage[paths.length];
        try {
            for (int i = 0; i < paths.length; i++) {
                // 경로가 "/image/" 형태라면 수정해야 합니다. (이전 문제 해결 시 사용했던 경로 사용)
                charAnim[i] = ImageIO.read(getClass().getResourceAsStream(paths[i]));
                if (charAnim[i] == null) {
                    System.err.println("❌ 캐릭터 애니메이션 이미지 로드 실패: " + paths[i]);
                }
            }
        } catch (Exception e) {
            System.err.println("캐릭터 애니메이션 이미지 로드 중 예외 발생");
            e.printStackTrace();
        }
        playerTimer = new Timer(ANIMATION_DELAY_MS, this);
        playerTimer.start();

        try {
            // [수정 제안]
            TBimage = ImageIO.read(getClass().getResourceAsStream("image/testBackground.png"));

            if (TBimage == null) {
                System.err.println("배경 이미지 로드 실패: testBackground.png 파일을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            System.err.println("배경 이미지 로드 중 예외 발생");
            e.printStackTrace();
        }

        initializeStairs();
        updateDirectionKey();
    }


    private void initializeStairs() {
        int currentX = playerX - (STAIR_WIDTH / 2) + (PLAYER_WIDTH / 2);
        int currentY =  PLAYER_Y_POSITION + PLAYER_HEIGHT - STAIR_HEIGHT;

        stairs.add(new StairInfo(currentX, currentY, STAIR_WIDTH, STAIR_HEIGHT, false, false, ObstacleType.NONE, ItemType.NONE));

        // ⭐ 2. 다음 3개 계단을 오른쪽으로 강제 직진 생성 (핵심 수정) ⭐
        for (int i = 0; i < 1; i++) {
            StairInfo lastStair = stairs.get(stairs.size() - 1);

            int newY = lastStair.bounds.y - STAIR_GAP;

            // ⭐ X 좌표는 이전 계단보다 STAIR_WIDTH 만큼 오른쪽으로 이동하도록 고정 ⭐
            int newX = lastStair.bounds.x + STAIR_WIDTH;

            // isLeftDirection=false (오른쪽으로), isTurn=false (꺾임 요구 없음)
            stairs.add(new StairInfo(newX, newY, STAIR_WIDTH, STAIR_HEIGHT, false, false, ObstacleType.NONE, ItemType.NONE));
        }

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
    }

    private void updateGameLogic() {
        if (isGameOver) {
            if (loopTimer.isRunning()) {
                loopTimer.stop();
            }

            if (launcher != null) {
                launcher.showGameOverPanel(score);
            }
            return;
        }

        // --- 추격자 로직 ---
        chaserMoveTimer += GAME_TICK_MS;
        while (chaserMoveTimer >= currentChaserInterval) {
            if(chaserStairIndex < playerStairIndex) {
                chaserClimb();
            }
            chaserMoveTimer -= currentChaserInterval;
            if (isGameOver) return;
        }
    }

    private void chaserClimb() {
        if (chaserStairIndex >= stairs.size() - 1) {
            return;
        }

        StairInfo nextStair = stairs.get(chaserStairIndex + 1);

        isChaserFacingLeft = nextStair.isLeftDirection;

        int targetX = nextStair.bounds.x + (STAIR_WIDTH / 2) - (chaser.getWidth() / 2);
        int targetY = nextStair.bounds.y - chaser.getHeight();

        chaser.setX(targetX);
        chaser.setY(targetY);
        chaserStairIndex++;

        if (chaserStairIndex >= playerStairIndex) {
            isGameOver = true;
            System.out.println("게임 오버! 추격자에게 잡혔습니다.");
        }
    }

    // --- 7. 캐릭터 이동 및 계단 체크 로직 ---
    private void playerClimb() {
        if (isGameOver || stairs.size() < playerStairIndex + 2) return;

        StairInfo targetStair = stairs.get(playerStairIndex + 1); // 밟을 계단

        // 방향 체크
        if (targetStair.isLeftDirection != isPlayerFacingLeft) {
            System.out.println("게임 오버! 잘못된 방향으로 올랐습니다.");
            isGameOver = true;
            return;
        }

        // 이동 확정: 다음 계단(targetStair)의 중앙에 스냅
        int targetX = targetStair.bounds.x + (STAIR_WIDTH / 2) - (PLAYER_WIDTH / 2);
        playerX = targetX;
        playerStairIndex++; // 플레이어 인덱스 증가

        // 방향 전환 키 요구 체크
        if (stairs.size() >= playerStairIndex + 2) {
            StairInfo currentStair = stairs.get(playerStairIndex);
            StairInfo nnextStair = stairs.get(playerStairIndex + 1);
            if (nnextStair.isLeftDirection != currentStair.isLeftDirection) {
                requiresDirectionChange = true;
                updateDirectionKey();
            } else {
                requiresDirectionChange = false;
            }
        } else {
            requiresDirectionChange = false;
        }

        // 스크롤 (계단과 추격자를 아래로 내림)
        for (StairInfo stair : stairs) {
            stair.bounds.y += STAIR_GAP;
        }
        chaser.setY(chaser.getY() + STAIR_GAP);

        // 화면 밖 계단 제거
        removeOldStairs();

        generateNewStair();
        score++;
        updateDifficulty();
        this.remainTime = this.timePerStair;
        playSound("climb");
    }

    private void playSound(String name) {
        try {
            java.net.URL soundURL = getClass().getResource("audio/"+name+".wav");

            if (soundURL == null) {
                System.err.println("❌ 사운드 파일 로드 실패: 경로를 확인하십시오.");
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);

            // 사운드를 한 번 재생합니다.
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("사운드 재생 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void removeOldStairs() {
        int removedCount = 0;
        while (!stairs.isEmpty() && stairs.get(0).bounds.y > GAME_HEIGHT) {
            stairs.remove(0);
            removedCount++;
        }

        if (removedCount > 0) {
            playerStairIndex = Math.max(0, playerStairIndex - removedCount);
            chaserStairIndex = Math.max(0, chaserStairIndex - removedCount);
        }
    }

    private void updateDifficulty() {
        if (score > 0 && score % DIFFICULTY == 0) {
            //double newTime = timePerStair - TIME_REDUCTION;
            //timePerStair = Math.max(newTime, MIN_TIME_PER_STAIR);

            int newInterval = currentChaserInterval - CHASER_INTERVAL_REDUCTION;
            currentChaserInterval = Math.max(newInterval,CHASER_MIN_INTERVAL);
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

        if (requiresDirectionChange) {
            if (pressedKey.equals(currentDirectionKey)) {
                isPlayerFacingLeft = !isPlayerFacingLeft;
                requiresDirectionChange = false;
                System.out.println("방향 전환 성공: " + currentDirectionKey);
                playerClimb();
            }
        }

        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (requiresDirectionChange) {
                System.out.println("게임 오버! 턴 키를 누르지 않았습니다.");
                isGameOver = true;
            } else {
                playerClimb();
            }
        }
        repaint();
    }

    // --- 9. 그리기 (랜더링) ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (TBimage != null) {
            // 패널 크기(GAME_WIDTH, GAME_HEIGHT)에 맞춰 이미지를 늘려 그립니다.
            g.drawImage(
                    TBimage,
                    0,
                    0,
                    GAME_WIDTH,
                    GAME_HEIGHT,
                    this
            );
        }

        // 1. 계단 그리기
        for (StairInfo stair : stairs) {
            g.setColor(Color.WHITE);
            if (stairs.indexOf(stair) == playerStairIndex) {
                g.setColor(Color.ORANGE);
            }
            if (stairs.indexOf(stair) == chaserStairIndex) {
                g.setColor(Color.RED);
            }
            g.fillRect(stair.bounds.x, stair.bounds.y, stair.bounds.width, stair.bounds.height);
        }

        Graphics2D g2d = (Graphics2D)g.create();

        BufferedImage currentCharImage = null;
        if (charAnim!=null && charAnim.length > 0) {
            currentCharImage = charAnim[currentCharFrame];
        }

        final int PLAYER_Y = PLAYER_Y_POSITION - 20;

        if (currentCharImage != null) {
            if (isPlayerFacingLeft) {
                g2d.scale(-1, 1);
                int flippedX = -(playerX + PLAYER_WIDTH);
                g2d.drawImage(
                        currentCharImage,
                        flippedX,
                        PLAYER_Y,
                        PLAYER_WIDTH,
                        PLAYER_HEIGHT,
                        this
                );
            } else {
                g2d.drawImage(
                        currentCharImage,
                        playerX,
                        PLAYER_Y,
                        PLAYER_WIDTH,
                        PLAYER_HEIGHT,
                        this
                );
            }
        }

        g2d.dispose(); // g2d 사용 후 반납

        drawCharacter(g, chaser, chaser.getX(), chaser.getY(), isChaserFacingLeft);

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

    }

    // 캐릭터 그리기 헬퍼 (좌우 반전 포함)
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
        if (e.getSource() == loopTimer) {
            updateGameLogic();
            repaint();
        }
        if (e.getSource() == playerTimer) {
            currentCharFrame = (currentCharFrame + 1) % PLAYER_ANIMATION_FRAMES;

            // 2. ⭐️ 캐릭터 프레임 변경
            if (charAnim != null && charAnim.length > 0) {
                currentCharFrame = (currentCharFrame + 1) % charAnim.length;
            }

            repaint();
        }
    }

    // 사용하지 않는 KeyListener 메소드
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}