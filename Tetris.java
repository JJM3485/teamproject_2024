import java.awt.*;
import java.util.Random;
import javax.swing.*;

public class Tetris extends JFrame {
    private JLabel label, currentBlockLabel, smallLabel1, smallLabel2, smallLabel3, smallLabel4, smallLabel5, gameLabel;
    private JButton startButton, exitButton,easyButton, mediumButton, hardButton;
    private ImageIcon originalIcon,easyIcon;
    private Timer timer; // 블록이 내려오는 타이머
    private int gameSpeed = 500; // 블록이 떨어지는 속도, 난이도에 따라 변경
    private final int initialWidth = 800;
    private final int initialHeight = 800;
    private int[][] board = new int[20][10];  // 10x20 테트리스 보드
    private JLabel[][] cellLabels = new JLabel[20][10];  // 각 셀을 표시할 라벨들
    private Tetromino currentBlock; // 현재 내려오는 블록
    private int currentX, currentY; // 블록의 현재 위치
    private final int fieldWidth = 10;
    private final int fieldHeight = 20;
    // 블록이 떨어지고 있는지 여부를 추적하는 변수
    private boolean isBlockFalling = false;


     // 테트로미노 모양 정의
    private final Tetromino[] tetrominoes = {
        new Tetromino(new int[][] {{1, 1, 1, 1}}, Color.CYAN),    // I
        new Tetromino(new int[][] {{1, 1, 0}, {0, 1, 1}}, Color.RED), // S
        new Tetromino(new int[][] {{0, 1, 1}, {1, 1, 0}}, Color.GREEN), // Z
        new Tetromino(new int[][] {{1, 1, 1}, {0, 1, 0}}, Color.MAGENTA), // T
        new Tetromino(new int[][] {{1, 1}, {1, 1}}, Color.YELLOW),   // O
        new Tetromino(new int[][] {{1, 0, 0}, {1, 1, 1}}, Color.BLUE),  // L
        new Tetromino(new int[][] {{0, 0, 1}, {1, 1, 1}}, Color.ORANGE) // J
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

        setVisible(true);
    }

    // "시작하기" 버튼을 눌렀을 때 난이도 버튼들이 표시되는 메서드
    private void showDifficultyButtons() {
        startButton.setVisible(false);
        exitButton.setVisible(false);

        // 난이도 하 버튼
        easyButton = new JButton("난이도 하");
        easyButton.setBounds(350, 400, 100, 50);
        easyButton.addActionListener(e -> startGame(500,1)); // 하: 500ms 간격으로 이동

        // 난이도 중 버튼
        mediumButton = new JButton("난이도 중");
        mediumButton.setBounds(350, 470, 100, 50);
        mediumButton.addActionListener(e -> startGame(300,2)); // 중: 300ms 간격으로 이동

        // 난이도 상 버튼
        hardButton = new JButton("난이도 상");
        hardButton.setBounds(350, 540, 100, 50);
        hardButton.addActionListener(e -> startGame(100,3)); // 상: 100ms 간격으로 이동

        label.add(easyButton);
        label.add(mediumButton);
        label.add(hardButton);

        label.revalidate();
        label.repaint();
    }

