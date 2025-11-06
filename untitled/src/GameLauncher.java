import javax.swing.*;
import java.awt.event.ActionListener;

// 게임 실행

public class GameLauncher {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("올라올라");
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            ActionListener startAction = e -> {
                String command = e.getActionCommand();
                GameMode selectedMode = GameMode.valueOf(command);

                frame.getContentPane().removeAll();

                JPanel playGamePanel = null;
                if (selectedMode == GameMode.CLASSIC_MODE) {
                    playGamePanel = new OlaOla();
                } else if (selectedMode == GameMode.TEST_MODE) {
                    playGamePanel = new TestIsComing();
                }

                if (playGamePanel != null) {
                    frame.add(playGamePanel);
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    playGamePanel.requestFocusInWindow();
                    frame.revalidate();
                    frame.repaint();
                }
            };

            StartPanel startPanel = new StartPanel(startAction);
            frame.add(startPanel);

            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}