package adris.altoclef.altomenu.cheatUtils;

public enum Target {
    Head("Head"),
    Body("Body"),
    Feet("Feet");

    private final String title;

    Target(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
