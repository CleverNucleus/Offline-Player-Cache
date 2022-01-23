package com.github.clevernucleus.opc.impl;

import java.util.UUID;
import java.util.function.Function;

import com.github.clevernucleus.opc.api.CacheableValue;
import com.github.clevernucleus.opc.api.PlayerCacheAPI;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public final class OfflinePlayerCacheCommands {
	private static final SuggestionProvider<ServerCommandSource> SUGGEST_KEYS = (context, builder) -> CommandSource.suggestIdentifiers(PlayerCacheImpl.keys(), builder);
	private static final SuggestionProvider<ServerCommandSource> SUGGEST_NAMES = (ctx, builder) -> {
		for(String name : PlayerCacheImpl.playerNames(ctx.getSource().getServer())) {
        	builder.suggest(name);
        }
		
		return builder.buildFuture();
	};
	private static final SuggestionProvider<ServerCommandSource> SUGGEST_UUIDS = (ctx, builder) -> {
		for(UUID uuid : PlayerCacheImpl.playerUUIDs(ctx.getSource().getServer())) {
        	builder.suggest(String.valueOf(uuid));
        }
		
		return builder.buildFuture();
	};
	
	private static <T> ArgumentCommandNode<ServerCommandSource, Identifier> getKey(Function<CommandContext<ServerCommandSource>, T> input) {
		return CommandManager.argument("key", IdentifierArgumentType.identifier()).suggests(SUGGEST_KEYS).executes(ctx -> {
			T id = input.apply(ctx);
			Identifier identifier = IdentifierArgumentType.getIdentifier(ctx, "key");
			CacheableValue<?> cacheable = PlayerCacheImpl.getKey(identifier);
			MinecraftServer server = ctx.getSource().getServer();
			Object value = (id instanceof String ? PlayerCacheAPI.get(server, (String)id, cacheable) : (id instanceof UUID ? PlayerCacheAPI.get(server, (UUID)id, cacheable) : (Object)null));
			
			ctx.getSource().sendFeedback((new LiteralText(id + " -> " + identifier + " = " + value)).formatted(Formatting.GRAY), false);
			
			if(value instanceof Number) {
				int number = (int)(Integer)value;
				return Math.abs(number) % 16;
			}
			
			return 1;
		}).build();
	}
	
	private static <T> ArgumentCommandNode<ServerCommandSource, Identifier> removeKey(Function<CommandContext<ServerCommandSource>, T> input) {
		return CommandManager.argument("key", IdentifierArgumentType.identifier()).suggests(SUGGEST_KEYS).executes(ctx -> {
			T id = input.apply(ctx);
			Identifier identifier = IdentifierArgumentType.getIdentifier(ctx, "key");
			CacheableValue<?> cacheable = PlayerCacheImpl.getKey(identifier);
			
			if(id instanceof String) {
				PlayerCacheAPI.uncacheValue((String)id, cacheable);
			} else if(id instanceof UUID) {
				PlayerCacheAPI.uncacheValue((UUID)id, cacheable);
			}
			
			ctx.getSource().sendFeedback((new LiteralText("-" + id + " -" + identifier)).formatted(Formatting.GRAY), false);
			
			return 1;
		}).build();
	}
	
	private static void get(LiteralCommandNode<ServerCommandSource> root) {
		LiteralCommandNode<ServerCommandSource> get = CommandManager.literal("get").build();
		LiteralCommandNode<ServerCommandSource> id1 = CommandManager.literal("name").build();
		LiteralCommandNode<ServerCommandSource> id2 = CommandManager.literal("uuid").build();
		ArgumentCommandNode<ServerCommandSource, String> name = CommandManager.argument("name", StringArgumentType.string()).suggests(SUGGEST_NAMES).build();
		ArgumentCommandNode<ServerCommandSource, Identifier> key1 = getKey(ctx -> StringArgumentType.getString(ctx, "name"));
		ArgumentCommandNode<ServerCommandSource, UUID> uuid = CommandManager.argument("uuid", UuidArgumentType.uuid()).suggests(SUGGEST_UUIDS).build();
		ArgumentCommandNode<ServerCommandSource, Identifier> key2 = getKey(ctx -> UuidArgumentType.getUuid(ctx, "uuid"));
		
		root.addChild(get);
		get.addChild(id1);
		get.addChild(id2);
		id1.addChild(name);
		id2.addChild(uuid);
		name.addChild(key1);
		uuid.addChild(key2);
	}
	
	private static void remove(LiteralCommandNode<ServerCommandSource> root) {
		LiteralCommandNode<ServerCommandSource> remove = CommandManager.literal("remove").build();
		LiteralCommandNode<ServerCommandSource> id1 = CommandManager.literal("name").build();
		LiteralCommandNode<ServerCommandSource> id2 = CommandManager.literal("uuid").build();
		ArgumentCommandNode<ServerCommandSource, String> name = CommandManager.argument("name", StringArgumentType.string()).suggests(SUGGEST_NAMES).executes(ctx -> {
			String player = StringArgumentType.getString(ctx, "name");
			
			
			
			PlayerCacheAPI.uncachePlayer(player);
			
			ctx.getSource().sendFeedback((new LiteralText("-" + player + " -*")).formatted(Formatting.GRAY), false);
			
			return 1;
		}).build();
		ArgumentCommandNode<ServerCommandSource, Identifier> key1 = removeKey(ctx -> StringArgumentType.getString(ctx, "name"));
		ArgumentCommandNode<ServerCommandSource, UUID> uuid = CommandManager.argument("uuid", UuidArgumentType.uuid()).suggests(SUGGEST_UUIDS).executes(ctx -> {
			UUID player = UuidArgumentType.getUuid(ctx, "uuid");
			PlayerCacheAPI.uncachePlayer(player);
			
			ctx.getSource().sendFeedback((new LiteralText("-" + player + " -*")).formatted(Formatting.GRAY), false);
			
			return 1;
		}).build();
		ArgumentCommandNode<ServerCommandSource, Identifier> key2 = removeKey(ctx -> UuidArgumentType.getUuid(ctx, "uuid"));
		
		root.addChild(remove);
		remove.addChild(id1);
		remove.addChild(id2);
		id1.addChild(name);
		id2.addChild(uuid);
		name.addChild(key1);
		uuid.addChild(key2);
	}
	
	public static void registerCommands(final CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralCommandNode<ServerCommandSource> root = CommandManager.literal("opc").requires(source -> source.hasPermissionLevel(2)).build();
		dispatcher.getRoot().addChild(root);
		get(root);
		remove(root);
	}
}
