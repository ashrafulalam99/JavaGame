import java.awt.*;

public class GameRenderer {
    private final GamePanel panel;

    public GameRenderer(GamePanel panel) {
        this.panel = panel;
    }

    public void render(Graphics g) {
        Image backgroundImage = panel.getBackgroundImage();
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, panel.getWidth(), panel.getHeight(), panel);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, panel.getWidth(), panel.getHeight());
        }

        if (!panel.getGameOver().shouldHidePlayer()) {
            panel.getPlayer().draw(g);
        }

        for (Bullet bullet : panel.getBullets()) {
            bullet.draw(g);
        }

        if (!panel.isShowDestruction()) {
            for (Enemy enemy : panel.getEnemies()) {
                enemy.draw(g);
            }
        } else if (panel.getDestructionImage() != null) {
            g.drawImage(panel.getDestructionImage(),
                    panel.getDestructionX(),
                    panel.getDestructionY(),
                    200, 200, panel);
        }

        if (!panel.getGameOver().isTriggered()) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("Score: " + panel.getScoreManager().getScore(), 650, 30);
        }

        if (panel.getMystery() != null && !panel.getGameOver().isTriggered()) {
            panel.getMystery().draw(g);
        }

        if (panel.getGameOver().isVisible()) {
            panel.getGameOver().draw(g, panel.getWidth());
        }
    }
}
