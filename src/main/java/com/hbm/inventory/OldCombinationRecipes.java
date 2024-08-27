package com.hbm.inventory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
//import com.hbm.blocks.BlockEnums.EnumStoneType;
//import com.hbm.blocks.ModBlocks;

import static com.hbm.inventory.OreDictManager.*;
//import com.hbm.inventory.OreDictManager.DictFrame;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.RecipesCommon.OreDictStack;
import com.hbm.forgefluid.ModForgeFluids;
import net.minecraft.item.Item;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
//import com.hbm.items.ItemEnums.EnumCokeType;
//import com.hbm.items.ItemEnums.EnumTarType;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFluidIcon;
//import com.hbm.items.special.ItemBedrockOre;
import com.hbm.main.MainRegistry;
//import com.hbm.items.special.ItemBedrockOre.BedrockOreGrade;
//import com.hbm.items.special.ItemBedrockOre.BedrockOreType;
import com.hbm.util.Tuple.Pair;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
//import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
//import net.minecraftforge.oredict.OreDictionary;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

//import javax.print.attribute.standard.MediaSize.NA;

/**
 * CombinationRecipes 类负责注册和管理物品组合的配方。
 * 它使用了一个静态的 HashMap 来存储输入与输出的映射关系，
 * 并且继承了 SerializableRecipe 类，以便于配方的序列化与反序列化。
 */
@Deprecated
public class OldCombinationRecipes{

    // 存储所有配方的静态 HashMap，键为输入物品或对应的字典键，值为物品和流体的 Pair 对象。
    private static HashMap<Object, Pair<ItemStack, FluidStack>> recipes = new HashMap<>();

