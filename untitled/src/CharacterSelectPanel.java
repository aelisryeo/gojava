import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.border.EmptyBorder;
import java.io.IOException;

public class CharacterSelectPanel extends JPanel {

    private final ActionListener completeListener;

    public CharacterSelectPanel(ActionListener completeListener, CharacterSelect currentCharacter) {
        this.completeListener = completeListener;
        setPreferredSize(new Dimension(800, 600));

        setBackground(Color.WHITE);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("캐릭터를 선택하세요", SwingConstants.CENTER);
        header.setFont(GameFont.getFont(Font.PLAIN, 45f));
        header.setForeground(new Color(50, 50, 50));
        header.setBorder(BorderFactory.createEmptyBorder(50, 0, 30, 0));
        add(header, BorderLayout.NORTH);

        JPanel characterContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 20));
        characterContainer.setOpaque(false);

        ButtonGroup characterGroup = new ButtonGroup();

        for (CharacterSelect charOption : CharacterSelect.values()) {
            JPanel charOptionPanel = createCharacterCard(charOption, characterGroup, currentCharacter);
            characterContainer.add(charOptionPanel);
        }

        add(characterContainer, BorderLayout.CENTER);

        JButton backButton = createStyledButton("결정", 24, new Color(200, 149, 237));
        backButton.setPreferredSize(new Dimension(300, 70));

        backButton.addActionListener(e -> {
            ButtonModel selectedModel = characterGroup.getSelection();
            String command = (selectedModel != null) ? selectedModel.getActionCommand() : currentCharacter.name();

            completeListener.actionPerformed(new java.awt.event.ActionEvent(
                    backButton, java.awt.event.ActionEvent.ACTION_PERFORMED, command
            ));
        });

        JPanel southPanel = new JPanel();
        southPanel.setOpaque(false);
        southPanel.setBorder(new EmptyBorder(0, 0, 40, 0));
        southPanel.add(backButton);
        add(southPanel, BorderLayout.SOUTH);
    }

    private JPanel createCharacterCard(CharacterSelect charOption, ButtonGroup group, CharacterSelect currentCharacter) {

        JRadioButton radioButton = new JRadioButton();
        radioButton.setActionCommand(charOption.name());

        if (charOption == currentCharacter) {
            radioButton.setSelected(true);
        }
        group.add(radioButton);

        final boolean[] isHovered = {false};

        JPanel cardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (radioButton.isSelected()) {
                    g2.setColor(new Color(245, 230, 245));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                }

                else if (isHovered[0]) {
                    g2.setColor(new Color(240, 240, 240));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);

                    g2.setColor(Color.LIGHT_GRAY);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 30, 30);
                }
            }
        };

        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setOpaque(false);
        cardPanel.setPreferredSize(new Dimension(180, 220));
        cardPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        cardPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        String[] paths = charOption.getImagePath();
        if (paths != null && paths.length > 0) {
            try {
                BufferedImage img = ImageIO.read(getClass().getResourceAsStream(paths[0]));
                if (img != null) {
                    JLabel imageLabel = new JLabel(new ImageIcon(img.getScaledInstance(150, 150, Image.SCALE_SMOOTH)));
                    imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    cardPanel.add(imageLabel);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        cardPanel.add(Box.createVerticalStrut(20));

        String charName = charOption.name().replace("CHARACTER_", "");
        JLabel nameLabel = new JLabel(charName);
        nameLabel.setFont(GameFont.getFont(Font.PLAIN, 20f));
        nameLabel.setForeground(Color.DARK_GRAY);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(nameLabel);

        cardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                radioButton.setSelected(true);
                cardPanel.getParent().repaint();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered[0] = true; // 호버 상태 ON
                cardPanel.repaint(); // 내 모습 다시 그리기
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered[0] = false;
                cardPanel.repaint();
            }
        });

        radioButton.addActionListener(e -> cardPanel.getParent().repaint());

        return cardPanel;
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

                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);


                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(2, 2, getWidth()-5, getHeight()-5, 40, 40);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        button.setFont(GameFont.getFont(Font.BOLD, (float)fontSize));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }
}