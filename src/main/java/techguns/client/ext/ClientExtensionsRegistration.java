package techguns.client.ext;

/**
 * NeoForge 21.1 still calls the deprecated Item/Block/MobEffect/FluidType#initializeClient hooks itself (through
 * ClientExtensionsManager) and refuses a second registration for the same object, so the Techguns classes keep their
 * 1.20.1 initializeClient overrides and nothing is registered here. When NeoForge removes those hooks (21.2+), register
 * the extensions from RegisterClientExtensionsEvent here.
 */
public final class ClientExtensionsRegistration {
    private ClientExtensionsRegistration() {}
}
