import java.awt.*;
import java.io.InputStream;

public class GameFont {
    private static Font baseFont; // 한 번 로딩된 폰트를 저장해둘 변수

    private static final String FONT_PATH = "font/Hakgyoansim Dunggeunmiso TTF B.ttf";


    public static Font getFont(int style, float size) {
        if (baseFont == null) {
            loadFont(); // 아직 로딩 안 됐으면 로딩함
        }

        if (baseFont != null) {
            return baseFont.deriveFont(style, size);
        }

        return new Font("SansSerif", style, (int)size);
    }

    private static void loadFont() {
        try {
            InputStream is = GameFont.class.getResourceAsStream(FONT_PATH);
            if (is == null) {
                System.err.println("폰트 파일을 찾을 수 없습니다: " + FONT_PATH);
                return;
            }
            baseFont = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(baseFont);
        } catch (Exception e) {
            System.err.println("폰트 로딩 중 에러 발생!");
            e.printStackTrace();
        }
    }
}