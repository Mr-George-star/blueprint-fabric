package net.george.blueprint.common.world.storage.tracking;

/**
 * A enum representing types of syncing.
 * <p> {@link #NOPE} for no syncing. </p>
 * <p> {@link #TO_CLIENT} for syncing to the client player. </p>
 * <p> {@link #TO_CLIENTS} for syncing to all client players. </p>
 *
 * @author Mr.George
 */
@SuppressWarnings("unused")
public enum SyncType {
    NOPE,
    TO_CLIENT,
    TO_CLIENTS
}
