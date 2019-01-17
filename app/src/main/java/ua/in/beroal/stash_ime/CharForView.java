package ua.in.beroal.stash_ime;

public class CharRow {
    private int codePoint;
    private String name;

    public CharRow(int codePoint, String name) {
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
