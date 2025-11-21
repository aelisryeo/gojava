public enum CharacterSelect {
    CHARACTER_수룡(new String[] {
            "character/soo0.png",
            "character/soo1.png",
            "character/soo2.png"
    }),
    CHARACTER_안경수룡(new String[] {
            "character/ch0.png",
            "character/ch1.png",
            "character/ch2.png"
    });
    private final String[] imagePath;
    CharacterSelect(String[] imagePath) {
        this.imagePath = imagePath;
    }
    public String[] getImagePath() {
        return imagePath;
    }
}
