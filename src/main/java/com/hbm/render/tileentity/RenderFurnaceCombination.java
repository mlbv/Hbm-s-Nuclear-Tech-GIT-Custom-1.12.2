package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityFurnaceCombination;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

/**
 * RenderFurnaceCombination 类用于渲染组合熔炉的 TileEntity 和物品。
 * 它继承自 TileEntitySpecialRenderer 并实现了 IItemRendererProvider 接口，
 * 提供了渲染 TileEntity 和物品的具体逻辑。
 */
public class RenderFurnaceCombination extends TileEntitySpecialRenderer<TileEntityFurnaceCombination> {

    // 定义用于渲染的火焰纹理
    public static final ResourceLocation texture = new ResourceLocation(RefStrings.MODID + ":textures/particle/rbmk_fire.png");

    /**
     * 渲染组合熔炉的 TileEntity。
     * @param tileEntity 当前渲染的 TileEntity
     * @param x X 坐标
     * @param y Y 坐标
     * @param z Z 坐标
     * @param partialTicks 渲染时传入的部分刻度时间
     * @param destroyStage 渲染销毁阶段
     * @param alpha 透明度
     */
    @Override
    public void render(TileEntityFurnaceCombination tileEntity, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.enableCull();

        // 绑定组合熔炉的纹理并渲染
        bindTexture(ResourceManager.combination_oven_tex);
        ResourceManager.combination_oven.renderAll();

        TileEntityFurnaceCombination furnace = (TileEntityFurnaceCombination) tileEntity;

        // 如果熔炉正在运行，则渲染火焰效果
        if (furnace.wasOn) {
            bindTexture(texture);  // 绑定火焰纹理

            GlStateManager.pushMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);  // 设置颜色为白色
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.alphaFunc(GL11.GL_GEQUAL, 0);
            GlStateManager.depthMask(false);  // 禁用深度掩码
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);  // 设置混合模式
            RenderHelper.disableStandardItemLighting();  // 禁用标准物品光照

            // 根据时间计算火焰纹理的索引
            int texIndex = (int) (furnace.getWorld().getTotalWorldTime() / 2 % 14);
            float f0 = 1F / 14F;

            // 计算纹理的 UV 坐标
            float uMin = texIndex % 5 * f0;
            float uMax = uMin + f0;
            float vMin = 0;
            float vMax = 1;

            // 设置火焰的水平和垂直缩放
            double scaleH = 1;
            double scaleV = 3;

            // 绘制火焰四边形
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buffer = tess.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.pos(-scaleH, 0, 0).tex(uMax, vMax).color(255, 255, 255, 255).endVertex();
            buffer.pos(-scaleH, scaleV, 0).tex(uMax, vMin).color(255, 255, 255, 255).endVertex();
            buffer.pos(scaleH, scaleV, 0).tex(uMin, vMin).color(255, 255, 255, 255).endVertex();
            buffer.pos(scaleH, 0, 0).tex(uMin, vMax).color(255, 255, 255, 255).endVertex();
            tess.draw();

            // 恢复 OpenGL 状态
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.depthFunc(GL11.GL_LEQUAL);
            GlStateManager.depthMask(true);  // 启用深度掩码
            GlStateManager.popMatrix();
            RenderHelper.enableStandardItemLighting();  // 启用标准物品光照
        }

        GlStateManager.popMatrix();  // 恢复 OpenGL 状态
    }

    @Override
    public boolean isGlobalRenderer(TileEntityFurnaceCombination te) {
        return true;
    }
}
