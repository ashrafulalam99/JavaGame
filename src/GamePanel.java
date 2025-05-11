import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private Player player;
    private ArrayList<Bullet> bullets;

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);

        player = new Player(400, 580, 40, 20);
        bullets = new ArrayList<>();

        timer = new Timer(16, this);
        timer.start();

        addKeyListener(this);
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.WHITE);
        g.drawString("Pixel Invaders - Starter", 5, 15);

        player.draw(g);

        for (Bullet bullet : bullets)
        {
            bullet.draw(g);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (Bullet bullet : bullets) {
            bullet.move();
        }

        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_LEFT) {
            player.moveLeft();
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            player.moveRight();
        } else if (keyCode == KeyEvent.VK_SPACE) {
            bullets.add(new Bullet(player.x + player.width / 2 - 2, player.y));
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}
