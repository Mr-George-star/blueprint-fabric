package net.george.blueprint.client.model.generator;

import net.george.blueprint.common.resource.ExistingFileHelper;
import net.minecraft.util.Identifier;

/**
 * Builder for block models, does not currently provide any additional
 * functionality over {@link ModelBuilder}, purely a stub class with a concrete
 * generic.
 *
 * @see ModelProvider
 * @see ModelBuilder
 */
public class BlockModelBuilder extends ModelBuilder<BlockModelBuilder> {
    public BlockModelBuilder(Identifier outputLocation, ExistingFileHelper existingFileHelper) {
        super(outputLocation, existingFileHelper);
    }
}
