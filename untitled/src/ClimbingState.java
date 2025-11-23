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
    }
}