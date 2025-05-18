import javax.swing.*;
import java.awt.*;

public class Enemy {
    public int x, y;
    private final int width = 80;
    private final int height = 32;
    private Image enemyImage;
    private final int speed;
    private boolean chasingPlayer = true;

    //Image array for generating different enemy
    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
        this.speed = 3 + (int)(Math.random() * 3);

        String[] enemyImages = {"/Image/Naruto.png", "/Image/Goku.png", "/Image/Gojo.png"};

        int index = (int)(Math.random() * enemyImages.length);
        try {
            enemyImage = new ImageIcon(getClass().getResource(enemyImages[index]))
                    .getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            System.out.println("Enemy image failed to load: " + enemyImages[index]);
        }
    }

    public void draw(Graphics g) {
        if (enemyImage != null) {
            g.drawImage(enemyImage, x, y, null);
        }
        else {
            g.setColor(Color.RED);
            g.fillRect(x, y, width, height);
        }
    }

    public void moveToward(Player player) {
        if (chasingPlayer) {
            int targetX = player.x + player.width / 2;
            int targetY = player.y + player.height / 2;

            int dx = targetX - (x + width / 2);
            int dy = targetY - (y + height / 2);
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance != 0) {
                x += (int) (speed * dx / distance);
                y += (int) (speed * dy / distance);
            }

            if (y + height >= player.y && x + width >= player.x && x <= player.x + player.width) {
                chasingPlayer = false;
            }
        }
        else {
            y += speed;
            if(y > 600)
            {
                //disappear
            }
        }
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
