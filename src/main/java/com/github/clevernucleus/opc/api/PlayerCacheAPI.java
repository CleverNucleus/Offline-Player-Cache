package com.github.clevernucleus.opc.api;

import java.util.Collection;
import java.util.UUID;

import net.minecraft.server.MinecraftServer;

/**
 * Primary API access.
 * <br></br>
 * When a player disconnects from the server, all registered CacheableValue's are cached from the player and stored on the server.
 * This way information about the disconnected player can still be accessed when they are offline. This is a read-only mechanic. 
 * When the same player reconnects to the server, their cache is removed (uncached) and any calls to the cache returns that player's 
 * realtime current value.
 * <br></br>
 * When player's are offline, their cache can be removed using {@link #uncachePlayer(String)} and {@link #uncachePlayer(UUID)}. This 
 * deletes the player's entire cache. Note that this does nothing when that player is connected to the server (online) as players are 
 * only cached when they are disconnected (offline).
 * <br></br>
 * Specific cached data can be removed instead of the entire player's cache using {@link #uncacheValue(String, CacheableValue)} and 
 * {@link #uncacheValue(UUID, CacheableValue)}. This removes only that player's CacheableValue cached value.
 * <br></br>
 * Note that this is all completely server-side and is not synced to the client.
 * 
 * @author CleverNucleus
 *
 */
public final class PlayerCacheAPI {
	/** Offline Player Cache mod id. */
	public static final String MODID = "opc";
	
	/**
	 * Registers a cacheable value to the server: these are keys that instruct the server to cache some data from players when they disconnect.
	 * @param <T> Value type to cache (String, Integer, Object etc).
	 * @param key A new CacheableValue implementation.
	 * @return A statically registered CacheableValue.
	 */
	public static <T> CacheableValue<T> registerCacheableValue(final CacheableValue<T> key) {
		return com.github.clevernucleus.opc.impl.PlayerCacheImpl.register(key);
	}
	
	/**
	 * Gets a cacheable value from a player with the input UUID.
	 * @param <T> Return value type.
	 * @param server MinecraftServer instance
	 * @param uuid UUID of player
	 * @param key CacheableValue
	 * @return If the player who's input uuid is online, returns the player's current value according to the input CacheableValue;
	 * if the player is not online, but has joined the server before (i.e. they have been cached) returns the cached value.
	 * Otherwise returns a null Object.
	 */
	public static <T> T get(final MinecraftServer server, final UUID uuid, final CacheableValue<T> key) {
		return com.github.clevernucleus.opc.impl.PlayerCacheImpl.get(server, uuid, key);
	}
	
	/**
	 * Gets a cacheable value from a player with the input name.
	 * @param <T> Return value type.
	 * @param server MinecraftServer instance
	 * @param name Name of player
	 * @param key CacheableValue
	 * @return If the player who's input name is online, returns the player's current value according to the input CacheableValue;
	 * if the player is not online, but has joined the server before (i.e. they have been cached) returns the cached value.
	 * Otherwise returns a null Object.
	 */
	public static <T> T get(final MinecraftServer server, final String name, final CacheableValue<T> key) {
		return com.github.clevernucleus.opc.impl.PlayerCacheImpl.get(server, name, key); 
	}
	
	/**
	 * Removes the input value cached to the input player, if it exists. Does nothing if the input uuid player is currently on the server,
	 * or has not been cached before, or if the cacheable value key does not exist in the cache.
	 * @param <T>
	 * @param uuid
	 * @param key
	 */
	public static <T> void uncacheValue(final UUID uuid, final CacheableValue<T> key) {
		com.github.clevernucleus.opc.impl.PlayerCacheImpl.uncache(uuid, key);
	}
	
	/**
	 * Removes the input value cached to the input player, if it exists. Does nothing if the input name player is currently on the server,
	 * or has not been cached before, or if the cacheable value key does not exist in the cache.
	 * @param <T>
	 * @param name
	 * @param key
	 */
	public static <T> void uncacheValue(final String name, final CacheableValue<T> key) {
		com.github.clevernucleus.opc.impl.PlayerCacheImpl.uncache(name, key);
	}
	
	/**
	 * Removes the player who's uuid this belongs to from the cache, if they exist.
	 * @param uuid
	 */
	public static void uncachePlayer(final UUID uuid) {
		com.github.clevernucleus.opc.impl.PlayerCacheImpl.uncache(uuid);
	}
	
	/**
	 * Removes the player who's name this belongs to from the cache, if they exist.
	 * @param name
	 */
	public static void uncachePlayer(final String name) {
		com.github.clevernucleus.opc.impl.PlayerCacheImpl.uncache(name);
	}
	
	/**
	 * Gets all online and offline (cached) player names.
	 * @param server
	 * @return A new HashSet of online and offline (cached) player names.
	 */
	public static Collection<String> getPlayerNames(final MinecraftServer server) {
		return com.github.clevernucleus.opc.impl.PlayerCacheImpl.playerNames(server);
	}
	
	/**
	 * Gets all online and offline (cached) player uuids.
	 * @param server
	 * @return A new HashSet of online and offline (cached) player uuids.
	 */
	public static Collection<UUID> getPlayerIds(final MinecraftServer server) {
		return com.github.clevernucleus.opc.impl.PlayerCacheImpl.playerUUIDs(server);
	}
}
