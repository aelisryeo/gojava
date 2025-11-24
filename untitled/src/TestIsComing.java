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
        setBackground(new Color(255,247,240));
        setFocusable(true);
        addKeyListener(this);

        this.timePerStair = INITIAL_STAIR_TIME;
        this.remainTime = this.timePerStair;

        this.currentChaserInterval = CHASER_MOVE_INTERVAL;

        loopTimer = new Timer(GAME_TICK_MS, this);
        loopTimer.start();


        chaser = new Character(
                playerX + CHASER_OFFSET_X,
                PLAYER_Y_POSITION + 150, // 플레이어보다 아래
                "image/chaser.png"
        );

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
        playerTimer = new Timer(ANIMATION_DELAY_MS, this);
        playerTimer.start();

        try {
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

    private void playerClimb() {
        if (isGameOver || stairs.size() < playerStairIndex + 2) return;

        StairInfo targetStair = stairs.get(playerStairIndex + 1);

        if (targetStair.isLeftDirection != isPlayerFacingLeft) {
            System.out.println("게임 오버! 잘못된 방향으로 올랐습니다.");
            isGameOver = true;
            return;
        }

        int targetX = targetStair.bounds.x + (STAIR_WIDTH / 2) - (PLAYER_WIDTH / 2);
        playerX = targetX;
        playerStairIndex++;


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


        for (StairInfo stair : stairs) {
            stair.bounds.y += STAIR_GAP;
        }
        chaser.setY(chaser.getY() + STAIR_GAP);

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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (TBimage != null) {
            g.drawImage(
                    TBimage,
                    0,
                    0,
                    GAME_WIDTH,
                    GAME_HEIGHT,
                    this
            );
        }
        Graphics2D g2d = (Graphics2D) g.create();

        float alpha = 0.5f;
        AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        g2d.setComposite(ac);

        g2d.setColor(Color.WHITE);

        g2d.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        float opaqueAlpha = 1.0f;
        AlphaComposite opaqueAc = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opaqueAlpha);
        g2d.setComposite(opaqueAc);

        for (StairInfo stair : stairs) {
            g.setColor(Color.DARK_GRAY);
            if (stairs.indexOf(stair) == playerStairIndex) {
                g.setColor(Color.ORANGE);
            }
            if (stairs.indexOf(stair) == chaserStairIndex) {
                g.setColor(Color.RED);
            }
            g.fillRoundRect(stair.bounds.x, stair.bounds.y + 10, stair.bounds.width, stair.bounds.height, 15, 15);
            if (stairs.indexOf(stair) == playerStairIndex && requiresDirectionChange) {
                g.setColor(new Color(0, 0, 0, 160));
                int keySize = 40;
                int keyX = stair.bounds.x + (stair.bounds.width - keySize) / 2;
                int keyY = stair.bounds.y;

                g.fillRoundRect(keyX, keyY, keySize, keySize, 10, 10);

                g.setColor(Color.WHITE);
                g.setFont(GameFont.getFont(Font.PLAIN, 24f));

                String keyText = currentDirectionKey;

                FontMetrics fm = g.getFontMetrics();
                int textX = keyX + (keySize - fm.stringWidth(keyText)) / 2;
                int textY = keyY + (keySize - fm.getHeight()) / 2 + fm.getAscent();

                g.drawString(keyText, textX, textY);
            }
        }


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

        g2d.dispose();

        drawCharacter(g, chaser, chaser.getX(), chaser.getY(), isChaserFacingLeft);
        Graphics2D g2d2 = (Graphics2D) g.create();
        int bgX =5;
        int bgY = 15;
        int bgWidth = 270;
        int bgHeight = 60;

        int arcWidth = 15;
        int arcHeight = 15;

        Composite originalComposite = g2d2.getComposite();

        float opacity = 0.7f;
        AlphaComposite alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
        g2d2.setComposite(alphaComposite);

        g2d2.setColor(new Color(255, 255, 255, 220));
        g2d2.fillRoundRect(bgX, bgY, bgWidth, bgHeight, arcWidth, arcHeight);

        g2d2.setComposite(originalComposite);
        g.setColor(Color.DARK_GRAY);
        g.setFont(GameFont.getFont(Font.PLAIN, 25));
        g.drawString("Score: " + score, 10, 42);
        g.drawString("Direction: " + (isPlayerFacingLeft ? "LEFT" : "RIGHT"), 10, 65);
        g2d2.dispose();


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

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loopTimer) {
            updateGameLogic();
            repaint();
        }
        if (e.getSource() == playerTimer) {
            currentCharFrame = (currentCharFrame + 1) % PLAYER_ANIMATION_FRAMES;

            if (charAnim != null && charAnim.length > 0) {
                currentCharFrame = (currentCharFrame + 1) % charAnim.length;
            }

            repaint();
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}