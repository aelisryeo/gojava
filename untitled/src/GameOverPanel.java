import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.nio.Buffer;

public class GameOverPanel extends JPanel{
    private BufferedImage GOBimage;
    private GameLauncher launcher;
    public GameOverPanel(GameLauncher launcher, int finalScore) {
        this.launcher = launcher;

        try {
            // [수정 제안]
            GOBimage = ImageIO.read(getClass().getResourceAsStream("gameoverBackground.png"));

            if (GOBimage == null) {
                System.err.println("배경 이미지 로드 실패: gameoverBackground.png 파일을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            System.err.println("배경 이미지 로드 중 예외 발생");
            e.printStackTrace();
        }

        setPreferredSize(new Dimension(800, 600));
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        JLabel messageLabel = new JLabel("GAME OVER", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 30));
        messageLabel.setForeground(Color.RED);

        JLabel scoreLabel = new JLabel("Final Score: "+finalScore, SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 30));
        scoreLabel.setForeground(Color.WHITE);

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
            // ⭐ 프로그램 종료 ⭐
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
    }
}
