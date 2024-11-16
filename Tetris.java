import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
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
    private JLabel nextBlockLabel,holdLabel,timeLabel;
    private int holdBlockType = -1; // 초기 상태, 홀드가 비어있음을 나타냄
    private boolean holdUsed = false; // 현재 턴에서 이미 홀드를 사용했는지 체크
    private int remainingTime = 180; // 제한시간 (초 단위, 3분)
    private Timer countdownTimer;   // 제한시간을 관리하는 타이머

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

    private void startGame(int speed, int nan) {
        gameSpeed = speed;
        initializeGameBoard(nan);
    
        // 제한시간 초기화 및 표시
        remainingTime = 180;
        timeLabel.setText("남은 시간: 03:00");
    
        // 제한시간 타이머 시작
        startCountdownTimer();
    
        // 블록 이동 타이머 시작
        timer = new Timer(gameSpeed, e -> moveBlockDown());
        timer.start();
    }

    private void startCountdownTimer() {
        countdownTimer = new Timer(1000, e -> {
            remainingTime--;
    
            // 시간 포맷팅 및 라벨 업데이트
            int minutes = remainingTime / 60;
            int seconds = remainingTime % 60;
            timeLabel.setText(String.format("남은 시간: %02d:%02d", minutes, seconds));
    
            if (remainingTime <= 0) {
                countdownTimer.stop();
                timer.stop();
                JOptionPane.showMessageDialog(this, "시간 초과! 게임 오버!", "알림", JOptionPane.INFORMATION_MESSAGE);
                resetGame(); // 게임 초기화
            }
        });
        countdownTimer.start(); // 타이머 시작
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

        timeLabel = new JLabel("남은 시간: 03:00");
        timeLabel.setBounds(400, 340, 100, 100); // 위치와 크기 설정
        timeLabel.setForeground(Color.WHITE); // 글자 색상 설정
        timeLabel.setOpaque(true); // 배경을 보이게 설정
        timeLabel.setBackground(Color.black); // 디버깅용 배경색 추가
        easyLabel.add(timeLabel);
        

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                cellLabels[i][j] = new JLabel();
                cellLabels[i][j].setOpaque(true);
                cellLabels[i][j].setBackground(Color.BLACK);
                gameLabel.add(cellLabels[i][j]);
            }
        }

            // 첫 블록과 다음 블록 설정
        Random rand = new Random();
        blockType = rand.nextInt(SHAPE.length);  // 현재 블록 랜덤 생성
        nextBlockType = rand.nextInt(SHAPE.length);  // 다음 블록 랜덤 생성

        // 다음 블록 표시
        showNextBlock();



        getContentPane().add(easyLabel);
        revalidate();
        repaint();

        startFallingBlock();
    }

    private void startFallingBlock() {
        rotation = 0;  // 초기 회전 상태
        currentX = 0;  // 초기 X 위치
        currentY = 3;  // 초기 Y 위치
    
        currentColor = getColorForBlock(blockType);
    
        // 게임 오버 여부 확인
        if (isGameOver()) {
            timer.stop();
            JOptionPane.showMessageDialog(this, "게임 오버!", "알림", JOptionPane.INFORMATION_MESSAGE);
            resetGame();
            return;
        }
    
        // 현재 블록을 화면에 그림
        drawBlock(rotation);
    }

// 다음에 나타날 블럭을 nextBlockLabel에 표시하는 메소드
private void showNextBlock() {
    nextBlockLabel.removeAll(); // 기존 블록 지우기

    if (nextBlockType != -1) {
        int[][] shape = SHAPE[nextBlockType][0]; // 초기 회전 상태

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                JLabel cell = new JLabel();
                cell.setOpaque(true);
                if (shape[i][j] == 1) {
                    cell.setBackground(getColorForBlock(nextBlockType)); // 색상 설정
                } else {
                    cell.setBackground(Color.BLACK); // 빈 칸은 검정
                }
                nextBlockLabel.add(cell);
            }
        }
    }

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
    clearFullLines(); // 꽉 찬 줄 제거
    holdUsed = false; // 홀드 사용 가능 상태 초기화

    // 다음 블록을 현재 블록으로 설정
    blockType = nextBlockType;

    // 새로운 다음 블록 생성
    Random rand = new Random();
    nextBlockType = rand.nextInt(SHAPE.length);

    // 다음 블록을 표시
    showNextBlock();

    // 새 블록 시작
    startFallingBlock();
}

