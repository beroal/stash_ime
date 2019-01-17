package ua.in.beroal.stash_ime;

public class CharForView {
    private int codePoint;
    private String name;

    public CharForView(int codePoint, String name) {
        this.codePoint = codePoint;
        this.name = name;
    }

    public int getCodePoint() {
        return codePoint;
    }

    public String getName() {
        return name;
    }
}
