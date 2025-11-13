package main;

public enum RepeatMode {
    OFF,
    ALL,
    ONE;

    public RepeatMode next() {
        return values()[(this.ordinal() + 1) % values().length];
    }
}
