import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;

public class StartPanel extends JPanel implements GameConstants {
    private BufferedImage bImage;
    private BufferedImage characterImage;
    private CharacterSelect selectedCharacter = CharacterSelect.CHARACTER_기쁜수룡;

    private Timer animTimer;
    private int bobbingOffset = 0;
    private double bobbingAngle = 0;


    public StartPanel(ActionListener startListener, ActionListener characterSelectListener, CharacterSelect currentCharacter) {
        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setBackground(Color.GRAY);
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("올라올라", SwingConstants.CENTER);

        titleLabel.setFont(GameFont.getFont(Font.PLAIN, 90f));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setOpaque(false);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(100, 0, 0, 0));

        JPanel characterSelectionArea = new JPanel();
        characterSelectionArea.setOpaque(false);
        characterSelectionArea.setLayout(new BoxLayout(characterSelectionArea, BoxLayout.Y_AXIS));


        JButton selectCharButton = createStyledButton("캐릭터 선택", 22, new Color(200, 149, 237));
        selectCharButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        selectCharButton.addActionListener(characterSelectListener);


        JButton startButton =  createStyledButton("올라올라", 30, new Color(200, 149, 237));
        startButton.setPreferredSize(new Dimension(300, 80));
        startButton.setActionCommand("CLASSIC_MODE");
        startButton.addActionListener(startListener);

        JButton modeStartButton =  createStyledButton("술래잡기", 30, new Color(200, 149, 237));
        modeStartButton.setPreferredSize(new Dimension(300, 80));
        modeStartButton.setActionCommand("TEST_MODE");
        modeStartButton.addActionListener(startListener);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 0));
        buttonPanel.add(startButton);
        buttonPanel.add(modeStartButton);

        JPanel centerContainer = new JPanel();
        centerContainer.setOpaque(false);
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));

        centerContainer.add(Box.createVerticalGlue());
        centerContainer.add(Box.createVerticalStrut(220));
        centerContainer.add(selectCharButton);
        centerContainer.add(Box.createVerticalStrut(40));

        centerContainer.add(buttonPanel);
        centerContainer.add(Box.createVerticalStrut(50));
        centerContainer.add(Box.createVerticalGlue());
        try {

            String[] charPath = currentCharacter.getImagePath();
            if (charPath != null && charPath.length > 0) {
                String displayPath = charPath[0];
                characterImage = ImageIO.read(getClass().getResourceAsStream(displayPath));
            }
            bImage = ImageIO.read(getClass().getResourceAsStream("image/startBackground.png"));
            if (bImage == null) {
                System.err.println("배경 이미지 로드 실패: startBackground.png 파일을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            System.err.println("배경 이미지 로드 중 예외 발생");
            e.printStackTrace();
        }

        add(titleLabel, BorderLayout.NORTH);
        add(centerContainer, BorderLayout.CENTER);

        animTimer = new Timer(50, e -> {
            bobbingAngle += 0.15;
            bobbingOffset = (int) (Math.sin(bobbingAngle) * 10);
            repaint();
        });
        animTimer.start();
    }

    private JButton createStyledButton(String text, int fontSize, Color baseColor) {
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

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                g2.dispose();

                super.paintComponent(g);
            }
        };

        button.setFont(GameFont.getFont(Font.PLAIN, (float)fontSize));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bImage != null) {
            g.drawImage(bImage, 0, 0, getWidth(), getHeight(), this);
        }

        if (characterImage != null) {
            int charWidth = 200;
            int charHeight = 200;
            int x = (getWidth() / 2) - (charWidth / 2);
            int y = (getHeight() / 2) - 150 + bobbingOffset;
            g.drawImage(characterImage, x, y, charWidth, charHeight, this);
        }
    }


}