    // 난이도 선택 시 게임 시작
    private void startGame(int speed,int nan) {
        getContentPane().removeAll();
        revalidate();
        repaint();

        gameSpeed = speed; // 선택한 난이도에 맞춰 속도 설정

        // 게임 화면 설정
        if(nan == 1) {
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

        //테트리스 부분
        gameLabel = new JLabel();
        gameLabel.setBounds(100, 100, 250, 600); // 큰 라벨 위치 및 크기 설정
        gameLabel.setOpaque(true);
        gameLabel.setBackground(Color.BLACK);
        gameLabel.setLayout(new GridLayout(20, 10));
        easyLabel.add(gameLabel);

        // 작은 라벨 생성 및 오른쪽에 배치
        int smallLabelWidth = 100;
        int smallLabelHeight = 100;
        int rightX = getWidth() - 150;

        smallLabel1 = createSmallLabel("다음 블록", rightX, 100, smallLabelWidth, smallLabelHeight);
        smallLabel2 = createSmallLabel("홀딩 블록", rightX, 220, smallLabelWidth, smallLabelHeight);
        smallLabel3 = createSmallLabel("현재 점수/최고 점수", rightX, 340, smallLabelWidth, smallLabelHeight);
        smallLabel4 = createSmallLabel("남은 블록/전체 블록", rightX, 460, smallLabelWidth, smallLabelHeight);
        smallLabel5 = createSmallLabel("햔재 시간/제한 시간", rightX, 580, smallLabelWidth, smallLabelHeight);

        easyLabel.add(smallLabel1);
        easyLabel.add(smallLabel2);
        easyLabel.add(smallLabel3);
        easyLabel.add(smallLabel4);
        easyLabel.add(smallLabel5);

        initializeGameBoard();
        getContentPane().add(easyLabel);

        spawnNewBlock(); // 첫 블록 생성
        startTimer(); // 타이머 시작하여 블록이 내려오도록 설정

        revalidate();
        repaint();
    }

    // 보드 초기화 메서드
private void initializeGameBoard() {
    for (int row = 0; row < 20; row++) {
        for (int col = 0; col < 10; col++) {
            cellLabels[row][col] = new JLabel();
            cellLabels[row][col].setOpaque(true); // 투명하게 설정하여 배경색을 볼 수 있도록 함
            cellLabels[row][col].setBackground(Color.BLACK); // 기본 배경은 검정색
            gameLabel.add(cellLabels[row][col]); // gameLabel에 추가
        }
    }
}

    // 타이머 시작 메서드
    private void startTimer() {
        if (timer != null) timer.stop();
        timer = new Timer(gameSpeed, e -> moveBlockDown());
        timer.start();
    }

    // 블록 클래스
    private class Tetromino {
        public int[][] shape;
        public Color color;

        public Tetromino(int[][] shape, Color color) {
            this.shape = shape;
            this.color = color;
        }
    }

// 블록 생성 메서드
private void spawnNewBlock() {
    if (isBlockFalling) {
        return;  // 블록이 떨어지고 있는 동안에는 새로운 블록을 생성하지 않음
    }
    
    Random random = new Random();
    currentBlock = tetrominoes[random.nextInt(tetrominoes.length)];
    
    // 초기 위치 설정 (가운데, 약간 위쪽에서 시작)
    currentX = 4;
    currentY = 0;  // 0에서 시작합니다.

    // 블록 생성 직후 겹치는지 검사
    if (!canMove(currentX, currentY, currentBlock.shape)) {
        // 블록이 겹칠 경우 게임 오버
    } else {
        // 겹치지 않을 경우 블록을 필드에 배치
        placeBlock(currentX, currentY, currentBlock.shape, true);
        isBlockFalling = true;  // 블록이 떨어지기 시작했다고 설정
    }
}

// 겹침 검사 메서드
private boolean canMove(int x, int y, int[][] shape) {
    for (int row = 0; row < shape.length; row++) {
        for (int col = 0; col < shape[row].length; col++) {
            if (shape[row][col] != 0) {  // 셀이 0이 아닌 경우에만 검사
                int newX = x + col;
                int newY = y + row;

                // 필드 범위 내에 있고 다른 블록과 겹치지 않는지 확인
                if (newX < 0 || newX >= fieldWidth || newY >= fieldHeight || (newY >= 0 && board[newY][newX] != 0)) {
                    return false; // 겹치거나 필드를 벗어나면 false 반환
                }
            }
        }
    }
    return true; // 겹치지 않으면 true 반환
}

// 블록 고정 메서드
private void placeBlock(int x, int y, int[][] shape, boolean place) {
    for (int row = 0; row < shape.length; row++) {
        for (int col = 0; col < shape[row].length; col++) {
            if (shape[row][col] != 0) {
                int newX = x + col;
                int newY = y + row;
                if (newY >= 0) { // 화면 위로 올라가지는 않도록
                    board[newY][newX] = place ? 1 : 0;
                    cellLabels[newY][newX].setBackground(place ? currentBlock.color : Color.BLACK);
                }
            }
        }
    }
}
// 블록 아래로 이동 메서드
private void moveBlockDown() {
    // 블록을 한 칸 아래로 이동할 수 있으면
    if (canMove(currentX, currentY + 1, currentBlock.shape)) {
        placeBlock(currentX, currentY, currentBlock.shape, false); // 이전 위치를 비웁니다.
        currentY++; // 한 칸 아래로 이동
        placeBlock(currentX, currentY, currentBlock.shape, true); // 새로운 위치에 블록을 배치합니다.
    } else {
        // 블록을 고정시키고, 가득 찬 줄을 체크 후 새로운 블록을 생성
        placeBlock(currentX, currentY, currentBlock.shape, true); 

        isBlockFalling = false;  // 블록이 떨어지지 않음으로 설정
        spawnNewBlock();   // 새 블록 생성
    }
}

    // 작은 라벨을 생성하는 메서드
    private JLabel createSmallLabel(String text, int x, int y, int width, int height) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setBounds(x, y, width, height);
        label.setOpaque(true);
        label.setBackground(new Color(255, 255, 255, 128));
        label.setForeground(Color.BLACK);
        return label;
    }

    // 배경 이미지 업데이트 메서드
    private void updateBackgroundImage() {
        int width = getWidth();
        int height = getHeight();

        Image img = originalIcon.getImage();
        Image resizedImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(resizedImage);

        label.setIcon(resizedIcon);
        label.setBounds(0, 0, width, height);
    }

    public static void main(String[] args) {
        new Tetris();
    }
}
