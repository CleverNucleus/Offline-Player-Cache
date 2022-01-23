package com.github.clevernucleus.opc.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.clevernucleus.opc.impl.PlayerCacheImpl;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.SaveVersionInfo;

@Mixin(LevelProperties.class)
abstract class LevelPropertiesMixin {
	
	@Inject(method = "updateProperties", at = @At("HEAD"))
	private void onUpdateProperties(DynamicRegistryManager registryManager, NbtCompound levelNbt, @Nullable NbtCompound playerNbt, CallbackInfo info) {
		NbtCompound nbtCompound = new NbtCompound();
		PlayerCacheImpl.toNbt(nbtCompound);
		levelNbt.put("PlayerCache", nbtCompound);
	}
	
	@Inject(method = "readProperties", at = @At("RETURN"))
    private static void readComponents(Dynamic<NbtElement> dynamic2, DataFixer dataFixer, int dataVersion, NbtCompound playerData, LevelInfo levelInfo, SaveVersionInfo saveVersionInfo, GeneratorOptions generatorOptions, Lifecycle lifecycle, CallbackInfoReturnable<LevelProperties> info) {
		dynamic2.get("PlayerCache").result().map(Dynamic::getValue).ifPresent(element -> PlayerCacheImpl.fromNbt((NbtCompound)element));
	}
}
