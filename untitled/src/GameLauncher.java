import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

// 게임 실행

public class GameLauncher {

    private JFrame launchFrame;
    public enum GamePanelState { GAME_START, GAME_PLAY, GAME_OVER }

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

        ActionListener startAction = e -> {
            String command = e.getActionCommand();
            GameMode selectedMode = GameMode.valueOf(command);
            showGamePanel(selectedMode);
        };


            StartPanel startPanel = new StartPanel(startAction);
            launchFrame.add(startPanel);

            launchFrame.pack();
            launchFrame.setLocationRelativeTo(null);
            launchFrame.setVisible(true);
            launchFrame.repaint();
        }
        public void showGamePanel(GameMode selectedMode) {
            launchFrame.getContentPane().removeAll();

            JPanel playGamePanel = null;
            if (selectedMode == GameMode.CLASSIC_MODE) {
                playGamePanel = new OlaOla(this);
            }
            else if (selectedMode==GameMode.TEST_MODE) {
                playGamePanel = new TestIsComing(this);
            }
            if (playGamePanel!=null) {
                launchFrame.add(playGamePanel);
                launchFrame.pack();
                launchFrame.setLocationRelativeTo(null);
                playGamePanel.requestFocus();
                launchFrame.revalidate();
                launchFrame.repaint();
            }
        } //게임오버패널만들면서 launcher도 수정함
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
