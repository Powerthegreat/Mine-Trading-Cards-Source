package com.is.mtc.displayer;

import org.lwjgl.opengl.GL11;

import com.is.mtc.card.CardItem;
import com.is.mtc.data_manager.CardStructure;
import com.is.mtc.data_manager.Databank;
import com.is.mtc.root.Tools;

import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class DisplayerBlockRenderer extends TileEntitySpecialRenderer {

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float par5) {
		Tessellator tessellator = Tessellator.instance;
		DisplayerBlockTileEntity displayerBlockTileEntity = (DisplayerBlockTileEntity) tileEntity;
		EntityItem item = null;

		GL11.glPushMatrix();
		RenderHelper.disableStandardItemLighting();
		GL11.glTranslated(x, y, z);

		// Facing north face
		tessellator.startDrawingQuads();
		int bindTextureResult = bindTextureForSlot(tessellator, displayerBlockTileEntity, 0);
		if (bindTextureResult == 0) {
			tessellator.addVertexWithUV(0, 0, 0 - 0.01D, 1, 1);
			tessellator.addVertexWithUV(0, 1D, 0 - 0.01D, 1, 0);
			tessellator.addVertexWithUV(1D, 1D, 0 - 0.01D, 0, 0);
			tessellator.addVertexWithUV(1D, 0, 0 - 0.01D, 0, 1);
			tessellator.draw();
		} else if (bindTextureResult == 1) {
			tessellator.draw();

			item = new EntityItem(tileEntity.getWorldObj(), x, y, z, displayerBlockTileEntity.getStackInSlot(0));
			item.hoverStart = 0;
			RenderItem.renderInFrame = true;
			RenderHelper.enableStandardItemLighting();
			GL11.glTranslated(0.5, 0.10, 0);
			GL11.glScaled(2, 2, 2);
			RenderManager.instance.renderEntityWithPosYaw(item, 0, 0, 0, 0, 0);
			RenderHelper.disableStandardItemLighting();
			GL11.glScaled(0.5, 0.5, 0.5);
			GL11.glTranslated(-0.5, -0.10, 0);
		} else {
			tessellator.draw();
		}

		// Facing south face
		tessellator.startDrawingQuads();
		bindTextureResult = bindTextureForSlot(tessellator, displayerBlockTileEntity, 1);
		if (bindTextureResult == 0) {
			tessellator.addVertexWithUV(1D, 0, 1.01D, 1, 1);
			tessellator.addVertexWithUV(1D, 1D, 1.01D, 1, 0);
			tessellator.addVertexWithUV(0, 1D, 1.01D, 0, 0);
			tessellator.addVertexWithUV(0, 0, 1.01D, 0, 1);
			tessellator.draw();
		} else if (bindTextureResult == 1) {
			tessellator.draw();

			item = new EntityItem(tileEntity.getWorldObj(), x, y, z, displayerBlockTileEntity.getStackInSlot(1));
			item.hoverStart = 0;
			RenderItem.renderInFrame = true;
			RenderHelper.enableStandardItemLighting();
			GL11.glTranslated(0.5, 0.10, 1);
			GL11.glScaled(2, 2, 2);
			GL11.glRotated(180, 0, 1, 0);
			RenderManager.instance.renderEntityWithPosYaw(item, 0, 0, 0, 0, 0);
			RenderHelper.disableStandardItemLighting();
			GL11.glRotated(-180, 0, 1, 0);
			GL11.glScaled(0.5, 0.5, 0.5);
			GL11.glTranslated(-0.5, -0.10, -1);
		} else {
			tessellator.draw();
		}

		// Facing east face
		tessellator.startDrawingQuads();
		bindTextureResult = bindTextureForSlot(tessellator, displayerBlockTileEntity, 2);
		if (bindTextureResult == 0) {
			tessellator.addVertexWithUV(1.01D, 0, 0, 1, 1);
			tessellator.addVertexWithUV(1.01D, 1D, 0, 1, 0);
			tessellator.addVertexWithUV(1.01D, 1D, 1D, 0, 0);
			tessellator.addVertexWithUV(1.01D, 0, 1D, 0, 1);
			tessellator.draw();
		} else if (bindTextureResult == 1) {
			tessellator.draw();

			item = new EntityItem(tileEntity.getWorldObj(), x, y, z, displayerBlockTileEntity.getStackInSlot(2));
			item.hoverStart = 0;
			RenderItem.renderInFrame = true;
			RenderHelper.enableStandardItemLighting();
			GL11.glTranslated(1, 0.10, 0.5);
			GL11.glScaled(2, 2, 2);
			GL11.glRotated(-90, 0, 1, 0);
			RenderManager.instance.renderEntityWithPosYaw(item, 0, 0, 0, 0, 0);
			RenderHelper.disableStandardItemLighting();
			GL11.glRotated(90, 0, 1, 0);
			GL11.glScaled(0.5, 0.5, 0.5);
			GL11.glTranslated(-1, -0.10, -0.5);
		} else {
			tessellator.draw();
		}

		// Facing west face
		tessellator.startDrawingQuads();
		bindTextureResult = bindTextureForSlot(tessellator, displayerBlockTileEntity, 3);
		if (bindTextureResult == 0) {
			tessellator.addVertexWithUV(0 - 0.01D, 0, 1D, 1, 1);
			tessellator.addVertexWithUV(0 - 0.01D, 1D, 1D, 1, 0);
			tessellator.addVertexWithUV(0 - 0.01D, 1D, 0, 0, 0);
			tessellator.addVertexWithUV(0 - 0.01D, 0, 0, 0, 1);
			tessellator.draw();
		} else if (bindTextureResult == 1) {
			tessellator.draw();

			item = new EntityItem(tileEntity.getWorldObj(), x, y, z, displayerBlockTileEntity.getStackInSlot(3));
			item.hoverStart = 0;
			RenderItem.renderInFrame = true;
			RenderHelper.enableStandardItemLighting();
			GL11.glTranslated(0, 0.10, 0.5);
			GL11.glScaled(2, 2, 2);
			GL11.glRotated(90, 0, 1, 0);
			RenderManager.instance.renderEntityWithPosYaw(item, 0, 0, 0, 0, 0);
			RenderHelper.disableStandardItemLighting();
			GL11.glRotated(-90, 0, 1, 0);
			GL11.glScaled(0.5, 0.5, 0.5);
			GL11.glTranslated(0, -0.10, -0.5);
		} else {
			tessellator.draw();
		}

		RenderHelper.enableStandardItemLighting();
		GL11.glPopMatrix();
	}

	private int bindTextureForSlot(Tessellator tessellator, DisplayerBlockTileEntity displayerBlockTileEntity, int slot) {
		ItemStack stack = displayerBlockTileEntity.getItemStackInSlot(slot);

		if (Tools.isValidCard(stack)) {
			CardItem ci = (CardItem) stack.getItem();
			CardStructure cStruct = Databank.getCardByCDWD(stack.stackTagCompound.getString("cdwd"));

			tessellator.setColorRGBA_F(1, 1, 1, 1);
			if (CardStructure.isValidCStructAsset(cStruct, stack)) {
				bindTexture(cStruct.getResourceLocations().get(stack.getTagCompound().getInteger("assetnumber")));
				return 0;
			} else { // Card not registered or unregistered illustration, use item image instead
				return 1;
			}
		} else {
			tessellator.setColorRGBA_F(1, 1, 1, 0);
			return -1;
		}
	}
}
