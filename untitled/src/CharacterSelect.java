public enum CharacterSelect {
    CHARACTER_기쁜수룡(new String[] {
            "character/soo0.png",
            "character/soo1.png",
            "character/soo2.png"
    }),
    CHARACTER_슬픈수룡(new String[] {
            "character/sad0.png",
            "character/sad1.png",
            "character/sad2.png"
    });
    private final String[] imagePath;
    CharacterSelect(String[] imagePath) {
        this.imagePath = imagePath;
    }
    public String[] getImagePath() {
        return imagePath;
    }
}
