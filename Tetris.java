import java.awt.*;
import java.awt.event.*;
import java.util.Random;
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
    private int blockType;

    // SHAPE 배열은 그대로 두고, 랜덤으로 블록을 선택할 예정입니다.
    static final int[][][][] SHAPE = {
        { // ㄱ 모양 블록
            { {0,0,0,0},{0,1,1,1},{0,0,0,1},{0,0,0,0} },
            { {0,0,0,0},{0,0,0,1},{0,0,0,1},{0,0,1,1} },
            { {0,0,0,0},{0,0,0,0},{0,1,0,0},{0,1,1,1} },
            { {0,1,1,0},{0,1,0,0},{0,1,0,0},{0,0,0,0} }
        },
        { // ㅁ 모양 블록
            { {0,0,0,0},{0,1,1,0},{0,1,1,0},{0,0,0,0} },
            { {0,0,0,0},{0,1,1,0},{0,1,1,0},{0,0,0,0} },
            { {0,0,0,0},{0,1,1,0},{0,1,1,0},{0,0,0,0} },
            { {0,0,0,0},{0,1,1,0},{0,1,1,0},{0,0,0,0} }
        },
        { // ㄴ 모양 블록
            { {0,0,0,0},{1,0,0,0},{1,1,1,0},{0,0,0,0} },
            { {0,0,0,0},{0,1,1,0},{0,1,0,0},{0,1,0,0} },
            { {0,0,0,0},{0,0,0,0},{1,1,1,0},{0,0,1,0} },
            { {0,1,0,0},{0,1,0,0},{1,1,0,0},{0,0,0,0} }
        },
        { // -_ 모양 블록 (S 모양)
            { {0,0,0,0},{1,1,0,0},{0,1,1,0},{0,0,0,0} },
            { {0,0,0,0},{0,0,1,0},{0,1,1,0},{0,1,0,0} },
            { {0,0,0,0},{1,1,0,0},{0,1,1,0},{0,0,0,0} },
            { {0,0,0,0},{0,0,1,0},{0,1,1,0},{0,1,0,0} }
        },
        { // _- 모양 블록 (Z 모양)
            { {0,0,0,0},{0,1,1,0},{1,1,0,0},{0,0,0,0} },
            { {0,0,0,0},{0,1,0,0},{0,1,1,0},{0,0,1,0} },
            { {0,0,0,0},{0,1,1,0},{1,1,0,0},{0,0,0,0} },
            { {0,0,0,0},{0,1,0,0},{0,1,1,0},{0,0,1,0} }
        },
        { // ㅡ 모양 블록 (I 모양)
            { {0,1,0,0},{0,1,0,0},{0,1,0,0},{0,1,0,0} },
            { {0,0,0,0},{1,1,1,1},{0,0,0,0},{0,0,0,0} },
            { {0,1,0,0},{0,1,0,0},{0,1,0,0},{0,1,0,0} },
            { {0,0,0,0},{1,1,1,1},{0,0,0,0},{0,0,0,0} }
        },
        { // ㅗ 모양 블록 (T 모양)
            { {0,0,0,0},{0,1,0,0},{1,1,1,0},{0,0,0,0} },
            { {0,0,0,0},{0,1,0,0},{0,1,1,0},{0,1,0,0} },
            { {0,0,0,0},{0,0,0,0},{1,1,1,0},{0,1,0,0} },
            { {0,0,0,0},{0,1,0,0},{1,1,0,0},{0,1,0,0} }
        }
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
        easyButton.addActionListener(e -> startGame(500,1));

        mediumButton = new JButton("난이도 중");
        mediumButton.setBounds(350, 470, 100, 50);
        mediumButton.addActionListener(e -> startGame(300,2));

        hardButton = new JButton("난이도 상");
        hardButton.setBounds(350, 540, 100, 50);
        hardButton.addActionListener(e -> startGame(100,3));

        label.add(easyButton);
        label.add(mediumButton);
        label.add(hardButton);

        label.revalidate();
        label.repaint();
    }

    private void startGame(int speed,int nan) {
        gameSpeed = speed;
        initializeGameBoard(nan);

        timer = new Timer(gameSpeed, e -> moveBlockDown());
        timer.start();
    }

    private void initializeGameBoard(int nan) {
        getContentPane().removeAll();
        revalidate();
        repaint();

        if (nan == 1) {
            easyIcon = new ImageIcon("images/easy.jpg");
        }
        else if (nan == 2) {
            easyIcon = new ImageIcon("images/normal.jpg");
        }
        else if (nan == 3) {
            easyIcon = new ImageIcon("images/hard.jpg");
        }

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

// 게임 시작 시 블록을 생성
private void startFallingBlock() {
    Random rand = new Random();
    blockType = rand.nextInt(SHAPE.length); // 0~6 사이의 난수로 블록 타입 선택
    rotation = 0; // 기본 회전 상태
    currentX = 0; // 초기 X 위치 (맨 위에서 시작)
    currentY = 4; // 초기 Y 위치 (중앙에서 시작)
    currentColor = getColorForBlock(blockType); // 블록 색 설정

    // 랜덤 블록을 화면에 그리기
    drawBlock(rotation);
}

// 블록을 그리기 전에 화면을 지운다.
private void drawBlock(int rotation) {
    clearBoard(); // 이전 상태 클리어
    int[][] shape = SHAPE[blockType][rotation]; // 선택된 블록 타입의 모양

    for (int i = 0; i < shape.length; i++) {
        for (int j = 0; j < shape[i].length; j++) {
            if (shape[i][j] == 1 && currentX + i < 20) {
                // 현재 블록을 화면에 그린다
                cellLabels[currentX + i][currentY + j].setBackground(currentColor);
            }
        }
    }
}

// 블록 색을 설정하는 메소드
private Color getColorForBlock(int blockType) {
    switch (blockType) {
        case 0: return Color.CYAN;  // I
        case 1: return Color.YELLOW; // O
        case 2: return Color.GREEN;  // S
        case 3: return Color.RED;    // Z
        case 4: return Color.BLUE;   // L
        case 5: return Color.PINK;   // T
        case 6: return Color.ORANGE; // J
        default: return Color.WHITE;
    }
}

// 블록을 아래로 이동
private void moveBlockDown() {
    if (canMove(currentX + 1, currentY)) {
        currentX++;
        drawBlock(rotation);
    } else {
        fixBlock();
        startFallingBlock(); // 새로운 블록을 시작
    }
}

// 블록 이동 여부 체크
private boolean canMove(int x, int y) {
    int[][] shape = SHAPE[blockType][rotation]; // 블록 타입과 회전 상태를 고려한 모양 가져오기

    for (int i = 0; i < shape.length; i++) {
        for (int j = 0; j < shape[i].length; j++) {
            if (shape[i][j] == 1) {
                int newX = x + i;
                int newY = y + j;

                // 벽을 넘어서거나 다른 블록과 충돌하는지 확인
                if (newX >= 20 || newY < 0 || newY >= 10 || board[newX][newY] == 1) {
                    return false;
                }
            }
        }
    }
    return true; // 모든 조건을 통과하면 이동 가능
}

// 블록을 고정시키는 메소드 수정
private void fixBlock() {
    int[][] shape = SHAPE[blockType][rotation]; // 블록의 모양을 가져옵니다.

    for (int i = 0; i < shape.length; i++) {
        for (int j = 0; j < shape[i].length; j++) {
            if (shape[i][j] == 1) {
                // 블록이 고정될 위치에 1을 설정하여 board 배열에 저장
                board[currentX + i][currentY + j] = 1;
                // 해당 위치에 블록 색상을 설정
                cellLabels[currentX + i][currentY + j].setBackground(currentColor);
            }
        }
    }

    // 블록을 고정시킨 후 한 줄을 채운 것이 있는지 확인
    clearFullLines();
}

// 보드를 초기화하여 화면을 비운다
private void clearBoard() {
    for (int i = 0; i < 20; i++) {
        for (int j = 0; j < 10; j++) {
            if (board[i][j] == 0) {
                cellLabels[i][j].setBackground(Color.BLACK);  // 이전 블록의 색상 지우기
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
                        drawBlock(rotation);
                    }
                    break;
                case KeyEvent.VK_RIGHT: // 오른쪽 이동
                    if (canMove(currentX, currentY + 1)) {
                        currentY++;
                        drawBlock(rotation);
                    }
                    break;
                case KeyEvent.VK_DOWN: // 빠르게 내려가기
                    if (canMove(currentX + 1, currentY)) {
                        currentX++;
                        drawBlock(rotation);
                    }
                    break;
                case KeyEvent.VK_UP: // 블록 회전
                    int nextRotation = (rotation + 1) % 4;
                    if (canMove(currentX, currentY)) { // 회전 후 이동할 수 있는지 체크
                        rotation = nextRotation; // 이동 가능하면 회전 적용
                        drawBlock(rotation);
                    }
                    break;
            }
        }
    }

    // 한 줄이 다 채워졌는지 확인하고, 채워졌으면 그 줄을 지우고 위의 줄을 내린다.
private void clearFullLines() {
    for (int i = 19; i >= 0; i--) {  // 아래에서부터 위로 확인
        boolean fullLine = true;
        
        // 해당 줄이 꽉 찼는지 확인
        for (int j = 0; j < 10; j++) {
            if (board[i][j] == 0) {
                fullLine = false;
                break;
            }
        }

        if (fullLine) {
            // 꽉 찬 줄은 삭제하고, 나머지 줄을 내린다
            removeLine(i);
            moveDownFullLines(i);
        }
    }
}

// 해당 줄을 지우고, 그 줄을 위의 모든 줄을 내린다.
private void removeLine(int row) {
    // 해당 줄을 지우고, 그 줄을 black으로 설정
    for (int col = 0; col < 10; col++) {
        board[row][col] = 0;
        cellLabels[row][col].setBackground(Color.BLACK);
    }
}

// 해당 줄 위에 있는 줄들을 한 칸씩 아래로 내린다.
private void moveDownFullLines(int row) {
    for (int i = row - 1; i >= 0; i--) {
        for (int j = 0; j < 10; j++) {
            board[i + 1][j] = board[i][j];  // 줄을 내린다
            if (board[i][j] == 1) {
                cellLabels[i + 1][j].setBackground(cellLabels[i][j].getBackground());  // 색상도 내린다
            } else {
                cellLabels[i + 1][j].setBackground(Color.BLACK);  // 빈 공간은 검은색으로 만든다
            }
            board[i][j] = 0;
        }
    }
}


    public static void main(String[] args) {
        new Tetris();
    }
}