// 게임 재시작을 위한 메소드 수정
private void resetGame() {
    // 제한시간 초기화
    remainingTime = 180;

    // 제한시간 타이머 중지
    if (countdownTimer != null) {
        countdownTimer.stop();
        countdownTimer = null; // 이전 타이머 해제
    }

    // 게임 타이머 중지
    if (timer != null) {
        timer.stop();
        timer = null; // 이전 타이머 해제
    }

    // 게임 보드 초기화
    for (int i = 0; i < 20; i++) {
        for (int j = 0; j < 10; j++) {
            board[i][j] = 0;
            cellLabels[i][j].setBackground(Color.BLACK); // 보드 색 초기화
        }
    }

    // 다음 블록 라벨 초기화
    if (nextBlockLabel != null) {
        nextBlockLabel.removeAll();
        nextBlockLabel.revalidate();
        nextBlockLabel.repaint();
    }

    // 메인 화면으로 돌아가기
    getContentPane().removeAll();
    getContentPane().add(label);
    startButton.setVisible(true);
    exitButton.setVisible(true);

    if (easyButton != null) easyButton.setVisible(false);
    if (mediumButton != null) mediumButton.setVisible(false);
    if (hardButton != null) hardButton.setVisible(false);

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
                    if (canRotate(nextRotation)) { // 회전 후 이동 가능 여부 확인
                        rotation = nextRotation;  // 이동 가능하면 회전 적용
                        drawBlock(rotation);      // 화면에 새 상태 그리기
                    }
                    break;
                case KeyEvent.VK_Z: // Z키 눌렀을 때, 블록을 바로 아래로 내리기
                    dropBlockInstantly(); 
                    break;
                case KeyEvent.VK_X:
                    handleHoldBlock(); // 홀드 블록 기능 처리
                    break;
            }
        }

        private void handleHoldBlock() {
            if (holdUsed) return; // 이미 홀드를 사용한 경우 무시
        
            if (holdBlockType == -1) {
                // 홀드가 비어있을 때: 현재 블록을 저장하고 새 블록 시작
                holdBlockType = blockType; // 현재 블록 타입 저장
                startFallingBlock(); // 새로운 블록 시작
            } else {
                // 홀드에 블록이 있을 때: 현재 블록과 홀드 블록 교체
                int temp = blockType;
                blockType = holdBlockType; // 홀드 블록을 현재 블록으로 설정
                holdBlockType = temp; // 현재 블록을 홀드에 저장
                rotation = 0; // 홀드 블록은 기본 회전 상태로 시작
                currentX = 0; // 초기 위치로 설정
                currentY = 3; 
                drawBlock(rotation); // 교체된 블록을 화면에 그리기
            }
        
            // 홀드 표시 업데이트
            updateHoldLabel();
        
            holdUsed = true; // 이번 턴에 홀드를 사용했음을 기록
        }
        private boolean canRotate(int nextRotation) {
            int[][] shape = SHAPE[blockType][nextRotation]; // 회전 후의 블록 모양 가져오기
        
            for (int i = 0; i < shape.length; i++) {
                for (int j = 0; j < shape[i].length; j++) {
                    if (shape[i][j] == 1) {
                        int newX = currentX + i;
                        int newY = currentY + j;
        
                        // 경계를 벗어나거나 고정된 블록과 충돌하는지 확인
                        if (newX < 0 || newX >= 20 || newY < 0 || newY >= 10 || board[newX][newY] == 1) {
                            return false;
                        }
                    }
                }
            }
            return true; // 회전 가능하면 true 반환
        }

        private void updateHoldLabel() {
            holdLabel.removeAll(); // 기존 블록 제거
        
            if (holdBlockType != -1) {
                int[][] shape = SHAPE[holdBlockType][0]; // 홀드 블록은 기본 회전 상태로 보여줌
        
                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 4; j++) {
                        JLabel cell = new JLabel();
                        cell.setOpaque(true);
                        if (shape[i][j] == 1) {
                            cell.setBackground(getColorForBlock(holdBlockType)); // 블록 색상 설정
                        } else {
                            cell.setBackground(Color.BLACK); // 빈 칸은 검은색
                        }
                        holdLabel.add(cell);
                    }
                }
            }
        
            holdLabel.revalidate();
            holdLabel.repaint();
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
    List<Integer> fullLines = new ArrayList<>(); // 꽉 찬 줄의 인덱스 저장

    // 모든 줄을 검사하여 꽉 찬 줄을 기록
    for (int i = 0; i < 20; i++) {
        boolean fullLine = true;
        for (int j = 0; j < 10; j++) {
            if (board[i][j] == 0) {
                fullLine = false;
                break;
            }
        }
        if (fullLine) {
            fullLines.add(i);
        }
    }

    // 기록된 줄을 지우고 위의 줄을 내린다
    for (int row : fullLines) {
        removeLine(row); // 해당 줄을 제거
    }

    // 위의 줄을 아래로 내린다
    for (int row : fullLines) {
        moveDownFullLines(row);
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
// 줄을 아래로 내리는 로직 수정
private void moveDownFullLines(int row) {
    for (int i = row - 1; i >= 0; i--) {
        for (int j = 0; j < 10; j++) {
            board[i + 1][j] = board[i][j]; // 위의 줄을 아래로 복사
            cellLabels[i + 1][j].setBackground(cellLabels[i][j].getBackground()); // 색상 이동

            board[i][j] = 0; // 위의 줄은 초기화
            cellLabels[i][j].setBackground(Color.BLACK);
        }
    }
}


    public static void main(String[] args) {
        new Tetris();
    }
}
