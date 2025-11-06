import jdk.jshell.tool.JavaShellToolBuilder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class StartPanel extends JPanel{
    private BufferedImage bImage;

    public enum GameMode {
        CLASSIC_MODE,
        TEST_MODE
    }
    private OlaOla.GameMode currentGameMode = OlaOla.GameMode.CLASSIC_MODE;

    public StartPanel(ActionListener startListener) {
        setPreferredSize(new Dimension(800,600));
        setBackground(Color.DARK_GRAY);
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("올라올라", SwingConstants.CENTER);
        titleLabel.setFont(new Font ("sansSerif", Font.BOLD,60));
        titleLabel.setForeground(Color.YELLOW);

        JButton startButton = new JButton("CLASSIC_MODE");
        startButton.setFont(new Font("SansSerif", Font.BOLD,30));
        startButton.setPreferredSize(new Dimension(300,80));

        startButton.addActionListener(startListener);

        JButton modeStartButton = new JButton("TEST_MODE");
        modeStartButton.setFont(new Font("SansSerif", Font.BOLD, 30));
        modeStartButton.setPreferredSize(new Dimension(300,80));

        modeStartButton.addActionListener(startListener);

        JPanel basicPanel = new JPanel();
        basicPanel.setOpaque(false); // 배경 투명하게
        basicPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 150)); // 중앙 하단 배치
        basicPanel.add(startButton);

        JPanel modePanel = new JPanel();
        modePanel.setOpaque(false);
        modePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 150));
        modePanel.add(modeStartButton);

        try {
            bImage = ImageIO.read(getClass().getResourceAsStream("/character.png"));
        } catch (Exception e) {
            System.err.println("배경 이미지 로드 실패");
        }

        // 제목은 상단에, 버튼은 중앙에 배치
        add(titleLabel, BorderLayout.NORTH);
        add(basicPanel, BorderLayout.WEST);
        add(modePanel, BorderLayout.EAST);

    }
    @Override
    protected void paintComponent(Graphics g) {
        if (bImage != null) {
            g.drawImage(bImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
