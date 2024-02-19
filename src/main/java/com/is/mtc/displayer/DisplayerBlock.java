package com.is.mtc.displayer;

import com.is.mtc.MineTradingCards;
import com.is.mtc.handler.GuiHandler;
import com.is.mtc.root.Tools;
import com.is.mtc.util.Reference;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class DisplayerBlock extends BlockContainer {

	private IIcon iFace, iSides;
	private boolean wasPowered;

	public DisplayerBlock() {
		super(Material.iron);

		setLightLevel(0.9375F);

		setBlockName("block_displayer");
		setBlockTextureName(Reference.MODID + ":block_displayer");
		setCreativeTab(MineTradingCards.MODTAB);

		setHardness(5.0F);
		setResistance(10.0F);

		isBlockContainer = true;
	}

	/*-*/

	@Override
	public boolean onBlockActivated(World w, int px, int py, int pz, EntityPlayer player,
									int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_) {
		TileEntity tileEntity = w.getTileEntity(px, py, pz);

		if (!(tileEntity instanceof DisplayerBlockTileEntity))
			return false;

		player.openGui(MineTradingCards.INSTANCE, GuiHandler.GUI_DISPLAYER, w, px, py, pz);
		return true;
	}

	private void emptyDisplayerBlockTileEntity(DisplayerBlockTileEntity dte, World w, int x, int y, int z) {
		ItemStack[] content;

		if (dte == null)
			return;
		content = dte.getContent();

		for (int i = 0; i < 4; ++i) {
			ItemStack stack = content[i];

			if (stack != null) {
				EntityItem entity = new EntityItem(w, x, y, z, stack);

				w.spawnEntityInWorld(entity);
			}
		}
	}

	@Override
	public void onBlockPreDestroy(World w, int x, int y, int z, int oldMeta) {
		if (w.isRemote)
			return;

		emptyDisplayerBlockTileEntity((DisplayerBlockTileEntity) w.getTileEntity(x, y, z), w, x, y, z);
		w.removeTileEntity(x, y, z);
	}

	/*-*/

	@Override
	public void registerBlockIcons(IIconRegister ireg) {
		iFace = ireg.registerIcon(getTextureName());
		iSides = ireg.registerIcon(getTextureName() + "_side");
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		return (side == Tools.SIDE_TOP || side == Tools.SIDE_BOTTOM) ? iFace : iSides;
	}

	/*-*/

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new DisplayerBlockTileEntity();
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
		boolean isPowered = world.isBlockIndirectlyGettingPowered(x, y, z);
		try {
			DisplayerBlockTileEntity thisEntity = (DisplayerBlockTileEntity) world.getTileEntity(x, y, z);
			if (isPowered && !wasPowered) {
				thisEntity.spinCards();
			}
		} catch (Exception ignored) {

		}
		wasPowered = isPowered;
	}
}
