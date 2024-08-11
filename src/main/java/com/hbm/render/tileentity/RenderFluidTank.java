package com.hbm.render.tileentity;

import org.lwjgl.opengl.GL11;

import java.io.IOException;

import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.forgefluid.FFUtils;
import com.hbm.lib.RefStrings;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityMachineFluidTank;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
//import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class RenderFluidTank extends TileEntitySpecialRenderer<TileEntityMachineFluidTank> {
    
    @Override
    public void render(TileEntityMachineFluidTank te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.disableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        // 根据元数据旋转 TileEntity
        rotateTileEntity(te.getBlockMetadata());

        // 渲染外框
        bindTexture(ResourceManager.tank_tex);
        ResourceManager.fluidtank.renderPart("Frame");

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.popMatrix();

        renderTank(te, x, y, z);
    }

    private void rotateTileEntity(int metadata) {
        switch (metadata) {
            case 2:
                GlStateManager.rotate(270, 0F, 1F, 0F);
                break;
            case 4:
                GlStateManager.rotate(0, 0F, 1F, 0F);
                break;
            case 3:
                GlStateManager.rotate(90, 0F, 1F, 0F);
                break;
            case 5:
                GlStateManager.rotate(180, 0F, 1F, 0F);
                break;
        }
    }

    public void renderTank(TileEntityMachineFluidTank te, double x, double y, double z) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);
        GlStateManager.enableLighting();
        GlStateManager.disableCull();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);

        // 根据元数据旋转 TileEntity
        rotateTileEntity(te.getBlockMetadata());

        Fluid type = null;
        String fluidName = "NONE";
        if (te.tank.getFluid() != null) {
            type = te.tank.getFluid().getFluid();
            fluidName = FluidRegistry.getFluidName(type).toUpperCase();
            if (fluidName.startsWith("HBM")) {
                fluidName = fluidName.substring(3);
            }
        }

        ResourceLocation texture = getFluidTexture(fluidName, type);

        bindTexture(texture);
        ResourceManager.fluidtank.renderPart("Tank");

        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.popMatrix();
    }

    private ResourceLocation getFluidTexture(String fluidName, Fluid type) {
        ResourceLocation texture = new ResourceLocation(RefStrings.MODID, "textures/models/tank/tank_" + fluidName + ".png");

        try {
            Minecraft.getMinecraft().getResourceManager().getResource(texture);
        } catch (IOException e) {
            texture = new ResourceLocation(RefStrings.MODID, "textures/models/tank/tank_generic.png");
            if (type != null) {
                FFUtils.setRGBFromHex(ModForgeFluids.getFluidColor(type));
            }
        }

        return texture;
    }
}
