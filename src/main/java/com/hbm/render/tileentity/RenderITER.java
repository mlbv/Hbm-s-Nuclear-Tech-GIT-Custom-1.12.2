package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityITER;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.Fluid;

public class RenderITER extends TileEntitySpecialRenderer<TileEntityITER> {

	private static final double RENDER_DISTANCE = 32.0;

    @Override
    public boolean isGlobalRenderer(TileEntityITER te) {
        return true;
    }

    @Override
    public void render(TileEntityITER iter, double x, double y, double z, float partialTicks, int destroyStage, float alpha2) {
        GlStateManager.pushMatrix();

        GlStateManager.translate((float)x + 0.5F, (float)y - 2, (float)z + 0.5F);
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        // 渲染 ITER 的各个部分
        renderIterParts(iter);

		if (shouldRender(iter.getPos().up())) {
        // 渲染旋转的 Solenoid 部件
        	renderSolenoid(iter, partialTicks);

        // 渲染 Plasma（等离子体）
        	if(iter.plasma.getFluidAmount() > 0) {
        	    renderPlasma(iter, partialTicks);
        	}
		}
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();
    }

	private boolean shouldRender(BlockPos pos) {
		EntityPlayer player = Minecraft.getMinecraft().player;
		double distance = player.getDistanceSq(pos.getX(), pos.getY(), pos.getZ());
		return distance <= RENDER_DISTANCE * RENDER_DISTANCE;
	}

    private void renderIterParts(TileEntityITER iter) {
        bindTexture(ResourceManager.iter_glass);
        ResourceManager.iter.renderPart("Windows");

        bindTexture(ResourceManager.iter_motor);
        ResourceManager.iter.renderPart("Motors");

        bindTexture(ResourceManager.iter_rails);
        ResourceManager.iter.renderPart("Rails");

        bindTexture(ResourceManager.iter_toroidal);
        ResourceManager.iter.renderPart("Toroidal");

        // 根据 blanket 类型渲染 Torus
        switch(iter.blanket) {
            case 0: bindTexture(ResourceManager.iter_torus); break;
            case 1: bindTexture(ResourceManager.iter_torus_tungsten); break;
            case 2: bindTexture(ResourceManager.iter_torus_desh); break;
            case 3: bindTexture(ResourceManager.iter_torus_chlorophyte); break;
            case 4: bindTexture(ResourceManager.iter_torus_vaporwave); break;
            default: bindTexture(ResourceManager.iter_torus); break;
        }
        ResourceManager.iter.renderPart("Torus");
    }

    private void renderSolenoid(TileEntityITER iter, float partialTicks) {
        GlStateManager.pushMatrix();
        float rotation = iter.lastRotor + (iter.rotor - iter.lastRotor) * partialTicks;
        GlStateManager.rotate(rotation, 0, 1, 0);

        bindTexture(ResourceManager.iter_solenoid);
        ResourceManager.iter.renderPart("Solenoid");

        GlStateManager.popMatrix();
    }

    private void renderPlasma(TileEntityITER iter, float partialTicks) {
        GlStateManager.pushMatrix();
        float rotation = iter.lastRotor + (iter.rotor - iter.lastRotor) * partialTicks;
        GlStateManager.rotate(rotation, 0, 1, 0);

        GlStateManager.disableLighting();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
        GlStateManager.depthMask(false);

        int color = getColor(iter.plasma.getFluid().getFluid());
        double alpha = (double)iter.plasma.getFluidAmount() / (double)iter.plasma.getCapacity();

        int r = (int)(((color & 0xFF0000) >> 16) / 2F * alpha);
        int g = (int)(((color & 0xFF00) >> 8) / 2F * alpha);
        int b = (int)((color & 0xFF) / 2F * alpha);

        GlStateManager.color(r / 255F, g / 255F, b / 255F);
        GlStateManager.translate(0, 2.5F, 0);
        GlStateManager.scale(1, alpha, 1);
        GlStateManager.translate(0, -2.5F, 0);

        bindTexture(ResourceManager.iter_plasma);
        ResourceManager.iter.renderPart("Plasma");

        GlStateManager.enableLighting();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);

        GlStateManager.popMatrix();
    }

    private int getColor(Fluid type) {
        if(type == ModForgeFluids.plasma_dt) {
            return 0xFF3FC2;
        } else if(type == ModForgeFluids.plasma_hd) {
            return 0xEB3FFF;
        } else if(type == ModForgeFluids.plasma_ht) {
            return 0x9F3FFF;
        } else if(type == ModForgeFluids.plasma_put) {
            return 0x3F99FF;
        } else if(type == ModForgeFluids.plasma_xm) {
            return 0x3FFFFF;
        } else if(type == ModForgeFluids.plasma_bf) {
            return 0xB8FF3F;
        }
        return 0;
    }
}
