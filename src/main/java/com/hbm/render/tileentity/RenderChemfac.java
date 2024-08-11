package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityMachineChemfac;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class RenderChemfac extends TileEntitySpecialRenderer<TileEntityMachineChemfac> {

	private static final double RENDER_DISTANCE = 16.0;

    @Override
    public boolean isGlobalRenderer(TileEntityMachineChemfac te) {
        return true;
    }

    @Override
    public void render(TileEntityMachineChemfac chemfac, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.disableCull();

        // 根据方块元数据进行旋转
        switch (chemfac.getBlockMetadata() - BlockDummyable.offset) {
            case 5:
                GlStateManager.rotate(180, 0F, 1F, 0F);
                break;
            case 2:
                GlStateManager.rotate(270, 0F, 1F, 0F);
                break;
            case 4:
                GlStateManager.rotate(0, 0F, 1F, 0F);
                break;
            case 3:
                GlStateManager.rotate(90, 0F, 1F, 0F);
                break;
        }

        GlStateManager.translate(0.5D, 0.0D, -0.5D);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.chemfac_tex);
        ResourceManager.chemfac.renderPart("Main");

        float rot = chemfac.prevRot + (chemfac.rot - chemfac.prevRot) * partialTicks;

		if(shouldRender(chemfac.getPos())){
			// 渲染风扇1
			renderFan(rot, 1, 0, 0, "Fan1");

			// 渲染风扇2
			renderFan(rot, -1, 0, 0, "Fan2");
		}
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();
    }

	private boolean shouldRender(BlockPos pos) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        double distance = player.getDistanceSq(pos.getX(), pos.getY(), pos.getZ());
        return distance <= RENDER_DISTANCE * RENDER_DISTANCE;
    }

    private void renderFan(float rotation, double x, double y, double z, String partName) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(rotation, 0, -1, 0);
        GlStateManager.translate(-x, y, -z);
        ResourceManager.chemfac.renderPart(partName);
        GlStateManager.popMatrix();
    }
}
