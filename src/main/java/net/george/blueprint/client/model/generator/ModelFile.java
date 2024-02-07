package net.george.blueprint.client.model.generator;

import com.google.common.base.Preconditions;
import net.george.blueprint.common.resource.ExistingFileHelper;
import net.minecraft.util.Identifier;

@SuppressWarnings("unused")
public abstract class ModelFile {
    protected Identifier id;

    protected ModelFile(Identifier identifier) {
        this.id = identifier;
    }

    protected abstract boolean exists();

    public Identifier getId() {
        assertExistence();
        return this.id;
    }

    /**
     * Assert that this model exists.
     * @throws IllegalStateException if this model does not exist
     */
    public void assertExistence() {
        Preconditions.checkState(exists(), "Model at %s does not exist", this.id);
    }

    public Identifier getUncheckedLocation() {
        return this.id;
    }

    public static class UncheckedModelFile extends ModelFile {
        public UncheckedModelFile(String id) {
            this(new Identifier(id));
        }

        public UncheckedModelFile(Identifier identifier) {
            super(identifier);
        }

        @Override
        protected boolean exists() {
            return true;
        }
    }

    public static class ExistingModelFile extends ModelFile {
        private final ExistingFileHelper existingHelper;

        public ExistingModelFile(Identifier id, ExistingFileHelper existingHelper) {
            super(id);
            this.existingHelper = existingHelper;
        }

        @Override
        protected boolean exists() {
            if (getUncheckedLocation().getPath().contains(".")) {
                return existingHelper.exists(getUncheckedLocation(), ModelProvider.MODEL_WITH_EXTENSION);
            } else {
                return existingHelper.exists(getUncheckedLocation(), ModelProvider.MODEL);
            }
        }
    }
}
