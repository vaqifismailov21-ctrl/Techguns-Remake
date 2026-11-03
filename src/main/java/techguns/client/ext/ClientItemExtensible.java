package techguns.client.ext;

import java.util.function.Consumer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/** 1.21.1: items provide their client extensions here; {@link ClientExtensionsRegistration} hands them to NeoForge. */
public interface ClientItemExtensible { void initializeClient(Consumer<IClientItemExtensions> consumer); }
