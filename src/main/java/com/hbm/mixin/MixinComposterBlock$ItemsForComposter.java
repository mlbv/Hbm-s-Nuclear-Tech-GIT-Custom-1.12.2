package com.hbm.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.ItemStack;
import thedarkcolour.futuremc.block.villagepillage.ComposterBlock;
import com.hbm.compat.futuremc.CustomCompostable;

@Mixin(value = ComposterBlock.ItemsForComposter.class, remap = false)
public class MixinComposterBlock$ItemsForComposter {

    @Inject(method = "getChance", at = @At("HEAD"), cancellable = true)
    private static void injectGetChance(ItemStack stack, CallbackInfoReturnable<Byte> cir) {
        if(stack.isEmpty()){
            cir.setReturnValue((byte) -1);
            cir.cancel();
        }
        if (CustomCompostable.getChance(stack) != -1) {
            cir.setReturnValue((byte) CustomCompostable.getChance(stack));
            cir.cancel();
        }
    }
}
