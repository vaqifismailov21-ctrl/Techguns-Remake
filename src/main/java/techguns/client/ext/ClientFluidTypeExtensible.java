package techguns.client.ext;

import java.util.function.Consumer;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

public interface ClientFluidTypeExtensible { void initializeClient(Consumer<IClientFluidTypeExtensions> consumer); }
