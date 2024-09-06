package com.hbm.compat.futuremc;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class CustomCompostable {
    private static final int COMMON = 30;
    // private static final int UNCOMMON = 50;
    // private static final int RARE = 65;
    // private static final int EPIC = 85;
    // private static final int LEGENDARY = 100;

    private static final Map<Item, Integer> COMPOSTABLES = new HashMap<>();
    /*
     * COMMON = 30 UNCOMMON = 50 RARE = 65 EPIC = 85 LEGENDARY = 100
     * COMMON == LEAVES MELON_SEEDS PUMPKIN_SEEDS WHEAT_SEEDS BEETROOT_SEEDS SAPLING
     * SWEET_BERRIES
     * UNCOMMON == MELON REEDS CACTUS VINE
     * RARE == APPLE DYE RED_FLOWER MELON_BLOCK
     * EPIC == BAKED_POTATO BREAD COOKIE HAY_BLOCK
     * LEGENDARY == CAKE PUMPKIN_PIE
     */
    static {
        Item immersiveSeed = Item.getByNameOrId("immersiveengineering:seed");
        if (immersiveSeed != null) {
            ItemStack immersiveSeedStack = new ItemStack(immersiveSeed, 1, 0);
            COMPOSTABLES.put(immersiveSeedStack.getItem(), COMMON);
        }
        // COMPOSTABLES.put(Items.DIAMOND, LEGENDARY);
    }

    private static boolean isOreDictMatch(ItemStack stack, String oreName) {
        int[] oreIDs = OreDictionary.getOreIDs(stack);
        for (int id : oreIDs) {
            if (OreDictionary.getOreName(id).equals(oreName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isOredictCOMMON(ItemStack stack) {
        return isOreDictMatch(stack, "listAllseed") || isOreDictMatch(stack, "treeSapling");
    }

    public static int getChance(ItemStack stack){
        if (stack == null || stack.isEmpty()) {
            return -1;
        }
        if (COMPOSTABLES.containsKey(stack.getItem())){
            return COMPOSTABLES.get(stack.getItem());
        }
        if(isOredictCOMMON(stack)){
            return COMMON;
        }
        return -1;
    }
}