    /**
     * 注册默认的配方，这些配方定义了输入物品与对应输出物品及流体的映射关系。
     */
    //@Override
    public void registerDefaults() {
        recipes.put(COAL.gem(), new Pair<>(new ItemStack(ModItems.coke), new FluidStack(ModForgeFluids.COALCREOSOTE, 100)));
        recipes.put(COAL.dust(), new Pair<>(new ItemStack(ModItems.coke), new FluidStack(ModForgeFluids.COALCREOSOTE, 100)));
        //recipes.put(new ComparableStack(DictFrame.fromOne(ModItems.briquette, EnumBriquetteType.COAL)), new Pair<>(DictFrame.fromOne(ModItems.coke, EnumCokeType.COAL), new FluidStack(Fluids.COALCREOSOTE, 150)));

        recipes.put(LIGNITE.gem(), new Pair<>(new ItemStack(ModItems.coke), new FluidStack(ModForgeFluids.COALCREOSOTE, 50)));
        recipes.put(LIGNITE.dust(), new Pair<>(new ItemStack(ModItems.coke), new FluidStack(ModForgeFluids.COALCREOSOTE, 50)));
        recipes.put(new ComparableStack(ModItems.briquette_lignite), new Pair<>(new ItemStack(ModItems.coke), new FluidStack(ModForgeFluids.COALCREOSOTE, 100)));

        //recipes.put(CHLOROCALCITE.dust(), new Pair<>(new ItemStack(ModItems.powder_calcium), new FluidStack(Fluids.CHLORINE, 250)));
        //recipes.put(MOLYSITE.dust(), new Pair<>(new ItemStack(Items.IRON_INGOT), new FluidStack(Fluids.CHLORINE, 250)));
        recipes.put(CINNABAR.crystal(), new Pair<>(new ItemStack(ModItems.sulfur), new FluidStack(ModForgeFluids.mercury, 100)));
        //recipes.put(new ComparableStack(Items.GLOWSTONE_DUST), new Pair<>(new ItemStack(ModItems.sulfur), new FluidStack(Fluids.CHLORINE, 100)));
        //recipes.put(SODALITE.gem(), new Pair<>(new ItemStack(ModItems.powder_sodium), new FluidStack(Fluids.CHLORINE, 100)));
        //recipes.put(new ComparableStack(DictFrame.fromOne(ModBlocks.stone_resource, EnumStoneType.BAUXITE)), new Pair<>(new ItemStack(ModItems.ingot_aluminium, 2), new FluidStack(Fluids.REDMUD, 250)));
        //recipes.put(NA.dust(), new Pair<>(null, new FluidStack(Fluids.SODIUM, 100)));
        //recipes.put(LIMESTONE.dust(), new Pair<>(new ItemStack(ModItems.powder_calcium), new FluidStack(Fluids.CARBONDIOXIDE, 50)));

        recipes.put(KEY_LOG, new Pair<>(new ItemStack(ModItems.coke), new FluidStack(ModForgeFluids.COALCREOSOTE, 250)));
        //recipes.put(KEY_SAPLING, new Pair<>(DictFrame.fromOne(ModItems.powder_ash, EnumAshType.WOOD), new FluidStack(Fluids.WOODOIL, 50)));
        //recipes.put(new ComparableStack(DictFrame.fromOne(ModItems.briquette, EnumBriquetteType.WOOD)), new Pair<>(new ItemStack(Items.COAL, 1, 1), new FluidStack(Fluids.WOODOIL, 500)));

        recipes.put(new ComparableStack(ModItems.oil_tar), new Pair<>(new ItemStack(ModItems.coke), null));
        //recipes.put(new ComparableStack(DictFrame.fromOne(ModItems.oil_tar, EnumTarType.CRACK)), new Pair<>(DictFrame.fromOne(ModItems.coke, EnumCokeType.PETROLEUM), null));
        //recipes.put(new ComparableStack(DictFrame.fromOne(ModItems.oil_tar, EnumTarType.COAL)), new Pair<>(DictFrame.fromOne(ModItems.coke, EnumCokeType.COAL), null));
        //recipes.put(new ComparableStack(DictFrame.fromOne(ModItems.oil_tar, EnumTarType.WOOD)), new Pair<>(DictFrame.fromOne(ModItems.coke, EnumCokeType.COAL), null));

        recipes.put(new ComparableStack(Items.REEDS), new Pair<>(new ItemStack(Items.SUGAR, 2), new FluidStack(ModForgeFluids.ethanol, 50)));
        recipes.put(new ComparableStack(Blocks.CLAY), new Pair<>(new ItemStack(Blocks.BRICK_BLOCK, 1), null));
        /*
        //TODO: 注册基岩矿石的处理配方
        for (BedrockOreType type : BedrockOreType.values()) {
            recipes.put(new ComparableStack(ItemBedrockOreNew.make(BedrockOreGrade.BASE, type)), new Pair<>(ItemBedrockOreNew.make(BedrockOreGrade.BASE_ROASTED, type), new FluidStack(Fluids.VITRIOL, 50)));
            recipes.put(new ComparableStack(ItemBedrockOreNew.make(BedrockOreGrade.PRIMARY, type)), new Pair<>(ItemBedrockOreNew.make(BedrockOreGrade.PRIMARY_ROASTED, type), new FluidStack(Fluids.VITRIOL, 50)));
            recipes.put(new ComparableStack(ItemBedrockOreNew.make(BedrockOreGrade.SULFURIC_BYPRODUCT, type)), new Pair<>(ItemBedrockOreNew.make(BedrockOreGrade.SULFURIC_ROASTED, type), new FluidStack(Fluids.VITRIOL, 50)));
            recipes.put(new ComparableStack(ItemBedrockOreNew.make(BedrockOreGrade.SOLVENT_BYPRODUCT, type)), new Pair<>(ItemBedrockOreNew.make(BedrockOreGrade.SOLVENT_ROASTED, type), new FluidStack(Fluids.VITRIOL, 50)));
            recipes.put(new ComparableStack(ItemBedrockOreNew.make(BedrockOreGrade.RAD_BYPRODUCT, type)), new Pair<>(ItemBedrockOreNew.make(BedrockOreGrade.RAD_ROASTED, type), new FluidStack(Fluids.VITRIOL, 50)));
        }
        */
    }


    /**
     * 获取指定输入物品的输出结果（包括物品和流体）。
     * @param stack 输入的物品堆
     * @return 包含输出物品和流体的 Pair 对象，如果没有匹配的配方，则返回 null
     */
    public static Pair<ItemStack, FluidStack> getOutput(ItemStack stack) {

        if (stack == null || stack.getItem() == null)
            return null;

        ComparableStack comp = new ComparableStack(stack.getItem(), 1, stack.getItemDamage());

        // 首先检查是否存在完全匹配的配方
        if (recipes.containsKey(comp)) {
            Pair<ItemStack, FluidStack> out = recipes.get(comp);
            return new Pair<>(out.getKey() == null ? null : out.getKey().copy(), out.getValue());
        }

        // 如果没有完全匹配的配方，检查物品字典中的条目
        String[] dictKeys = comp.getDictKeys();

        for (String key : dictKeys) {

            if (recipes.containsKey(key)) {
                Pair<ItemStack, FluidStack> out = recipes.get(key);
                return new Pair<>(out.getKey() == null ? null : out.getKey().copy(), out.getValue());
            }
        }

        return null;
    }

