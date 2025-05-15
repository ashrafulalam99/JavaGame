import java.awt.*;
import java.awt.image.BufferedImage;
public class Bullet {
    public int x, y;
    private final int width = 5;
    private final int height = 10;
    private final int speed = 10;

    public Bullet(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void move() {
        y -= speed;
    }

    public void draw(Graphics g) {
        g.setColor(Color.YELLOW.brighter());
        g.fillRect(x, y, width, height);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean collidesWith(Enemy enemy) {
        int enemyWidth = enemy.getWidth();
        int enemyHeight = enemy.getHeight();

        return this.x < enemy.x + enemyWidth &&
                this.x + this.width > enemy.x &&
                this.y < enemy.y + enemyHeight &&
                this.y + this.height > enemy.y;
    }
}
