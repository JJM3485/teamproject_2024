import java.awt.Color;

public class Tetromino {
    private int[][][] shape;
    private Color color;
    private int rotation; // 현재 회전 상태를 나타내는 변수 (0, 90, 180, 270도)
    
    public Tetromino(int[][][] shape, Color color) {
        this.shape = shape;
        this.color = color;
        this.rotation = 0; // 기본 회전 상태는 0도
    }
    
    public int[][] getShape() {
        return shape[rotation];
    }
    
    public Color getColor() {
        return color;
    }
    
    public void rotate() {
        rotation = (rotation + 1) % shape.length; // 회전 상태 변경
    }
    
    public void resetRotation() {
        rotation = 0; // 회전 상태 초기화
    }
}