    /**
     * 获取所有配方的哈希表。
     * 该方法将配方转换为一种更加易于读取的格式，便于外部调用。
     * @return 转换后的配方哈希表
     */
    public static HashMap<Object, Object[]> getRecipes() {

        HashMap<Object, Object[]> recipes = new HashMap<>();

        // 遍历所有的配方条目并转换它们
        for (Entry<Object, Pair<ItemStack, FluidStack>> entry : OldCombinationRecipes.recipes.entrySet()) {
            Object key = entry.getKey();
            Pair<ItemStack, FluidStack> val = entry.getValue();
            Object o = key instanceof String ? new OreDictStack((String) key) : key;

            if (val.getKey() != null && val.getValue() != null) {
                //recipes.put(o, new ItemStack[]{val.getKey(), ItemFluidIcon.make(val.getValue())});
                recipes.put(o, new ItemStack[]{val.getKey(), ItemFluidIcon.getStackWithQuantity(val.getValue())});
            } else if (val.getKey() != null) {
                recipes.put(o, new ItemStack[]{val.getKey()});
            } else if (val.getValue() != null) {
                //recipes.put(o, new ItemStack[]{ItemFluidIcon.make(val.getValue())});
                recipes.put(o, new ItemStack[]{ItemFluidIcon.getStackWithQuantity(val.getValue())});
            }
        }

        return recipes;
    }

    /**
     * 获取配方文件的文件名，通常用于配方的序列化与反序列化。
     * @return 配方文件名
     */
    //@Override
    public String getFileName() {
        return "hbmCombination.json";
    }

    /**
     * 获取所有配方对象，便于序列化。
     * @return 所有配方对象
     */
    //@Override
    public Object getRecipeObject() {
        return recipes;
    }

    /**
     * 从 JSON 元素读取配方并将其添加到配方表中。
     * @param recipe JSON 元素
     */
    //@Override
    public void readRecipe(JsonElement recipe) {
        JsonObject obj = (JsonObject) recipe;
        AStack in = readAStack(obj.get("input").getAsJsonArray());
        FluidStack fluid = null;
        ItemStack out = null;

        // 读取 JSON 中的流体和物品输出
        if (obj.has("fluid")) fluid = readFluidStack(obj.get("fluid").getAsJsonArray());
        if (obj.has("output")) out = readItemStack(obj.get("output").getAsJsonArray());

        // 根据输入物品的类型添加到配方表中
        if (in instanceof ComparableStack) {
            recipes.put(((ComparableStack) in).makeSingular(), new Pair<>(out, fluid));
        } else if (in instanceof OreDictStack) {
            recipes.put(((OreDictStack) in).name, new Pair<>(out, fluid));
        }
    }

    /**
     * 将配方写入到 JSON 文件中，用于配方的序列化。
     * @param recipe 要写入的配方对象
     * @param writer JSON 写入器
     * @throws IOException 当写入过程发生错误时抛出
     */

    //@Override
    public void writeRecipe(Object recipe, JsonWriter writer) throws IOException {
        Entry<Object, Pair<ItemStack, FluidStack>> rec = (Entry<Object, Pair<ItemStack, FluidStack>>) recipe;
        Object in = rec.getKey();
        Pair<ItemStack, FluidStack> pair = rec.getValue();
        ItemStack output = pair.getKey();
        FluidStack fluid = pair.getValue();

        writer.name("input");
        if (in instanceof String) {
            writeAStack(new OreDictStack((String) in), writer);
        } else if (in instanceof ComparableStack) {
            writeAStack((ComparableStack) in, writer);
        }
        if (output != null) {
            writer.name("output");
            writeItemStack(output, writer);
        }
        if (fluid != null) {
            writer.name("fluid");
            writeFluidStack(fluid, writer);
        }
    }

    public static AStack readAStack(JsonArray array) {
        try {
            String type = array.get(0).getAsString();
            int stacksize = array.size() > 2 ? array.get(2).getAsInt() : 1;
            
            // 处理 "item" 类型的堆栈
            if ("item".equals(type)) {
                // 在 1.12.2 中，我们使用 ResourceLocation 和 ForgeRegistries 来获取物品
                ResourceLocation itemResource = new ResourceLocation(array.get(1).getAsString());
                Item item = ForgeRegistries.ITEMS.getValue(itemResource);
                
                if (item == null) {
                    throw new IllegalArgumentException("Item not found: " + array.get(1).getAsString());
                }

                int meta = array.size() > 3 ? array.get(3).getAsInt() : 0;
                return new ComparableStack(item, stacksize, meta);
            }

            // 处理 "dict" 类型的堆栈（物品字典）
            if ("dict".equals(type)) {
                String dict = array.get(1).getAsString();
                return new OreDictStack(dict, stacksize);
            }

        } catch (Exception ex) {
            // 错误处理
            MainRegistry.logger.error("Error reading stack array: " + array.toString(), ex);
        }

        // 如果读取失败，返回一个默认的空堆栈
        return new ComparableStack(ModItems.nothing);
    }

