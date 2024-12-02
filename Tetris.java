import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.swing.*;

public class Tetris extends JFrame {
    private JLabel label, gameLabel, easyLabel;
    private JButton startButton, exitButton, easyButton, mediumButton, hardButton;
    private ImageIcon originalIcon, easyIcon,chIcon,abIcon;
    private Timer timer;
    private boolean isEasyCleared = false,isMediumCleared = false,isHardCleared = false;
    private int gameSpeed = 500;
    private final int initialWidth = 800;
    private final int initialHeight = 800;
    private int[][] board = new int[20][10];
    private JLabel[][] cellLabels = new JLabel[20][10];
    private int currentX, currentY, rotation = 0;
    private Color currentColor = Color.BLUE;
    private int blockType,nextBlockType;
    private JLabel nextBlockLabel,holdLabel,timeLabel,progressLabel,chlabel,abilitylabel,explainlabel;
    private int holdBlockType = -1; // 초기 상태, 홀드가 비어있음을 나타냄
    private boolean holdUsed = false,revived = false,abilityUsed = false; // 현재 턴에서 이미 홀드를 사용했는지 체크
    private int remainingTime = 180; // 제한시간 (초 단위, 3분)
    private Timer countdownTimer;   // 제한시간을 관리하는 타이머
    private int totalBlocks = 80,clearedBlocks = 0; // 전체 블록 수
    private int currentDifficulty; // 1: 하, 2: 중, 3: 상
    private String selectedCharacter = ""; // 선택된 캐릭터
    private String[] characters = {"에린 카르테스", "레온 하르트", "셀레나", "루미엘", "슬리"};
    private String[] imagePaths = {
            "images/ch/man_1.png",
            "images/ch/man_2.png",
            "images/ch/woman_1.png",
            "images/ch/woman_2.png",
            "images/ch/slime.png"
    };
    private MusicManager musicManager = new MusicManager();
    // 현재 난이도에 맞는 배경음악 경로 반환
    private String getBackgroundMusicForCurrentDifficulty() {
        return switch (currentDifficulty) {
            case 1 -> "sings/easy.wav";
            case 2 -> "sings/hard.wav";
            case 3 -> "sings/normal.wav";
            default -> null;
        };
    }
    

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

    public class MusicManager {
        private Clip clip;
    
