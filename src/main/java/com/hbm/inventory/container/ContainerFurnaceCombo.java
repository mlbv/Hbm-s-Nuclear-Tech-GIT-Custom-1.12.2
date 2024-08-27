package com.hbm.inventory.container;

import com.hbm.inventory.SlotMachineOutput;

import com.hbm.tileentity.machine.TileEntityFurnaceCombination;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerFurnaceCombo extends Container {

    protected TileEntityFurnaceCombination furnace;

    public ContainerFurnaceCombo(InventoryPlayer invPlayer, TileEntityFurnaceCombination furnace) {
        this.furnace = furnace;

        // Input slot
        this.addSlotToContainer(new SlotItemHandler(furnace.inventory, 0, 26, 36));
        // Output slot
        this.addSlotToContainer(new SlotMachineOutput(furnace.inventory, 1, 89, 36));
        // Fuel slot
        this.addSlotToContainer(new SlotItemHandler(furnace.inventory, 2, 136, 18));
        // Crafting output slot
        this.addSlotToContainer(new SlotMachineOutput(furnace.inventory, 3, 136, 54));

        // Player inventory slots
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 104 + i * 18));
            }
        }

        // Player hotbar slots
        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 162));
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack originalStack = slot.getStack();
            stack = originalStack.copy();

            if (index < 4) { // From furnace to player inventory
                if (!this.mergeItemStack(originalStack, 4, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onSlotChange(originalStack, stack);
            } else { // From player inventory to furnace
                if (!this.mergeItemStack(originalStack, 0, 4, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (originalStack.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return stack;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.furnace.isUseableByPlayer(player);
    }
}
