import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;

// 게임 실행

public class GameLauncher {

    private JFrame launchFrame;

    private CharacterSelect selectedCharacter = CharacterSelect.CHARACTER_수룡; // 기본값

    //public enum GamePanelState { GAME_START, GAME_PLAY, GAME_OVER }

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
            // ⭐️ 저장된 selectedCharacter를 사용하여 게임을 시작합니다.
            showGamePanel(selectedMode, this.selectedCharacter);
        };
        // 2. 캐릭터 선택 리스너 정의: StartPanel에서 캐릭터 선택 버튼을 눌렀을 때 실행됨.
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
    // ⭐️ 캐릭터 선택 패널을 띄우는 새로운 메소드
    public void showCharacterSelectPanel() {
        launchFrame.getContentPane().removeAll();

        // 캐릭터 선택 완료 리스너 정의: CharacterSelectPanel에서 선택 완료 후 실행됨.
        ActionListener selectCompleteAction = e -> {
            try {
                // 선택된 캐릭터 정보를 업데이트합니다.
                this.selectedCharacter = CharacterSelect.valueOf(e.getActionCommand());
            } catch (IllegalArgumentException ex) {
                System.err.println("잘못된 캐릭터 선택 커맨드: " + e.getActionCommand());
            }
            // 다시 StartPanel로 돌아갑니다.
            showStartPanel();
        };

        // 현재 선택된 캐릭터 정보를 전달하여 CharacterSelectPanel을 생성합니다.
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
            String[] characterImagePath = selectedCharacter.getImagePath(); // 경로 추출
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
