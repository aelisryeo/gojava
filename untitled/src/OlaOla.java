import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OlaOla extends JPanel implements ActionListener, KeyListener, GameConstants {

    private Character player;
    private List<StairInfo> stairs = new ArrayList<>();

    private boolean isGameOver = false;
    private int score = 0;

    private Timer loopTimer;
    private double timePerStair;
    private double remainTime;

    private double minigameTimer;
    private double maxMinigameTime;

    private String currentDirectionKey;
    private Random random = new Random();

    private boolean requiresDirectionChange = false;

    private ObstacleType pendingObstacle = ObstacleType.NONE;

    private boolean isPlayerFacingLeft = false;
    private int currentLife;

    private int totalStudentSpawnCount = 0;

    private enum GameState {
        CLIMBING,
        MINIGAME_STUDENT,
        MINIGAME_MUSHROOM
    }

    private GameState currentState = GameState.CLIMBING;
    private String studentMinigameWord;
    private String studentMinigameInput;
    private String[] mushroomMinigameKeys;
    private int mushroomMinigameProgress;
    private final int MUSHROOM_GOAL = 10;

    private BufferedImage OBimage;

    private GameLauncher launcher;

    public OlaOla(GameLauncher launcher) {
        this();
        this.launcher = launcher;
    } //이것도추가

    public OlaOla() {
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));

        setFocusable(true);
        addKeyListener(this);

        this.timePerStair = INITIAL_STAIR_TIME;
        this.remainTime = this.timePerStair;

        this.currentLife = LIFE;

        loopTimer = new Timer(GAME_TICK_MS, this);
        loopTimer.start();

        player = new Character(
                (GAME_WIDTH / 2) - 32,
                PLAYER_Y_POSITION,
                "image/character.png"
        );

        try {
            OBimage = ImageIO.read(getClass().getResourceAsStream("image/olaolaBackground.png"));

            if (OBimage == null) {
                System.err.println("배경 이미지 로드 실패: olaolaBackground.png 파일을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            System.err.println("배경 이미지 로드 중 예외 발생");
            e.printStackTrace();
        }

        // 초기 계단 생성
        initializeStairs();
        updateDirectionKey();
    }


    private void initializeStairs() {
        int currentX = player.getX() - (STAIR_WIDTH / 2) + (player.getWidth() / 2);
        int currentY = GAME_HEIGHT - 40;

        stairs.add(new StairInfo(currentX, currentY, STAIR_WIDTH, STAIR_HEIGHT, false, false, ObstacleType.NONE, ItemType.NONE));

        for (int i = 0; i < 1; i++) {
            StairInfo lastStair = stairs.get(stairs.size() - 1);

            int newY = lastStair.bounds.y - STAIR_GAP;

            int newX = lastStair.bounds.x + STAIR_WIDTH;

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

        int expectedX_C = lastStair.isLeftDirection ? lastStair.bounds.x - STAIR_WIDTH : lastStair.bounds.x + STAIR_WIDTH;

        boolean willHitLeft_C = (expectedX_C <= 50);
        boolean willHitRight_C = (expectedX_C + STAIR_WIDTH >= GAME_WIDTH - 50);
        boolean randomTurn_C = (random.nextDouble() < 0.4);

        boolean isTurnPoint_C = willHitLeft_C || willHitRight_C || randomTurn_C;

        int newX_C;
        boolean nextIsLeft_C = lastStair.isLeftDirection;

        if (isTurnPoint_C) {
            int targetLeftX = lastStair.bounds.x - STAIR_WIDTH;
            int targetRightX = lastStair.bounds.x + STAIR_WIDTH;

            if (willHitLeft_C) {
                nextIsLeft_C = false;
            } else if (willHitRight_C) {
                nextIsLeft_C = true;
            } else {
                nextIsLeft_C = !lastStair.isLeftDirection;
            }

            newX_C = nextIsLeft_C ? targetLeftX : targetRightX;

        } else {
            newX_C = expectedX_C;
        }


        //새로운 장애물 생성 결정
        if (pendingObstacle == ObstacleType.NONE) {

            // TODO: 점수 기반 학생 스폰 로직

            if (random.nextDouble() < BASE_OBSTACLE_SPAWN_CHANCE) {

                boolean canSpawnStudent = (totalStudentSpawnCount < 5);

                ArrayList<ObstacleType> possibleObstacles = new ArrayList<>();
                possibleObstacles.add(ObstacleType.PROFESSOR);
                possibleObstacles.add(ObstacleType.MUSHROOM);
                if (canSpawnStudent) {
                    possibleObstacles.add(ObstacleType.STUDENT);
                }

                if (!possibleObstacles.isEmpty()) {
                    int obstacleIndex = random.nextInt(possibleObstacles.size());

                    pendingObstacle = possibleObstacles.get(obstacleIndex);
                    System.out.println(pendingObstacle + " 배치 대기");
                }
            }
        }

        // 다음 계단 검사
        boolean isTurnPoint_B = lastStair.isTurnPoint;
        boolean isSafeToSpawn = !isTurnPoint_B && !isTurnPoint_C;

        if (pendingObstacle != ObstacleType.NONE) {

            if (isSafeToSpawn) {
                lastStair.obstacle = pendingObstacle;

                if (pendingObstacle == ObstacleType.STUDENT) {
                    totalStudentSpawnCount++;
                    System.out.println("학생 " + totalStudentSpawnCount + "번째 배치");
                } else {
                    System.out.println(pendingObstacle + " 배치");
                }

                pendingObstacle = ObstacleType.NONE;

            } else {
                System.out.println(pendingObstacle + " 배치 미룸");
            }
        }

        //turn 이후 계단
        stairs.add(new StairInfo(newX_C, newY, STAIR_WIDTH, STAIR_HEIGHT, nextIsLeft_C, isTurnPoint_C, ObstacleType.NONE, ItemType.NONE));


        // 리스트 관리 (기존 로직)
        if (stairs.size() > INITIAL_STAIR_COUNT + 10) {
            stairs.remove(0);
        }
    }

    private void updateGameLogic() {
        if (isGameOver) {
            if (loopTimer.isRunning()) {
                loopTimer.stop();
            }
            if (launcher!=null) {
                launcher.showGameOverPanel(score);
            }
            return;
        }

        if (currentState == GameState.CLIMBING) {
            remainTime -= GAME_TICK_MS;
            if (remainTime <= 0) {
                isGameOver = true;
                System.out.println("Game Over");
            }
        } else {
            minigameTimer -= GAME_TICK_MS;
            if (minigameTimer <= 0) {
                System.out.println("미니게임 실패, 생명 -1");
                endMinigame(false);
            }
        }
    }


    private void playerClimb() {
        if (isGameOver || stairs.size() < 2) return;

        StairInfo currentStair = stairs.get(0);
        StairInfo nextStair = stairs.get(1);
        StairInfo nnextStair = stairs.get(2);

        if (nnextStair.isLeftDirection != nextStair.isLeftDirection) {
            currentStair.turnHere = true;
            requiresDirectionChange = true;
            updateDirectionKey();
        } else {
            requiresDirectionChange = false;
            currentStair.turnHere = false;
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

        StairInfo stairJustLandedOn = stairs.get(0);
        if (stairJustLandedOn.obstacle != ObstacleType.NONE) {
            triggerObstacle(stairJustLandedOn.obstacle);
            stairJustLandedOn.obstacle = ObstacleType.NONE;
        }

        for (StairInfo stair : stairs) {
            stair.bounds.y += STAIR_GAP;
        }

        generateNewStair();
        score++;
        updateDifficulty();
        this.remainTime = this.timePerStair;
    }

    private void triggerObstacle(ObstacleType obstacle) {
        System.out.println(obstacle + " 장애물");

        if (obstacle == ObstacleType.PROFESSOR) {
            double dvalue = Math.random();
            score -= (int)(dvalue * 10 + 1);
            currentLife -= 1;
            System.out.println("과제를 왜 이렇게 해왓나 학생... 자네는 감점이네 (현재: " + score + "점)");
            return;
        }


        if (obstacle == ObstacleType.STUDENT) {
            minigameTimer = 3000.0;
            maxMinigameTime = 3000.0;
            currentState = GameState.MINIGAME_STUDENT;
            studentMinigameWord = "TYPE";
            studentMinigameInput = "";
        } else if (obstacle == ObstacleType.MUSHROOM) {
            minigameTimer = 2000.0;
            maxMinigameTime = 2000.0;
            currentState = GameState.MINIGAME_MUSHROOM;
            mushroomMinigameKeys = new String[2];
            String key1 = LEFT_HAND_KEYS[random.nextInt(LEFT_HAND_KEYS.length)];
            String key2 = RIGHT_HAND_KEYS[random.nextInt(RIGHT_HAND_KEYS.length)];

            if (random.nextBoolean()) {
                mushroomMinigameKeys[0] = key2;
                mushroomMinigameKeys[1] = key1;
            } else {
                mushroomMinigameKeys[0] = key1;
                mushroomMinigameKeys[1] = key2;
            }
            mushroomMinigameProgress = 0;
            System.out.println("버섯 키: " + mushroomMinigameKeys[0] + ", " + mushroomMinigameKeys[1]);
        }
    }

    private void endMinigame(boolean success) {
        if (success) {
            System.out.println("미니게임 성공");
            currentState = GameState.CLIMBING;
        } else {
            System.out.println("미니게임 실패");
            handleWrongKey();

            if (!isGameOver) {
                currentState = GameState.CLIMBING;
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

    private void handleWrongKey() {
        if (isGameOver) return;

        currentLife--;
        System.out.println("life--" + currentLife);

        // TODO: 여기서 화면을 붉게 깜빡이도록

        if (currentLife <= 0) {
            isGameOver = true;
            System.out.println("game over");
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (isGameOver) {
            return;
        }

        switch (currentState) {
            case CLIMBING:
                handleClimbingInput(e);
                break;
            case MINIGAME_STUDENT:
                handleStudentMinigameInput(e);
                break;
            case MINIGAME_MUSHROOM:
                handleMushroomMinigameInput(e);
                break;
        }

        repaint();
    }

    private void handleClimbingInput(KeyEvent e) {
        String pressedKey = KeyEvent.getKeyText(e.getKeyCode()).toUpperCase();
        boolean isKeyValid = false;

        if (pressedKey.equals(currentDirectionKey)) {
            isPlayerFacingLeft = !isPlayerFacingLeft;
            requiresDirectionChange = false;
            System.out.println("방향 전환 성공: "+ currentDirectionKey);
            playerClimb();
            isKeyValid = true;
        }

        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            playerClimb();
            isKeyValid = true;
        }

        if (!isKeyValid) {
            if (e.getKeyCode() != KeyEvent.VK_SHIFT &&
                    e.getKeyCode() != KeyEvent.VK_CONTROL &&
                    e.getKeyCode() != KeyEvent.VK_ALT &&
                    e.getKeyCode() != KeyEvent.VK_META)
            {
                handleWrongKey();
            }
        }
    }


    private void handleStudentMinigameInput(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (!studentMinigameInput.isEmpty()) {
                studentMinigameInput = studentMinigameInput.substring(0, studentMinigameInput.length() - 1);
            }
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (studentMinigameInput.equals(studentMinigameWord)) {
                endMinigame(true);
            } else {
                endMinigame(false);
            }
        } else if (java.lang.Character.isLetter(e.getKeyChar())) {
            studentMinigameInput += java.lang.Character.toUpperCase(e.getKeyChar());
        }
    }


    private void handleMushroomMinigameInput(KeyEvent e) {
        String pressedKey = KeyEvent.getKeyText(e.getKeyCode()).toUpperCase();

        if (pressedKey.equals(mushroomMinigameKeys[0]) || pressedKey.equals(mushroomMinigameKeys[1])) {
            int expectedKeyIndex = mushroomMinigameProgress % 2;
            if (pressedKey.equals(mushroomMinigameKeys[expectedKeyIndex])) {
                mushroomMinigameProgress++;
                System.out.println("연타 " + mushroomMinigameProgress);
            }
            if (mushroomMinigameProgress >= MUSHROOM_GOAL) {
                endMinigame(true);
            }
        }
    }

    // --- 9. 그리기 (랜더링) ---
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (OBimage != null) {
            g.drawImage(
                    OBimage,
                    0,
                    0,
                    GAME_WIDTH,
                    GAME_HEIGHT,
                    this
            );
        }

        //남은 시간 바
        if (!isGameOver) {
            double currentTimerValue;
            double maxTimerValue;

            if (currentState == GameState.CLIMBING) {
                currentTimerValue = remainTime;
                maxTimerValue = timePerStair;
            } else {
                currentTimerValue = minigameTimer;
                maxTimerValue = maxMinigameTime;
            }

            double timePercent = Math.max(0, currentTimerValue / maxTimerValue);

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

            if (stair.obstacle != ObstacleType.NONE) {

                String obstacleText = "";

                if (stair.obstacle == ObstacleType.PROFESSOR) {
                    g.setColor(Color.RED);
                    obstacleText = "P"; // Professor
                } else if (stair.obstacle == ObstacleType.STUDENT) {
                    g.setColor(Color.RED);
                    obstacleText = "S"; // Student
                } else if (stair.obstacle == ObstacleType.MUSHROOM) {
                    g.setColor(Color.RED);
                    obstacleText = "M"; // Mushroom
                }

                g.setFont(new Font("SansSerif", Font.BOLD, 14));
                g.drawString(obstacleText, stair.bounds.x + 5, stair.bounds.y + 15);
            }
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


        if (currentState != GameState.CLIMBING) {
            g.setColor(new Color(0, 0, 0, 150)); // 반투명한 검은색 배경
            g.fillRect(0, 15, GAME_WIDTH, GAME_HEIGHT);

            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 30));

            if (currentState == GameState.MINIGAME_STUDENT) {
                g.drawString("입력하세요 (입력 후 엔터)", GAME_WIDTH / 2 - 100, GAME_HEIGHT / 2 - 100);
                g.setColor(Color.CYAN);
                g.drawString("단어: " + studentMinigameWord, GAME_WIDTH / 2 - 100, GAME_HEIGHT / 2);
                g.setColor(Color.YELLOW);
                g.drawString("입력: " + studentMinigameInput, GAME_WIDTH / 2 - 100, GAME_HEIGHT / 2 + 50);

            } else if (currentState == GameState.MINIGAME_MUSHROOM) {

                g.drawString("버섯을 물리치세요", GAME_WIDTH / 2 - 100, GAME_HEIGHT / 2 - 100);
                g.setColor(Color.GREEN);
                g.drawString("[" + mushroomMinigameKeys[0] + "] 와 [" + mushroomMinigameKeys[1] + "] 연타",
                        GAME_WIDTH / 2 - 250, GAME_HEIGHT / 2);

                g.setColor(Color.GRAY);
                g.fillRect(GAME_WIDTH / 2 - 150, GAME_HEIGHT / 2 + 50, 300, 30);
                g.setColor(Color.YELLOW);
                int progressWidth = (int) (300.0 * (mushroomMinigameProgress / (double)MUSHROOM_GOAL));
                g.fillRect(GAME_WIDTH / 2 - 150, GAME_HEIGHT / 2 + 50, progressWidth, 30);
            }
        }


        // 4. 게임 오버 메시지
        if (isGameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("SansSerif", Font.BOLD, 40));
            g.drawString("GAME OVER", GAME_WIDTH / 2 - 120, GAME_HEIGHT / 2);
        }

        g.setColor(Color.RED);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString("Life: " + currentLife, GAME_WIDTH - 80, 40);
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