        // 음악을 재생하는 메소드
        public void playMusic(String filePath) {
            // 기존 음악을 멈추고 새로운 음악을 재생
            stopMusic(); // 기존 음악 종료
            try {
                File file = new File(filePath);
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
                clip = AudioSystem.getClip();
                clip.open(audioStream);
    
                // 음악 종료 후 Clip 리소스 해제
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
    
                clip.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    
        // 음악을 멈추는 메소드
        public void stopMusic() {
            // clip이 존재하고 음악이 실행 중이면 멈추기
            if (clip != null && clip.isRunning()) {
                clip.stop();
                clip.close();
            }
            }
        }

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

        startButton.addActionListener(e -> {
            musicManager.playMusic("sings/start.wav"); 
            showDifficultyButtons();});

        exitButton.addActionListener(e -> {
            musicManager.playMusic("sings/start.wav"); 
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
        easyButton.addActionListener(e -> showCharacterSelection(300, 1));

        mediumButton = new JButton("난이도 중");
        mediumButton.setBounds(350, 470, 100, 50);
        mediumButton.addActionListener(e -> showCharacterSelection(200, 2));

        hardButton = new JButton("난이도 상");
        hardButton.setBounds(350, 540, 100, 50);
        hardButton.addActionListener(e -> showCharacterSelection(100, 3));

        label.add(easyButton);
        label.add(mediumButton);
        label.add(hardButton);

        label.revalidate();
        label.repaint();
    }

    private void showCharacterSelection(int speed, int difficulty) {
        gameSpeed = speed;
        currentDifficulty = difficulty;
    
        getContentPane().removeAll(); // 기존 UI 제거
        revalidate();
        repaint();
    
        musicManager.stopMusic(); // 기존 음악 정지
        // 시작 화면 배경음악 재생
        musicManager.playMusic("sings/first.wav");

        JPanel charSelectionPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g); // 기존의 컴포넌트 그리기
    
                // 배경 이미지 그리기
                ImageIcon backgroundImage = new ImageIcon("images/select.png");
                g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        };
    
        charSelectionPanel.setLayout(null);
        charSelectionPanel.setBounds(0, 0, getWidth(), getHeight());
    
        JLabel charLabel = new JLabel("캐릭터를 선택하세요!");
        charLabel.setBounds(300, 50, 200, 50);
        charLabel.setForeground(Color.WHITE);
        charLabel.setHorizontalAlignment(SwingConstants.CENTER);
        charSelectionPanel.add(charLabel);
    
        JLabel charImageLabel = new JLabel();
        charImageLabel.setBounds(50, 150, 370, 400);
        charImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        charImageLabel.setVerticalAlignment(SwingConstants.CENTER);
        charSelectionPanel.add(charImageLabel);
    
        JLabel charDescLabel = new JLabel();
        charDescLabel.setBounds(500, 150, 200, 20);
        charDescLabel.setForeground(Color.WHITE);
        charDescLabel.setHorizontalAlignment(SwingConstants.CENTER);
        charSelectionPanel.add(charDescLabel);
    
        // 새로운 설명을 표시할 라벨 추가
        JLabel descriptionLabel = new JLabel("");
        descriptionLabel.setBounds(430, 180, 400, 400); // 설명 위치 및 크기 조정
        descriptionLabel.setOpaque(true);
        descriptionLabel.setBackground(Color.LIGHT_GRAY);
        descriptionLabel.setForeground(Color.black);
        descriptionLabel.setHorizontalAlignment(SwingConstants.CENTER);
        descriptionLabel.setVerticalAlignment(SwingConstants.TOP);
        charSelectionPanel.add(descriptionLabel);
    
        // 각 캐릭터 설명 준비 (배열에 캐릭터 설명 추가)
        String[] characterDescriptions = {
            "직업: 검성(Blade Master) / 왕국의 기사\n\n나이: 24세\n\n출신지: 루멘테르 왕국의 수도, 크라운스피어(Crownspear)\n\n역할: 파티장, 주 공격수, 전략의 중심\n\n루멘테르 왕국 출신의 기사로, 정의와 명예를 중시합니다.\n\n어릴 때부터 빛의 신 루멘의 가르침을 받았으며,\n 에테르 기사단의 마지막 계승자로서 \n조상 대대로 전해내려온 성검을 지니고\n 사명을 지니고 있습니다.\n\n냉철하지만 팀원들을 아끼며 책임감이 강합니다.",
        
            "직업: 성기사 (Paladin)\n\n나이: 29세\n\n출신지: 루멘테르 왕국의 외곽 마을, 실바린(Silvarin)\n\n역할: 파티의 탱커 및 정신적 지주\n\n레온은 루멘테르 왕국의 외곽 농촌 마을에서 태어나\n 어린 시절부터 착하고 강인한 성격으로\n 동네 사람들에게 존경받았습니다.\n\n 그의 마을은 마족의 습격을 받아\n 거의 전멸했지만,\n 레온은 홀로 마을을 지키기 위해 싸우다\n 루멘 신전의 성기사단에 의해 구출됩니다.",
        
            "직업: 원소 마법사(Elemental Mage) / 화염 전문가\n\n나이: 24세\n\n출신지: 아르카디아의 외곽 마을, 에텔로스(Ethelos)\n\n역할: 강력한 마법 딜러, 전략적인 제압 스페셜리스트\n\n셀레나는 평화로운 에텔로스 마을에서 태어났지만,\n\n 그녀가 8살이 되던 해,\n 마을은 정체불명의 마족의 습격으로 전멸당했습니다.\n 그녀는 화염 속에서 기적적으로 살아남았으며,\n 이후 자신의 내면에\n 원소 마법의 재능이 깃들어 있다는 것을 알게 되었습니다.\n\n 그녀의 고향이 불타오르는 장면은 셀레나의 트라우마이지만,\n 동시에 그녀의 가장 큰 힘의 원천이기도 합니다.",
        
            "직업: 성녀 (Saint) / 치유와 축복의 사제\n\n나이: 19세\n\n출신지: 신성 제국, 일루미네르 수도\n\n역할: 치유와 보호, 파티의 정신적 지주\n\n루미엘은 태어날 때부터 \n신성한 빛에 선택받은 축복받은 아이로,\n 일루미네르 성당에서 자랐습니다.\n 그녀의 부모는 이름 없는 농민이었지만,\n 그녀가 태어난 날 밤 \n하늘에 빛나는 성운이 나타나\n 그녀의 신성한 운명을 예고했습니다.\n\n 어린 시절부터 치유의 기적을 보여준\n 그녀는 신성 제국의 주목을 받았고,\n 성녀로서 수련을 받게 되었습니다.\n\n 그러나 그녀는 단순히 역할에 만족하지 않고\n 자신의 힘으로 사람들을 진정으로 돕고 싶어했습니다.",
        
            "종족: 마법 슬라임\n\n성별: 없음 \n(하지만 모든 파티원이 각각 다르게 부릅니다. \n루미엘은 '그 아이', \n셀레나는 '얘', \n레온은 '녀석', \n에린은 '슬리'로 부름.)\n\n나이: 추정 불가 (외관상 아기처럼 보임)\n\n출신지: 불명의 고대 유적 (마법으로 태어난 존재)\n\n역할: 파티의 귀여운 마스코트이자, \n서포터 겸 의외의 전투원\n\n슬리는 원래 고대 유적지에서 잠들어 있던 존재로,\n 루미엘이 처음으로 발견했습니다.\n\n 유적의 신성한 에너지가 반응하며 \n슬라임이 생명체로 깨어났고,\n 파티원들과 함께 행동하기 시작했습니다."
        };
    
        int x = 60;
        for (int i = 0; i < characters.length; i++) {
            final int index = i;  // i를 final 변수로 캡처
        
            String character = characters[i];
            String imagePath = imagePaths[i];
        
            JButton button = new JButton(character);
            button.setBounds(x + i * 30, 600, 120, 50);
            button.addActionListener(e -> {
                selectedCharacter = character;
                charImageLabel.setIcon(scaleImageToLabel(new ImageIcon(imagePath), charImageLabel));
                charDescLabel.setText(character); // 캐릭터 이름 변경
                descriptionLabel.setText("<html>" + characterDescriptions[index].replace("\n", "<br>") + "</html>"); // 캐릭터 설명 변경
            });
            charSelectionPanel.add(button);
            x += 120;
        }
    
        JButton confirmButton = new JButton("확인");
        confirmButton.setBounds(350, 680, 100, 50);
        confirmButton.addActionListener(e -> {
            if (!selectedCharacter.isEmpty()) {
                JOptionPane.showMessageDialog(this, selectedCharacter + "로 게임을 시작합니다!", "캐릭터 선택 완료", JOptionPane.INFORMATION_MESSAGE);
                startGame(speed, difficulty,selectedCharacter);
            } else {
                JOptionPane.showMessageDialog(this, "캐릭터를 선택해주세요!", "오류", JOptionPane.WARNING_MESSAGE);
            }
        });
        charSelectionPanel.add(confirmButton);
    
        getContentPane().add(charSelectionPanel);
        revalidate();
        repaint();
    }
    
