import java.awt.Color;
import java.awt.Graphics;

public class Player {
    int x, y, width, height;

    public Player(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void draw(Graphics g) {
        g.setColor(Color.GREEN);
        g.fillRect(x, y, width, height);
    }

    public void moveLeft() {
        if (x > 0) {  // Prevent moving off the left edge
            x -= 5;  // Move left by 5 pixels
        }
    }

// Move player right
    public void moveRight() {
        if (x < 760) {  // Prevent moving off the right edge (1000 - 40)
            x += 5;  // Move right by 5 pixels
        }
    }
}