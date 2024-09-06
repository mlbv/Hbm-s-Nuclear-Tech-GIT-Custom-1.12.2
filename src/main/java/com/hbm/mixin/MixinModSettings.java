package com.hbm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import xaero.common.settings.ModSettings;

@Mixin(value = ModSettings.class, remap = false)
public class MixinModSettings {

    @Shadow
    public boolean allowInternetAccess;

    @Inject(method = "readSetting", at = @At("TAIL"))
    private void InjectreadSetting(String[] args, CallbackInfo ci){
        this.allowInternetAccess = false;
    }
}