    // 이미지 크기를 라벨 크기에 맞게 조정하는 메서드
    private ImageIcon scaleImageToLabel(ImageIcon icon, JLabel label) {
        Image img = icon.getImage();
        int width = label.getWidth();
        int height = label.getHeight();
        Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH); // 라벨 크기에 맞게 이미지 조정
        return new ImageIcon(scaledImg);
    }

    private void startGame(int speed, int difficulty, String selectedCharacter) {
        gameSpeed = speed;
        currentDifficulty = difficulty; // 현재 난이도 저장
        initializeGameBoard(difficulty,selectedCharacter);
    
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

    private void initializeGameBoard(int difficulty,String selectedCharacter) {
        currentDifficulty = difficulty;
        getContentPane().removeAll();
        revalidate();
        repaint();

        musicManager.stopMusic(); // 기존 음악 정지

                // 난이도에 따라 음악 파일 설정
        String musicFilePath = switch (currentDifficulty) {
            case 1 -> "sings/easy.wav";  // 난이도 하
            case 2 -> "sings/hard.wav"; // 난이도 중
            case 3 -> "sings/normal.wav";  // 난이도 상
            default -> {
                System.err.println("Invalid difficulty level");
                yield null; // null 반환 방지
            }
        };

                // 음악 파일 경로가 유효하면 재생
        if (musicFilePath != null) {
            musicManager.playMusic(musicFilePath);
        }

        if (difficulty == 1) {
            easyIcon = new ImageIcon("images/easy.jpg");
        }
        else if (difficulty == 2) {
            easyIcon = new ImageIcon("images/normal.jpg");
        }
        else if (difficulty == 3) {
            easyIcon = new ImageIcon("images/hard.jpg");
        }
        //"에린 카르테스", "레온 하르트", "셀레나 블레이즈", "루미엘 에테리아", "슬리"
        if (selectedCharacter.equals("에린 카르테스")) {chIcon = new ImageIcon("images/ch/man_1.png"); abIcon = new ImageIcon("images/ability/sword.png");}
        else if (selectedCharacter.equals("레온 하르트")) {chIcon = new ImageIcon("images/ch/man_2.png"); abIcon = new ImageIcon("images/ability/shield_icon.png");}
        else if (selectedCharacter.equals("셀레나")) {chIcon = new ImageIcon("images/ch/woman_1.png"); abIcon = new ImageIcon("images/ability/fire.png");}
        else if (selectedCharacter.equals("루미엘")) {chIcon = new ImageIcon("images/ch/woman_2.png"); abIcon = new ImageIcon("images/ability/cross.png");}
        else if (selectedCharacter.equals("슬리")) {chIcon = new ImageIcon("images/ch/slime.png"); abIcon = new ImageIcon("images/ability/red.png");}

        easyLabel = new JLabel(easyIcon);
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

        // 현재 진행 상황을 표시할 라벨 추가
        progressLabel = new JLabel(String.format("블록: %d / %d", clearedBlocks, totalBlocks));
        progressLabel.setBounds(400, 460, 100, 50);
        progressLabel.setForeground(Color.WHITE); // 글자 색상 설정
        progressLabel.setOpaque(true); // 배경을 보이게 설정
        progressLabel.setBackground(Color.BLACK);
        easyLabel.add(progressLabel);

        explainlabel = new JLabel("<html>Z : 블록 다운<br>X : 홀딩<br>C : 능력 사용</html>");
        explainlabel.setBounds(400, 580, 100, 100); // 위치와 크기 설정
        explainlabel.setForeground(Color.WHITE); // 글자 색상 설정
        explainlabel.setOpaque(true); // 배경을 보이게 설정
        explainlabel.setBackground(Color.black); // 디버깅용 배경색 추가
        easyLabel.add(explainlabel);
        
        //능력에 대한 이미지를 넣고 사용지 없는것처럼 보이게 할 겁니다.abIcon ability label 550, 130, 100, 50
        abilitylabel = new JLabel(abIcon);
        abilitylabel.setBounds(550, 130, 100, 50); // 위치와 크기 설정
        abilitylabel.setOpaque(false); // 배경을 보이게 설정
        // 이미지 크기를 라벨 크기에 맞게 조정
        Image abimg = abIcon.getImage();
        int abwidth = abilitylabel.getWidth();
        int abheight = abilitylabel.getHeight();
        Image abscaledImg = abimg.getScaledInstance(abwidth, abheight, Image.SCALE_SMOOTH); // 라벨 크기에 맞게 이미지 크기 조정
        abIcon = new ImageIcon(abscaledImg);  // 크기 조정된 이미지를 다시 chIcon에 저장
        abilitylabel.setIcon(abIcon); // 크기 조정된 이미지를 라벨에 적용

        easyLabel.add(abilitylabel);

        // 캐릭터 이미지 라벨 생성 및 크기 조정
        chlabel = new JLabel(chIcon);
        chlabel.setBounds(550, 200, 200, 500); // 위치와 크기 설정
        chlabel.setOpaque(false); // 배경을 보이게 설정
        // 이미지 크기를 라벨 크기에 맞게 조정
        Image img = chIcon.getImage();
        int width = chlabel.getWidth();
        int height = chlabel.getHeight();
        Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH); // 라벨 크기에 맞게 이미지 크기 조정
        chIcon = new ImageIcon(scaledImg);  // 크기 조정된 이미지를 다시 chIcon에 저장
        chlabel.setIcon(chIcon); // 크기 조정된 이미지를 라벨에 적용

        easyLabel.add(chlabel);
        

        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                cellLabels[i][j] = new JLabel();
                cellLabels[i][j].setOpaque(true);
                cellLabels[i][j].setBackground(Color.BLACK);
                gameLabel.add(cellLabels[i][j]);
            }
        }

        clearedBlocks=0;
        progressLabel.setText(String.format("블록: %d / %d", clearedBlocks, totalBlocks));

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
    
        currentColor = getColorForBlock(blockType); // 현재 블록의 색상 설정
    
        // 게임 오버 여부 확인
        if (isGameOver()) {
            timer.stop();
            musicManager.stopMusic();  // 게임 오버 후 음악 멈추기
            musicManager.playMusic("sings/gameover.wav"); // 게임 오버 소리 재생
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


private void drawBlock(int rotation) {
    clearBoard(); // 기존 블록의 셀만 초기화

    int[][] shape;
    if (blockType == -2) { // 에린의 능력 블록
        shape = new int[][] {
            {1, 1},
            {1, 1},
            {1, 1},
            {1, 1},
        };
    } else if (blockType == -1) { // 셀레나의 능력 블록 (1x1 하얀 블록)
        shape = new int[][] { {1} };
    } else {
        shape = SHAPE[blockType][rotation];
    }

    for (int i = 0; i < shape.length; i++) {
        for (int j = 0; j < shape[i].length; j++) {
            if (shape[i][j] == 1) {
                int x = currentX + i;
                int y = currentY + j;

                if (x >= 0 && x < 20 && y >= 0 && y < 10) {
                    if (blockType == -1) {
                        // 능력 블록인 경우 이미지 설정
                        ImageIcon fireIcon = new ImageIcon("images/ability/fire.png");
                        setBlockImage(fireIcon, x, y);
                    } else {
                        // 일반 블록인 경우 색상 설정
                        cellLabels[x][y].setBackground(getColorForBlock(blockType));
                        cellLabels[x][y].setIcon(null);
                    }
                }
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
        case 3: return Color.GRAY; // Z
        case 4: return Color.BLUE;   // L
        case 5: return Color.PINK;   // T
        case 6: return Color.ORANGE; // J
        default: return Color.WHITE; // 기본 값
    }
}


// 블록을 아래로 이동
private void moveBlockDown() {
    if (blockType == -2) { // 에린의 능력 블록일 경우
            // 이동 중 충돌한 블록을 삭제
        deleteCollidedBlock(currentX, currentY);

        // 아래로 이동 가능한지 확인
        if (canMove(currentX + 1, currentY)) {
            currentX++;
            drawBlock(rotation); // 블록을 화면에 그리기
        } else {
            // 더 이상 이동 불가 -> 바닥 도달 시 능력 블록 제거
            clearCurrentBlock();
            abilityUsed = false; // 능력 초기화

            // 다음 블록 생성
            blockType = nextBlockType;
            Random rand = new Random();
            nextBlockType = rand.nextInt(SHAPE.length);
            showNextBlock(); // 다음 블록 표시
            startFallingBlock(); // 새로운 블록 시작
            }
    } else {
        if (canMove(currentX + 1, currentY)) {
            // 이동 가능하면 이동
            currentX++;
            drawBlock(rotation);
    
            // 능력 블록의 새로운 위치에 이미지 설정
            if (blockType == -1) {
                ImageIcon fireIcon = new ImageIcon("images/ability/fire.png");
                setBlockImage(fireIcon, currentX, currentY);
            }
        } else {
            if (blockType == -1) {
                // 능력 블록인 경우 즉시 발동
                removeSurroundingBlocks(currentX, currentY);
        
                // 다음 블록으로 전환
                blockType = nextBlockType;
                Random rand = new Random();
                nextBlockType = rand.nextInt(SHAPE.length);
                showNextBlock(); // 다음 블록 표시
                startFallingBlock(); // 새로운 블록 시작
            } else{
                fixBlock(); // 일반 블록은 고정
            }
        }

    }
    
}



// 블록 이동 여부 체크
private boolean canMove(int x, int y) {
    int[][] shape = blockType >= 0 ? SHAPE[blockType][rotation] : new int[][] { {1} };

    for (int i = 0; i < shape.length; i++) {
        for (int j = 0; j < shape[i].length; j++) {
            if (shape[i][j] == 1) {
                int newX = x + i;
                int newY = y + j;

                // 경계를 벗어나거나 고정된 블록과 충돌 확인
                if (newX >= 20 || newY < 0 || newY >= 10) {
                    return false;
                }

                // 능력 블록은 다른 블록과의 충돌을 무시
                if (blockType != -2 && board[newX][newY] == 1) {
                    return false;
                }
            }
        }
    }
    return true; // 이동 가능
}



// 블럭 생성 시 고정된 블럭과의 충돌 여부를 체크하여 게임 오버 상태를 결정하는 메소드
private boolean isGameOver() {
    int[][] shape = SHAPE[blockType][rotation];
    for (int i = 0; i < shape.length; i++) {
        for (int j = 0; j < shape[i].length; j++) {
            if (shape[i][j] == 1 && board[currentX + i][currentY + j] == 1) {
                // 게임 오버 상태에서 성녀 부활 기능 체크
                if (selectedCharacter.equals("루미엘") && !revived) {
                    ruminel(); // 외부 클래스의 메서드 호출
                    return false; // 게임 오버 방지
                }
                return true; // 부활 불가 시 게임 오버
            }
        }
    }
    return false;
}


private void fixBlock() {
    if (blockType == -1) {
        // 능력 블록일 경우 주변 블록 제거
        removeSurroundingBlocks(currentX, currentY);

        // 새로운 블록 시작
        blockType = nextBlockType;
        Random rand = new Random();
        nextBlockType = rand.nextInt(SHAPE.length);
        showNextBlock(); // 다음 블록 표시
        startFallingBlock(); // 새로운 블록 시작
        return; // 이후 일반 블록 고정 로직 실행하지 않음
    }

    // 일반 블록 고정
    int[][] shape = blockType >= 0 ? SHAPE[blockType][rotation] : new int[][] { {1} };

    for (int i = 0; i < shape.length; i++) {
        for (int j = 0; j < shape[i].length; j++) {
            if (shape[i][j] == 1) {
                board[currentX + i][currentY + j] = 1;
                cellLabels[currentX + i][currentY + j].setBackground(currentColor);
            }
        }
    }

    clearFullLines(); // 줄 삭제 확인

    holdUsed = false; // 홀드 사용 가능 상태 초기화

    clearedBlocks++;
    updateProgress();

    if (clearedBlocks >= totalBlocks) {
        timer.stop();
        countdownTimer.stop();
        handleRewards(currentDifficulty);
        resetGame();
        return;
    }

    blockType = nextBlockType; // 현재 블록을 다음 블록으로 설정
    Random rand = new Random();
    nextBlockType = rand.nextInt(SHAPE.length);

    showNextBlock(); // 다음 블록 UI 갱신
    startFallingBlock(); // 새로운 블록 시작
}

// 진행 상황 라벨 업데이트
private void updateProgress() {
    progressLabel.setText(String.format("블록: %d / %d", clearedBlocks, totalBlocks));
}

// 게임 재시작을 위한 메소드 수정
private void resetGame() {
    // 제한시간 초기화
    remainingTime = 180;
    revived = false;
    abilityUsed = false; // 레온의 능력 초기화
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

    abilityUsed = false; 

    revalidate();
    repaint();
}

private void handleRewards(int difficulty) {
    if (difficulty == 1 && !isEasyCleared) {
        isEasyCleared = true;
        JOptionPane.showMessageDialog(this, "축하합니다! \n골드: 10000, 정수: 250\n 칭호 : <하의 정복자>", "Easy 보상", JOptionPane.INFORMATION_MESSAGE);
    } else if (difficulty == 2 && !isMediumCleared) {
        isMediumCleared = true;
        JOptionPane.showMessageDialog(this, "축하합니다! \n골드: 20000, 정수: 500\n 칭호 : <중의 정복자>", "Medium 보상", JOptionPane.INFORMATION_MESSAGE);
    } else if (difficulty == 3 && !isHardCleared) {
        isHardCleared = true;
        JOptionPane.showMessageDialog(this, "축하합니다! \n골드: 30000, 정수: 750\n 칭호 : <상의 정복자>", "Hard 보상", JOptionPane.INFORMATION_MESSAGE);
    } else {
        JOptionPane.showMessageDialog(this, "이미 정복한 난이도입니다. 보상이 지급되지 않습니다.", "알림", JOptionPane.WARNING_MESSAGE);
    }

    if (isEasyCleared && isMediumCleared && isHardCleared) {
        JOptionPane.showMessageDialog(this, "축하합니다! 모든 난이도를 정복했습니다! 칭호: '완전한 정복자'", "정복자 칭호", JOptionPane.INFORMATION_MESSAGE);
        JOptionPane.showMessageDialog(this, "에린파티의 서브 스토리가 해금되었습니다.", "스토리 해금", JOptionPane.INFORMATION_MESSAGE);
    }
}

private void erin() {
    if (abilityUsed) return; // 이미 능력을 사용했다면 실행하지 않음
    musicManager.playMusic("sings/knife.wav");

    // 효과음 종료 후 배경음악 재생
    musicManager.clip.addLineListener(event -> {
        if (event.getType() == LineEvent.Type.STOP) {
            musicManager.playMusic(getBackgroundMusicForCurrentDifficulty());
        }
    });
    

    // 기존 블록 제거
    clearCurrentBlock();

    // 능력 블록 생성
    blockType = -2; // 에린 특수 블록 타입
    rotation = 0;
    currentX = 0; // 최상단에서 시작
    currentY = 3; // 중앙 정렬

    // 능력 블록 화면 표시
    drawBlock(rotation);

    abilityUsed = true; // 능력 사용 상태 갱신
    abilitylabel.setVisible(false); // 능력 버튼 숨김
}



private void deleteCollidedBlock(int x, int y) {
    // 에린의 능력 블록 크기 (4x2)
    int[][] erinBlockShape = {
        {1, 1},
        {1, 1},
        {1, 1},
        {1, 1}
    };

    for (int i = 0; i < erinBlockShape.length; i++) {
        for (int j = 0; j < erinBlockShape[i].length; j++) {
            if (erinBlockShape[i][j] == 1) {
                int checkX = x + i;
                int checkY = y + j;

                // 보드 범위 확인
                if (checkX >= 0 && checkX < 20 && checkY >= 0 && checkY < 10) {
                    if (board[checkX][checkY] == 1) { // 고정된 블록이 존재하면
                        board[checkX][checkY] = 0; // 보드에서 제거
                        cellLabels[checkX][checkY].setBackground(Color.BLACK); // UI 초기화
                        cellLabels[checkX][checkY].setIcon(null); // 아이콘 제거
                    }
                }
            }
        }
    }
}

private void reon() {
    if (abilityUsed) return; // 이미 능력을 사용했다면 실행되지 않음
    musicManager.playMusic("sings/knife2.wav");

    musicManager.clip.addLineListener(event -> {
        if (event.getType() == LineEvent.Type.STOP) {
            musicManager.playMusic(getBackgroundMusicForCurrentDifficulty());
        }
    });

    // 가장 높은 블록과 낮은 블록의 Y 좌표를 찾기
    int highestBlock = 19; // 초기값: 가장 낮은 위치
    int lowestBlock = 0;   // 초기값: 가장 높은 위치

    for (int i = 0; i < 20; i++) { // 위에서부터 검사
        for (int j = 0; j < 10; j++) {
            if (board[i][j] == 1) {
                highestBlock = Math.min(highestBlock, i); // 가장 높은 블록 갱신
                lowestBlock = Math.max(lowestBlock, i);  // 가장 낮은 블록 갱신
            }
        }
    }

    // 블록의 범위를 확인하여 절반 계산
    int midRow = (highestBlock + lowestBlock) / 2;
    if ((highestBlock + lowestBlock) % 2 != 0) midRow++; // 홀수인 경우 올림 처리

    // 하단 절반 제거
    for (int i = midRow; i <= lowestBlock; i++) {
        for (int j = 0; j < 10; j++) {
            board[i][j] = 0; // 데이터 초기화
            cellLabels[i][j].setBackground(Color.BLACK); // 라벨 초기화
        }
    }

    // 상단 블록을 아래로 이동
    int rowsToMove = lowestBlock - midRow + 1; // 하단 제거된 줄 수 계산
    for (int i = midRow - 1; i >= 0; i--) {
        for (int j = 0; j < 10; j++) {
            board[i + rowsToMove][j] = board[i][j]; // 아래로 이동
            cellLabels[i + rowsToMove][j].setBackground(cellLabels[i][j].getBackground()); // 색상 이동

            board[i][j] = 0; // 원래 위치 초기화
            cellLabels[i][j].setBackground(Color.BLACK);
        }
    }

    // 게임 화면 숨기기 (gameLabel)
    gameLabel.setVisible(false);

    // 방패 이미지를 덮기 위한 JLabel 생성
    JLabel shieldLabel = new JLabel();
    shieldLabel.setIcon(new ImageIcon("images/ability/shield_icon.png"));
    shieldLabel.setBounds(gameLabel.getBounds()); // gameLabel과 같은 위치에 설정
    shieldLabel.setOpaque(false); // 배경을 투명하게 설정

    // easyLabel에 방패 이미지 추가
    easyLabel.add(shieldLabel);
    easyLabel.revalidate(); 
    easyLabel.repaint();   

    // 항상 0.5초 동안 방패 이미지가 보이도록 설정
    int shieldTime = 500; // 0.5초 (500ms)

    // 지정된 시간 후 방패 이미지 제거하고 gameLabel 다시 보이게 하기
    new Timer(shieldTime, e -> {
        easyLabel.remove(shieldLabel); // 방패 이미지 제거
        gameLabel.setVisible(true);     // gameLabel 다시 보이게 하기
        easyLabel.revalidate();         // 레이아웃 갱신
        easyLabel.repaint();            // 화면 갱신
    }).start();

    // 능력 발동 후 abilitylabel 숨김
    abilitylabel.setVisible(false);

    abilityUsed = true; // 능력 사용 처리
}


private void serena() {
    if (abilityUsed) return; // 이미 능력을 사용했다면 실행하지 않음
    musicManager.playMusic("sings/wizard.wav");

    musicManager.clip.addLineListener(event -> {
        if (event.getType() == LineEvent.Type.STOP) {
            musicManager.playMusic(getBackgroundMusicForCurrentDifficulty());
        }
    });

    // 현재 블록 삭제
    clearCurrentBlock();

    // 능력 블록 설정
    blockType = -1; // 셀레나의 특수 블록 타입
    rotation = 0;
    currentX = 0; // 화면 최상단
    currentY = 4; // 중앙에서 시작

    // 능력 블록 이미지 설정
    ImageIcon fireIcon = new ImageIcon("images/ability/fire.png");
    setBlockImage(fireIcon, currentX, currentY);

    // 능력 사용 처리
    abilityUsed = true;
    abilitylabel.setVisible(false); // 능력 버튼 숨김
}

private void clearCurrentBlock() {
    int[][] shape = blockType >= 0 ? SHAPE[blockType][rotation] : new int[][] { {1} };

    for (int i = 0; i < shape.length; i++) {
        for (int j = 0; j < shape[i].length; j++) {
            if (shape[i][j] == 1) {
                int x = currentX + i;
                int y = currentY + j;

                if (x >= 0 && x < 20 && y >= 0 && y < 10) {
                    board[x][y] = 0; // 보드 데이터 초기화
                    cellLabels[x][y].setBackground(Color.BLACK); // 색상 초기화
                    cellLabels[x][y].setIcon(null); // 이미지 제거
                }
            }
        }
    }
}

private void setBlockImage(ImageIcon icon, int x, int y) {
    Image img = icon.getImage();
    int width = cellLabels[0][0].getWidth(); // 블록 크기에 맞춤
    int height = cellLabels[0][0].getHeight();
    Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
    icon = new ImageIcon(scaledImg);

    // 셀에 이미지 설정
    cellLabels[x][y].setIcon(icon);
    cellLabels[x][y].setBackground(null); // 배경 제거
}


// 특정 좌표 주변 2칸의 블록을 제거하고 능력 블록도 제거
private void removeSurroundingBlocks(int centerX, int centerY) {
    int startX = Math.max(centerX - 2, 0);
    int endX = Math.min(centerX + 2, 19);
    int startY = Math.max(centerY - 2, 0);
    int endY = Math.min(centerY + 2, 9);

    for (int i = startX; i <= endX; i++) {
        for (int j = startY; j <= endY; j++) {
            board[i][j] = 0; // 보드 데이터 초기화
            cellLabels[i][j].setBackground(Color.BLACK); // 색상 초기화
            cellLabels[i][j].setIcon(null); // 이미지 제거
        }
    }

    // 능력 블록도 제거
    board[centerX][centerY] = 0;
    cellLabels[centerX][centerY].setBackground(Color.BLACK);
    cellLabels[centerX][centerY].setIcon(null);
}


private void ruminel() {
    if (!revived) { // 부활 가능 여부 확인
        musicManager.playMusic("sings/nun.wav");
        for (int i = 0; i < 15; i++) { // 상단 10줄 제거
            for (int j = 0; j < 10; j++) {
                board[i][j] = 0; // 보드 데이터 초기화
                cellLabels[i][j].setBackground(Color.BLACK); // 라벨 색상 초기화
            }
        }

        musicManager.clip.addLineListener(event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                musicManager.playMusic(getBackgroundMusicForCurrentDifficulty());
            }
        });

        // 능력 라벨 숨기기
        abilitylabel.setVisible(false);

        // 부활 상태 기록
        revived = true;

        // UI 갱신
        revalidate();
        repaint();

        JOptionPane.showMessageDialog(Tetris.this, "성녀의 부활 능력이 발동되었습니다!", "부활", JOptionPane.INFORMATION_MESSAGE);

        // 새로운 블록 시작
        startFallingBlock();
    }
}



private void sily() {
    if (abilityUsed) return; // 이미 능력을 사용했다면 실행하지 않음
    abilitylabel.setVisible(false); // 기존 능력 관련 레이블 숨김

    musicManager.playMusic("sings/slime_sound.wav");

    musicManager.clip.addLineListener(event -> {
        if (event.getType() == LineEvent.Type.STOP) {
            musicManager.playMusic(getBackgroundMusicForCurrentDifficulty());
        }
    });

    // 슬라임 캐릭터 이미지를 red_slime 이미지로 변경
    ImageIcon redSlimeIcon = new ImageIcon("images/ability/red_slime.png"); // 새로운 이미지 로드
    Image img = redSlimeIcon.getImage(); // 이미지 객체 가져오기
    int width = chlabel.getWidth(); // chlabel의 너비 가져오기
    int height = chlabel.getHeight(); // chlabel의 높이 가져오기
    Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH); // 라벨 크기에 맞게 이미지 크기 조정
    redSlimeIcon = new ImageIcon(scaledImg); // 조정된 이미지를 다시 아이콘으로 저장
    chlabel.setIcon(redSlimeIcon); // chlabel의 이미지를 새로운 이미지로 변경

    abilityUsed = true;
}

