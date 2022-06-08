package com.github.clevernucleus.opc.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.github.clevernucleus.opc.impl.OfflinePlayerCacheData;
import com.github.clevernucleus.opc.impl.OfflinePlayerCacheImpl;

import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.UnmodifiableLevelProperties;

@Mixin(UnmodifiableLevelProperties.class)
abstract class UnmodifiableLevelPropertiesMixin implements OfflinePlayerCacheData {
	
	@Final
	@Shadow
	private ServerWorldProperties worldProperties;
	
	@Override
	public OfflinePlayerCacheImpl offlinePlayerCache() {
		return ((OfflinePlayerCacheData)this.worldProperties).offlinePlayerCache();
	}
}
