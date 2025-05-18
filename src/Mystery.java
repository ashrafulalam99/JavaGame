import javax.swing.*;
import java.awt.*;

public class Mystery {
    public int x, y;
    public int width = 40, height = 60;
    private Image image;

    //Initialize with Image
    public Mystery(int x, int y) {
        this.x = x;
        this.y = y;
        this.image = new ImageIcon(getClass().getResource("/Image/Mystery.png")).getImage();
    }

    public void draw(Graphics g) {
        g.drawImage(image, x, y, width, height, null);
    }

    public void move() {
        y += 8; //Downward movement
    }

    public Rectangle getBounds()
    {
        return new Rectangle(x, y, width, height);
    }
}
