import java.awt.*;

public class Player {
    public int x, y, width, height;

    private final int pixelSize = 5;

    private final int[][] shape = {
            {0,1,0,1,0},
            {1,1,1,1,1},
            {1,0,1,0,1},
            {1,1,1,1,1},
            {0,1,0,1,0}
    };

    public Player(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void moveLeft() {
        x -= 10;
    }

    public void moveRight() {
        x += 10;
    }

    public void draw(Graphics g) {
        g.setColor(Color.CYAN.darker());
        for (int row = 0; row < shape.length; row++) {
            for (int col = 0; col < shape[row].length; col++) {
                if (shape[row][col] == 1) {
                    g.fillRect(x + col * pixelSize, y + row * pixelSize, pixelSize, pixelSize);
                }
            }
        }
    }
}
