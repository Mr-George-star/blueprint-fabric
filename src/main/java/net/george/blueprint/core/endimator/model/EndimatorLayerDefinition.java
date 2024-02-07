package net.george.blueprint.core.endimator.model;

import net.george.blueprint.core.endimator.EndimatorModelPart;
import net.minecraft.client.model.TexturedModelData;

/**
 * A slightly hacky subclass of {@link TexturedModelData} to ease creation of {@link EndimatorModelPart} trees.
 *
 * @author SmellyModder
 */
@SuppressWarnings("unused")
public final class EndimatorLayerDefinition extends TexturedModelData {
    private final EndimatorPartDefinition root;
    private final int xTexSize;
    private final int yTexSize;

    public EndimatorLayerDefinition(EndimatorPartDefinition root, int xTexSize, int yTexSize) {
        super(null, null);
        this.root = root;
        this.xTexSize = xTexSize;
        this.yTexSize = yTexSize;
    }

    @Override
    public EndimatorModelPart createModel() {
        return this.root.bake(this.xTexSize, this.yTexSize);
    }
}
