package com.github.clevernucleus.opc.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.clevernucleus.opc.impl.PlayerCacheImpl;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(PlayerManager.class)
abstract class PlayerManagerMixin {
	
	@Shadow
	@Final
	private List<ServerPlayerEntity> players;
	
	@Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0, shift = Shift.BEFORE))
	private void connect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
		PlayerCacheImpl.uncache(player.getGameProfile().getId());
	}
	
	@Inject(method = "remove", at = @At("HEAD"))
	private void disconnect(ServerPlayerEntity player, CallbackInfo info) {
		PlayerCacheImpl.cache(player);
	}
	
	@Inject(method = "disconnectAllPlayers", at = @At("HEAD"))
	private void kick(CallbackInfo info) {
		for(ServerPlayerEntity player : players) {
			PlayerCacheImpl.cache(player);
		}
	}
}
