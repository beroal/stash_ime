package ua.in.beroal.stash_ime;

import java.util.Objects;

public class EditKbModeLine extends EditKbMode implements Cloneable {
    private int op;
    private int coord;

    public EditKbModeLine(int op, int coord) {
        this.op = op;
        this.coord = coord;
    }

    @Override
    public EditKbModeLine clone() {
        return (EditKbModeLine) super.clone();
    }

    public int getOp() {
        return op;
    }

    public int getCoord() {
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

    @Override
    public int hashCode() {
        return Objects.hash(op, coord);
    }
}