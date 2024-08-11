package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;
import com.hbm.main.ResourceManager;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.render.RenderHelper;
import com.hbm.tileentity.machine.TileEntityMachineCrystallizer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class RenderCrystallizer extends TileEntitySpecialRenderer<TileEntityMachineCrystallizer> {

	private static final double RENDER_DISTANCE = 8.0;

    @Override
    public boolean isGlobalRenderer(TileEntityMachineCrystallizer te) {
        return true;
    }

    @Override
    public void render(TileEntityMachineCrystallizer crys, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.disableCull();

        // 根据方块的元数据进行旋转
        switch (crys.getBlockMetadata() - 10) {
            case 2:
                GlStateManager.rotate(90, 0F, 1F, 0F);
                break;
            case 4:
                GlStateManager.rotate(180, 0F, 1F, 0F);
                break;
            case 3:
                GlStateManager.rotate(270, 0F, 1F, 0F);
                break;
            case 5:
                GlStateManager.rotate(0, 0F, 1F, 0F);
                break;
        }

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.crystallizer_tex);
        ResourceManager.crystallizer.renderPart("Body");

        // 渲染额外部分（Spinner 和 Windows）
		if (shouldRender(crys.getPos())){
        	renderExtras(crys, partialTicks);
			renderFill(crys);
		}

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableCull();

        GlStateManager.popMatrix();
    }
	private boolean shouldRender(BlockPos pos) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        double distance = player.getDistanceSq(pos.getX(), pos.getY(), pos.getZ());
        return distance <= RENDER_DISTANCE * RENDER_DISTANCE;
    }
    private void renderExtras(TileEntityMachineCrystallizer crys, float partialTicks) {
        // 渲染旋转部分（Spinner）
        GlStateManager.pushMatrix();
        float rotation = crys.prevAngle + (crys.angle - crys.prevAngle) * partialTicks;
        GlStateManager.rotate(rotation, 0, 1, 0);
        bindTexture(ResourceManager.crystallizer_spinner_tex);
        ResourceManager.crystallizer.renderPart("Spinner");
        GlStateManager.popMatrix();

        // 渲染 Windows 部分
        bindTexture(ResourceManager.crystallizer_window_tex);
        ResourceManager.crystallizer.renderPart("Windows");
    }

    public void renderFill(TileEntityMachineCrystallizer crys) {
        if (crys.tank.getFluid() == null) return;

        GlStateManager.pushMatrix();
        GlStateManager.enableCull();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);

        // 设置流体颜色
        RenderHelper.setColor(ModForgeFluids.getFluidColor(crys.tank.getFluid().getFluid()));
        ResourceManager.crystallizer.renderPart("Windows");

        GlStateManager.color(1F, 1F, 1F, 1F);  // 重置颜色
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.disableCull();
        GlStateManager.popMatrix();
    }
}
