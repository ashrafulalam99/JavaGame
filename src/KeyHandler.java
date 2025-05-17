import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyHandler extends KeyAdapter {
    private final Player player;
    private final GamePanel panel;

    public KeyHandler(Player player, GamePanel panel) {
        this.player = player;
        this.panel = panel;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (panel.isGameOver()) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                player.moveLeft();
                break;
            case KeyEvent.VK_RIGHT:
                player.moveRight();
                break;
            case KeyEvent.VK_SPACE:
                panel.handleBulletFire(); // ✅ Correct usage — called on GamePanel instance
                break;
        }
    }
}
