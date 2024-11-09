import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Tetris extends JFrame {
    private JLabel label;
    private JButton startButton, exitButton;
    private JButton easyButton, mediumButton, hardButton; // 난이도 버튼 추가
    private ImageIcon originalIcon;
    
    // 초기 창 크기와 버튼 위치를 기준으로 한 비율 계산을 위한 변수
    private final int initialWidth = 800;
    private final int initialHeight = 800;
    private final Rectangle startButtonBounds = new Rectangle(350, 500, 100, 50);
    private final Rectangle exitButtonBounds = new Rectangle(350, 580, 100, 50);

    // 난이도 버튼의 초기 위치를 위한 Rectangle 객체
    private final Rectangle easyButtonBounds = new Rectangle(350, 400, 100, 50);
    private final Rectangle mediumButtonBounds = new Rectangle(350, 470, 100, 50);
    private final Rectangle hardButtonBounds = new Rectangle(350, 540, 100, 50);

    public Tetris() {
        setTitle("테트리스");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container c = getContentPane();
        c.setLayout(null);

        setSize(initialWidth, initialHeight);

        // 원본 이미지 불러오기
        originalIcon = new ImageIcon("images/first.png");

        // JLabel에 초기 이미지 추가
        label = new JLabel();
        label.setBounds(0, 0, initialWidth, initialHeight);
        updateBackgroundImage();  // 초기 배경 이미지를 설정

        // "시작하기" 버튼 생성 및 위치 설정
        startButton = new JButton("시작하기");
        startButton.setBounds(startButtonBounds);

        // "종료하기" 버튼 생성 및 위치 설정
        exitButton = new JButton("종료하기");
        exitButton.setBounds(exitButtonBounds);

        // 버튼에 리스너 추가
        startButton.addActionListener(new MyActionListener());
        exitButton.addActionListener(new MyActionListener());

        label.add(startButton);
        label.add(exitButton);

        c.add(label);

        // 창 크기 변경 이벤트를 처리할 리스너 추가
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateBackgroundImage();  // 창 크기가 바뀔 때마다 배경 이미지 업데이트
                updateButtonPositions();  // 창 크기에 맞춰 버튼 위치 업데이트
            }
        });

        setVisible(true);
    }

    // 액션 리스너 클래스
    class MyActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String command = e.getActionCommand();
            if (command.equals("시작하기")) {
                System.err.println("시작 버튼을 누름");
                showDifficultyButtons();  // 난이도 선택 버튼 표시 메서드 호출
            } else if (command.equals("종료하기")) {
                int result = JOptionPane.showConfirmDialog(null, "정말 종료하시겠습니까?", "종료 확인",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                if (result == JOptionPane.YES_OPTION) {
                    System.exit(0); // 프로그램 종료
                }
            }
        }
    }

    // 난이도 버튼 표시 메서드
    private void showDifficultyButtons() {
        startButton.setVisible(false);
        exitButton.setVisible(false);

        // '난이도 하' 버튼 생성 및 위치 설정
        easyButton = new JButton("난이도 하");
        easyButton.setBounds(easyButtonBounds);
        easyButton.addActionListener(e -> System.out.println("난이도 하 선택"));

        // '난이도 중' 버튼 생성 및 위치 설정
        mediumButton = new JButton("난이도 중");
        mediumButton.setBounds(mediumButtonBounds);
        mediumButton.addActionListener(e -> System.out.println("난이도 중 선택"));

        // '난이도 상' 버튼 생성 및 위치 설정
        hardButton = new JButton("난이도 상");
        hardButton.setBounds(hardButtonBounds);
        hardButton.addActionListener(e -> System.out.println("난이도 상 선택"));

        // 난이도 버튼들을 label에 추가
        label.add(easyButton);
        label.add(mediumButton);
        label.add(hardButton);

        label.revalidate();
        label.repaint();
    }

    // 배경 이미지를 현재 창 크기에 맞게 업데이트하는 메서드
    private void updateBackgroundImage() {
        int width = getWidth();
        int height = getHeight();

        Image img = originalIcon.getImage();
        Image resizedImage = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(resizedImage);

        label.setIcon(resizedIcon);
        label.setBounds(0, 0, width, height);
    }

    // 창 크기에 맞춰 버튼 위치를 비율에 따라 재조정하는 메서드
    private void updateButtonPositions() {
        int currentWidth = getWidth();
        int currentHeight = getHeight();

        // 창 크기 비율 계산
        double widthRatio = (double) currentWidth / initialWidth;
        double heightRatio = (double) currentHeight / initialHeight;

        // 시작/종료 버튼 위치와 크기를 비율에 맞게 조정
        startButton.setBounds(
            (int) (startButtonBounds.x * widthRatio),
            (int) (startButtonBounds.y * heightRatio),
            (int) (startButtonBounds.width * widthRatio),
            (int) (startButtonBounds.height * heightRatio)
        );

        exitButton.setBounds(
            (int) (exitButtonBounds.x * widthRatio),
            (int) (exitButtonBounds.y * heightRatio),
            (int) (exitButtonBounds.width * widthRatio),
            (int) (exitButtonBounds.height * heightRatio)
        );

        // 난이도 버튼들이 생성된 경우에만 위치를 조정
        if (easyButton != null) {
            easyButton.setBounds(
                (int) (easyButtonBounds.x * widthRatio),
                (int) (easyButtonBounds.y * heightRatio),
                (int) (easyButtonBounds.width * widthRatio),
                (int) (easyButtonBounds.height * heightRatio)
            );
        }

        if (mediumButton != null) {
            mediumButton.setBounds(
                (int) (mediumButtonBounds.x * widthRatio),
                (int) (mediumButtonBounds.y * heightRatio),
                (int) (mediumButtonBounds.width * widthRatio),
                (int) (mediumButtonBounds.height * heightRatio)
            );
        }

        if (hardButton != null) {
            hardButton.setBounds(
                (int) (hardButtonBounds.x * widthRatio),
                (int) (hardButtonBounds.y * heightRatio),
                (int) (hardButtonBounds.width * widthRatio),
                (int) (hardButtonBounds.height * heightRatio)
            );
        }
    }

    public static void main(String[] args) {
        new Tetris();
    }
}
