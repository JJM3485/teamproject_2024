import java.awt.*;
import javax.swing.*;

public class Tetris extends JFrame {
    private JLabel label;
    private JButton startButton, exitButton;
    private JButton easyButton, mediumButton, hardButton;
    private ImageIcon originalIcon;
    private JLabel smallLabel1,smallLabel2,smallLabel3,smallLabel4,smallLabel5, gameLabel; //1은 다음 블록 나오기 2는 점수 3은 시간 4는 최고기록 5는 저장 
    private final int initialWidth = 800;
    private final int initialHeight = 800;

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
        easyButton.addActionListener(e -> showEasyLabelWithImage());

        // 난이도 중 버튼
        mediumButton = new JButton("난이도 중");
        mediumButton.setBounds(350, 470, 100, 50);
        mediumButton.addActionListener(e -> System.out.println("난이도 중 선택"));

        // 난이도 상 버튼
        hardButton = new JButton("난이도 상");
        hardButton.setBounds(350, 540, 100, 50);
        hardButton.addActionListener(e -> System.out.println("난이도 상 선택"));

        label.add(easyButton);
        label.add(mediumButton);
        label.add(hardButton);

        label.revalidate();
        label.repaint();
    }

    // 난이도 "하" 선택 시 실행되는 메서드로, easyLabel을 표시
    private void showEasyLabelWithImage() {
        getContentPane().removeAll();

        ImageIcon easyIcon = new ImageIcon("images/easy.jpg");
        JLabel easyLabel = new JLabel(easyIcon);
        easyLabel.setLayout(null);
        easyLabel.setBounds(0, 0, getWidth(), getHeight());

        gameLabel = new JLabel("2", SwingConstants.CENTER);
        gameLabel.setBounds(100, 100, 250, 600); // 큰 라벨 위치 및 크기 설정
        gameLabel.setOpaque(true);
        gameLabel.setBackground(Color.BLACK);
        gameLabel.setForeground(Color.WHITE);
        gameLabel.setFont(new Font("Arial", Font.BOLD, 48));
        easyLabel.add(gameLabel);

        // 작은 라벨 4개 생성 및 오른쪽에 배치
        int smallLabelWidth = 100;
        int smallLabelHeight = 100;
        int rightX = getWidth() - 150;

        smallLabel1 = createSmallLabel("Label 1", rightX, 100, smallLabelWidth, smallLabelHeight);
        smallLabel2 = createSmallLabel("Label 2", rightX, 220, smallLabelWidth, smallLabelHeight);
        smallLabel3 = createSmallLabel("Label 3", rightX, 340, smallLabelWidth, smallLabelHeight);
        smallLabel4 = createSmallLabel("Label 4", rightX, 460, smallLabelWidth, smallLabelHeight);
        smallLabel5 = createSmallLabel("Label 5", rightX, 580, smallLabelWidth, smallLabelHeight);


        easyLabel.add(smallLabel1);
        easyLabel.add(smallLabel2);
        easyLabel.add(smallLabel3);
        easyLabel.add(smallLabel4);
        easyLabel.add(smallLabel5);

        getContentPane().add(easyLabel);
        revalidate();
        repaint();
    }

    private JLabel createSmallLabel(String text, int x, int y, int width, int height) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setBounds(x, y, width, height);
        label.setOpaque(true);
        label.setBackground(new Color(255, 255, 255, 128));
        label.setForeground(Color.BLACK);
        return label;
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

    public static void main(String[] args) {
        new Tetris();
    }
}
