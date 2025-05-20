import javax.swing.*;
import java.awt.*;

public abstract class GameObject {
    protected int x, y, width, height;
    protected Image image;

    public GameObject(int x, int y, int width, int height, String imagePath) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        try {
            image = new ImageIcon(getClass().getResource(imagePath))
                    .getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        } catch (Exception e) {
            System.out.println("Failed to load image: " + imagePath);
        }
    }

    public void draw(Graphics g) {
        g.drawImage(image, x, y, null);
    }

    public abstract void relocate(); 
}
