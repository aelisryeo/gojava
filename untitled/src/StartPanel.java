import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class StartPanel extends JPanel {
    private BufferedImage bImage;
    private BufferedImage characterImage;
    private CharacterSelect selectedCharacter = CharacterSelect.CHARACTER_ONE;

    public StartPanel(ActionListener startListener, ActionListener characterSelectListener, CharacterSelect currentCharacter) {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.DARK_GRAY);
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("올라올라", SwingConstants.CENTER);
        titleLabel.setFont(new Font("sansSerif", Font.BOLD, 60));
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setOpaque(false);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));

        // 1. 캐릭터 선택 영역 (이미지 + 버튼)
        JPanel characterSelectionArea = new JPanel();
        characterSelectionArea.setOpaque(false);
        characterSelectionArea.setLayout(new BoxLayout(characterSelectionArea, BoxLayout.Y_AXIS));

        // 캐릭터 선택 버튼
        JButton selectCharButton = new JButton("캐릭터 선택");
        selectCharButton.setFont(new Font("SansSerif", Font.BOLD, 20));
        selectCharButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        selectCharButton.addActionListener(characterSelectListener); // CharacterSelectPanel로 이동

        /*
        JPanel characterSelectPanel = new JPanel((new FlowLayout(FlowLayout.CENTER,20,10)));
        characterSelectPanel.setOpaque(false);
        JLabel charSelectTitle = new JLabel("캐릭터 선택:");
        charSelectTitle.setForeground(Color.WHITE);
        charSelectTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        characterSelectPanel.add(charSelectTitle);

        ButtonGroup characterGroup = new ButtonGroup();

        // 캐릭터 옵션들을 순회하며 라디오 버튼 생성
        for (CharacterSelect charOption : CharacterSelect.values()) {
            JRadioButton charButton = new JRadioButton(
                    charOption.name().replace("CHARACTER_", "캐릭터 ") // 버튼 텍스트
            );
            charButton.setActionCommand(charOption.name()); // ActionCommand에 캐릭터 enum 이름 설정
            charButton.setForeground(Color.WHITE);
            charButton.setOpaque(false);
            characterGroup.add(charButton);
            characterSelectPanel.add(charButton);

            // 첫 번째 캐릭터를 기본으로 선택
            if (charOption == CharacterSelect.CHARACTER_ONE) {
                charButton.setSelected(true);
            }

            // 라디오 버튼 리스너: 선택된 캐릭터를 업데이트
            charButton.addActionListener(e -> {
                try {
                    selectedCharacter = CharacterSelect.valueOf(e.getActionCommand());
                } catch (IllegalArgumentException ex) {
                    System.err.println("알 수 없는 캐릭터 선택: " + e.getActionCommand());
                }
            });
        }

        ActionListener modeSelectListener = e -> {
            String combinedCommand = e.getActionCommand() + "_" + selectedCharacter.name();
            // 최종 조합된 ActionCommand를 GameLauncher의 리스너에게 전달
            startListener.actionPerformed(new ActionEvent(e.getSource(), e.getID(), combinedCommand));
        };

         */

        JButton startButton = new JButton("올라올라");
        startButton.setFont(new Font("SansSerif", Font.BOLD, 30));
        startButton.setPreferredSize(new Dimension(300, 80));
        startButton.setActionCommand("CLASSIC_MODE");
        startButton.addActionListener(startListener);

        JButton modeStartButton = new JButton("술래잡기");
        modeStartButton.setFont(new Font("SansSerif", Font.BOLD, 30));
        modeStartButton.setPreferredSize(new Dimension(300, 80));
        modeStartButton.setActionCommand("TEST_MODE");
        modeStartButton.addActionListener(startListener);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 150));
        buttonPanel.add(startButton);
        buttonPanel.add(modeStartButton);





        /*
        JPanel centerContainer = new JPanel();
        centerContainer.setOpaque(false);
        centerContainer.setLayout(new BoxLayout(centerContainer,BoxLayout.Y_AXIS));

        // 가운데 정렬을 위해 컴포넌트의 정렬을 CENTER로 설정
        charSelectTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        characterSelectPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerContainer.add(Box.createVerticalStrut(50)); // 상단 여백
        centerContainer.add(characterSelectPanel);
        centerContainer.add(Box.createVerticalStrut(50)); // 버튼과 선택창 사이 여백
        centerContainer.add(buttonPanel);
        centerContainer.add(Box.createVerticalGlue()); // 하단 여백
         */

        // 4. 중앙 컨테이너 구성
        JPanel centerContainer = new JPanel();
        centerContainer.setOpaque(false);
        centerContainer.setLayout(new BoxLayout(centerContainer, BoxLayout.Y_AXIS));

        // 캐릭터 선택 영역 추가
        centerContainer.add(Box.createVerticalGlue()); // 상단 여백
        centerContainer.add(selectCharButton);
        centerContainer.add(Box.createVerticalStrut(30)); // 버튼과 게임 버튼 사이 여백

        // 게임 모드 버튼 패널 추가
        centerContainer.add(buttonPanel);
        centerContainer.add(Box.createVerticalStrut(50));
        centerContainer.add(Box.createVerticalGlue()); // 하단 여백
        try {

            String charPath = currentCharacter.getImagePath();
            characterImage = ImageIO.read(getClass().getResourceAsStream(charPath));
            bImage = ImageIO.read(getClass().getResourceAsStream("image/startBackground.png"));

            if (bImage == null) {
                System.err.println("배경 이미지 로드 실패: startBackground.png 파일을 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            System.err.println("배경 이미지 로드 중 예외 발생");
            e.printStackTrace();
        }

        // 제목은 상단(NORTH)에, 버튼 패널은 중앙(CENTER)에 배치
        add(titleLabel, BorderLayout.NORTH);
        //add(buttonPanel, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bImage != null) {
            g.drawImage(bImage, 0, 0, getWidth(), getHeight(), this);
        }
        // ⭐️ 캐릭터 이미지 그리기
        if (characterImage != null) {
            int charWidth = 64; // 캐릭터 크기 설정 (예시)
            int charHeight = 64;
            int x = (getWidth() / 2) - (charWidth / 2); // 중앙 정렬
            int y = (getHeight() / 2) - 150; // 버튼 위에 위치 조정 (조정 필요)
            g.drawImage(characterImage, x, y, charWidth, charHeight, this);
        }
    }
}