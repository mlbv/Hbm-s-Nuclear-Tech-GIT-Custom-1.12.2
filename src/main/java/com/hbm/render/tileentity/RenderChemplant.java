package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.render.util.HmfController;
import com.hbm.tileentity.machine.TileEntityMachineChemplant;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidRegistry;

public class RenderChemplant extends TileEntitySpecialRenderer<TileEntityMachineChemplant> {

    private static final double RENDER_DISTANCE = 8.0;

    @Override
    public void render(TileEntityMachineChemplant te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.rotate(180, 0F, 1F, 0F);

        switch(te.getBlockMetadata()) {
            case 2:
                GlStateManager.rotate(180, 0F, 1F, 0F);
                GlStateManager.translate(0.5D, 0.0D, -0.5D);
                break;
            case 4:
                GlStateManager.rotate(270, 0F, 1F, 0F);
                GlStateManager.translate(0.5D, 0.0D, -0.5D);
                break;
            case 3:
                GlStateManager.translate(0.5D, 0.0D, -0.5D);
                break;
            case 5:
                GlStateManager.rotate(90, 0F, 1F, 0F);
                GlStateManager.translate(0.5D, 0.0D, -0.5D);
                break;
        }

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.chemplant_body_tex);
        ResourceManager.chemplant_body.renderAll();
        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.popMatrix();
        if(shouldRender(te.getPos().up())){
            renderExtras(te, x, y, z, partialTicks);
        }
    }

    private boolean shouldRender(BlockPos pos) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        double distance = player.getDistanceSq(pos.getX(), pos.getY(), pos.getZ());
        return distance <= RENDER_DISTANCE * RENDER_DISTANCE;
    }
    
    public void renderExtras(TileEntity tileEntity, double x, double y, double z, float f) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(180, 0F, 1F, 0F);

        TileEntityMachineChemplant chem = (TileEntityMachineChemplant) tileEntity;

        switch (chem.getBlockMetadata()) {
            case 2:
                GlStateManager.translate(-1, 0, 0);
                GlStateManager.rotate(180, 0F, 1F, 0F);
                break;
            case 4:
                GlStateManager.rotate(270, 0F, 1F, 0F);
                break;
            case 3:
                GlStateManager.translate(0, 0, -1);
                GlStateManager.rotate(0, 0F, 1F, 0F);
                break;
            case 5:
                GlStateManager.translate(-1, 0, -1);
                GlStateManager.rotate(90, 0F, 1F, 0F);
                break;
        }

        bindTexture(ResourceManager.chemplant_spinner_tex);

        int rotation = (int) (System.currentTimeMillis() % (360 * 5)) / 5;

        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.625, 0, 0.625);

        if (chem.tanks[0].getFluid() != null && chem.isProgressing)
            GlStateManager.rotate(-rotation, 0F, 1F, 0F);
        else
            GlStateManager.rotate(-45, 0F, 1F, 0F);

        ResourceManager.chemplant_spinner.renderAll();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.625, 0, 0.625);

        if (chem.tanks[1].getFluid() != null && chem.isProgressing)
            GlStateManager.rotate(rotation, 0F, 1F, 0F);
        else
            GlStateManager.rotate(45, 0F, 1F, 0F);

        ResourceManager.chemplant_spinner.renderAll();
        GlStateManager.popMatrix();

        double push = Math.sin((System.currentTimeMillis() % 2000) / 1000D * Math.PI) * 0.25 - 0.25;

        bindTexture(ResourceManager.chemplant_piston_tex);

        GlStateManager.pushMatrix();

        if (chem.isProgressing)
            GlStateManager.translate(0, push, 0);
        else
            GlStateManager.translate(0, -0.25, 0);

        ResourceManager.chemplant_piston.renderAll();
        GlStateManager.popMatrix();

        bindTexture(ResourceManager.chemplant_fluid_tex);

        GlStateManager.disableLighting();
        if (chem.tanks[0].getFluid() != null) {
            ResourceLocation test;
            if (chem.tanks[0].getFluid().getFluid() == FluidRegistry.LAVA || chem.tanks[0].getFluid().getFluid() == FluidRegistry.WATER) {
                test = new ResourceLocation(RefStrings.MODID, "textures/blocks/forgefluid/" + chem.tanks[0].getFluid().getFluid().getUnlocalizedName().substring(11) + "_chemplant.png");
            } else {
                String s = chem.tanks[0].getFluid().getFluid().getStill().toString();
                String textureBase = "textures/";
                String[] test1 = s.split(":");
                String location = test1[0] + ":" + textureBase + test1[1] + ".png";
                test = new ResourceLocation(location);
            }
            bindTexture(test);
            GlStateManager.pushMatrix();

            if (chem.isProgressing)
                HmfController.setMod(50000D, -250D);
            else
                HmfController.setMod(50000D, -50000D);

            GlStateManager.translate(-0.625, 0, 0.625);

            int count = chem.tanks[0].getFluidAmount() * 16 / 24000;
            for (int i = 0; i < count; i++) {

                if (i < count - 1)
                    ResourceManager.chemplant_fluid.renderAll();
                else
                    ResourceManager.chemplant_fluidcap.renderAll();
                GlStateManager.translate(0, 0.125, 0);
            }
            GlStateManager.popMatrix();
        }

        if (chem.tanks[1].getFluid() != null) {
            ResourceLocation test;
            if (chem.tanks[1].getFluid().getFluid() == FluidRegistry.LAVA || chem.tanks[1].getFluid().getFluid() == FluidRegistry.WATER) {
                test = new ResourceLocation(RefStrings.MODID, "textures/blocks/forgefluid/" + chem.tanks[1].getFluid().getFluid().getUnlocalizedName().substring(11) + "_chemplant.png");
            } else {
                String s = chem.tanks[1].getFluid().getFluid().getStill().toString();
                String textureBase = "textures/";
                String[] test1 = s.split(":");
                String location = test1[0] + ":" + textureBase + test1[1] + ".png";
                test = new ResourceLocation(location);
            }
            bindTexture(test);
            GlStateManager.pushMatrix();

            if (chem.isProgressing)
                HmfController.setMod(50000D, 250D);
            else
                HmfController.setMod(50000D, 50000D);

            GlStateManager.translate(0.625, 0, 0.625);

            int count = chem.tanks[1].getFluidAmount() * 16 / 24000;
            for (int i = 0; i < count; i++) {

                if (i < count - 1)
                    ResourceManager.chemplant_fluid.renderAll();
                else
                    ResourceManager.chemplant_fluidcap.renderAll();
                GlStateManager.translate(0, 0.125, 0);
            }
            GlStateManager.popMatrix();
        }
        GlStateManager.enableLighting();

        HmfController.resetMod();

        GlStateManager.popMatrix();
    }
}
