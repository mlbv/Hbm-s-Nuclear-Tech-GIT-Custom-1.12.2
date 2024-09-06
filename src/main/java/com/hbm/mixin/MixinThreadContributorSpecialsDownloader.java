package com.hbm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import blusunrize.immersiveengineering.ImmersiveEngineering.ThreadContributorSpecialsDownloader;
import blusunrize.immersiveengineering.common.util.IELogger;

@Mixin(value = ThreadContributorSpecialsDownloader.class, remap = false)
public class MixinThreadContributorSpecialsDownloader {

    @Inject(method = "run", at = @At("HEAD"), cancellable = true)
    private void InjectRun(CallbackInfo ci) {
        IELogger.info("Contributor+special revolver list loading is currently disabled.");
        ci.cancel();
    }
}
