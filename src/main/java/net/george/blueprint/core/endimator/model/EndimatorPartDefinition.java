package net.george.blueprint.core.endimator.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.george.blueprint.core.endimator.EndimatorModelPart;
import net.minecraft.client.model.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Similar to {@link ModelPartData} but used to bake {@link EndimatorModelPart} instances.
 *
 * @author SmellyModder
 */
@SuppressWarnings("unused")
public final class EndimatorPartDefinition {
    private final List<ModelCuboidData> cubes;
    private final EndimatorPartPose partPose;
    private final Map<String, EndimatorPartDefinition> children = Maps.newHashMap();

    private EndimatorPartDefinition(List<ModelCuboidData> cubes, EndimatorPartPose partPose) {
        this.cubes = cubes;
        this.partPose = partPose;
    }

    /**
     * Creates a new {@link EndimatorPartDefinition} instance that has no cubes and no children.
     *
     * @return A new {@link EndimatorPartDefinition} instance that has no cubes and no children.
     */
    public static EndimatorPartDefinition root() {
        return new EndimatorPartDefinition(ImmutableList.of(), new EndimatorPartPose());
    }

    /**
     * Essentially a tweaked version of {@link ModelPartData#addChild(String, ModelPartBuilder, ModelTransform)}.
     *
     * @param name            The name of the child to add.
     * @param builder The {@link ModelPartBuilder} instance for the cubes of the child.
     * @param pose            An {@link EndimatorPartPose} instance to pose the child.
     * @return The constructed child.
     */
    public EndimatorPartDefinition addOrReplaceChild(String name, ModelPartBuilder builder, EndimatorPartPose pose) {
        EndimatorPartDefinition partDefinition = new EndimatorPartDefinition(builder.build(), pose);
        EndimatorPartDefinition previous = this.children.put(name, partDefinition);
        if (previous != null) previous.children.putAll(partDefinition.children);
        return partDefinition;
    }

    /**
     * An alternative version of {@link #addOrReplaceChild(String, ModelPartBuilder, EndimatorPartPose)} that takes in a {@link ModelTransform} instance.
     *
     * @param name            The name of the child to add.
     * @param modelPartBuilder The {@link ModelPartBuilder} instance for the cubes of the child.
     * @param transform            An {@link ModelTransform} instance to pose the child.
     * @return The constructed child.
     */
    public EndimatorPartDefinition addOrReplaceChild(String name, ModelPartBuilder modelPartBuilder, ModelTransform transform) {
        return this.addOrReplaceChild(name, modelPartBuilder, new EndimatorPartPose().setPartPose(transform));
    }

    /**
     * Bakes this part and its children.
     *
     * @param xTexSize The x texture size.
     * @param yTexSize The y texture size.
     * @return A {@link EndimatorModelPart} instance representing a baked version of this part.
     */
    public EndimatorModelPart bake(int xTexSize, int yTexSize) {
        Object2ObjectArrayMap<String, ModelPart> bakedChildren = this.children.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, (entry) -> entry.getValue().bake(xTexSize, yTexSize), (modelPart, modelPart2) -> modelPart, Object2ObjectArrayMap::new));
        List<ModelPart.Cuboid> bakedCubes = this.cubes.stream().map((data) -> data.createCuboid(xTexSize, yTexSize)).collect(ImmutableList.toImmutableList());
        EndimatorModelPart modelPart = new EndimatorModelPart(bakedCubes, bakedChildren);
        modelPart.loadPose(this.partPose);
        return modelPart;
    }

    /**
     * Gets a child part by its name.
     *
     * @param name The name of the child part.
     * @return The child part for the given name.
     */
    public EndimatorPartDefinition getChild(String name) {
        return this.children.get(name);
    }
}

