package techguns.client.models;

import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;

/** Captures the actual legacy body geometry rather than the modern animation skeleton. */
public interface LegacyDeathModel {
    List<MeshSnapshot> deathParts(PoseStack pose);
}
