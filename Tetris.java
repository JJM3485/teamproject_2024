import java.awt.*;
import javax.swing.*;

import java.util.Random;

import javax.swing.*;

import java.awt.event.*;
import java.awt.*;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class Tetris extends JFrame {
    private JLabel label;
    private JButton startButton, exitButton;
    private JButton easyButton, mediumButton, hardButton;
    private ImageIcon originalIcon;
    private JLabel smallLabel1, smallLabel2, smallLabel3, smallLabel4, smallLabel5, gameLabel; // 부가 정보 표시 라벨들
    private Timer timer; // 블록이 내려오는 타이머
    private JLabel currentBlockLabel; // 현재 내려오는 블록
    private final int blockSize = 50; // 블록 크기
    private int gameSpeed = 500; // 블록이 떨어지는 속도, 난이도에 따라 변경
    private final int initialWidth = 800;
    private final int initialHeight = 800;

    private final String[] blockImages = {
            "images/block/block_I.png",
            "images/block/block_J.png",
            "images/block/block_L.png",
            "images/block/block_O.png",
            "images/block/block_S.png",
            "images/block/block_T.png",
            "images/block/block_Z.png"
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
        easyButton.addActionListener(e -> startGame(500)); // 하: 500ms 간격으로 이동

        // 난이도 중 버튼
        mediumButton = new JButton("난이도 중");
        mediumButton.setBounds(350, 470, 100, 50);
        mediumButton.addActionListener(e -> startGame(300)); // 중: 300ms 간격으로 이동

        // 난이도 상 버튼
        hardButton = new JButton("난이도 상");
        hardButton.setBounds(350, 540, 100, 50);
        hardButton.addActionListener(e -> startGame(100)); // 상: 100ms 간격으로 이동

        label.add(easyButton);
        label.add(mediumButton);
        label.add(hardButton);

        label.revalidate();
        label.repaint();
    }

    // 난이도 선택 시 게임 시작
    private void startGame(int speed) {
        getContentPane().removeAll();
        revalidate();
        repaint();

        gameSpeed = speed; // 선택한 난이도에 맞춰 속도 설정

        // 게임 화면 설정
        ImageIcon easyIcon = new ImageIcon("images/easy.jpg");
        JLabel easyLabel = new JLabel(easyIcon);
        easyLabel.setLayout(null);
        easyLabel.setBounds(0, 0, getWidth(), getHeight());

        gameLabel = new JLabel();
        gameLabel.setBounds(100, 100, 250, 600); // 큰 라벨 위치 및 크기 설정
        gameLabel.setOpaque(true);
        gameLabel.setBackground(Color.BLACK);
        easyLabel.add(gameLabel);

        // 작은 라벨 생성 및 오른쪽에 배치
        int smallLabelWidth = 100;
        int smallLabelHeight = 100;
        int rightX = getWidth() - 150;

        smallLabel1 = createSmallLabel("다음 블록", rightX, 100, smallLabelWidth, smallLabelHeight);
        smallLabel2 = createSmallLabel("점수", rightX, 220, smallLabelWidth, smallLabelHeight);
        smallLabel3 = createSmallLabel("시간", rightX, 340, smallLabelWidth, smallLabelHeight);
        smallLabel4 = createSmallLabel("최고 기록", rightX, 460, smallLabelWidth, smallLabelHeight);
        smallLabel5 = createSmallLabel("저장", rightX, 580, smallLabelWidth, smallLabelHeight);

        easyLabel.add(smallLabel1);
        easyLabel.add(smallLabel2);
        easyLabel.add(smallLabel3);
        easyLabel.add(smallLabel4);
        easyLabel.add(smallLabel5);

        getContentPane().add(easyLabel);

        spawnNewBlock(); // 첫 블록 생성
        startTimer(); // 타이머 시작하여 블록이 내려오도록 설정

        revalidate();
        repaint();
    }

    // 블록 타이머 시작
    private void startTimer() {
        if (timer != null) {
            timer.stop(); // 기존 타이머가 있으면 정지
        }

        timer = new Timer(gameSpeed, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveBlockDown();
            }
        });
        timer.start();
    }

    // 새 블록 생성 및 중앙 맨 위에서 표시
    private void spawnNewBlock() {
        if (currentBlockLabel != null) {
            gameLabel.remove(currentBlockLabel); // 이전 블록 제거
        }

        // 랜덤으로 블록 이미지 선택
        String randomBlockImage = blockImages[new Random().nextInt(blockImages.length)];
        ImageIcon blockIcon = new ImageIcon(randomBlockImage);

        currentBlockLabel = new JLabel(blockIcon);
        currentBlockLabel.setBounds((gameLabel.getWidth() - blockSize) / 2, 0, blockSize, blockSize); // 중앙 맨 위
        gameLabel.add(currentBlockLabel);
        gameLabel.revalidate();
        gameLabel.repaint();
    }

    // 블록이 한 칸 아래로 이동
    private void moveBlockDown() {
        int x = currentBlockLabel.getX();
        int y = currentBlockLabel.getY() + blockSize;

        // 화면의 아래쪽 경계에 도달했을 때 처리
        if (y + blockSize > gameLabel.getHeight()) {
            spawnNewBlock(); // 바닥에 도달하면 새 블록 생성
        } else {
            currentBlockLabel.setLocation(x, y); // 블록을 한 칸 아래로 이동
        }

        currentBlockLabel.setFocusable(true);
        currentBlockLabel.requestFocusInWindow();
        currentBlockLabel.addKeyListener(new MyKeyListener());
    }

    class MyKeyListener extends KeyAdapter {
		public void keyPressed(KeyEvent e) {
			int keyCode = e.getKeyCode();

			switch(keyCode) {
				case KeyEvent.VK_DOWN: 
                    currentBlockLabel.setLocation(currentBlockLabel.getX(), currentBlockLabel.getY()+15); 
					break;
                case KeyEvent.VK_LEFT:
                    currentBlockLabel.setLocation(currentBlockLabel.getX() - 15, currentBlockLabel.getY());
                    break;
                case KeyEvent.VK_RIGHT:
                    currentBlockLabel.setLocation(currentBlockLabel.getX() + 15, currentBlockLabel.getY());
                    break;

			}
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
