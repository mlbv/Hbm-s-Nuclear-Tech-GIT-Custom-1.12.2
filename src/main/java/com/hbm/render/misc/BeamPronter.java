package com.hbm.render.misc;

import java.util.Random;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import com.hbm.config.GeneralConfig;
import com.hbm.handler.HbmShaderManager2;
import com.hbm.main.ResourceManager;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.util.BobMathUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BeamPronter {

    public static enum EnumWaveType {
        RANDOM, SPIRAL
    }

    public static enum EnumBeamType {
        SOLID, LINE
    }

    private static boolean depthMask = false;

    public static void prontBeamwithDepth(Vec3d skeleton, EnumWaveType wave, EnumBeamType beam, int outerColor, int innerColor, int start, int segments, float size, int layers, float thickness) {
        depthMask = true;
        prontBeam(skeleton, wave, beam, outerColor, innerColor, start, segments, size, layers, thickness);
        depthMask = false;
    }

	public static void prontBeamwithDepth(Vec3 skeleton, EnumWaveType wave, EnumBeamType beam, int outerColor, int innerColor, int start, int segments, float size, int layers, float thickness) {
		// 将 Vec3 转换为 net.minecraft.util.math.Vec3d
		Vec3d skeletonVec3d = new Vec3d(skeleton.xCoord, skeleton.yCoord, skeleton.zCoord);
		
		// 调用原有的 Vec3d 版本的 prontBeamwithDepth 方法
		prontBeamwithDepth(skeletonVec3d, wave, beam, outerColor, innerColor, start, segments, size, layers, thickness);
	}

	public static void prontBeam(Vec3 skeleton, EnumWaveType wave, EnumBeamType beam, int outerColor, int innerColor, int start, int segments, float spinRadius, int layers, float thickness) {
		Vec3d skeleton3d = new Vec3d(skeleton.xCoord, skeleton.yCoord, skeleton.zCoord);
		prontBeam(skeleton3d, wave, beam, outerColor, innerColor, start, segments, spinRadius, layers, thickness);
	}

    public static void prontBeam(Vec3d skeleton, EnumWaveType wave, EnumBeamType beam, int outerColor, int innerColor, int start, int segments, float size, int layers, float thickness) {

        GL11.glPushMatrix();
        GL11.glDepthMask(depthMask);

        float sYaw = (float) (Math.atan2(skeleton.x, skeleton.z) * 180F / Math.PI);
        float sqrt = MathHelper.sqrt(skeleton.x * skeleton.x + skeleton.z * skeleton.z);
        float sPitch = (float) (Math.atan2(skeleton.y, (double) sqrt) * 180F / Math.PI);

        GL11.glRotatef(180, 0, 1F, 0);
        GL11.glRotatef(sYaw, 0, 1F, 0);
        GL11.glRotatef(sPitch - 90, 1F, 0, 0);

        GL11.glPushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();

        if (beam == EnumBeamType.SOLID) {
            GlStateManager.disableCull();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        }

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        Vec3d unit = new Vec3d(0, 1, 0);
        Random rand = new Random(start);
        double length = skeleton.length();
        double segLength = length / segments;
        double lastX = 0;
        double lastY = 0;
        double lastZ = 0;

        for (int i = 0; i <= segments; i++) {

            Vec3d spinner = new Vec3d(size, 0, 0);

            if (wave == EnumWaveType.SPIRAL) {
                spinner = spinner.rotateYaw((float) Math.PI * start / 180F);
                spinner = spinner.rotateYaw((float) Math.PI * 45F / 180F * i);
            } else if (wave == EnumWaveType.RANDOM) {
                spinner = spinner.rotateYaw((float) Math.PI * 2 * rand.nextFloat());
                spinner = spinner.rotateYaw((float) Math.PI * 2 * rand.nextFloat());
            }

            double pX = unit.x * segLength * i + spinner.x;
            double pY = unit.y * segLength * i + spinner.y;
            double pZ = unit.z * segLength * i + spinner.z;

            if (beam == EnumBeamType.LINE && i > 0) {
                buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
				applyColor(buffer.pos(pX, pY, pZ), outerColor, 255).endVertex();
				applyColor(buffer.pos(lastX, lastY, lastZ), outerColor, 255).endVertex();
                tessellator.draw();
            }

            if (beam == EnumBeamType.SOLID && i > 0) {
                float radius = thickness / layers;
                for (int j = 1; j <= layers; j++) {
                    float inter = (float) (j - 1) / (float) (layers - 1);
                    int color = interpolateColor(innerColor, outerColor, inter);

                    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                    applyColor(buffer.pos(lastX + (radius * j), lastY, lastZ + (radius * j)), color, 255).endVertex();
                    applyColor(buffer.pos(lastX + (radius * j), lastY, lastZ - (radius * j)), color, 255).endVertex();
                    applyColor(buffer.pos(pX + (radius * j), pY, pZ - (radius * j)), color, 255).endVertex();
                    applyColor(buffer.pos(pX + (radius * j), pY, pZ + (radius * j)), color, 255).endVertex();
                    tessellator.draw();
                }
            }

            lastX = pX;
            lastY = pY;
            lastZ = pZ;
        }

        if (beam == EnumBeamType.SOLID) {
            GlStateManager.disableBlend();
            GlStateManager.enableCull();
        }

        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GL11.glPopMatrix();
        GL11.glDepthMask(true);
        GL11.glPopMatrix();
    }

    private static int interpolateColor(int color1, int color2, float factor) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int) (r1 + (r2 - r1) * factor);
        int g = (int) (g1 + (g2 - g1) * factor);
        int b = (int) (b1 + (b2 - b1) * factor);

        return (r << 16) | (g << 8) | b;
    }

	private static BufferBuilder applyColor(BufferBuilder buffer, int color, int alpha) {
		int red = (color >> 16) & 0xFF;
		int green = (color >> 8) & 0xFF;
		int blue = color & 0xFF;
		return buffer.color(red, green, blue, alpha);
	}

		public static void gluonBeam(Vec3 pos1, Vec3 pos2, float size){
		//long l = System.nanoTime();
		GL11.glPushMatrix();
		GlStateManager.depthMask(false);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE);
		GlStateManager.disableCull();
		if(!GeneralConfig.useShaders2){
			GlStateManager.color(0.4F, 0.7F, 1, 1);
		}
		
		Vec3 diff = pos1.subtract(pos2);
		float len = (float) diff.lengthVector();
		Vec3 angles = BobMathUtil.getEulerAngles(diff);
		GL11.glTranslated(pos1.xCoord, pos1.yCoord, pos1.zCoord);
		
		GL11.glRotated(angles.xCoord+90, 0, 1, 0);
		GL11.glRotated(-angles.yCoord, 0, 0, 1);
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.noise_1);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.noise_2);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.bfg_core_lightning);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		
		net.minecraft.client.renderer.Tessellator tes = net.minecraft.client.renderer.Tessellator.getInstance();
		BufferBuilder buf = tes.getBuffer();
		
		GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, HbmShaderManager2.AUX_GL_BUFFER);
		HbmShaderManager2.AUX_GL_BUFFER.rewind();
		Matrix4f mvMatrix = new Matrix4f();
		mvMatrix.load(HbmShaderManager2.AUX_GL_BUFFER);
		HbmShaderManager2.AUX_GL_BUFFER.rewind();
		Matrix4f.invert(mvMatrix, mvMatrix);
		Vector4f billboardPos = Matrix4f.transform(mvMatrix, new Vector4f(0, 0, 0, 1), null);
		//System.out.println(billboardPos);
		//GL20.glUniform3f(GL20.glGetUniformLocation(ResourceManager.gluon_beam.getShaderId(), "playerPos"), billboardPos.x, billboardPos.y, billboardPos.z);
		
		//GL20.glUniform3f(GL20.glGetUniformLocation(ResourceManager.gluon_beam.getShaderId(), "playerPos"), 0.0F, 0.1F, 0F);
		int SUBDIVISIONS_PER_BLOCK = 16;
		int subdivisions = (int)Math.ceil(len*SUBDIVISIONS_PER_BLOCK);
		
		//System.out.println(billboardPos);
		ResourceManager.gluon_spiral.use();
		ResourceManager.gluon_spiral.uniform3f("playerPos", billboardPos.x, billboardPos.y, billboardPos.z);
		ResourceManager.gluon_spiral.uniform1f("subdivXAmount", 1/(float)SUBDIVISIONS_PER_BLOCK);
		ResourceManager.gluon_spiral.uniform1f("subdivUAmount", 1/(float)(subdivisions+1));
		ResourceManager.gluon_spiral.uniform1f("len", len);
		
		buf.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX);
		for(int i = 0; i <= subdivisions; i ++){
			float iN = ((float)i/(float)subdivisions);
			float pos = iN*len;
			buf.pos(pos, 0, -size*0.025).tex(iN, 0.45).endVertex();
			buf.pos(pos, 0, size*0.025).tex(iN, 0.55).endVertex();
		}
		tes.draw();
		
		SUBDIVISIONS_PER_BLOCK *= 0.5;
		subdivisions = (int)Math.ceil(len*SUBDIVISIONS_PER_BLOCK);
		
		Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.gluon_beam_tex);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		//GlStateManager.depthMask(true);
		ResourceManager.gluon_beam.use();
		ResourceManager.gluon_beam.uniform1f("beam_length", len);
		
		buf.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_TEX);
		//GL20.glUniform1f(GL20.glGetUniformLocation(ResourceManager.gluon_beam.getShaderId(), "subdivXAmount"), 1/(float)SUBDIVISIONS_PER_BLOCK);
		//GL20.glUniform1f(GL20.glGetUniformLocation(ResourceManager.gluon_beam.getShaderId(), "subdivUAmount"), 1/(float)(subdivisions+1));
		
		Vec3d vec = new Vec3d(billboardPos.x, billboardPos.y, billboardPos.z).crossProduct(new Vec3d(1, 0, 0)).normalize();
		for(int i = 0; i <= subdivisions; i ++){
			float iN = ((float)i/(float)subdivisions);
			float pos = iN*len;
			buf.pos(pos, -vec.y, -vec.z).tex(iN, 0).endVertex();
			buf.pos(pos, vec.y, vec.z).tex(iN, 1).endVertex();
		}
		tes.draw();
		
		HbmShaderManager2.releaseShader();
		
		//System.out.println(System.nanoTime() - l);
		if(!GeneralConfig.useShaders2){
			GlStateManager.color(1, 1, 1, 1);
		}
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
		GL11.glPopMatrix();
	}
}