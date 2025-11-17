public enum CharacterSelect {
    CHARACTER_ONE("image/character1.png"),
    CHARACTER_TWO("image/character2.png");
    private final String imagePath;
    CharacterSelect(String imagePath) {
        this.imagePath = imagePath;
    }
    public String getImagePath() {
        return imagePath;
    }
}
