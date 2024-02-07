package net.george.blueprint.core.api.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;

@SuppressWarnings("unused")
public interface IConfigSpec<T extends IConfigSpec<T>> extends UnmodifiableConfig {
    @SuppressWarnings("unchecked")
    default T self() {
        return (T)this;
    }

    void acceptConfig(CommentedConfig config);

    boolean isCorrecting();

    boolean isCorrect(CommentedConfig config);

    int correct(CommentedConfig config);

    void afterReload();
}

