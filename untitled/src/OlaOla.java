import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.*;

public class OlaOla extends JPanel implements ActionListener, KeyListener, GameConstants {

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
    private int testItemCount = 0;
    private double clockBuffTimer = 0;
    private double testBuffeTimer = 0;

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

    private BufferedImage imageProfessor;
    private BufferedImage imageStudent;
    private BufferedImage imageMushroom;

    private BufferedImage imageTest;
    private BufferedImage imageClock;
    private BufferedImage imageHeart;

    private BufferedImage[] hitMushrooms;
    private int currentMushroomFrame = 0;
    private Timer mushroomATimer;
    private static final int MUSHROOM_ANIMATION_FRAMES = 3;
    private static final int ANIMATION_DELAY_MS = 150;

    private BufferedImage[] typing;
    private int currentTypeFrame = 0;
    private Timer typingTimer;
    private static final int TYPE_ANIMATION_FRAMES = 2;

    private BufferedImage[] charAnim;
    private int currentCharFrame = 0;
    private static final int playerWidth = 80;
    private static final int playerHeight = 80;
    private int playerX = 368;

    private GameLauncher launcher;

    public OlaOla(GameLauncher launcher, CharacterSelect selectedCharacter) {
        this.launcher = launcher;

        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        this.timePerStair = INITIAL_STAIR_TIME;
        this.remainTime = this.timePerStair;

        this.currentLife = LIFE;

        loopTimer = new Timer(GAME_TICK_MS, this);
        loopTimer.start();

        String[] paths = selectedCharacter.getImagePath();
        charAnim = new BufferedImage[paths.length];
        try {
            for (int i = 0; i < paths.length; i++) {
                charAnim[i] = ImageIO.read(getClass().getResourceAsStream(paths[i]));
                if (charAnim[i] == null) {
                    System.err.println("캐릭터 애니메이션 이미지 로드 실패: " + paths[i]);
                }
            }
        } catch (Exception e) {
            System.err.println("캐릭터 애니메이션 이미지 로드 중 예외 발생");
            e.printStackTrace();
        }

        try {
            imageProfessor = ImageIO.read(getClass().getResourceAsStream("image/professor.png"));
        } catch (Exception e) {
            System.err.println("교수 이미지 로드 실패!");
            e.printStackTrace();
        }
        try {
            imageStudent = ImageIO.read(getClass().getResourceAsStream("image/student.png"));
        } catch (Exception e) {
            System.err.println("학생 이미지 로드 실패!");
            e.printStackTrace();
        }
        try {
            imageMushroom = ImageIO.read(getClass().getResourceAsStream("/image/mushroom.png"));
        } catch (Exception e) {
            System.err.println("버섯 이미지 로드 실패!");
            e.printStackTrace();
        }

        try {
            imageTest = ImageIO.read(getClass().getResourceAsStream("image/test.png"));
        } catch (Exception e) {
            System.err.println("시험지 이미지 로드 실패!");
            e.printStackTrace();
        }
        try {
            imageClock = ImageIO.read(getClass().getResourceAsStream("image/clock.png"));
        } catch (Exception e) {
            System.err.println("시계 이미지 로드 실패!");
            e.printStackTrace();
        }
        try {
            imageHeart = ImageIO.read(getClass().getResourceAsStream("/image/heart.png"));
        } catch (Exception e) {
            System.err.println("하트 이미지 로드 실패!");
            e.printStackTrace();
        }

        hitMushrooms = new BufferedImage[MUSHROOM_ANIMATION_FRAMES];
        try {
            for (int i = 0; i < MUSHROOM_ANIMATION_FRAMES; i++) {
                String path = "mushroom/hitmushroom" + i + ".png";
                hitMushrooms[i] = ImageIO.read(getClass().getResourceAsStream(path));
                if (hitMushrooms[i] == null) {
                    System.err.println("버섯 애니메이션 이미지 로드 실패 : " + path);
                }
            }
        }catch (Exception e) {
            System.err.println("버섯 애니메이션 이미지 로드 중 예외 발생");
            e.printStackTrace();
        }
        mushroomATimer = new Timer(ANIMATION_DELAY_MS, this);
        mushroomATimer.start();

        typing = new BufferedImage[TYPE_ANIMATION_FRAMES];
        try {
            for (int i = 0; i < TYPE_ANIMATION_FRAMES; i++) {
                String path = "type/typing" + i + ".png";
                typing[i] = ImageIO.read(getClass().getResourceAsStream(path));
                if (typing[i] == null) {
                    System.err.println("typing이미지로드실패 : " + path);
                }
            }
        }catch (Exception e) {
            System.err.println("typing 애니메이션 이미지 로드 중 예외 발생");
            e.printStackTrace();
        }
        typingTimer = new Timer(ANIMATION_DELAY_MS, this);
        typingTimer.start();

        try {
            OBimage = ImageIO.read(getClass().getResourceAsStream("image/olaolaBackground.png"));

            if (OBimage == null) {
                System.err.println("배경 이미지 로드 실패: olaolaBackground.png 파일을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            System.err.println("배경 이미지 로드 중 예외 발생");
            e.printStackTrace();
        }

        initializeStairs();
        updateDirectionKey();
    }


    private void initializeStairs() {
        int currentX = playerX - (STAIR_WIDTH / 2) + (playerWidth / 2);
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

        if (pendingObstacle == ObstacleType.NONE) {
            if (random.nextDouble() < BASE_OBSTACLE_SPAWN_CHANCE) {
                ArrayList<ObstacleType> possibleObstacles = new ArrayList<>();
                possibleObstacles.add(ObstacleType.PROFESSOR);
                possibleObstacles.add(ObstacleType.MUSHROOM);
                if (totalStudentSpawnCount < 5) {
                    possibleObstacles.add(ObstacleType.STUDENT);
                }

                if (!possibleObstacles.isEmpty()) {
                    int obstacleIndex = random.nextInt(possibleObstacles.size());
                    pendingObstacle = possibleObstacles.get(obstacleIndex);
                }
            }
        }

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
                System.out.println(pendingObstacle + " 배치 유예");
            }
        }

        if (lastStair.obstacle == ObstacleType.NONE) {

            final double ITEM_SPAWN_CHANCE = 0.25;
            if (random.nextDouble() < ITEM_SPAWN_CHANCE) {

                ItemType[] possibleItems = {ItemType.CLOCK, ItemType.HEART, ItemType.TEST};
                int itemIndex = random.nextInt(possibleItems.length);
                ItemType newItem = possibleItems[itemIndex];

                lastStair.item = newItem;
            }
        }

        stairs.add(new StairInfo(newX_C, newY, STAIR_WIDTH, STAIR_HEIGHT, nextIsLeft_C, isTurnPoint_C, ObstacleType.NONE, ItemType.NONE));


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
            if (clockBuffTimer > 0) {
                clockBuffTimer -= GAME_TICK_MS;
            }
            if (testBuffeTimer > 0) {
                testBuffeTimer -= GAME_TICK_MS;
            }

            double currentTickSpeed = GAME_TICK_MS;
            if (clockBuffTimer > 0) {
                currentTickSpeed /= 2.0;
            }
            remainTime -= currentTickSpeed;

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
            nextPlayerX = playerX - moveDistance;
        } else {
            nextPlayerX = playerX + moveDistance;
        }

