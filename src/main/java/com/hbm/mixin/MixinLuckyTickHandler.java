package com.hbm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import mod.lucky.world.LuckyTickHandler;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mixin(value = LuckyTickHandler.class, remap = false)
public class MixinLuckyTickHandler {

	@Inject(method = "onClientTick", at = @At("HEAD"), cancellable = true)
	private void InjectonClientTick(TickEvent.ClientTickEvent event, CallbackInfo ci) {
        ci.cancel();
	}
}
