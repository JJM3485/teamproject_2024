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
    private int blockType,nextBlockType;
    private JLabel nextBlockLabel,holdLabel;
    

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



            // 다음 블럭을 보여줄 4x4 라벨 생성 및 위치 지정
        nextBlockLabel = new JLabel();
        nextBlockLabel.setBounds(400, 100, 100, 100);
        nextBlockLabel.setLayout(new GridLayout(4, 4)); // 4x4 그리드 설정
        easyLabel.add(nextBlockLabel);

        
        holdLabel = new JLabel();
        holdLabel.setBounds(400, 220, 100, 100);
        holdLabel.setOpaque(true); // 배경을 보이게 설정
        holdLabel.setBackground(Color.black); // 디버깅용 배경색 추가
        holdLabel.setLayout(new GridLayout(4, 4)); // 4x4 그리드 설정
        easyLabel.add(holdLabel);

        System.out.println("holdLabel 위치: " + holdLabel.getBounds());
        System.out.println("easyLabel 크기: " + easyLabel.getBounds());
        

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
    blockType = nextBlockType;  // 현재 블록을 다음 블록으로 설정
    nextBlockType = rand.nextInt(SHAPE.length); // 새로운 다음 블록 설정
    rotation = 0;
    currentX = 0; // 초기 X 위치
    currentY = 3; // 초기 Y 위치

    currentColor = getColorForBlock(blockType);

    // 게임 오버 여부를 체크하여, 오버일 경우 게임을 종료
    if (isGameOver()) {
        timer.stop();  // 타이머를 중지해 게임을 중단
        JOptionPane.showMessageDialog(this, "게임 오버!", "알림", JOptionPane.INFORMATION_MESSAGE);
        resetGame();  // 게임 초기화
        return;
    }

    // 다음 블럭을 미리 보여준다
    showNextBlock();

    // 새로운 블록을 화면에 그리기
    drawBlock(rotation);
}

// 다음에 나타날 블럭을 nextBlockLabel에 표시하는 메소드
private void showNextBlock() {
    int[][] shape = SHAPE[nextBlockType][0]; // 다음 블럭의 초기 회전 상태

    // nextBlockLabel의 배경을 지운다
    nextBlockLabel.removeAll();
    
    // 다음 블럭의 모양을 nextBlockLabel에 그린다
    for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) {
            JLabel cell = new JLabel();
            cell.setOpaque(true);
            if (shape[i][j] == 1) {
                cell.setBackground(getColorForBlock(nextBlockType)); // 블럭 색상 적용
            } else {
                cell.setBackground(Color.BLACK); // 빈 공간은 검은색
            }
            nextBlockLabel.add(cell);
        }
    }

    // 변경 사항 적용
    nextBlockLabel.revalidate();
    nextBlockLabel.repaint();
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

// 블럭 생성 시 고정된 블럭과의 충돌 여부를 체크하여 게임 오버 상태를 결정하는 메소드
private boolean isGameOver() {
    int[][] shape = SHAPE[blockType][rotation];
    for (int i = 0; i < shape.length; i++) {
        for (int j = 0; j < shape[i].length; j++) {
            // 블럭이 있는 위치에 고정된 블럭이 있으면 게임 오버
            if (shape[i][j] == 1 && board[currentX + i][currentY + j] == 1) {
                return true;
            }
        }
    }
    return false;
}

// 블록을 고정시키는 메소드
private void fixBlock() {
    int[][] shape = SHAPE[blockType][rotation];
    for (int i = 0; i < shape.length; i++) {
        for (int j = 0; j < shape[i].length; j++) {
            if (shape[i][j] == 1) {
                board[currentX + i][currentY + j] = 1;
                cellLabels[currentX + i][currentY + j].setBackground(currentColor);
            }
        }
    }
    clearFullLines();
}

// 게임 재시작을 위한 메소드 수정
private void resetGame() {
    // 게임 보드 초기화
    for (int i = 0; i < 20; i++) {
        for (int j = 0; j < 10; j++) {
            board[i][j] = 0;
            cellLabels[i][j].setBackground(Color.BLACK); // 보드 색 초기화
        }
    }

    // nextBlockLabel 초기화하여 다음 블럭을 보이지 않게 설정
    if (nextBlockLabel != null) {
        nextBlockLabel.removeAll();
        nextBlockLabel.revalidate();
        nextBlockLabel.repaint();
    }

    // 메인 화면 (label)으로 돌아가도록 설정
    getContentPane().removeAll(); // 모든 컴포넌트 제거
    getContentPane().add(label);  // label을 다시 추가하여 초기 화면으로 전환
    startButton.setVisible(true); // 게임 시작 버튼 표시
    exitButton.setVisible(true);  // 종료 버튼 표시

    easyButton.setVisible(false);
    mediumButton.setVisible(false);
    hardButton.setVisible(false);



    revalidate();
    repaint();
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
                case KeyEvent.VK_Z: // Z키 눌렀을 때, 블록을 바로 아래로 내리기
                    dropBlockInstantly(); 
                    break;
            }
        }
    
        // Z키 눌렀을 때, 블록이 바닥까지 빠르게 내려가도록 처리
        private void dropBlockInstantly() {
            // 블록이 더 이상 내려갈 수 없을 때까지 내려보낸다
            while (canMove(currentX + 1, currentY)) {
                currentX++; // 아래로 한 칸 내려가기
                drawBlock(rotation); // 블록을 화면에 그리기
            }
    
            // 블록을 고정시키고, 새로운 블록을 시작
            fixBlock();
            startFallingBlock();
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
