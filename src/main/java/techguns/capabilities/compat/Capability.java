package techguns.capabilities.compat;

/** Identity token for a Techguns-side capability (item handler, energy, fluids). */
public final class Capability<T> {
    private final String name;
    Capability(String name) { this.name = name; }
    @Override public String toString() { return name; }
}
