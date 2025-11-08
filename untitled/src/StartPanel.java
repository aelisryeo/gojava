import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class StartPanel extends JPanel {
    private BufferedImage bImage;

    public StartPanel(ActionListener startListener) {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.DARK_GRAY);
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("올라올라", SwingConstants.CENTER);
        titleLabel.setFont(new Font("sansSerif", Font.BOLD, 60));
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setOpaque(false);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));


        JButton startButton = new JButton("CLASSIC_MODE");
        startButton.setFont(new Font("SansSerif", Font.BOLD, 30));
        startButton.setPreferredSize(new Dimension(300, 80));
        startButton.setActionCommand("CLASSIC_MODE");
        startButton.addActionListener(startListener);

        JButton modeStartButton = new JButton("TEST_MODE");
        modeStartButton.setFont(new Font("SansSerif", Font.BOLD, 30));
        modeStartButton.setPreferredSize(new Dimension(300, 80));
        modeStartButton.setActionCommand("TEST_MODE");
        modeStartButton.addActionListener(startListener);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 150));
        buttonPanel.add(startButton);
        buttonPanel.add(modeStartButton);

        try {
            // [수정 제안]
            bImage = ImageIO.read(getClass().getResourceAsStream("startBackground.png"));

            if (bImage == null) {
                System.err.println("배경 이미지 로드 실패: startBackground.png 파일을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            System.err.println("배경 이미지 로드 중 예외 발생");
            e.printStackTrace();
        }

        // 제목은 상단(NORTH)에, 버튼 패널은 중앙(CENTER)에 배치
        add(titleLabel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bImage != null) {
            g.drawImage(bImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}