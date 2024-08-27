package com.hbm.inventory.gui;

import org.lwjgl.opengl.GL11;

import com.hbm.inventory.container.ContainerFurnaceCombo;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityFurnaceCombination;
import com.hbm.forgefluid.FFUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

/**
 * GUIFurnaceCombo 类负责为组合熔炉提供用户界面。
 */
public class GUIFurnaceCombo extends GuiInfoContainer {

    // 定义 GUI 的背景纹理
    private static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/gui/processing/gui_furnace_combination.png");

    // 关联的 TileEntity，用于获取熔炉的状态信息
    private final TileEntityFurnaceCombination furnace;

    /**
     * 构造函数，用于初始化 GUI 实例并设置界面尺寸。
     * 
     * @param invPlayer 玩家物品栏
     * @param tedf      关联的组合熔炉 TileEntity
     */
    public GUIFurnaceCombo(InventoryPlayer invPlayer, TileEntityFurnaceCombination tedf) {
        super(new ContainerFurnaceCombo(invPlayer, tedf));
        this.furnace = tedf;

        this.xSize = 176; // 设置 GUI 的宽度
        this.ySize = 186; // 设置 GUI 的高度
    }

    /**
     * 绘制屏幕上的元素，包括自定义的信息和流体储罐的渲染。
     * 
     * @param mouseX      鼠标的 X 坐标
     * @param mouseY      鼠标的 Y 坐标
     * @param interp 插值参数
     */
    @Override
    public void drawScreen(int mouseX, int mouseY, float interp) {
        super.drawScreen(mouseX, mouseY, interp);

        // 渲染流体储罐的信息
        FFUtils.renderTankInfo(this, mouseX, mouseY, guiLeft + 118, guiTop + 18, 16, 52, furnace.tank, furnace.tank.getFluid() != null ? furnace.tank.getFluid().getFluid() : null);

        // 显示进度和热量的信息
        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 44, guiTop + 36, 39, 7, mouseX, mouseY,
                new String[] { String.format("%,d", furnace.progress) + " / " + String.format("%,d", TileEntityFurnaceCombination.processTime) + "TU" });
        this.drawCustomInfoStat(mouseX, mouseY, guiLeft + 44, guiTop + 45, 39, 7, mouseX, mouseY,
                new String[] { String.format("%,d", furnace.heat) + " / " + String.format("%,d", TileEntityFurnaceCombination.maxHeat) + "TU" });
        super.renderHoveredToolTip(mouseX, mouseY);
        
    }

    /**
     * 绘制 GUI 的前景层，包括标题和物品栏的文字。
     * 
     * @param mouseX 鼠标的 X 坐标
     * @param mouseY 鼠标的 Y 坐标
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String name = this.furnace.hasCustomInventoryName() ? this.furnace.getInventoryName() : I18n.format(this.furnace.getInventoryName());

        this.fontRenderer.drawString(name, this.xSize / 2 - this.fontRenderer.getStringWidth(name) / 2, 6, 4210752);
        this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 4210752);
    }

    /**
     * 绘制 GUI 的背景层，包括背景纹理、进度条和热量条的更新。
     * 
     * @param interp 插值参数
     * @param mouseX      鼠标的 X 坐标
     * @param mouseY      鼠标的 Y 坐标
     */
    @Override
    protected void drawGuiContainerBackgroundLayer(float interp, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F); // 设置颜色为白色
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture); // 绑定背景纹理
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize); // 绘制背景

        // 根据进度计算进度条的长度并绘制
        int p = furnace.progress * 38 / TileEntityFurnaceCombination.processTime;
        drawTexturedModalRect(guiLeft + 45, guiTop + 37, 176, 0, p, 5);

        // 根据热量计算热量条的长度并绘制
        int h = furnace.heat * 37 / TileEntityFurnaceCombination.maxHeat;
        drawTexturedModalRect(guiLeft + 45, guiTop + 46, 176, 5, h, 5);

        // 渲染流体储罐的内容
        FFUtils.drawLiquid(furnace.tank, guiLeft, guiTop, this.zLevel, 16, 52, 118, 70);
    }
}
