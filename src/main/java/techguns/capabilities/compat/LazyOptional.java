package techguns.capabilities.compat;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Minimal stand-in for Forge's LazyOptional, kept so the machine code stays close to 1.20.1.
 * NeoForge 1.21 capabilities are plain nullable lookups; {@link Capabilities} bridges both worlds.
 */
public final class LazyOptional<T> {
    private static final LazyOptional<?> EMPTY = new LazyOptional<>(null);
    private Supplier<T> supplier; private T value; private boolean resolved, valid = true;
    private LazyOptional(Supplier<T> supplier) { this.supplier = supplier; if (supplier == null) valid = false; }
    public static <T> LazyOptional<T> of(Supplier<T> supplier) { return supplier == null ? empty() : new LazyOptional<>(supplier); }
    @SuppressWarnings("unchecked") public static <T> LazyOptional<T> empty() { return (LazyOptional<T>) EMPTY; }
    public static <T> LazyOptional<T> ofNullable(T value) { return value == null ? empty() : of(() -> value); }
    private T get() { if (!valid) return null; if (!resolved) { value = supplier.get(); resolved = true; } return value; }
    public boolean isPresent() { return get() != null; }
    public Optional<T> resolve() { return Optional.ofNullable(get()); }
    public T orElse(T other) { T v = get(); return v != null ? v : other; }
    public T orElseGet(Supplier<? extends T> other) { T v = get(); return v != null ? v : other.get(); }
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> e) throws X { T v = get(); if (v == null) throw e.get(); return v; }
    public T orElseThrow() { T v = get(); if (v == null) throw new IllegalStateException("Capability not present"); return v; }
    public void ifPresent(Consumer<? super T> c) { T v = get(); if (v != null) c.accept(v); }
    public <U> Optional<U> map(Function<? super T, ? extends U> f) { return resolve().map(f); }
    @SuppressWarnings("unchecked") public <X> LazyOptional<X> cast() { return (LazyOptional<X>) this; }
    private java.util.List<Consumer<LazyOptional<T>>> listeners;
    /** Called once when this optional is invalidated (Forge NonNullConsumer listener). */
    public void addListener(Consumer<LazyOptional<T>> listener) { if (this == EMPTY) return; if (!valid) { listener.accept(this); return; } if (listeners == null) listeners = new java.util.ArrayList<>(); listeners.add(listener); }
    public void invalidate() { if (this != EMPTY && valid) { valid = false; value = null; if (listeners != null) { var l = listeners; listeners = null; l.forEach(c -> c.accept(this)); } } }
}
