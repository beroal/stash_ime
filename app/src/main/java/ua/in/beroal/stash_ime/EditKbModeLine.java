package ua.in.beroal.stash_ime;

/**
 * A mode for working with keyboard rows and columns.
 */
public class EditKbModeLine extends EditKbMode implements Cloneable {
    private Op op;
    private Coord coord;

    public EditKbModeLine(Op op, Coord coord) {
        this.op = op;
        this.coord = coord;
    }

    public Op getOp() {
        return op;
    }

    public Coord getCoord() {
        return coord;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EditKbModeLine that = (EditKbModeLine) o;
        return op == that.op &&
                coord == that.coord;
    }

    public enum Op {DELETE, INSERT}

    public enum Coord {ROW, COLUMN}
}