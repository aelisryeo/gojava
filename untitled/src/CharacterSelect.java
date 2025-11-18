public enum CharacterSelect {
    CHARACTER_수룡("image/character1.png"),
    CHARACTER_안경수룡("image/character2.png");
    private final String imagePath;
    CharacterSelect(String imagePath) {
        this.imagePath = imagePath;
    }
    public String getImagePath() {
        return imagePath;
    }
}
