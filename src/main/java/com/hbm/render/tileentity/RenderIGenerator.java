package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;
import com.hbm.blocks.BlockDummyable;
import com.hbm.main.ResourceManager;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.render.misc.BeamPronter;
import com.hbm.render.misc.BeamPronter.EnumBeamType;
import com.hbm.render.misc.BeamPronter.EnumWaveType;
import com.hbm.tileentity.machine.TileEntityMachineIGenerator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class RenderIGenerator extends TileEntitySpecialRenderer<TileEntityMachineIGenerator> {

    private static final double RENDER_DISTANCE = 16.0;

    @Override
    public boolean isGlobalRenderer(TileEntityMachineIGenerator te) {
        return true;
    }

    @Override
    public void render(TileEntityMachineIGenerator te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);

        // 旋转模型，使其方向正确
        switch (te.getBlockMetadata() - BlockDummyable.offset) {
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

        GlStateManager.enableLighting();
        GlStateManager.disableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        // 渲染主机体
        bindTexture(ResourceManager.igen_tex);
        ResourceManager.igen.renderPart("Base");

        float angle = te.prevRotation + (te.rotation - te.prevRotation) * partialTicks;
        float px = 0.0625F;
        float sine = (float) Math.sin(Math.toRadians(angle));
        float cosine = (float) Math.cos(Math.toRadians(angle));
        float armAng = 22.5F;
        if(shouldRender(te.getPos().up())){
            // 渲染旋转部件
            renderRotor(angle);
            renderCog(angle, px, "CogLeft", true);
            renderCog(angle, px, "CogRight", false);

            // 渲染活塞
            renderPistons(cosine);

            // 渲染手臂
            renderArm(angle, sine, cosine, px, -armAng, "ArmLeft");
            renderArm(angle, -sine, cosine, px, armAng, "ArmRight");

            // 渲染光束
            renderBeams(te, px);
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
   
    private void renderRotor(float angle) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 3.5, 0);
        GlStateManager.rotate(angle, 0, 0, 1);
        GlStateManager.translate(0, -3.5, 0);

        bindTexture(ResourceManager.igen_rotor);
        ResourceManager.igen.renderPart("Rotor");
        GlStateManager.popMatrix();
    }

    private void renderCog(float angle, float px, String partName, boolean isLeft) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 3.5, px * 5);
        GlStateManager.rotate(angle, isLeft ? -1 : 1, 0, 0);
        GlStateManager.translate(0, -3.5, px * -5);

        bindTexture(ResourceManager.igen_cog);
        ResourceManager.igen.renderPart(partName);
        GlStateManager.popMatrix();
    }

    private void renderPistons(float cosine) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 0, cosine * 0.8725 - 1);

        bindTexture(ResourceManager.igen_pistons);
        ResourceManager.igen.renderPart("Pistons");
        GlStateManager.popMatrix();
    }

    private void renderArm(float angle, float sine, float cosine, float px, float armAng, String partName) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, sine * 0.55, cosine * 0.8725 - 1.125);
        GlStateManager.translate(0, 3.5, px * 6.5);
        GlStateManager.rotate(sine * armAng, 1, 0, 0);
        GlStateManager.translate(0, -3.5, px * -5);

        bindTexture(ResourceManager.igen_arm);
        ResourceManager.igen.renderPart(partName);
        GlStateManager.popMatrix();
    }

    private void renderBeams(TileEntityMachineIGenerator te, float px) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.75, 5.5625, -7);

        if (te.torque > 0) {
            for (int i = 0; i < 2; i++) {
                BeamPronter.prontBeam(Vec3.createVectorHelper(1.5, 0, 0), EnumWaveType.RANDOM, EnumBeamType.LINE, 0x8080ff, 0x0000ff, (int) te.getWorld().getTotalWorldTime() % 1000 + i, 5, px * 4, 0, 0);
            }
        }

        GlStateManager.popMatrix();
    }
}
