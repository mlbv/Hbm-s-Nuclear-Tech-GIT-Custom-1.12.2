package com.hbm.lib;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraft.util.NonNullList;

public class NonNullListItemHandlerWrapper implements IItemHandlerModifiable {

    private final NonNullList<ItemStack> itemList;
    private final int[] validSlots;

    // 构造函数：只使用 NonNullList
    public NonNullListItemHandlerWrapper(NonNullList<ItemStack> itemList) {
        this.itemList = itemList;
        this.validSlots = new int[] {}; // 默认没有有效槽限制
    }

    // 构造函数：带有 validSlots 参数
    public NonNullListItemHandlerWrapper(NonNullList<ItemStack> itemList, int[] validSlots) {
        this.itemList = itemList;
        this.validSlots = validSlots;
    }

    @Override
    public int getSlots() {
        return itemList.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot >= 0 && slot < itemList.size()) {
            return itemList.get(slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!isValidSlot(slot)) {
            return stack; // 如果槽位无效，则不插入物品
        }

        ItemStack currentStack = getStackInSlot(slot);

        if (currentStack.isEmpty()) {
            if (!simulate) {
                itemList.set(slot, stack.copy());
            }
            return ItemStack.EMPTY;
        }

        // 合并堆叠逻辑
        if (ItemHandlerHelper.canItemStacksStack(currentStack, stack)) {
            int limit = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));
            int newStackSize = Math.min(currentStack.getCount() + stack.getCount(), limit);

            if (!simulate) {
                currentStack.setCount(newStackSize);
                itemList.set(slot, currentStack);
            }

            return stack.copy().splitStack(stack.getCount() - (newStackSize - currentStack.getCount()));
        }

        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!isValidSlot(slot)) {
            return ItemStack.EMPTY; // 槽位无效则不提取物品
        }

        ItemStack currentStack = getStackInSlot(slot);

        if (currentStack.isEmpty() || amount <= 0) {
            return ItemStack.EMPTY;
        }

        // 计算可提取的数量
        int extractAmount = Math.min(currentStack.getCount(), amount);
        ItemStack extractedStack = currentStack.copy();
        extractedStack.setCount(extractAmount);

        if (!simulate) {
            currentStack.shrink(extractAmount);
            if (currentStack.getCount() == 0) {
                itemList.set(slot, ItemStack.EMPTY);
            }
        }

        return extractedStack;
    }

    @Override
    public int getSlotLimit(int slot) {
        if (slot >= 0 && slot < itemList.size()) {
            ItemStack stack = itemList.get(slot);
            if (!stack.isEmpty()) {
                return stack.getMaxStackSize(); // 返回该物品的最大堆叠数
            }
        }
        return 64; // 如果槽位为空，默认返回64（Minecraft中的通用堆叠限制）
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        if (slot >= 0 && slot < itemList.size()) {
            itemList.set(slot, stack);
        }
    }

    // 检查是否为有效槽
    private boolean isValidSlot(int slot) {
        if (validSlots.length == 0) {
            return true; // 如果没有限制，所有槽位都是有效的
        }

        for (int validSlot : validSlots) {
            if (validSlot == slot) {
                return true;
            }
        }
        return false;
    }
}