        int nextPlayerCenterX = nextPlayerX + (playerWidth / 2);
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

        playerX = nextPlayerX;
        stairs.remove(0);

        StairInfo stairJustLandedOn = stairs.get(0);
        if (stairJustLandedOn.obstacle != ObstacleType.NONE) {
            triggerObstacle(stairJustLandedOn.obstacle);
            stairJustLandedOn.obstacle = ObstacleType.NONE;
        } else if (stairJustLandedOn.item != ItemType.NONE) {
            triggerItem(stairJustLandedOn.item);
            stairJustLandedOn.item = ItemType.NONE;
        }

        for (StairInfo stair : stairs) {
            stair.bounds.y += STAIR_GAP;
        }

        generateNewStair();

        int pointsEarned = 1;
        if (testBuffeTimer > 0) {
            pointsEarned = 2;
            System.out.println("점수 두 배");
        }
        score += pointsEarned;

        updateDifficulty();
        this.remainTime = this.timePerStair;

        playSound("climb");
    }

    private void playSound(String name) {
        try {
            java.net.URL soundURL = getClass().getResource("audio/"+name+".wav");

            if (soundURL == null) {
                System.err.println("사운드 파일 로드 실패: 경로를 확인하십시오.");
                return;
            }
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);

            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("사운드 재생 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            }
    }

    private void triggerItem(ItemType item) {
        System.out.println(item + "아이템");
        playSound("item");

        if (item == ItemType.CLOCK) {
            clockBuffTimer = 5000.0;
            System.out.println("시계 획득");
        } else if (item == ItemType.HEART) {
            int newLife = currentLife + 1;
            currentLife = Math.min(newLife, LIFE);
            System.out.println("생명 추가");
        } else if (item == ItemType.TEST) {
            testItemCount++;
            System.out.println("시험지" + testItemCount + "번째 획득");

            if (testItemCount >= 5) {
                System.out.println("점수 두 배");
                testBuffeTimer = 5000.0;

                testItemCount = 0;
                System.out.println("시험지 초기화");
            }
        }
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
            playSound("type");
        }
    }


    private void handleMushroomMinigameInput(KeyEvent e) {
        String pressedKey = KeyEvent.getKeyText(e.getKeyCode()).toUpperCase();

        if (pressedKey.equals(mushroomMinigameKeys[0]) || pressedKey.equals(mushroomMinigameKeys[1])) {
            int expectedKeyIndex = mushroomMinigameProgress % 2;
            if (pressedKey.equals(mushroomMinigameKeys[expectedKeyIndex])) {
                mushroomMinigameProgress++;
                System.out.println("연타 " + mushroomMinigameProgress);
                playSound("mushroom");
            }
            if (mushroomMinigameProgress >= MUSHROOM_GOAL) {
                endMinigame(true);
            }
        }
    }


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

        for (StairInfo stair : stairs) {
            if (stair == stairs.get(0)) {
                g.setColor(Color.ORANGE);
            } else {
                g.setColor(Color.DARK_GRAY);
            }
            g.fillRect(stair.bounds.x, stair.bounds.y, stair.bounds.width, stair.bounds.height);

            BufferedImage obstacleImage = null;
            BufferedImage itemImage = null;
            if (stair.obstacle != ObstacleType.NONE) {

                String obstacleText = "";

                if (stair.obstacle == ObstacleType.PROFESSOR) {
                    g.setColor(Color.RED);
                    obstacleText = "P";
                    obstacleImage = imageProfessor;
                } else if (stair.obstacle == ObstacleType.STUDENT) {
                    g.setColor(Color.RED);
                    obstacleText = "S";
                    obstacleImage = imageStudent;
                } else if (stair.obstacle == ObstacleType.MUSHROOM) {
                    g.setColor(Color.RED);
                    obstacleText = "M";
                    obstacleImage = imageMushroom;
                }

                if (obstacleImage!= null) {

                    int obstacleX = stair.bounds.x + (STAIR_WIDTH / 2) - (ITEM_HEIGHT / 2);
                    int obstacleY = stair.bounds.y - ITEM_HEIGHT;

                    g.drawImage(
                            obstacleImage,
                            obstacleX,
                            obstacleY,
                            ITEM_HEIGHT,
                            ITEM_HEIGHT,
                            this
                    );
                }

                g.setFont(new Font("SansSerif", Font.BOLD, 14));
                g.drawString(obstacleText, stair.bounds.x + 5, stair.bounds.y + 15);
            } else if (stair.item != ItemType.NONE) {
                String itemText = "";
                if (stair.item == ItemType.CLOCK) {
                    g.setColor(Color.CYAN);
                    itemText = "C";
                    itemImage = imageClock;
                } else if (stair.item == ItemType.HEART) {
                    g.setColor(Color.PINK);
                    itemText = "H";
                    itemImage = imageHeart;
                } else if (stair.item == ItemType.TEST) {
                    g.setColor(Color.GREEN);
                    itemText = "T";
                    itemImage = imageTest;
                }

                g.setFont(new Font("SansSerif", Font.BOLD, 14));
                g.drawString(itemText, stair.bounds.x + 5, stair.bounds.y + 15);
                if (itemImage!= null) {

                    int itemX = stair.bounds.x + (STAIR_WIDTH / 2) - (ITEM_HEIGHT / 2);
                    int itemY = stair.bounds.y - ITEM_HEIGHT;

                    g.drawImage(
                            itemImage,
                            itemX,
                            itemY,
                            ITEM_HEIGHT,
                            ITEM_HEIGHT,
                            this
                    );
                }

            }
        }
        Graphics2D g2d = (Graphics2D)g.create();

        BufferedImage currentCharImage = null;
        if (charAnim!=null && charAnim.length > 0) {
            currentCharImage = charAnim[currentCharFrame];
        }

        final int PLAYER_Y = PLAYER_Y_POSITION;

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

        g2d.dispose();

        g.setColor(Color.YELLOW);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Direction: " + (isPlayerFacingLeft ? "LEFT" : "RIGHT"), 10, 40);

        if(requiresDirectionChange) {
            g.setColor(Color.CYAN);
            g.setFont(new Font("SansSerif", Font.BOLD, 24));
            g.drawString("TURN KEY : [" + currentDirectionKey + "]", GAME_WIDTH - 200, 400);
        }

        int buffY_Position = 80;

        if (clockBuffTimer > 0) {
            g.setColor(Color.ORANGE);
            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            String clockText = "Clock: " + clockBuffTimer;
            g.drawString(clockText, 10, buffY_Position);
            buffY_Position += 20;
        }

        if (testBuffeTimer > 0) {
            g.setColor(Color.ORANGE);
            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            String testText = "Test: " + testBuffeTimer;
            g.drawString(testText, 10, buffY_Position);
            buffY_Position += 20;
        }

        if (testItemCount > 0) {
            g.setColor(Color.ORANGE);
            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            String testText = "Test: " + testItemCount + "/5";
            g.drawString(testText, 10, buffY_Position);
        }


        if (currentState != GameState.CLIMBING) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 15, GAME_WIDTH, GAME_HEIGHT);

            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 30));

            if (currentState == GameState.MINIGAME_STUDENT) {
                g.drawString("입력하세요 (입력 후 엔터)", GAME_WIDTH / 2 - 100, GAME_HEIGHT / 2 - 100);
                g.setColor(Color.CYAN);
                g.drawString("단어: " + studentMinigameWord, GAME_WIDTH / 2 - 100, GAME_HEIGHT / 2);
                g.setColor(Color.YELLOW);
                g.drawString("입력: " + studentMinigameInput, GAME_WIDTH / 2 - 100, GAME_HEIGHT / 2 + 50);

                BufferedImage currentImage = typing[currentTypeFrame];
                if (currentImage != null) {
                    int x = GAME_WIDTH / 2 + 140;
                    int y = GAME_HEIGHT / 2 - 50;
                    int width = 200;
                    int height = 200;

                    g.drawImage(
                            currentImage,
                            x,
                            y,
                            width,
                            height,
                            this
                    );
                }
                else {
                    System.out.println("typing이미지가null입니다...");
                }

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

                BufferedImage currentImage = hitMushrooms[currentMushroomFrame];
                if (currentImage != null) {
                    int x = GAME_WIDTH / 2 + 140;
                    int y = GAME_HEIGHT / 2 - 50;
                    int width = 200;
                    int height = 200;

                    g.drawImage(
                            currentImage,
                            x,
                            y,
                            width,
                            height,
                            this
                    );
                }
                else {
                    System.out.println("버섯이미지가null입니다...");
                }
            }
        }


        g.setColor(Color.RED);
        g.setFont(new Font("SansSerif", Font.BOLD, 18));
        g.drawString("Life: " + currentLife, GAME_WIDTH - 80, 40);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loopTimer) {
            updateGameLogic();
            repaint();
        }
        else if (e.getSource() == mushroomATimer) {
            currentMushroomFrame = (currentMushroomFrame + 1) % MUSHROOM_ANIMATION_FRAMES;

            if (charAnim != null && charAnim.length > 0) {
                currentCharFrame = (currentCharFrame + 1) % charAnim.length;
            }

            repaint();
        }
        else if (e.getSource() == typingTimer) {
            currentTypeFrame = (currentTypeFrame + 1) % TYPE_ANIMATION_FRAMES;
            repaint();
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}

}