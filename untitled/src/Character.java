import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Character {

    private BufferedImage characterImage;
    private int x;
    private int y;
    private final int width = 130;
    private final int height = 130;

    public Character(int initialX, int initialY, String imagePath) {
        this.x = initialX;
        this.y = initialY;
        loadCharacterImage(imagePath);
    }


    private void loadCharacterImage(String filePath) {
        try {

            characterImage = ImageIO.read(getClass().getResourceAsStream("/" + filePath));

            if (characterImage == null) {
                System.err.println("오류: 리소스 스트림을 읽을 수 없습니다. 경로를 확인하세요: /" + filePath);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("오류: 이미지 로딩 중 문제 발생.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BufferedImage getImage() {
        return characterImage;
    }


    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

}