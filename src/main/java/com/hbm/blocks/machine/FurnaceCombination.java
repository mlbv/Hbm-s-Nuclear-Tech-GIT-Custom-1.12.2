package com.hbm.blocks.machine;

import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityFurnaceCombination;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * FurnaceCombination 类是一个自定义的方块类，继承自 BlockDummyable，并实现了 ITooltipProvider 接口。
 * 这个类主要用于表示一个多方块结构的组合熔炉，具有特殊的功能和交互逻辑。
 */
public class FurnaceCombination extends BlockDummyable implements ITooltipProvider {

    /**
     * 构造函数，用于初始化 FurnaceCombination 实例，设置材质为岩石（rock）。
     */
    public FurnaceCombination(Material mat, String s) {
        super(Material.ROCK, "furnace_combination");
    }

    /**
     * 创建新的 TileEntity 实例，用于处理方块的逻辑。
     * @param world 当前世界
     * @param meta 方块的元数据，用于确定创建哪种类型的 TileEntity
     * @return 返回对应的 TileEntity 实例
     */
    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        if (meta >= 12) {  // 如果元数据大于等于 12，则创建组合熔炉 TileEntity
            return new TileEntityFurnaceCombination();
        }
        return new TileEntityProxyCombo(true, true, true);  // 否则创建代理 TileEntity
    }
    
    /**
     * 处理方块的右键点击事件，通常用于打开 GUI 界面。
     * @param world 当前世界
     * @param pos 方块的位置
     * @param state 方块的状态
     * @param player 执行点击操作的玩家
     * @param hand 玩家使用的手
     * @param facing 点击的方块面
     * @param hitX 点击位置的 X 坐标
     * @param hitY 点击位置的 Y 坐标
     * @param hitZ 点击位置的 Z 坐标
     * @return 返回是否成功激活方块
     */
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        return this.standardOpenBehavior(world, pos.getX(), pos.getY(), pos.getZ(), player, 0);  // 调用标准的打开行为
    }

    /**
     * 获取方块的结构尺寸，主要用于确定多方块结构的范围。
     * @return 返回表示方块结构尺寸的数组
     */
    @Override
    public int[] getDimensions() {
        return new int[]{1, 0, 1, 1, 1, 1};  // 返回方块的尺寸信息
    }

    /**
     * 获取方块的偏移量，用于在多方块结构中进行位置调整。
     * @return 返回偏移量
     */
    @Override
    public int getOffset() {
        return 1;  // 返回方块的偏移量
    }

    /**
     * 添加方块的提示信息，显示在物品描述中。
     * @param stack 方块对应的物品堆
     * @param world 当前世界
     * @param list 用于显示信息的列表
     * @param flag 显示信息的标志
     */
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag) {
        this.addStandardInfo(list);  // 添加标准的提示信息
    }
}
