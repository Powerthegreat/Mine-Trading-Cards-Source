package com.is.mtc.village;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.is.mtc.MineTradingCards;
import com.is.mtc.card.CardItem;
import com.is.mtc.data_manager.CardStructure;
import com.is.mtc.data_manager.Databank;
import com.is.mtc.integration.villagenames.VNCompat;
import com.is.mtc.root.Rarity;
import com.is.mtc.util.Functions;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.block.Block;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;

public class CardMasterHome extends StructureVillagePieces.Village {
	public static final int STRUCTURE_HEIGHT = 7;
	// Make foundation with blanks as empty air and F as foundation spaces
	private static final String[] foundationPattern = new String[]{
			"          ",
			" FFFFFFFF ",
			" FFFFFFFF ",
			" FFFFFFFF ",
			" FFFFFFFF ",
			" FFFFFFFF ",
			" FFFFFFFF ",
			" FFFFFFFF ",
			" FFFFFFFF ",
			" FFFFFFFF ",
			"      P   ",
	};
	// Here are values to assign to the bounding box
	public static final int STRUCTURE_WIDTH = foundationPattern[0].length();
	public static final int STRUCTURE_DEPTH = foundationPattern.length;
	// Values for lining things up
	private static final int GROUND_LEVEL = 2; // Spaces above the bottom of the structure considered to be "ground level"
	private static final int INCREASE_MIN_U = 3;
	private static final int DECREASE_MAX_U = 0;
	private static final int INCREASE_MIN_W = 0;
	private static final int DECREASE_MAX_W = 4;
	// Stuff to be used in the construction
	public boolean entitiesGenerated = false;
	public ArrayList<Integer> decorHeightY = new ArrayList();
	public int townColor = 15; // Black
	public int townColor2 = 14; // Red
	public int townColor3 = 13; // Green
	public int townColor4 = 12; // Brown
	public BiomeGenBase biome = null;
	private int averageGroundLevel = -1;

	public CardMasterHome() {
	}

	public CardMasterHome(StructureVillagePieces.Start start, int componentType, Random random, StructureBoundingBox boundingBox, int coordBaseMode) {
		super();
		this.coordBaseMode = coordBaseMode;
		this.boundingBox = boundingBox;
		// Additional stuff to be used in the construction
		if (start != null) {
			this.biome = start.biome;
		}
	}

	public static CardMasterHome buildComponent(StructureVillagePieces.Start start, List pieces, Random random, int x, int y, int z, int coordBaseMode, int componentType) {
		StructureBoundingBox structureboundingbox = StructureBoundingBox.getComponentToAddBoundingBox(x, y, z, 0, 0, 0, STRUCTURE_WIDTH, STRUCTURE_HEIGHT, STRUCTURE_DEPTH, coordBaseMode);

		return CardMasterHome.canVillageGoDeeper(structureboundingbox) && StructureComponent.findIntersecting(pieces, structureboundingbox) == null ? new CardMasterHome(start, componentType, random, structureboundingbox, coordBaseMode) : null;
	}


