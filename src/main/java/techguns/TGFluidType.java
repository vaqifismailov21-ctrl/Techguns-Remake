package techguns;

import java.util.function.Consumer;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;
import techguns.client.ext.ClientFluidTypeExtensible;

/** FluidType whose client textures are registered through RegisterClientExtensionsEvent. */
public class TGFluidType extends FluidType implements ClientFluidTypeExtensible {
    public TGFluidType(Properties properties) { super(properties); }
    @Override public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {}
}
