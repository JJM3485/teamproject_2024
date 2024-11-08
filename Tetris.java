import java.awt.*;
import javax.swing.*;

public class Tetris extends JFrame {
    public Tetris() {
        setTitle("테트리스");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // JFrame으로 수정
        Container c = getContentPane();
        c.setLayout(new FlowLayout());

        setSize(350, 150);
        setVisible(true);
    }

    public static void main(String [] args) {
        new Tetris();
    }
}