	@Override
	public boolean addComponentParts(World world, Random random, StructureBoundingBox structureBB) {
		int[] townColors = VNCompat.getTownColorsVN(world, boundingBox,
				this.townColor,
				this.townColor2,
				this.townColor3,
				this.townColor4,
				this.townColor,
				this.townColor,
				this.townColor
		);
		this.townColor = townColors[0];
		this.townColor2 = townColors[1];
		this.townColor3 = townColors[2];
		this.townColor4 = townColors[3];

		if (this.averageGroundLevel < 0) {
			this.averageGroundLevel = Functions.getMedianGroundLevel(world,
					// Set the bounding box version as this bounding box but with Y going from 0 to 512
					new StructureBoundingBox(
							this.boundingBox.minX + (new int[]{INCREASE_MIN_U, DECREASE_MAX_W, INCREASE_MIN_U, INCREASE_MIN_W}[this.coordBaseMode]), this.boundingBox.minZ + (new int[]{INCREASE_MIN_W, INCREASE_MIN_U, DECREASE_MAX_W, INCREASE_MIN_U}[this.coordBaseMode]),
							this.boundingBox.maxX - (new int[]{DECREASE_MAX_U, INCREASE_MIN_W, DECREASE_MAX_U, DECREASE_MAX_W}[this.coordBaseMode]), this.boundingBox.maxZ - (new int[]{DECREASE_MAX_W, DECREASE_MAX_U, INCREASE_MIN_W, DECREASE_MAX_U}[this.coordBaseMode])),
					true, (byte) 1, this.coordBaseMode);

			if (this.averageGroundLevel < 0) {
				return true;
			} // Do not construct in a void

			this.boundingBox.offset(0, this.averageGroundLevel - this.boundingBox.minY - GROUND_LEVEL, 0);
		}

		WorldChunkManager chunkManager = world.getWorldChunkManager();
		int bbCenterX = (this.boundingBox.minX + this.boundingBox.maxX) / 2;
		int bbCenterZ = (this.boundingBox.minZ + this.boundingBox.maxZ) / 2;
		BiomeGenBase biome = chunkManager.getBiomeGenAt(bbCenterX, bbCenterZ);

		// Reestablish biome if start was null or something
		if (this.biome == null) {
			this.biome = world.getBiomeGenForCoords((this.boundingBox.minX + this.boundingBox.maxX) / 2, (this.boundingBox.minZ + this.boundingBox.maxZ) / 2);
		}

		if (this.biome == BiomeGenBase.desert || this.biome == BiomeGenBase.desertHills) {
			ReflectionHelper.setPrivateValue(StructureVillagePieces.Village.class, this, true, new String[]{"field_143014_b", "field_143014_b"});
		}

		Object[] blockObject;
		blockObject = VNCompat.getBiomeSpecificBlockObject(Blocks.dirt, 0, chunkManager, bbCenterX, bbCenterZ, this.biome);
		Block biomeDirtBlock = (Block) blockObject[0];
		int biomeDirtMeta = (Integer) blockObject[1];
		blockObject = VNCompat.getBiomeSpecificBlockObject(Blocks.grass, 0, chunkManager, bbCenterX, bbCenterZ, this.biome);
		Block biomeGrassBlock = (Block) blockObject[0];
		int biomeGrassMeta = (Integer) blockObject[1];
		// Establish top and filler blocks, substituting Grass and Dirt if they're null
		Block biomeTopBlock = biomeGrassBlock;
		int biomeTopMeta = biomeGrassMeta;
		if (this.biome != null && this.biome.topBlock != null) {
			biomeTopBlock = this.biome.topBlock;
			biomeTopMeta = 0;
		}
		Block biomeFillerBlock = biomeDirtBlock;
		int biomeFillerMeta = biomeDirtMeta;
		if (this.biome != null && this.biome.fillerBlock != null) {
			biomeFillerBlock = this.biome.fillerBlock;
			biomeFillerMeta = 0;
		}

		// Clear space above
		for (int u = 0; u < STRUCTURE_WIDTH; ++u) {
			for (int w = 0; w < STRUCTURE_DEPTH; ++w) {
				this.clearCurrentPositionBlocksUpwards(world, u, GROUND_LEVEL, w, structureBB);
			}
		}

		// Follow the blueprint to set up the starting foundation
		for (int w = 0; w < foundationPattern.length; w++) {
			for (int u = 0; u < foundationPattern[0].length(); u++) {

				String unitLetter = foundationPattern[foundationPattern.length - 1 - w].substring(u, u + 1).toUpperCase();
				int posX = this.getXWithOffset(u, w);
				int posY = this.getYWithOffset(GROUND_LEVEL - 1);
				int posZ = this.getZWithOffset(u, w);

				if (unitLetter.equals("F")) {
					// If marked with F: fill with dirt foundation
					this.func_151554_b(world, biomeFillerBlock, biomeFillerMeta, u, GROUND_LEVEL - 1, w, structureBB);
				} else if (unitLetter.equals("P")) {
					// If marked with P: fill with dirt foundation and top with block-and-biome-appropriate path
					this.func_151554_b(world, biomeFillerBlock, biomeFillerMeta, u, GROUND_LEVEL - 1 + (world.getBlock(posX, posY, posZ).isNormalCube() ? -1 : 0), w, structureBB);
					if (MineTradingCards.hasVillageNamesInstalled
							&& astrotibs.villagenames.config.village.VillageGeneratorConfigHandler.newVillageGenerator) {
						VNCompat.setPathSpecificBlock(world, chunkManager, bbCenterX, bbCenterZ, this.biome, posX, posY, posZ, false);
					} else {
						this.placeBlockAtCurrentPosition(world, Blocks.gravel, 0, u, GROUND_LEVEL - 1, w, structureBB);
					}
				} else if (world.getBlock(posX, posY, posZ) == biomeFillerBlock) {
					// If the space is blank and the block itself is dirt, add dirt foundation
					this.func_151554_b(world, biomeFillerBlock, biomeFillerMeta, u, GROUND_LEVEL - 2, w, structureBB);
				}

				// Then, if the top is dirt with a non-full cube above it, make it grass
				if (world.getBlock(posX, posY, posZ) != null && world.getBlock(posX, posY + 1, posZ) != null
						&& world.getBlock(posX, posY, posZ) == biomeFillerBlock && !world.getBlock(posX, posY + 1, posZ).isNormalCube()) {
					// If the space is blank and the block itself is dirt, add dirt foundation and then cap with grass:
					this.placeBlockAtCurrentPosition(world, biomeTopBlock, biomeTopMeta, u, GROUND_LEVEL - 1, w, structureBB);
				}
			}
		}


		// Cobblestone
		blockObject = VNCompat.getBiomeSpecificBlockObject(Blocks.cobblestone, 0, chunkManager, bbCenterX, bbCenterZ, this.biome);
		Block biomeCobblestoneBlock = (Block) blockObject[0];
		int biomeCobblestoneMeta = (Integer) blockObject[1];
		for (int[] uuvvww : new int[][]{
				// Foundation
				{1, 0, 1, 8, 0, 9},
				{1, 1, 1, 8, 1, 1}, {1, 1, 9, 8, 1, 9}, {1, 1, 1, 2, 1, 9}, {8, 1, 1, 8, 1, 9},
				// Corner pillars
				{1, 2, 1, 1, 4, 1}, {8, 2, 1, 8, 4, 1},
				{1, 2, 9, 1, 4, 9}, {8, 2, 9, 8, 4, 9},
		}) {
			this.fillWithMetadataBlocks(world, structureBB, uuvvww[0], uuvvww[1], uuvvww[2], uuvvww[3], uuvvww[4], uuvvww[5], biomeCobblestoneBlock, biomeCobblestoneMeta, biomeCobblestoneBlock, biomeCobblestoneMeta, false);
		}


		// Terracotta
		for (int[] uuvvwwm : new int[][]{
				// Loss, lol
				{2, 0, 4, 3, 0, 4, this.townColor}, {2, 0, 8, 3, 0, 8, this.townColor}, {6, 0, 4, 7, 0, 4, this.townColor}, {6, 0, 7, 7, 0, 7, this.townColor},
				{2, 0, 2, 2, 0, 3, this.townColor2},
				{2, 0, 6, 3, 0, 6, this.townColor3},
				{6, 0, 2, 7, 0, 2, this.townColor4},
		}) {
			this.fillWithMetadataBlocks(world, structureBB, uuvvwwm[0], uuvvwwm[1], uuvvwwm[2], uuvvwwm[3], uuvvwwm[4], uuvvwwm[5], Blocks.stained_hardened_clay, uuvvwwm[6], Blocks.stained_hardened_clay, uuvvwwm[6], false);
		}


		// Wool
		for (int[] uuvvwwm : new int[][]{
				{3, 1, 2, 7, 1, 8, this.townColor},
		}) {
			this.fillWithMetadataBlocks(world, structureBB, uuvvwwm[0], uuvvwwm[1], uuvvwwm[2], uuvvwwm[3], uuvvwwm[4], uuvvwwm[5], Blocks.wool, uuvvwwm[6], Blocks.wool, uuvvwwm[6], false);
		}


		// Carpet
		for (int[] uuvvwwm : new int[][]{
				{4, 3, 3, 4, 3, 8, this.townColor2},
		}) {
			this.fillWithMetadataBlocks(world, structureBB, uuvvwwm[0], uuvvwwm[1], uuvvwwm[2], uuvvwwm[3], uuvvwwm[4], uuvvwwm[5], Blocks.carpet, uuvvwwm[6], Blocks.carpet, uuvvwwm[6], false);
		}


		// Logs Vertical
		blockObject = VNCompat.getBiomeSpecificBlockObject(Blocks.log, 0, chunkManager, bbCenterX, bbCenterZ, this.biome);
		Block biomeLogVertBlock = (Block) blockObject[0];
		int biomeLogVertMeta = (Integer) blockObject[1];
		for (int[] uuvvww : new int[][]{
				// Posts beneath displays
				{2, 2, 3, 2, 2, 3}, {2, 2, 5, 2, 2, 5}, {2, 2, 7, 2, 2, 7},
		}) {
			this.fillWithMetadataBlocks(world, structureBB, uuvvww[0], uuvvww[1], uuvvww[2], uuvvww[3], uuvvww[4], uuvvww[5], biomeLogVertBlock, biomeLogVertMeta, biomeLogVertBlock, biomeLogVertMeta, false);
		}


		// Logs Across
		blockObject = VNCompat.getBiomeSpecificBlockObject(Blocks.log, 4 + (this.coordBaseMode % 2 == 0 ? 0 : 4), chunkManager, bbCenterX, bbCenterZ, this.biome);
		Block biomeLogHorAcrossBlock = (Block) blockObject[0];
		int biomeLogHorAcrossMeta = (Integer) blockObject[1]; // Perpendicular to you
		for (int[] uuvvww : new int[][]{
				// Front beam
				{2, 4, 1, 7, 4, 1},
				// Rear beam
				{2, 4, 9, 7, 4, 9},
		}) {
			this.fillWithMetadataBlocks(world, structureBB, uuvvww[0], uuvvww[1], uuvvww[2], uuvvww[3], uuvvww[4], uuvvww[5], biomeLogHorAcrossBlock, biomeLogHorAcrossMeta, biomeLogHorAcrossBlock, biomeLogHorAcrossMeta, false);
		}

		// Logs Along
		blockObject = VNCompat.getBiomeSpecificBlockObject(Blocks.log, 4 + (this.coordBaseMode % 2 == 0 ? 4 : 0), chunkManager, bbCenterX, bbCenterZ, this.biome);
		Block biomeLogHorAlongBlock = (Block) blockObject[0];
		int biomeLogHorAlongMeta = (Integer) blockObject[1]; // Toward you
		for (int[] uw : new int[][]{
				// Left wall
				{1, 4, 2, 1, 4, 8},
				// Right wall
				{8, 4, 2, 8, 4, 8},
		}) {
			this.fillWithMetadataBlocks(world, structureBB, uw[0], uw[1], uw[2], uw[3], uw[4], uw[5], biomeLogHorAlongBlock, biomeLogHorAlongMeta, biomeLogHorAlongBlock, biomeLogHorAlongMeta, false);
		}


		// Bookshelves
		blockObject = VNCompat.getBiomeSpecificBlockObject(Blocks.bookshelf, 0, chunkManager, bbCenterX, bbCenterZ, this.biome);
		Block biomeBookshelfBlock = (Block) blockObject[0];
		int biomeBookshelfMeta = (Integer) blockObject[1];
		for (int[] uuvvww : new int[][]{
				{2, 4, 2, 2, 4, 8},
		}) {
			this.fillWithMetadataBlocks(world, structureBB, uuvvww[0], uuvvww[1], uuvvww[2], uuvvww[3], uuvvww[4], uuvvww[5], biomeBookshelfBlock, biomeBookshelfMeta, biomeBookshelfBlock, biomeBookshelfMeta, false);
		}

		// Torches
		for (int[] uvwo : new int[][]{ // Orientation - 0:forward, 1:rightward, 2:backward (toward you), 3:leftward, -1:upright;
				// Over door
				{6, 4, 2, 0},
				// Over table
				{7, 4, 6, 3},
		}) {
			this.placeBlockAtCurrentPosition(world, Blocks.torch, Functions.getTorchRotationMeta(uvwo[3], this.coordBaseMode), uvwo[0], uvwo[1], uvwo[2], structureBB);
		}

		// Wooden Planks
		blockObject = VNCompat.getBiomeSpecificBlockObject(Blocks.planks, 0, chunkManager, bbCenterX, bbCenterZ, this.biome);
		Block biomeWoodenPlanksBlock = (Block) blockObject[0];
		int biomeWoodenPlanksMeta = (Integer) blockObject[1];
		for (int[] uuvvww : new int[][]{
				// Front wall
				{2, 2, 1, 2, 3, 1}, {3, 2, 1, 3, 2, 1}, {4, 2, 1, 5, 3, 1}, {7, 2, 1, 7, 3, 1},
				// Left wall
				{1, 2, 2, 1, 3, 8},
				// Right wall
				{8, 2, 2, 8, 3, 8},
				// Back wall
				{2, 2, 9, 2, 3, 9}, {3, 2, 9, 3, 2, 9}, {4, 2, 9, 5, 3, 9}, {6, 2, 9, 6, 2, 9}, {7, 2, 9, 7, 3, 9},
				// Roof
				{1, 5, 1, 8, 5, 1}, {8, 5, 2, 8, 5, 8}, {2, 5, 9, 8, 5, 9}, {1, 5, 2, 1, 5, 8},
		}) {
			this.fillWithMetadataBlocks(world, structureBB, uuvvww[0], uuvvww[1], uuvvww[2], uuvvww[3], uuvvww[4], uuvvww[5], biomeWoodenPlanksBlock, biomeWoodenPlanksMeta, biomeWoodenPlanksBlock, biomeWoodenPlanksMeta, false);
		}


		// Windows
		for (int[] uvw : new int[][]{
				{3, 3, 1}, // Front
				{3, 3, 9}, {6, 3, 9}, // Rear
		}) {
			this.placeBlockAtCurrentPosition(world, Blocks.glass_pane, 0, uvw[0], uvw[1], uvw[2], structureBB);
		}


		// Wooden stairs
		blockObject = VNCompat.getBiomeSpecificBlockObject(Blocks.oak_stairs, 0, chunkManager, bbCenterX, bbCenterZ, this.biome);
		Block biomeWoodStairsBlock = (Block) blockObject[0];
		for (int[] uuvvwwo : new int[][]{ // Orientation - 0: leftward, 1: rightward, 3:backward, 2:forward
				// Roof
				{0, 5, 0, 8, 5, 0, 3}, {9, 5, 0, 9, 5, 9, 1}, {1, 5, 10, 9, 5, 10, 2}, {0, 5, 1, 0, 5, 10, 0},

				// Counter
				{4, 2, 3, 4, 2, 8, 0 + 4},

				// Shelves
				{2, 2, 2, 2, 3, 2, 1}, {2, 2, 4, 2, 3, 4, 1}, {2, 2, 6, 2, 3, 6, 1}, {2, 2, 8, 2, 3, 8, 1},

				// Seats
				{7, 2, 5, 7, 2, 5, 2}, {7, 2, 7, 7, 2, 7, 3},
		}) {
			this.fillWithMetadataBlocks(world, structureBB, uuvvwwo[0], uuvvwwo[1], uuvvwwo[2], uuvvwwo[3], uuvvwwo[4], uuvvwwo[5], biomeWoodStairsBlock, this.getMetadataWithOffset(Blocks.oak_stairs, uuvvwwo[6] % 4) + (uuvvwwo[6] / 4) * 4, biomeWoodStairsBlock, this.getMetadataWithOffset(Blocks.oak_stairs, uuvvwwo[6] % 4) + (uuvvwwo[6] / 4) * 4, false);
		}

		// Table
		Object[][] tableComponentObjects = VNCompat.chooseModWoodenTable(biomeWoodenPlanksBlock == Blocks.planks ? biomeWoodenPlanksMeta : 0);
		for (int[] uuvvww : new int[][]{
				{7, 2, 6},
		}) {
			for (int i = 1; i >= 0; i--) {
				this.placeBlockAtCurrentPosition(world, (Block) tableComponentObjects[i][0], (Integer) tableComponentObjects[i][1], uuvvww[0], uuvvww[1] + 1 - i, uuvvww[2], structureBB);
			}
		}


		// Bottom wood slab
		blockObject = VNCompat.getBiomeSpecificBlockObject(Blocks.wooden_slab, 0, chunkManager, bbCenterX, bbCenterZ, this.biome);
		Block biomeWoodSlabBottomBlock = (Block) blockObject[0];
		int biomeWoodSlabBottomMeta = (Integer) blockObject[1];
		for (int[] uuvvww : new int[][]{
				// Roof
				{1, 6, 1, 8, 6, 9},
		}) {
			this.fillWithMetadataBlocks(world, structureBB, uuvvww[0], uuvvww[1], uuvvww[2], uuvvww[3], uuvvww[4], uuvvww[5], biomeWoodSlabBottomBlock, biomeWoodSlabBottomMeta, biomeWoodSlabBottomBlock, biomeWoodSlabBottomMeta, false);
		}


		// Top wood slab
		blockObject = VNCompat.getBiomeSpecificBlockObject(Blocks.wooden_slab, 8, chunkManager, bbCenterX, bbCenterZ, this.biome);
		Block biomeWoodSlabTopBlock = (Block) blockObject[0];
		int biomeWoodSlabTopMeta = (Integer) blockObject[1];
		for (int[] uuvvww : new int[][]{
				// Ceiling
				{4, 4, 2, 4, 4, 8},
		}) {
			this.fillWithMetadataBlocks(world, structureBB, uuvvww[0], uuvvww[1], uuvvww[2], uuvvww[3], uuvvww[4], uuvvww[5], biomeWoodSlabTopBlock, biomeWoodSlabTopMeta, biomeWoodSlabTopBlock, biomeWoodSlabTopMeta, false);
		}


		// Fence
		blockObject = VNCompat.getBiomeSpecificBlockObject(Blocks.fence, 0, chunkManager, bbCenterX, bbCenterZ, this.biome);
		Block biomeFenceBlock = (Block) blockObject[0];
		int biomeFenceMeta = (Integer) blockObject[1];
		for (int[] uvw : new int[][]{
				// Above counter
				{4, 5, 3}, {4, 5, 5}, {4, 5, 7},
		}) {
			this.placeBlockAtCurrentPosition(world, biomeFenceBlock, biomeFenceMeta, uvw[0], uvw[1], uvw[2], structureBB);
		}


		// Fence Gate (Along)
		blockObject = VNCompat.getBiomeSpecificBlockObject(Blocks.fence_gate, 0, chunkManager, bbCenterX, bbCenterZ, this.biome);
		Block biomeFenceGateBlock = (Block) blockObject[0];
		int biomeFenceGateMeta = (Integer) blockObject[1];
		for (int[] uvw : new int[][]{
				{4, 2, 2, 1, 0},
		}) {
			this.placeBlockAtCurrentPosition(world, biomeFenceGateBlock, Functions.getMetadataWithOffset(biomeFenceGateBlock, (biomeFenceGateMeta + 1) % 8, this.coordBaseMode), uvw[0], uvw[1], uvw[2], structureBB);
		}


		// Card Display cases
		for (int[] uvwoc : new int[][]{ // 0=fore-facing (away from you); 1=right-facing; 2=back-facing (toward you); 3=left-facing
				{2, 3, 3, 1, 0}, {2, 3, 5, 1, 2}, {2, 3, 7, 1, 0},
		}) {
			this.placeBlockAtCurrentPosition(world, MineTradingCards.monoDisplayerBlock, 0, uvwoc[0], uvwoc[1], uvwoc[2], structureBB);
			world.setBlockMetadataWithNotify(this.getXWithOffset(uvwoc[0], uvwoc[2]), this.getYWithOffset(uvwoc[1]), this.getZWithOffset(uvwoc[0], uvwoc[2]), Functions.chooseFurnaceMeta(uvwoc[3], this.coordBaseMode), 2);
			TileEntity te = world.getTileEntity(this.getXWithOffset(uvwoc[0], uvwoc[2]), this.getYWithOffset(uvwoc[1]), this.getZWithOffset(uvwoc[0], uvwoc[2]));
			if (te != null && te instanceof IInventory) {
				// The center card will sometimes be an uncommon
				boolean cardIsUncommon = uvwoc[4] != 0 && (random.nextInt(100) < uvwoc[4]);

				ItemStack displaystack = new ItemStack(cardIsUncommon ? MineTradingCards.cardUncommon : MineTradingCards.cardCommon, 1);

				// Turn card into specific type
				CardStructure cStruct = Databank.generateACard(cardIsUncommon ? Rarity.UNCOMMON : Rarity.COMMON, new Random()); // Using new Random() because world random can cause issues generating cards
				if (cStruct != null) {
					displaystack.stackTagCompound = new NBTTagCompound();
					displaystack = CardItem.applyCDWDtoStack(displaystack, cStruct, random);

					((IInventory) te).setInventorySlotContents(0, displaystack);
				}
			}
		}


		// Doors
		blockObject = VNCompat.getBiomeSpecificBlockObject(Blocks.wooden_door, 0, chunkManager, bbCenterX, bbCenterZ, this.biome);
		Block biomeWoodDoorBlock = (Block) blockObject[0];
		for (int[] uvwoor : new int[][]{ // u, v, w, orientation, isShut (1/0 for true/false), isRightHanded (1/0 for true/false)
				{6, 2, 1, 2, 1, 1},
		}) {
			for (int height = 0; height <= 1; height++) {
				this.placeBlockAtCurrentPosition(world, biomeWoodDoorBlock, Functions.getDoorMetas(uvwoor[3], this.coordBaseMode, uvwoor[4] == 1, uvwoor[5] == 1)[height],
						uvwoor[0], uvwoor[1] + height, uvwoor[2], structureBB);
			}
		}


		// Clear path for easier entry
		for (int[] uvw : new int[][]{
				{6, GROUND_LEVEL, -1},
		}) {
			int pathU = uvw[0];
			int pathV = uvw[1];
			int pathW = uvw[2];

			// Clear above and set foundation below
			this.clearCurrentPositionBlocksUpwards(world, pathU, pathV, pathW, structureBB);
			this.func_151554_b(world, biomeFillerBlock, biomeFillerMeta, pathU, pathV - 2, pathW, structureBB);
			// Top is grass which is converted to path
			if (world.isAirBlock(this.getXWithOffset(pathU, pathW), this.getYWithOffset(pathV - 1), this.getZWithOffset(pathU, pathW))) {
				this.placeBlockAtCurrentPosition(world, biomeGrassBlock, biomeGrassMeta, pathU, pathV - 1, pathW, structureBB);
			}

			if (MineTradingCards.hasVillageNamesInstalled
					&& astrotibs.villagenames.config.village.VillageGeneratorConfigHandler.newVillageGenerator) {
				VNCompat.setPathSpecificBlock(world, chunkManager, bbCenterX, bbCenterZ, this.biome, this.getXWithOffset(pathU, pathW), this.getYWithOffset(pathV - 1), this.getZWithOffset(pathU, pathW), false);
			} else {
				this.placeBlockAtCurrentPosition(world, Blocks.gravel, 0, pathU, pathV - 1, pathW, structureBB);
			}
		}


		// Villagers
		if (!this.entitiesGenerated) {
			this.entitiesGenerated = true;

			// Card master behind counter
			int u = 3;
			int v = 2;
			int w = 2 + random.nextInt(7);

			EntityVillager entityvillager = Functions.makeVillagerWithProfession(world, random, VillagerHandler.ID_CARD_MASTER, 0);

			entityvillager.setLocationAndAngles((double) this.getXWithOffset(u, w) + 0.5D, (double) this.getYWithOffset(v) + 0.5D, (double) this.getZWithOffset(u, w) + 0.5D, random.nextFloat() * 360F, 0.0F);
			world.spawnEntityInWorld(entityvillager);

			// Up to two card traders in main area
			for (int i = 0; i < 2; i++) {
				if (random.nextBoolean()) {
					u = 5 + random.nextInt(2);
					v = 2;
					w = 3 + random.nextInt(3) + i * 3;

					entityvillager = new EntityVillager(world);
					entityvillager = Functions.makeVillagerWithProfession(world, random, VillagerHandler.ID_CARD_TRADER, 0);

					entityvillager.setLocationAndAngles((double) this.getXWithOffset(u, w) + 0.5D, (double) this.getYWithOffset(v) + 0.5D, (double) this.getZWithOffset(u, w) + 0.5D, random.nextFloat() * 360F, 0.0F);
					world.spawnEntityInWorld(entityvillager);
				}
			}
		}

		// Clean items
		Functions.cleanEntityItems(world, this.boundingBox);

		return true;
	}

	/**
	 * Returns the villager type to spawn in this component, based on the number
	 * of villagers already spawned.
	 */
	@Override
	protected int getVillagerType(int number) {
		return VillagerHandler.ID_CARD_MASTER;
	}
}

