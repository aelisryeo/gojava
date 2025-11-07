import java.awt.Graphics;
import java.awt.event.KeyEvent;

public class ClimbingState implements GameState {
    @Override
    public void handleInput(KeyEvent e, GameContext context) {
        String pressedKey = KeyEvent.getKeyText(e.getKeyCode()).toUpperCase();
        boolean isKeyValid = false;

    }

    @Override
    public void draw(Graphics g, GameContext context) {
        // 등반 상태에서는 특별히 덧그릴 UI가 없습니다 (배경, 캐릭터는 OlaOla가 그림)
    }
}