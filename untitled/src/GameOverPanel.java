import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;

public class GameOverPanel extends JPanel implements ActionListener, GameConstants{
    private BufferedImage GOBimage;
    private GameLauncher launcher;

    private BufferedImage[] falling;
    private int fallingFrame = 0;
    private Timer fallingTimer;
    private static final int FALLING_ANIMATION_FRAMES = 3;
    private static final int ANIMATION_DELAY_MS = 200;

    public GameOverPanel(GameLauncher launcher, int finalScore) {
        this.launcher = launcher;

        try {
            if(finalScore==0) {
                GOBimage = ImageIO.read(getClass().getResourceAsStream("image/gameoverWorst.png"));

                if (GOBimage == null) {
                    System.err.println("배경 이미지 로드 실패: gameoverWorst.png 파일을 찾을 수 없습니다.");
                }
            }
            else if(finalScore <= 20) {
                GOBimage = ImageIO.read(getClass().getResourceAsStream("image/gameoverBad.png"));

                if (GOBimage == null) {
                    System.err.println("배경 이미지 로드 실패: gameoverBad.png 파일을 찾을 수 없습니다.");
                }
            } else if (finalScore <=50) {
                GOBimage = ImageIO.read(getClass().getResourceAsStream("image/gameoverSoso.png"));

                if (GOBimage == null) {
                    System.err.println("배경 이미지 로드 실패: gameoverSoso.png 파일을 찾을 수 없습니다.");
                }
            } else if (finalScore <=100) {
                GOBimage = ImageIO.read(getClass().getResourceAsStream("image/gameoverGood.png"));

                if (GOBimage == null) {
                    System.err.println("배경 이미지 로드 실패: gameoverGood.png 파일을 찾을 수 없습니다.");
                }
            } else if (finalScore <= 150){
                GOBimage = ImageIO.read(getClass().getResourceAsStream("image/gameoverBest.png"));

                if (GOBimage == null) {
                    System.err.println("배경 이미지 로드 실패: gameoverBest.png 파일을 찾을 수 없습니다.");
                }
            } else {
                GOBimage = ImageIO.read(getClass().getResourceAsStream("image/gameoverGyosoo.png"));
                if (GOBimage == null) {
                    System.err.println("배경 이미지 로드 실패: gameoverGyosoo.png 파일을 찾을 수 없읍니다.");
                }
            }
        } catch (Exception e) {
            System.err.println("배경 이미지 로드 중 예외 발생.");
            e.printStackTrace();
        }

        falling = new BufferedImage[FALLING_ANIMATION_FRAMES];
        try {
            for (int i = 0; i < FALLING_ANIMATION_FRAMES; i++) {
                String path = "over/falling" + i + ".png";
                falling[i] = ImageIO.read(getClass().getResourceAsStream(path));
                if (falling[i] == null) {
                    System.err.println("falling 이미지 로드 실패 : " + path);
                }
            }
        }catch (Exception e) {
            System.err.println("falling 이미지 로드 중 예외 발생");
            e.printStackTrace();
        }
        fallingTimer = new Timer(ANIMATION_DELAY_MS, this);
        fallingTimer.start();

        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        JLabel messageLabel = new JLabel("GAME OVER", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 40));
        messageLabel.setForeground(Color.RED);

        JLabel scoreLabel = new JLabel("Final Score: "+finalScore, SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 30));
        scoreLabel.setForeground(Color.MAGENTA);

        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);
        infoPanel.setLayout(new GridLayout(2,1));
        infoPanel.add(messageLabel);
        infoPanel.add(scoreLabel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER,30,100));

        JButton restartButton = new JButton("Return to Main");
        restartButton.setFont(new Font("Arial", Font.BOLD, 20));
        restartButton.addActionListener(e-> {
            launcher.showStartPanel();
        });

        JButton exitButton = new JButton("Exit Game");
        exitButton.setFont(new Font("Arial", Font.BOLD, 20));
        exitButton.addActionListener(e -> {
            System.exit(0);
        });
        buttonPanel.add(restartButton);
        buttonPanel.add(exitButton);

        add(infoPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (GOBimage != null) {
            g.drawImage(GOBimage, 0, 0, getWidth(), getHeight(), this);
        }

        BufferedImage currentImage = falling[fallingFrame];
        if (currentImage != null) {
            int x = GAME_WIDTH / 2 + 120;
            int y = GAME_HEIGHT / 2 - 50;
            int width = 150;
            int height = 150;

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
            System.out.println("falling이미지가null입니다...");
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