    public static FluidStack readFluidStack(JsonArray array) {
        try {
            // 从 FluidRegistry 中获取流体
            Fluid fluid = FluidRegistry.getFluid(array.get(0).getAsString());
            int fill = array.get(1).getAsInt();

            // 如果 JSON 中包含压强字段，但在 1.12.2 中不需要，可以忽略
            // int pressure = array.size() < 3 ? 0 : array.get(2).getAsInt();

            if (fluid != null) {
                return new FluidStack(fluid, fill);
            }
        } catch (Exception ex) {
            MainRegistry.logger.error("Error reading fluid array: " + array.toString(), ex);
        }
        
        // 默认返回空流体堆
        return new FluidStack(FluidRegistry.WATER, 0);
    }

    public static ItemStack readItemStack(JsonArray array) {
        try {
            // 使用 ResourceLocation 和 ForgeRegistries 获取物品
            ResourceLocation itemResource = new ResourceLocation(array.get(0).getAsString());
            Item item = ForgeRegistries.ITEMS.getValue(itemResource);
    
            int stacksize = array.size() > 1 ? array.get(1).getAsInt() : 1;
            int meta = array.size() > 2 ? array.get(2).getAsInt() : 0;
    
            if (item != null) {
                return new ItemStack(item, stacksize, meta);
            }
        } catch (Exception ex) {
            MainRegistry.logger.error("Error reading stack array: " + array.toString() + " - defaulting to NOTHING item!", ex);
        }
    
        // 默认返回空物品堆
        return new ItemStack(ModItems.nothing);
    }

    public static void writeAStack(AStack astack, JsonWriter writer) throws IOException {
        writer.beginArray();
        writer.setIndent("");
    
        if (astack instanceof ComparableStack) {
            ComparableStack comp = (ComparableStack) astack;
            writer.value("item"); // ITEM identifier
    
            // 获取物品的 ResourceLocation 名称
            ResourceLocation itemName = ForgeRegistries.ITEMS.getKey(comp.toStack().getItem());
            writer.value(itemName != null ? itemName.toString() : "unknown_item");
    
            if (comp.stacksize != 1 || comp.meta > 0) writer.value(comp.stacksize); // stack size
            if (comp.meta > 0) writer.value(comp.meta); // metadata
        }
    
        if (astack instanceof OreDictStack) {
            OreDictStack ore = (OreDictStack) astack;
            writer.value("dict"); // DICT identifier
            writer.value(ore.name); // dict name
            if (ore.stacksize != 1) writer.value(ore.stacksize); // stacksize
        }
    
        writer.endArray();
        writer.setIndent("  ");
    }

    public static void writeItemStack(ItemStack stack, JsonWriter writer) throws IOException {
        writer.beginArray();
        writer.setIndent("");
    
        // 获取物品的 ResourceLocation 名称
        ResourceLocation itemName = ForgeRegistries.ITEMS.getKey(stack.getItem());
        writer.value(itemName != null ? itemName.toString() : "unknown_item");
    
        if (stack.getCount() != 1 || stack.getItemDamage() != 0) writer.value(stack.getCount()); // stack size
        if (stack.getItemDamage() != 0) writer.value(stack.getItemDamage()); // metadata
    
        writer.endArray();
        writer.setIndent("  ");
    }

    public static void writeFluidStack(FluidStack stack, JsonWriter writer) throws IOException {
        writer.beginArray();
        writer.setIndent("");
    
        // 获取流体的名称
        writer.value(stack.getFluid().getName()); // fluid type
        writer.value(stack.amount); // amount in mB
    
        // 如果 `pressure` 属性仍然存在并且非零，则写入
        //if (stack.tag != null && stack.tag.getInteger("pressure") != 0) {
        //    writer.value(stack.tag.getInteger("pressure"));
        //}
    
        writer.endArray();
        writer.setIndent("  ");
    }

    /**
     * 删除所有配方，通常在重载配方或清理时使用。
     */
    //@Override
    public void deleteRecipes() {
        recipes.clear();
    }
}
