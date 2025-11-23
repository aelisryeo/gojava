import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class CharacterSelectPanel extends JPanel {

    private final ActionListener completeListener;

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

        for (CharacterSelect charOption : CharacterSelect.values()) {
            JPanel charOptionPanel = createCharacterOptionPanel(charOption, characterGroup, currentCharacter);
            characterContainer.add(charOptionPanel);
        }

        add(characterContainer, BorderLayout.CENTER);

        JButton backButton = new JButton("선택 완료");
        backButton.setFont(new Font("SansSerif", Font.BOLD, 30));
        backButton.setPreferredSize(new Dimension(350, 80));

        backButton.addActionListener(e -> {
            ButtonModel selectedModel = characterGroup.getSelection();
            if (selectedModel != null) {
                completeListener.actionPerformed(new java.awt.event.ActionEvent(
                        backButton, java.awt.event.ActionEvent.ACTION_PERFORMED, selectedModel.getActionCommand()
                ));
            } else {
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

    private JPanel createCharacterOptionPanel(CharacterSelect charOption, ButtonGroup group, CharacterSelect currentCharacter) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        String[] paths = charOption.getImagePath();
        if (paths != null && paths.length > 0) {
            String displayPath = paths[0];
            try {
                BufferedImage img = ImageIO.read(getClass().getResourceAsStream(displayPath));
                if (img != null) {
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
        }
        else {
            JLabel errorLabel = new JLabel("경로 없음");
            errorLabel.setForeground(Color.GRAY);
            errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(errorLabel);
        }


        JRadioButton radioButton = new JRadioButton(charOption.name().replace("CHARACTER_", ""));
        radioButton.setActionCommand(charOption.name());
        radioButton.setForeground(Color.WHITE);
        radioButton.setOpaque(false);
        radioButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (charOption == currentCharacter) {
            radioButton.setSelected(true);
        }

        group.add(radioButton);
        panel.add(radioButton);

        return panel;
    }
}