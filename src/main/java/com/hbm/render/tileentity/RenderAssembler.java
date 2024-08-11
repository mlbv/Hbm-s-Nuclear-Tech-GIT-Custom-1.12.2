package com.hbm.render.tileentity;

//import org.lwjgl.opengl.GL11;
import com.hbm.inventory.AssemblerRecipes;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityMachineAssembler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraft.entity.player.EntityPlayer;

public class RenderAssembler extends TileEntitySpecialRenderer<TileEntityMachineAssembler> {

    private static final double RENDER_DISTANCE = 8.0;

    @Override
    public boolean isGlobalRenderer(TileEntityMachineAssembler te) {
        return true;
    }

    @Override
    public void render(TileEntityMachineAssembler assembler, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);  // 修正平移操作，使得渲染相对方块坐标

        // 根据元数据旋转和平移
        switch (assembler.getBlockMetadata()) {
            case 2:
                GlStateManager.rotate(180, 0F, 1F, 0F);
                GlStateManager.translate(-0.5D, 0.0D, 0.5D);
                break;
            case 4:
                GlStateManager.rotate(270, 0F, 1F, 0F);
                GlStateManager.translate(-0.5D, 0.0D, 0.5D);
                break;
            case 3:
                GlStateManager.rotate(0, 0F, 1F, 0F);
                GlStateManager.translate(-0.5D, 0.0D, 0.5D);
                break;
            case 5:
                GlStateManager.rotate(90, 0F, 1F, 0F);
                GlStateManager.translate(-0.5D, 0.0D, 0.5D);
                break;
        }

        // 绑定纹理并渲染主体模型
        bindTexture(ResourceManager.assembler_body_tex);
        ResourceManager.assembler_body.renderAll();

        // 渲染物品模型
        if (assembler.recipe != -1 && shouldRender(assembler.getPos())) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(-1, 0.875, 0);  // 修正物品的渲染位置

            try {
                ItemStack stack = AssemblerRecipes.recipeList.get(assembler.recipe).toStack();
                GlStateManager.translate(1, 0, 1);
                if (!(stack.getItem() instanceof ItemBlock)) {
                    GlStateManager.rotate(-90, 1F, 0F, 0F);
                } else {
                    GlStateManager.scale(0.5, 0.5, 0.5);
                    GlStateManager.translate(0, -0.875, -2);
                }

                IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, assembler.getWorld(), null);
                model = ForgeHooksClient.handleCameraTransforms(model, TransformType.FIXED, false);
                Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                GlStateManager.translate(0.0F, 1.0F - 0.0625F * 165 / 100, 0.0F);
                Minecraft.getMinecraft().getRenderItem().renderItem(stack, model);
            } catch (Exception ex) {
                // Log or handle exception as needed
            }

            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
        if(shouldRender(assembler.getPos())){
            renderSlider(assembler, x, y, z, partialTicks);
        }
    }
    private boolean shouldRender(BlockPos pos) {
        EntityPlayer player = Minecraft.getMinecraft().player;
        double distance = player.getDistanceSq(pos.getX(), pos.getY(), pos.getZ());
        return distance <= RENDER_DISTANCE * RENDER_DISTANCE;
    }
    public void renderSlider(TileEntityMachineAssembler tileEntity, double x, double y, double z, float f) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(180, 0F, 1F, 0F);

        switch (tileEntity.getBlockMetadata()) {
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

        bindTexture(ResourceManager.assembler_slider_tex);

        int offset = (int) (System.currentTimeMillis() % 5000) / 5;

        if (offset > 500) {
            offset = 500 - (offset - 500);
        }

        if (tileEntity.isProgressing) {
            GlStateManager.translate(offset * 0.003 - 0.75, 0, 0);
        }

        ResourceManager.assembler_slider.renderAll();

        bindTexture(ResourceManager.assembler_arm_tex);

        double sway = Math.sin((System.currentTimeMillis() % 2000) / 1000.0 * Math.PI / 50) * 0.3;

        if (tileEntity.isProgressing) {
            GlStateManager.translate(0, 0, sway);
        }

        ResourceManager.assembler_arm.renderAll();

        GlStateManager.popMatrix();

        renderCogs(tileEntity, x, y, z, f);
    }

    public void renderCogs(TileEntityMachineAssembler tileEntity, double x, double y, double z, float f) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.enableLighting();
        GlStateManager.disableCull();
        GlStateManager.rotate(180, 0F, 1F, 0F);

        switch (tileEntity.getBlockMetadata()) {
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

        bindTexture(ResourceManager.assembler_cog_tex);

        int rotation = (int) (System.currentTimeMillis() % (360 * 5)) / 5;

        if (!tileEntity.isProgressing) {
            rotation = 0;
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.6, 0.75, 1.0625);
        GlStateManager.rotate(-rotation, 0F, 0F, 1F);
        ResourceManager.assembler_cog.renderAll();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.6, 0.75, 1.0625);
        GlStateManager.rotate(rotation, 0F, 0F, 1F);
        ResourceManager.assembler_cog.renderAll();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(-0.6, 0.75, -1.0625);
        GlStateManager.rotate(-rotation, 0F, 0F, 1F);
        ResourceManager.assembler_cog.renderAll();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.6, 0.75, -1.0625);
        GlStateManager.rotate(rotation, 0F, 0F, 1F);
        ResourceManager.assembler_cog.renderAll();
        GlStateManager.popMatrix();

        GlStateManager.popMatrix();
    }
}
