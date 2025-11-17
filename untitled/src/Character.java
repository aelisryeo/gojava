import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Character {

    private BufferedImage characterImage;
    private int x;
    private int y;
    private final int width = 80;  // 캐릭터 너비
    private final int height = 80; // 캐릭터 높이

    // 생성자: 초기 위치와 이미지 파일을 받아 로드합니다.
    public Character(int initialX, int initialY, String imagePath) {
        this.x = initialX;
        this.y = initialY;
        loadCharacterImage(imagePath);
    }

    // 이미지 로드 메소드
    // Character.java 파일 내 수정할 부분

    private void loadCharacterImage(String filePath) {
        try {
            // ClassLoader를 사용하여 리소스를 스트림으로 읽어옵니다.
            // 파일이 src 폴더 바로 아래 있으므로, 경로를 "/"로 시작하여 루트 경로에서 찾습니다.
            // IDE나 빌드 시스템은 보통 src 폴더의 내용을 클래스 경로의 루트(root)로 만듭니다.

            // 주의: 파일 경로는 "/파일명.확장자" 형태로 사용합니다.

            // /character1.png 는 src 폴더 아래의 character1.png 파일을 의미합니다.
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

    // 그리기 메소드: JPanel의 paintComponent에서 호출됩니다.
    public void draw(Graphics g, Component observer) {
        if (characterImage != null) {
            g.drawImage(characterImage, x, y, width, height, observer);
        } else {
            // 이미지가 없을 경우 대체 사각형 그리기 (디버깅용)
            g.setColor(Color.RED);
            g.fillRect(x, y, width, height);
        }
    }

    public BufferedImage getImage() {
        return characterImage;
    }


    // --- Getter 및 Setter 메소드 ---
    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    // (TODO: 나중에 캐릭터 방향, 움직임 상태 등을 추가할 수 있습니다.)
}