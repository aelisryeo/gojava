import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class GameOverPanel extends JPanel implements ActionListener, GameConstants {
    private BufferedImage GOBimage;
    private GameLauncher launcher;

    private BufferedImage[] falling;
    private int fallingFrame = 0;
    private Timer fallingTimer;
    private static final int FALLING_ANIMATION_FRAMES = 3;
    private static final int ANIMATION_DELAY_MS = 200;

    public GameOverPanel(GameLauncher launcher, int finalScore, GameLauncher.GameMode mode) {
        this.launcher = launcher;

        loadImages(finalScore);

        boolean isNewRecord = HighScoreManager.saveScoreIfNewBest(mode, finalScore);
        int bestScore = HighScoreManager.getHighScore(mode);

        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setLayout(new GridBagLayout());

        JPanel whiteBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(new Color(255, 255, 255, 220));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);

            }
        };

        whiteBox.setLayout(new BoxLayout(whiteBox, BoxLayout.Y_AXIS));
        whiteBox.setPreferredSize(new Dimension(400, 400));
        whiteBox.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));


        JLabel messageLabel = new JLabel("GAME OVER");
        messageLabel.setFont(new Font("SansSerif", Font.BOLD, 40));
        messageLabel.setForeground(new Color(50, 50, 50));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel scoreLabel = new JLabel("SCORE: " + finalScore);
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        scoreLabel.setForeground(new Color(255, 80, 80));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel bestScoreLabel = new JLabel("BEST: " + bestScore);
        bestScoreLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        bestScoreLabel.setForeground(Color.GRAY);
        bestScoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel newRecordLabel = new JLabel("NEW RECORD!");
        newRecordLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        newRecordLabel.setForeground(new Color(255, 215, 0));
        newRecordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        newRecordLabel.setVisible(isNewRecord);

        JButton restartButton = createStyledButton("Return to Main", new Color(180, 130, 180));
        restartButton.addActionListener(e -> launcher.showStartPanel());

        JButton exitButton = createStyledButton("Exit Game", new Color(205, 92, 92));
        exitButton.addActionListener(e -> System.exit(0));

        whiteBox.add(Box.createVerticalStrut(20));
        whiteBox.add(messageLabel);
        whiteBox.add(Box.createVerticalStrut(20));
        whiteBox.add(scoreLabel);
        whiteBox.add(Box.createVerticalStrut(5));
        whiteBox.add(bestScoreLabel);
        if (isNewRecord) {
            whiteBox.add(Box.createVerticalStrut(10));
            whiteBox.add(newRecordLabel);
        }
        whiteBox.add(Box.createVerticalStrut(40));
        whiteBox.add(restartButton);
        whiteBox.add(Box.createVerticalStrut(15));
        whiteBox.add(exitButton);

        add(whiteBox);
    }

    private void loadImages(int finalScore) {
        try {
            String bgPath;
            if (finalScore == 0) bgPath = "image/gameoverWorst.png";
            else if (finalScore <= 20) bgPath = "image/gameoverBad.png";
            else if (finalScore <= 50) bgPath = "image/gameoverSoso.png";
            else if (finalScore <= 150) bgPath = "image/gameoverGood.png";
            else if (finalScore <= 250) bgPath = "image/gameoverBest.png";
            else bgPath = "image/gameoverGyosoo.png";

            GOBimage = ImageIO.read(getClass().getResourceAsStream(bgPath));

            falling = new BufferedImage[FALLING_ANIMATION_FRAMES];
            for (int i = 0; i < FALLING_ANIMATION_FRAMES; i++) {
                falling[i] = ImageIO.read(getClass().getResourceAsStream("over/falling" + i + ".png"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        fallingTimer = new Timer(ANIMATION_DELAY_MS, this);
        fallingTimer.start();
    }

    private JButton createStyledButton(String text, Color baseColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isRollover()) {
                    g2.setColor(baseColor.brighter());
                } else {
                    g2.setColor(baseColor);
                }

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();

                super.paintComponent(g);
            }
        };

        button.setFont(new Font("SansSerif", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        Dimension btnSize = new Dimension(200, 45);
        button.setPreferredSize(btnSize);
        button.setMaximumSize(btnSize);

        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (GOBimage != null) {
            g.drawImage(GOBimage, 0, 0, getWidth(), getHeight(), this);
        }

        if (falling[fallingFrame] != null) {
            int x = GAME_WIDTH / 2 - 350;
            int y = GAME_HEIGHT / 2 - 50;
            g.drawImage(falling[fallingFrame], x, y, 150, 150, this);
        }
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (e.getSource() == fallingTimer) {
            fallingFrame = (fallingFrame + 1) % FALLING_ANIMATION_FRAMES;
            repaint();
        }
    }
}