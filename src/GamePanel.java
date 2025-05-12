import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private Player player;
    private ArrayList<Bullet> bullets;
    private ArrayList<Enemy> enemies;

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);

        player = new Player(400, 580, 40, 20);
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            enemies.add(new Enemy(100 + i * 60, 50));
        }

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

        for (Bullet bullet : bullets) {
            bullet.draw(g);
        }

        for (Enemy enemy : enemies) {
            enemy.draw(g);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (Bullet bullet : bullets) {
            bullet.move();
        }

        for (Enemy enemy : enemies) {
            enemy.move();
        }

        // Collision detection and removal of enemies and bullets
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);
                if (bullet.collidesWith(enemy)) {
                    bullets.remove(i);
                    enemies.remove(j);
                    break; // Exit the loop once a collision is detected
                }
            }
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