// 보드를 초기화하여 화면을 비운다
private void clearBoard() {
    for (int i = 0; i < 20; i++) {
        for (int j = 0; j < 10; j++) {
            if (board[i][j] == 0) { // 고정된 블록은 초기화하지 않음
                cellLabels[i][j].setBackground(Color.BLACK); // 빈 칸 초기화
                cellLabels[i][j].setIcon(null); // 아이콘 초기화
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
                case KeyEvent.VK_C:
                    if (selectedCharacter.equals("에린 카르테스")) {erin();}
                    else if (selectedCharacter.equals("레온 하르트")) {reon();}
                    else if (selectedCharacter.equals("셀레나")) {serena();}
                    else if (selectedCharacter.equals("슬리")) {sily();}
                    break;
            }
        }

        private void handleHoldBlock() {
            if (holdUsed) return; // 이미 홀드를 사용한 경우 무시
        
            if (holdBlockType == -1) {
                // 홀드가 비어있을 때: 현재 블록을 저장하고 새 블록 시작
                holdBlockType = blockType; // 현재 블록 타입 저장
                blockType = nextBlockType; // 다음 블록으로 교체
                nextBlockType = new Random().nextInt(SHAPE.length); // 새로운 다음 블록 생성
            } else {
                // 홀드에 블록이 있을 때: 현재 블록과 홀드 블록 교체
                int temp = blockType;
                blockType = holdBlockType; // 홀드 블록을 현재 블록으로 설정
                holdBlockType = temp; // 현재 블록을 홀드에 저장
            }
        
            rotation = 0; // 홀드 블록은 기본 회전 상태로 시작
            currentX = 0; // 초기 위치로 설정
            currentY = 3;
        
            // 홀드 표시와 다음 블록 라벨 업데이트
            updateHoldLabel();
            showNextBlock(); // 다음 블록 갱신
        
            holdUsed = true; // 홀드 사용 처리
        
            // 새 블록 화면에 표시
            drawBlock(rotation);
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
        // Z키 눌렀을 때, 블록이 바닥까지 빠르게 내려가도록 처리
        private void dropBlockInstantly() {
            // 블록이 더 이상 내려갈 수 없을 때까지 내려보낸다
            while (canMove(currentX + 1, currentY)) {
                currentX++; // 아래로 한 칸 내려가기
            }

    // 블록이 도착한 위치에서 추가 동작 처리
            if (blockType == -1) {
                // 셀레나의 능력 블록일 경우, 주변 블록 제거
                removeSurroundingBlocks(currentX, currentY);
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
