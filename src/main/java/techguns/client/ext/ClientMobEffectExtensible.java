package techguns.client.ext;

import java.util.function.Consumer;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

public interface ClientMobEffectExtensible { void initializeClient(Consumer<IClientMobEffectExtensions> consumer); }
