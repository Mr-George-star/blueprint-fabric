package net.george.blueprint.core.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

@ApiStatus.NonExtendable
public class DistExecutor {
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Run the callable in the supplier only on the specified {@link EnvType}.
     * This method is NOT sided-safe and special care needs to be taken in code using this method that implicit class
     * loading is not triggered by the {@link Callable}.
     * <p>
     * This method can cause unexpected {@link ClassNotFoundException} exceptions.
     * <p>
     * Use {@link #safeCallWhenOn(EnvType, Supplier)} where possible.
     *
     * @param dist The dist to run on
     * @param toRun A supplier of the callable to run (Supplier wrapper to ensure classloading only on the appropriate dist)
     * @param <T> The return type from the callable
     * @return The callable's result
     * @deprecated use {@link #safeCallWhenOn(EnvType, Supplier)} instead. This remains for advanced use cases.
     */
    @Deprecated
    public static <T> T callWhenOn(EnvType dist, Supplier<Callable<T>> toRun) {
        return unsafeCallWhenOn(dist, toRun);
    }

    public static <T> T unsafeCallWhenOn(EnvType dist, Supplier<Callable<T>> toRun) {
        if (dist == FabricLoader.getInstance().getEnvironmentType()) {
            try {
                return toRun.get().call();
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }
        return null;
    }

    /**
     * Call the {@link SafeCallable} when on the correct {@link EnvType}.
     * <p>
     * <strong>The lambda supplied here is required to be a method reference to a method defined in
     * another class, otherwise an invalid SafeReferent error will be thrown</strong>
     * @param dist the dist which this will run on
     * @param toRun the SafeCallable to run and return the result from
     * @param <T> The type of the {@link SafeCallable}
     * @return the result of the {@link SafeCallable} or null if on the wrong side
     */
    public static <T> T safeCallWhenOn(EnvType dist, Supplier<SafeCallable<T>> toRun) {
        validateSafeReferent(toRun);
        return callWhenOn(dist, toRun::get);
    }

    /**
     * Runs the supplied Runnable on the specified side. Same warnings apply as {@link #callWhenOn(EnvType, Supplier)}.
     * <p>
     * This method can cause unexpected {@link ClassNotFoundException} exceptions.
     *
     * @see #callWhenOn(EnvType, Supplier)
     * @param dist Dist to run this code on
     * @param toRun The code to run
     * @deprecated use {@link #safeRunWhenOn(EnvType, Supplier)} where possible. Advanced uses only.
     */
    @Deprecated
    public static void runWhenOn(EnvType dist, Supplier<Runnable> toRun) {
        unsafeRunWhenOn(dist, toRun);
    }
    /**
     * Runs the supplied Runnable on the specified side. Same warnings apply as {@link #unsafeCallWhenOn(EnvType, Supplier)}.
     * <p>
     * This method can cause unexpected {@link ClassNotFoundException} problems in common scenarios. Understand the pitfalls of
     * the way the class verifier works to load classes before using this.
     * <p>
     * Use {@link #safeRunWhenOn(EnvType, Supplier)} if you can.
     *
     * @see #unsafeCallWhenOn(EnvType, Supplier)
     * @param dist Dist to run this code on
     * @param toRun The code to run
     */
    public static void unsafeRunWhenOn(EnvType dist, Supplier<Runnable> toRun) {
        if (dist == FabricLoader.getInstance().getEnvironmentType()) {
            toRun.get().run();
        }
    }

    /**
     * Call the supplied SafeRunnable when on the correct Dist.
     * @param dist The dist to run on
     * @param toRun The code to run
     */
    public static void safeRunWhenOn(EnvType dist, Supplier<SafeRunnable> toRun) {
        validateSafeReferent(toRun);
        if (dist == FabricLoader.getInstance().getEnvironmentType())  {
            toRun.get().run();
        }
    }
    /**
     * Executes one of the two suppliers, based on which side is active.
     *
     * <p>
     *     Example (replacement for old SidedProxy):<br/>
     * {@code Proxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);}
     *
     * NOTE: the double supplier is required to avoid classloading the secondary target.
     *
     * @param clientTarget The supplier to run when on the {@link EnvType#CLIENT}
     * @param serverTarget The supplier to run when on the {@link EnvType#SERVER}
     * @param <T> The common type to return
     * @return The returned instance
     * @deprecated Use {@link #safeRunForDist(Supplier, Supplier)}
     */
    @Deprecated
    public static <T> T runForDist(Supplier<Supplier<T>> clientTarget, Supplier<Supplier<T>> serverTarget) {
        return unsafeRunForDist(clientTarget, serverTarget);
    }

    /**
     * Unsafe version of {@link #safeRunForDist(Supplier, Supplier)}. Use only when you know what you're doing
     * and understand why the verifier can cause unexpected {@link ClassNotFoundException} crashes even when code is apparently
     * not sided. Ensure you test both sides fully to be confident in using this.
     *
     * @param clientTarget The supplier to run when on the {@link EnvType#CLIENT}
     * @param serverTarget The supplier to run when on the {@link EnvType#SERVER}
     * @param <T> The common type to return
     * @return The returned instance
     */
    public static <T> T unsafeRunForDist(Supplier<Supplier<T>> clientTarget, Supplier<Supplier<T>> serverTarget) {
        return switch (FabricLoader.getInstance().getEnvironmentType()) {
            case CLIENT -> clientTarget.get().get();
            case SERVER -> serverTarget.get().get();
        };
    }
    /**
     * Executes one of the two suppliers, based on which side is active.
     *
     * <p>
     *     Example (replacement for old SidedProxy):<br/>
     * {@code Proxy proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);}
     *
     * NOTE: the double supplier is required to avoid classloading the secondary target.
     *
     * @param clientTarget The supplier to run when on the {@link EnvType#CLIENT}
     * @param serverTarget The supplier to run when on the {@link EnvType#SERVER}
     * @param <T> The common type to return
     * @return The returned instance
     */
    public static <T> T safeRunForDist(Supplier<SafeSupplier<T>> clientTarget, Supplier<SafeSupplier<T>> serverTarget) {
        validateSafeReferent(clientTarget);
        validateSafeReferent(serverTarget);
        return switch (FabricLoader.getInstance().getEnvironmentType()) {
            case CLIENT -> clientTarget.get().get();
            case SERVER -> serverTarget.get().get();
        };
    }

    /**
     * A safe referent. This will assert that it is being called via a separated class method reference. This will
     * avoid the common pitfalls of {@link #callWhenOn(EnvType, Supplier)} above.
     * <p>
     * SafeReferents assert that they are defined as a separate method outside the scope of the calling class.
     * <p>
     * <strong>Implementations need to be defined in a separate class to the referring site, with appropriate
     * visibility to be accessible at the call-site (generally, avoid private methods).</strong>
     *
     * <p>
     * Valid:<br/>
     *
     * {@code DistExecutor.safeCallWhenOn(EnvType.CLIENT, () -> AnotherClass::clientOnlyMethod);}
     *
     * <p>
     * Invalid:<br/>
     *
     * {@code DistExecutor.safeCallWhenOn(EnvType.CLIENT, () -> () -> MinecraftClient.getInstance().world);}
     */
    public interface SafeReferent {}

    /**
     * SafeCallable version of {@link SafeReferent}.
     * @see SafeReferent
     * @param <T> The return type of the Callable
     */
    public interface SafeCallable<T> extends SafeReferent, Callable<T>, Serializable {}

    /**
     * SafeSupplier version of {@link SafeReferent}
     * @param <T> The return type of the Supplier
     */
    public interface SafeSupplier<T> extends SafeReferent, Supplier<T>, Serializable {}

    /**
     * SafeRunnable version of {@link SafeReferent}
     */
    public interface SafeRunnable extends SafeReferent, Runnable, Serializable {}

    private static void validateSafeReferent(Supplier<? extends SafeReferent> safeReferentSupplier) {
        final SafeReferent setter;
        try {
            setter = safeReferentSupplier.get();
        } catch (Exception exception) {
            return;
        }

        for (Class<?> clazz = setter.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Method method = clazz.getDeclaredMethod("writeReplace");
                method.setAccessible(true);
                Object replacement = method.invoke(setter);
                if (!(replacement instanceof SerializedLambda lambda)) {
                    break;
                }
                if (Objects.equals(lambda.getCapturingClass(), lambda.getImplClass())) {
                    LOGGER.fatal("Detected unsafe referent usage, please view the code at {}",Thread.currentThread().getStackTrace()[3]);
                    throw new RuntimeException("Unsafe Referent usage found in safe referent method");
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
                break;
            }
        }
    }
}
