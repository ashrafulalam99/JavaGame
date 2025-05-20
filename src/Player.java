public class Player extends GameObject implements Movable{

    public Player(int x, int y, int width, int height) {
        super(x, y, width, height, "/Image/Player.png");
    }

    public void moveLeft() {
        x = Math.max(0, x - 10);
    }

    public void moveRight() {
        x = Math.min(700, x + 10);
    }

    @Override
    public void relocate() {
        x = 350; 
    }
}
