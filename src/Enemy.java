import javax.swing.*;
import java.awt.*;

public class Enemy {
    public int x, y;
    private Image enemyImage;
    private final int width = 40; // Width of the enemy
    private final int height = 16; // Height of the enemy
    boolean movingRight = true;

    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;

        try {
            enemyImage = new ImageIcon(getClass().getResource("/Image/Enemy.png"))
                    .getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            System.out.println("Enemy image failed to load.");
        }
    }

    public void draw(Graphics g) {
        if (enemyImage != null) {
            g.drawImage(enemyImage, x, y, null);
        } else {
            g.setColor(Color.RED);
            g.fillRect(x, y, width, height); // Drawing a fallback shape
        }
    }

    public void move() {
        int screenWidth = 800;

        if (movingRight) {
            x += 5;
            if (x + width > screenWidth) {
                movingRight = false;
                y += 30;
            }
        } else {
            x -= 5;
            if (x < 0) {
                movingRight = true;
                y += 30;
            }
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
