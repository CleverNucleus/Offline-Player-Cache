package com.github.clevernucleus.opc.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.clevernucleus.opc.impl.OfflinePlayerCacheCommands;
import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

@Mixin(CommandManager.class)
abstract class CommandManagerMixin {
	
	@Shadow
	@Final
	private CommandDispatcher<ServerCommandSource> dispatcher;
	
	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;findAmbiguities(Lcom/mojang/brigadier/AmbiguityConsumer;)V"))
	private void fabric_addCommands(CommandManager.RegistrationEnvironment environment, CallbackInfo ci) {
		OfflinePlayerCacheCommands.registerCommands(this.dispatcher);
	}
}
