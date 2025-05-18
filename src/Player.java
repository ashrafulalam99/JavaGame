import javax.swing.*;
import java.awt.*;

public class Player {
    public int x, y, width, height;
    private Image playerImage;
    private final int pixelSize = 5;

    //Initialization with Image
    public Player(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        try {
            playerImage = new ImageIcon(getClass().getResource("/Image/Player.png"))
                    .getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            System.out.println("Player image failed to load.");
        }
    }

    public void moveLeft() {
        x = Math.max(0, x - 10);
    }

    public void moveRight() {
        x = Math.min(700, x + 10);
    }

    public void draw(Graphics g) {
        g.drawImage(playerImage, x, y, null);
    }
}
