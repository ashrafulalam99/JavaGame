import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GamePanel extends JPanel implements ActionListener {
    private Timer timer;

    public GamePanel() {
        setPreferredSize(new Dimension(1000, 800));
        setBackground(Color.BLACK); // Set the background color

        timer = new Timer(16, this);
        timer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.WHITE);
        g.drawString("Pixel Invaders - Starter", 350, 300);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}
