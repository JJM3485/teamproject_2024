import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Tetris extends JFrame {
    private JLabel label, gameLabel;
    private JButton startButton, exitButton, easyButton, mediumButton, hardButton;
    private ImageIcon originalIcon, easyIcon;
    private Timer timer;
    private int gameSpeed = 500;
    private final int initialWidth = 800;
    private final int initialHeight = 800;
    private int[][] board = new int[20][10];
    private JLabel[][] cellLabels = new JLabel[20][10];
    private int currentX, currentY, rotation = 0;
    private Color currentColor = Color.BLUE;

    // L자 모양 블록 정의 (회전 상태 포함)
    private final int[][][] L_SHAPE = {
        {{0, 0, 0, 0}, {0, 1, 1, 1}, {0, 0, 0, 1}, {0, 0, 0, 0}},  // 0도
        {{0, 0, 0, 0}, {0, 0, 0, 1}, {0, 0, 0, 1}, {0, 0, 1, 1}},  // 90도
        {{0, 0, 0, 0}, {0, 0, 0, 0}, {0, 1, 0, 0}, {0, 1, 1, 1}},  // 180도
        {{0, 1, 1, 0}, {0, 1, 0, 0}, {0, 1, 0, 0}, {0, 0, 0, 0}}   // 270도
    };

    public Tetris() {
        setTitle("테트리스");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container c = getContentPane();
        c.setLayout(null);
        setSize(initialWidth, initialHeight);

        originalIcon = new ImageIcon("images/first.png");

        label = new JLabel();
        label.setBounds(0, 0, initialWidth, initialHeight);
        updateBackgroundImage();

        startButton = new JButton("시작하기");
        startButton.setBounds(350, 500, 100, 50);

        exitButton = new JButton("종료하기");
        exitButton.setBounds(350, 580, 100, 50);

        startButton.addActionListener(e -> showDifficultyButtons());
        exitButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(null, "정말 종료하시겠습니까?", "종료 확인",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) System.exit(0);
        });

        label.add(startButton);
        label.add(exitButton);
        c.add(label);

        setFocusable(true); // 키보드 입력 활성화
        addKeyListener(new TetrisKeyListener()); // 키 리스너 추가
        setVisible(true);
    }

    private void showDifficultyButtons() {
        startButton.setVisible(false);
        exitButton.setVisible(false);

        easyButton = new JButton("난이도 하");
        easyButton.setBounds(350, 400, 100, 50);
        easyButton.addActionListener(e -> startGame(500));

        mediumButton = new JButton("난이도 중");
        mediumButton.setBounds(350, 470, 100, 50);
        mediumButton.addActionListener(e -> startGame(300));

        hardButton = new JButton("난이도 상");
        hardButton.setBounds(350, 540, 100, 50);
        hardButton.addActionListener(e -> startGame(100));

        label.add(easyButton);
        label.add(mediumButton);
        label.add(hardButton);

        label.revalidate();
        label.repaint();
    }

    private void startGame(int speed) {
        gameSpeed = speed;
        initializeGameBoard();

        timer = new Timer(gameSpeed, e -> moveBlockDown());
        timer.start();
    }

    private void initializeGameBoard() {
        getContentPane().removeAll();
        revalidate();
        repaint();

        easyIcon = new ImageIcon("images/easy.jpg");
        JLabel easyLabel = new JLabel(easyIcon);
        easyLabel.setLayout(null);
        easyLabel.setBounds(0, 0, getWidth(), getHeight());

        gameLabel = new JLabel();
        gameLabel.setBounds(100, 100, 250, 600);
        gameLabel.setOpaque(true);
        gameLabel.setBackground(Color.BLACK);
        gameLabel.setLayout(new GridLayout(20, 10));
        easyLabel.add(gameLabel);

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                cellLabels[i][j] = new JLabel();
                cellLabels[i][j].setOpaque(true);
                cellLabels[i][j].setBackground(Color.BLACK);
                gameLabel.add(cellLabels[i][j]);
            }
        }

        getContentPane().add(easyLabel);
        revalidate();
        repaint();

        startFallingBlock();
    }

    private void startFallingBlock() {
        rotation = 0;
        currentX = 0;
        currentY = 4;

        if (timer == null || !timer.isRunning()) {
            timer = new Timer(gameSpeed, e -> moveBlockDown());
            timer.start();
        }
    }

    private void moveBlockDown() {
        if (canMove(currentX + 1, currentY)) {
            currentX++;
            drawBlock();
        } else {
            fixBlock();
            startFallingBlock();
        }
    }

    private void drawBlock() {
        clearBoard();
        int[][] shape = L_SHAPE[rotation];

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1 && currentX + i < 20) {
                    cellLabels[currentX + i][currentY + j].setBackground(currentColor);
                }
            }
        }
    }

    private boolean canMove(int x, int y) {
        int[][] shape = L_SHAPE[rotation];

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    int newX = x + i;
                    int newY = y + j;
                    if (newX >= 20 || newY < 0 || newY >= 10 || board[newX][newY] == 1) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void fixBlock() {
        int[][] shape = L_SHAPE[rotation];

        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] == 1) {
                    board[currentX + i][currentY + j] = 1;
                    cellLabels[currentX + i][currentY + j].setBackground(currentColor);
                }
            }
        }
    }

    private void clearBoard() {
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                if (board[i][j] == 0) {
                    cellLabels[i][j].setBackground(Color.BLACK);
                }
            }
        }
    }

    private void updateBackgroundImage() {
        int width = getWidth();
        int height = getHeight();

        Image img = originalIcon.getImage();
        Image resizedImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(resizedImage);

        label.setIcon(resizedIcon);
        label.setBounds(0, 0, width, height);
    }

    // 키 입력 처리
    private class TetrisKeyListener extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT: // 왼쪽 이동
                    if (canMove(currentX, currentY - 1)) {
                        currentY--;
                        drawBlock();
                    }
                    break;
                case KeyEvent.VK_RIGHT: // 오른쪽 이동
                    if (canMove(currentX, currentY + 1)) {
                        currentY++;
                        drawBlock();
                    }
                    break;
                case KeyEvent.VK_DOWN: // 빠르게 내려가기
                    if (canMove(currentX + 1, currentY)) {
                        currentX++;
                        drawBlock();
                    }
                    break;
                case KeyEvent.VK_UP: // 블록 회전
                    int nextRotation = (rotation + 1) % 4;
                    rotation = canMove(currentX, currentY) ? nextRotation : rotation;
                    drawBlock();
                    break;
            }
        }
    }

    public static void main(String[] args) {
        new Tetris();
    }
}
