package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityMachineGasCent;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class RenderGasCent extends TileEntitySpecialRenderer<TileEntityMachineGasCent> {

    @Override
    public boolean isGlobalRenderer(TileEntityMachineGasCent te) {
        return true;
    }

    @Override
    public void render(TileEntityMachineGasCent te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        // 根据方块的元数据进行旋转
        switch (te.getBlockMetadata()) {
            case 4:
                GlStateManager.rotate(90, 0F, 1F, 0F);
                break;
            case 3:
                GlStateManager.rotate(180, 0F, 1F, 0F);
                break;
            case 5:
                GlStateManager.rotate(270, 0F, 1F, 0F);
                break;
            case 2:
                GlStateManager.rotate(0, 0F, 1F, 0F);
                break;
        }

        // 绑定纹理并渲染模型
        bindTexture(ResourceManager.centrifuge_gas_tex);
        ResourceManager.centrifuge_gas.renderAll();

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();
    }
}
