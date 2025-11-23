import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;


public class GameLauncher {

    private JFrame launchFrame;

    private CharacterSelect selectedCharacter = CharacterSelect.CHARACTER_기쁜수룡;


    public enum GameMode {CLASSIC_MODE, TEST_MODE}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GameLauncher().initialize();

        });
    }
    public void initialize() {
        launchFrame = new JFrame("올라올라");
        launchFrame.setResizable(false);
        launchFrame.setLocationRelativeTo(null);
        launchFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        showStartPanel();
        launchFrame.setVisible(true);
    }
    public void showStartPanel() {

        launchFrame.getContentPane().removeAll();

        ActionListener startAction = e-> {
            GameMode selectedMode = GameMode.valueOf(e.getActionCommand());
            showGamePanel(selectedMode, this.selectedCharacter);
        };

        ActionListener characterSelectAction = e -> {
            showCharacterSelectPanel();
        };

            StartPanel startPanel = new StartPanel(startAction, characterSelectAction, this.selectedCharacter);
            launchFrame.add(startPanel);

            launchFrame.pack();
            launchFrame.setLocationRelativeTo(null);
            launchFrame.setVisible(true);
            launchFrame.repaint();
        }

    public void showCharacterSelectPanel() {
        launchFrame.getContentPane().removeAll();

        ActionListener selectCompleteAction = e -> {
            try {
                this.selectedCharacter = CharacterSelect.valueOf(e.getActionCommand());
            } catch (IllegalArgumentException ex) {
                System.err.println("잘못된 캐릭터 선택 커맨드: " + e.getActionCommand());
            }
            showStartPanel();
        };

        CharacterSelectPanel selectPanel = new CharacterSelectPanel(selectCompleteAction, this.selectedCharacter);
        launchFrame.add(selectPanel);

        launchFrame.pack();
        launchFrame.setLocationRelativeTo(null);
        launchFrame.revalidate();
        launchFrame.repaint();
    }

        public void showGamePanel(GameMode selectedMode, CharacterSelect selectedCharacter) {
            launchFrame.getContentPane().removeAll();

            JPanel playGamePanel = null;
            if (selectedMode == GameMode.CLASSIC_MODE) {
                playGamePanel = new OlaOla(this, selectedCharacter);
            }
            else if (selectedMode==GameMode.TEST_MODE) {
                playGamePanel = new TestIsComing(this, selectedCharacter);
            }
            if (playGamePanel!=null) {
                launchFrame.add(playGamePanel);
                launchFrame.pack();
                launchFrame.setLocationRelativeTo(null);
                playGamePanel.requestFocus();
                launchFrame.revalidate();
                launchFrame.repaint();
            }
        }
        public void showGameOverPanel(int finalScore) {
            launchFrame.getContentPane().removeAll();

            GameOverPanel gameOverPanel = new GameOverPanel(this, finalScore);
            launchFrame.add(gameOverPanel);
            launchFrame.pack();
            launchFrame.setLocationRelativeTo(null);
            launchFrame.revalidate();
            launchFrame.repaint();
        }
    }
