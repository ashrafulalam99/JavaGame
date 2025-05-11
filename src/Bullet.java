import java.awt.Color;
import java.awt.Graphics;

public class Bullet {
    int x, y, width, height, speed;

    public Bullet(int x, int y) {
        this.x = x;
        this.y = y;
        this.width = 5;
        this.height = 10;
        this.speed = 10;
    }

    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(x, y, width, height);
    }

    public void move() {
        y -= speed;
    }
}
