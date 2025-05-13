import javax.swing.*;
import java.awt.*;

public class Enemy {
    public int x, y;
    private final int width = 40;
    private final int height = 16;
    private Image enemyImage;
    private final int speed = 2;
    private boolean chasingPlayer = true;

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
            g.fillRect(x, y, width, height);
        }
    }

    public void moveToward(Player player) {
        if (chasingPlayer) {
            // Calculate target position (center of the player)
            int targetX = player.x + player.width / 2;
            int targetY = player.y + player.height / 2;

            // Calculate the change in position (dx, dy)
            int dx = targetX - (x + width / 2);
            int dy = targetY - (y + height / 2);

            // Calculate distance between enemy and player
            double distance = Math.sqrt(dx * dx + dy * dy);

            // Move the enemy towards the player, adjusting for speed
            if (distance != 0) {
                // Move horizontally towards the player
                x += (int) (speed * dx / distance);

                // Move vertically towards the player
                y += (int) (speed * dy / distance);
            }

            // If the enemy collides with the player, remove the enemy (this is handled in GamePanel later)
            if (y + height >= player.y && x + width >= player.x && x <= player.x + player.width) {
                chasingPlayer = false; // Stop chasing if the enemy touches the player
            }
        } else {
            // Move the enemy down the screen after stopping the chase
            y += speed;

            // Once the enemy moves off the screen, remove it
            if (y > 600) {
                // The enemy moves out of screen area
            }
        }
    }

    public int getWidth() { return width; }

    public int getHeight() { return height; }
}
