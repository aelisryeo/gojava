import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class CharacterSelectPanel extends JPanel {

    private final ActionListener completeListener;

    // ⭐️ 현재 선택된 캐릭터 정보를 받아서 라디오 버튼 초기 선택에 사용합니다.
    public CharacterSelectPanel(ActionListener completeListener, CharacterSelect currentCharacter) {
        this.completeListener = completeListener;
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.DARK_GRAY);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("캐릭터를 선택하세요", SwingConstants.CENTER);
        header.setFont(new Font("sansSerif", Font.BOLD, 40));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(50, 0, 30, 0));
        add(header, BorderLayout.NORTH);

        JPanel characterContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 50));
        characterContainer.setOpaque(false);

        ButtonGroup characterGroup = new ButtonGroup();

        // 각 캐릭터에 대한 선택 영역 생성
        for (CharacterSelect charOption : CharacterSelect.values()) {
            JPanel charOptionPanel = createCharacterOptionPanel(charOption, characterGroup, currentCharacter);
            characterContainer.add(charOptionPanel);
        }

        add(characterContainer, BorderLayout.CENTER);

        // 선택 완료 버튼
        JButton backButton = new JButton("선택 완료");
        backButton.setFont(new Font("SansSerif", Font.BOLD, 30));
        backButton.setPreferredSize(new Dimension(350, 80));

        // ⭐️ ActionCommand는 현재 그룹에서 선택된 라디오 버튼의 ActionCommand (캐릭터 ENUM 이름)이 됩니다.
        backButton.addActionListener(e -> {
            ButtonModel selectedModel = characterGroup.getSelection();
            if (selectedModel != null) {
                // 선택된 캐릭터의 ENUM 이름을 ActionCommand로 전달합니다.
                completeListener.actionPerformed(new java.awt.event.ActionEvent(
                        backButton, java.awt.event.ActionEvent.ACTION_PERFORMED, selectedModel.getActionCommand()
                ));
            } else {
                // 선택된 것이 없으면 기본값(또는 이전 값)으로 돌아가게 할 수도 있습니다.
                // 여기서는 선택된 것이 없어도 기본 ActionCommand를 넘겨서 StartPanel로 돌아가게 합니다.
                completeListener.actionPerformed(new java.awt.event.ActionEvent(
                        backButton, java.awt.event.ActionEvent.ACTION_PERFORMED, currentCharacter.name()
                ));
            }
        });

        JPanel southPanel = new JPanel();
        southPanel.setOpaque(false);
        southPanel.add(backButton);
        add(southPanel, BorderLayout.SOUTH);
    }

    // 캐릭터 이미지와 라디오 버튼을 포함하는 개별 패널 생성
    private JPanel createCharacterOptionPanel(CharacterSelect charOption, ButtonGroup group, CharacterSelect currentCharacter) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        try {
            BufferedImage img = ImageIO.read(getClass().getResourceAsStream(charOption.getImagePath()));
            if (img != null) {
                // 이미지를 JLabell Icon으로 변환하여 추가
                JLabel imageLabel = new JLabel(new ImageIcon(img.getScaledInstance(128, 128, Image.SCALE_SMOOTH)));
                imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(imageLabel);
            }
        } catch (IOException e) {
            JLabel errorLabel = new JLabel("이미지 로드 실패");
            errorLabel.setForeground(Color.RED);
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(errorLabel);
        }

        JRadioButton radioButton = new JRadioButton(charOption.name().replace("CHARACTER_", ""));
        radioButton.setActionCommand(charOption.name()); // ActionCommand에 캐릭터 enum 이름 설정
        radioButton.setForeground(Color.WHITE);
        radioButton.setOpaque(false);
        radioButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 현재 선택된 캐릭터라면 기본으로 선택되게 합니다.
        if (charOption == currentCharacter) {
            radioButton.setSelected(true);
        }

        group.add(radioButton);
        panel.add(radioButton);

        return panel;
    }
}