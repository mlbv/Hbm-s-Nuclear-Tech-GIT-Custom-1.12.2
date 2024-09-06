package com.hbm.mixin;

import mekanism.common.tile.TileEntityFluidTank;
import mekanism.common.tile.prefab.TileEntityContainerBlock;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.hbm.forgefluid.FFUtils;
import com.hbm.lib.NonNullListItemHandlerWrapper;

@Mixin(value = TileEntityFluidTank.class, remap = false)
public abstract class MixinTileEntityFluidTank extends TileEntityContainerBlock {

    public MixinTileEntityFluidTank(String name) {
        super(name);
    }

    @Shadow
    public FluidTank fluidTank;

    @Inject(method = "manageInventory", at = @At("HEAD"), cancellable = true)
    private void injectSpecialFill(CallbackInfo ci) {
        if (handleSpecialContainer()) {
            //System.out.println("Called handleSpecialContainer!");
            ci.cancel();
        }
    }

    private boolean handleSpecialContainer() {
        // 检查 inventory 是否为空
        if (inventory == null || fluidTank == null) {
            return false;
        }

        // 将 NonNullList<ItemStack> inventory 转换为 IItemHandlerModifiable
        IItemHandlerModifiable inventoryHandler = new NonNullListItemHandlerWrapper(inventory);
        if (inventoryHandler == null || inventoryHandler.getStackInSlot(0) == null || inventoryHandler.getStackInSlot(0).isEmpty()) {
            return false;
        }

        boolean processed = false;

        // 尝试从 fluidTank 填充到 item
        if (fluidTank.getFluid() != null) {
            processed = FFUtils.trySpecialFillFluidContainer(inventoryHandler, fluidTank, 0, 1);
        }

        // 如果未填充，则尝试从 item 填充到 fluidTank
        return processed || FFUtils.trySpecialFillFromFluidContainer(inventoryHandler, fluidTank, 0, 1);
    }
}
