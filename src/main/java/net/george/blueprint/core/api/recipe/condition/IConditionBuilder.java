package net.george.blueprint.core.api.recipe.condition;

import net.minecraft.util.Identifier;

@SuppressWarnings("unused")
public interface IConditionBuilder {
    default ICondition andCondition(ICondition... conditions) {
        return new AndCondition(conditions);
    }

    default ICondition orCondition(ICondition... conditions) {
        return new OrCondition(conditions);
    }

    default ICondition falseCondition() {
        return new FalseCondition();
    }

    default ICondition trueCondition() {
        return new TrueCondition();
    }

    default ICondition itemExistsCondition(String location) {
        return new ItemExistsCondition(location);
    }

    default ICondition itemExistsCondition(String nameplace, String path) {
        return new ItemExistsCondition(nameplace, path);
    }

    default ICondition itemExistsCondition(Identifier item) {
        return new ItemExistsCondition(item);
    }

    default ICondition modLoadedCondition(String modid) {
        return new ModLoadedCondition(modid);
    }

    default ICondition tagEmptyCondition(String location) {
        return new TagEmptyCondition(location);
    }

    default ICondition tagEmptyCondition(String nameplace, String path) {
        return new TagEmptyCondition(nameplace, path);
    }

    default ICondition tagEmptyCondition(Identifier tag) {
        return new TagEmptyCondition(tag);
    }

    default ICondition notCondition(ICondition child) {
        return new NotCondition(child);
    }
}
