package com.github.clevernucleus.opc.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.github.clevernucleus.opc.api.CacheableValue;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class PlayerCacheImpl {
	private static final Map<UUID, Map<CacheableValue<?>, Object>> CACHE = new HashMap<UUID, Map<CacheableValue<?>, Object>>();
	private static final BiMap<String, UUID> NAME_TO_ID = HashBiMap.create();
	private static final Map<Identifier, CacheableValue<?>> TYPES = new HashMap<Identifier, CacheableValue<?>>();
	
	@SuppressWarnings("unchecked")
	private static <T> T get(final UUID uuid, final CacheableValue<T> key) {
		var cache = CACHE.get(uuid);
		return (T)(cache != null ? cache.get(key) : (Object)null);
	}
	
	private static <T> T nonNullGet(final ServerPlayerEntity player, final CacheableValue<T> key) {
		if(key == null) return (T)null;
		return key.get(player);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> CacheableValue<T> register(final CacheableValue<T> key) {
		return (CacheableValue<T>)TYPES.computeIfAbsent(new Identifier(key.toString()), id -> key);
	}
	
	public static <T> T get(final MinecraftServer server, final UUID uuid, final CacheableValue<T> key) {
		ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
		
		if(player == null) return get(uuid, key);
		return nonNullGet(player, key);
	}
	
	public static <T> T get(final MinecraftServer server, final String name, final CacheableValue<T> key) {
		ServerPlayerEntity player = server.getPlayerManager().getPlayer(name);
		UUID uuid = NAME_TO_ID.get(name);
		
		if(player == null) return get(uuid, key);
		return nonNullGet(player, key);
	}
	
	public static void cache(final ServerPlayerEntity playerIn) {
		UUID uuid = playerIn.getGameProfile().getId();
		String name = playerIn.getGameProfile().getName();
		Map<CacheableValue<?>, Object> map = new HashMap<CacheableValue<?>, Object>();
		
		TYPES.forEach((id, key) -> map.put(key, key.get(playerIn)));
		CACHE.put(uuid, map);
		NAME_TO_ID.put(name, uuid);
	}
	
	public static <T> void uncache(final String name, final CacheableValue<T> key) {
		UUID uuid = NAME_TO_ID.get(name);
		
		uncache(uuid, key);
	}
	
	public static <T> void uncache(final UUID uuid, final CacheableValue<T> key) {
		var cache = CACHE.get(uuid);
		
		if(cache != null) {
			cache.remove(key);
		}
	}
	
	public static void uncache(final String name) {
		UUID uuid = NAME_TO_ID.get(name);
		
		uncache(uuid);
	}
	
	public static void uncache(final UUID uuid) {
		if(uuid != null) {
			CACHE.remove(uuid);
			NAME_TO_ID.inverse().remove(uuid);
		}
	}
	
	public static Collection<String> playerNames(final MinecraftServer server) {
		Set<String> names = new HashSet<String>();
		
		for(String name : NAME_TO_ID.keySet()) {
			names.add(name);
		}
		
		for(String name : server.getPlayerNames()) {
			names.add(name);
		}
		
		return names;
	}
	
	public static Collection<UUID> playerUUIDs(final MinecraftServer server) {
		Set<UUID> uuids = new HashSet<UUID>();
		
		for(UUID uuid : NAME_TO_ID.values()) {
			uuids.add(uuid);
		}
		
		Collection<UUID> online = server.getPlayerManager().getPlayerList().stream().map(player -> player.getGameProfile().getId()).toList();
		
		uuids.addAll(online);
		
		return uuids;
	}
	
	public static Collection<Identifier> keys() {
		return TYPES.keySet();
	}
	
	public static CacheableValue<?> getKey(final Identifier identifier) {
		return TYPES.getOrDefault(identifier, (CacheableValue<?>)null);
	}
	
	public static boolean isKeyValid(final Identifier identifier) {
		return TYPES.containsKey(identifier);
	}
	
	public static boolean isPlayerCached(final UUID uuid) {
		return NAME_TO_ID.containsValue(uuid);
	}
	
	public static boolean isPlayerCached(final String name) {
		return NAME_TO_ID.containsKey(name);
	}
	
	public static void toNbt(NbtCompound tag) {
		NbtList list = new NbtList();
		Map<UUID, String> names = NAME_TO_ID.inverse();
		
		for(var player : CACHE.entrySet()) {
			NbtCompound data = new NbtCompound();
			NbtList entries = new NbtList();
			UUID uuid = player.getKey();
			data.putUuid("uuid", uuid);
			data.putString("name", names.get(uuid));
			
			for(var cache : player.getValue().entrySet()) {
				NbtCompound entry = new NbtCompound();
				CacheableValue<?> key = cache.getKey();
				Object value = cache.getValue();
				entry.putString("key", key.toString());
				key.writeToNbt(entry, value);
				entries.add(entry);
			}
			
			data.put("data", entries);
			list.add(data);
		}
		
		tag.put("Cache", list);
	}
	
	public static void fromNbt(NbtCompound tag) {
		CACHE.clear();
		NAME_TO_ID.clear();
		
		NbtList list = tag.getList("Cache", 10);
		
		for(int i = 0; i < list.size(); i++) {
			NbtCompound data = list.getCompound(i);
			NbtList entries = data.getList("data", 10);
			UUID uuid = data.getUuid("uuid");
			String name = data.getString("name");
			Map<CacheableValue<?>, Object> cache = new HashMap<CacheableValue<?>, Object>();
			
			for(int j = 0; j < entries.size(); j++) {
				NbtCompound entry = entries.getCompound(j);
				CacheableValue<?> key = getKey(new Identifier(entry.getString("key")));
				
				if(key == null) continue;
				
				Object value = key.readFromNbt(entry);
				cache.put(key, value);
			}
			
			CACHE.put(uuid, cache);
			NAME_TO_ID.put(name, uuid);
		}
	}
}
