import java.awt.Graphics;
import java.awt.event.KeyEvent;


public interface GameState {
    void handleInput(KeyEvent e, GameContext context);
    void draw(Graphics g, GameContext context);
}