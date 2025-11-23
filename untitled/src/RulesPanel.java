import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class RulesPanel extends JPanel implements GameConstants {

    private ActionListener backListener;

    public RulesPanel(ActionListener backListener) {
        this.backListener = backListener;

        setPreferredSize(new Dimension(GAME_WIDTH, GAME_HEIGHT));
        setBackground(Color.DARK_GRAY);
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("게임 규칙", SwingConstants.CENTER);
        titleLabel.setFont(GameFont.getFont( Font.PLAIN, 60));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 30, 0));
        add(titleLabel, BorderLayout.NORTH);

        String rulesText =
                "## 📌 조작 방법\n\n" +
                        " - 방향 전환: 화면에 표시된 방향키를 눌러 진행 방향을 바꿉니다.\n" +
                        " - 전진: 스페이스바를 누르거나, 방향키를 누를 때마다 캐릭터가 전진합니다.\n" +
                        " - 점프: 스페이스바를 길게 눌러 캐릭터 위의 게이지가 전부 차면 점프를 합니다.\n\n" +
                        "## ⚠️ 게임 오버의 경우\n\n" +
                        " 1. 잘못된 방향으로 움직여 계단 경로를 이탈했을 때\n" +
                        " 2. 주어진 제한 시간 내에 계단을 밟지 못했을 때\n" +
                        " 3. 생명이 0이 되었을 때\n\n" +
                        "## 아이템\n\n" +
                        " - 시계 획득: 시간 제한이 잠시 느려집니다.\n" +
                        " - 시험지 획득: 잠시 동안 점수가 두 배가 됩니다.\n" +
                        " - 하트 획득: 생명이 증가합니다.\n\n"+
                        "## 장애물\n\n" +
                        " - 교수님: 점수가 깎입니다. 점프로 피하십시오.\n" +
                        " - 학생: 화면에 표시되는 문자열을 입력하시오.\n" +
                        " - 버섯: 화면에 표시되는 키를 연타하시오.";;

        JTextArea rulesArea = new JTextArea(rulesText);
        rulesArea.setFont(GameFont.getFont( Font.PLAIN, 18));
        rulesArea.setForeground(Color.WHITE);
        rulesArea.setBackground(new Color(50, 50, 50)); // 어두운 배경색
        rulesArea.setEditable(false);
        rulesArea.setLineWrap(true);
        rulesArea.setWrapStyleWord(true);
        rulesArea.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JScrollPane scrollPane = new JScrollPane(rulesArea);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        add(scrollPane, BorderLayout.CENTER);


        JButton backButton = createStyledButton("돌아가기", 20, new Color(200, 149, 237));
        backButton.setFont(GameFont.getFont( Font.PLAIN, 20));
        backButton.setForeground(Color.BLACK);
        backButton.setBackground(new Color(237, 237, 237));
        backButton.setFocusPainted(false);
        backButton.setActionCommand("BACK_TO_START");
        backButton.addActionListener(this.backListener);

        JPanel southPanel = new JPanel();
        southPanel.setOpaque(false);
        southPanel.add(backButton);
        southPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

        add(southPanel, BorderLayout.SOUTH);
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
}