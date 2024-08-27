package com.hbm.inventory;

import static com.hbm.inventory.OreDictManager.*;
//import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
//import com.hbm.inventory.RecipesCommon.NbtComparableStack;
//import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.forgefluid.ModForgeFluids;
import net.minecraftforge.fluids.FluidStack;
//import net.minecraftforge.oredict.OreDictionary;

import com.hbm.items.ModItems;
import com.hbm.util.Tuple.Pair;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.LinkedHashMap;


/**
 * CombinationRecipes 类负责注册和管理物品组合的配方。
 * 它使用了一个静态的 LinkedHashMap 来存储输入与输出的映射关系，
 */
public class CombinationRecipes{
    private static final LinkedHashMap<Object, Pair<ItemStack, FluidStack>> comboRecipes = new LinkedHashMap<>();

    public static final void registerRecipes(){
        comboRecipes.put(COAL.gem(), new Pair<>(new ItemStack(ModItems.coke, 1), new FluidStack(ModForgeFluids.COALCREOSOTE, 100)));
        comboRecipes.put(COAL.dust(), new Pair<>(new ItemStack(ModItems.coke, 1), new FluidStack(ModForgeFluids.COALCREOSOTE, 100)));
        
        comboRecipes.put(LIGNITE.gem(), new Pair<>(new ItemStack(ModItems.coke, 1), new FluidStack(ModForgeFluids.COALCREOSOTE, 50)));
        comboRecipes.put(LIGNITE.dust(), new Pair<>(new ItemStack(ModItems.coke, 1), new FluidStack(ModForgeFluids.COALCREOSOTE, 50)));
        comboRecipes.put(new ComparableStack(ModItems.briquette_lignite), new Pair<>(new ItemStack(ModItems.coke), new FluidStack(ModForgeFluids.COALCREOSOTE, 100)));

        comboRecipes.put(CINNABAR.crystal(), new Pair<>(new ItemStack(ModItems.sulfur), new FluidStack(ModForgeFluids.mercury, 100)));
        comboRecipes.put(KEY_LOG, new Pair<>(new ItemStack(ModItems.coke), new FluidStack(ModForgeFluids.COALCREOSOTE, 250)));
        //comboRecipes.put(new OreDictStack(KEY_LOG), new Pair<>(new ItemStack(ModItems.coke), new FluidStack(ModForgeFluids.COALCREOSOTE, 250)));
        comboRecipes.put(new ComparableStack(ModItems.oil_tar), new Pair<>(new ItemStack(ModItems.coke), null));
        comboRecipes.put(new ComparableStack(Items.REEDS), new Pair<>(new ItemStack(Items.SUGAR, 2), new FluidStack(ModForgeFluids.ethanol, 50)));
        comboRecipes.put(new ComparableStack(Blocks.CLAY), new Pair<>(new ItemStack(Blocks.BRICK_BLOCK, 1), null));
        
        //基岩矿
        /*
        for(Entry<Integer, String> entry : BedrockOreRegistry.oreIndexes.entrySet()) {
			int oreMeta = entry.getKey();
			String oreName = entry.getValue();
 			comboRecipes.put(new ComparableStack(ModItems.ore_bedrock, 1, oreMeta), new ItemStack[] { 
				new ItemStack(ModItems.ore_bedrock_centrifuged, 1, oreMeta), 
				new ItemStack(ModItems.ore_bedrock_centrifuged, 1, oreMeta), 
				new ItemStack(Blocks.GRAVEL, 1),
				new ItemStack(Blocks.GRAVEL, 1) });
            comboRecipes.put(new ComparableStack(ModItems.ore_bedrock_cleaned, 1, oreMeta), new ItemStack[] { 
				new ItemStack(ModItems.ore_bedrock_separated, 1, oreMeta), 
				new ItemStack(ModItems.ore_bedrock_separated, 1, oreMeta), 
				new ItemStack(Blocks.GRAVEL, 1),
				new ItemStack(Blocks.GRAVEL, 1) });
            comboRecipes.put(new ComparableStack(ModItems.ore_bedrock_deepcleaned, 1, oreMeta), new ItemStack[] { 
				new ItemStack(ModItems.ore_bedrock_purified, 1, oreMeta), 
				new ItemStack(ModItems.ore_bedrock_purified, 1, oreMeta), 
				new ItemStack(Blocks.GRAVEL, 1),
				new ItemStack(Blocks.GRAVEL, 1) });
            comboRecipes.put(new ComparableStack(ModItems.ore_bedrock_nitrated, 1, oreMeta), new ItemStack[] { 
				new ItemStack(ModItems.ore_bedrock_nitrocrystalline, 1, oreMeta), 
				new ItemStack(ModItems.ore_bedrock_nitrocrystalline, 1, oreMeta), 
				getNugget(oreName), 
				new ItemStack(Blocks.GRAVEL, 1) });
            comboRecipes.put(new ComparableStack(ModItems.ore_bedrock_seared, 1, oreMeta), new ItemStack[] { 
				new ItemStack(ModItems.ore_bedrock_exquisite, 1, oreMeta), 
				new ItemStack(ModItems.ore_bedrock_exquisite, 1, oreMeta), 
				getNugget(oreName),
				new ItemStack(Blocks.GRAVEL, 1) });
            comboRecipes.put(new ComparableStack(ModItems.ore_bedrock_perfect, 1, oreMeta), new ItemStack[] { 
				new ItemStack(ModItems.ore_bedrock_enriched, 1, oreMeta), 
				new ItemStack(ModItems.ore_bedrock_enriched, 1, oreMeta), 
				new ItemStack(Blocks.GRAVEL, 1),
				new ItemStack(Blocks.GRAVEL, 1) });
            comboRecipes.put(new ComparableStack(ModItems.ore_bedrock_enriched, 1, oreMeta), new ItemStack[] { 
				ItemBedrockOre.getOut(oreMeta, 1), 
				ItemBedrockOre.getOut(oreMeta, 1), 
				ItemBedrockOre.getOut(oreMeta, 1), 
				ItemBedrockOre.getOut(oreMeta, 1) });
		}
        */
    }

    /**
     * 获取指定输入物品的输出结果（包括物品和流体）。
     * @param stack 输入的物品堆
     * @return 包含输出物品和流体的 Pair 对象，如果没有匹配的配方，则返回 null
     */
    public static Pair<ItemStack, FluidStack> getOutput(ItemStack stack) {

        if (stack == null || stack.isEmpty()){
            //System.out.println("getOutput(): Stack is empty!");
            return null;
        }
        ComparableStack comp = new ComparableStack(stack.getItem(), 1, stack.getItemDamage());
        if(comboRecipes.containsKey(comp))
			return comboRecipes.get(comp);
        //System.out.println("getOutput(): No perfect match! Checking ore dictionary...");
        String[] dictKeys = comp.getDictKeys();
        for(String key : dictKeys) {
			if(comboRecipes.containsKey(key))
				return comboRecipes.get(key);
		}
        return null;
    }
}