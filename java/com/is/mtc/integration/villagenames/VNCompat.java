package com.is.mtc.integration.villagenames;

import java.util.ArrayList;
import java.util.Map;

import com.is.mtc.MineTradingCards;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public class VNCompat {
	
	public static Object[] getBiomeSpecificBlockObject(Block block, int meta, WorldChunkManager chunkManager, int bbCenterX, int bbCenterZ, BiomeGenBase biome) {
		
		if (MineTradingCards.hasVillageNamesInstalled
				&& astrotibs.villagenames.config.village.VillageGeneratorConfigHandler.newVillageGenerator) {
			
			// Determine MaterialType
			astrotibs.villagenames.utility.FunctionsVN.MaterialType materialType = astrotibs.villagenames.utility.FunctionsVN.MaterialType.OAK;
			Map<String, ArrayList<String>> mappedBiomes = astrotibs.villagenames.config.village.VillageGeneratorConfigHandler.unpackBiomes(
					astrotibs.villagenames.config.village.VillageGeneratorConfigHandler.spawnBiomesNames
					);
			
			// Determine whether mod subs are allowed
			boolean disallowModSubs = false;
			String mappeddisallowModSubs = (String) (mappedBiomes.get("DisallowModSubs")).get(mappedBiomes.get("BiomeNames").indexOf(biome.biomeName));
			if (mappeddisallowModSubs.toLowerCase().trim().equals("nosub")) {
				disallowModSubs = true;
			}
			
			try {
            	String mappedMaterialType = (String) (mappedBiomes.get("MaterialTypes")).get(mappedBiomes.get("BiomeNames").indexOf(biome.biomeName));
            	if (mappedMaterialType.equals("")) {
            		materialType = astrotibs.villagenames.utility.FunctionsVN.MaterialType.getMaterialTemplateForBiome(chunkManager, bbCenterX, bbCenterZ);
            	} else {
            		materialType = astrotibs.villagenames.utility.FunctionsVN.MaterialType.getMaterialTypeFromName(mappedMaterialType, astrotibs.villagenames.utility.FunctionsVN.MaterialType.OAK);
            	}
            }
			catch (Exception e) {
				materialType = astrotibs.villagenames.utility.FunctionsVN.MaterialType.getMaterialTemplateForBiome(chunkManager, bbCenterX, bbCenterZ);
			}
			
			return astrotibs.villagenames.village.StructureVillageVN.getBiomeSpecificBlockObject(block, meta, materialType, biome, disallowModSubs);
		} else {
			return new Object[] {block, meta};
		}
	}
	
	public static int[] getTownColorsVN(World world, StructureBoundingBox boundingBox, int color1, int color2, int color3, int color4, int color5, int color6, int color7) {
		
		int[] color_a = new int[] {color1, color2, color3, color4, color5, color6, color7};
		
		if (MineTradingCards.hasVillageNamesInstalled
				&& astrotibs.villagenames.config.village.VillageGeneratorConfigHandler.newVillageGenerator
				&& astrotibs.villagenames.config.GeneralConfig.useVillageColors) {
			
			NBTTagCompound villageNBTtag = astrotibs.villagenames.village.StructureVillageVN.getOrMakeVNInfo(world, 
        			(boundingBox.minX+boundingBox.maxX)/2,
        			(boundingBox.minY+boundingBox.maxY)/2,
        			(boundingBox.minZ+boundingBox.maxZ)/2);
			
        	// Load the values of interest into memory
			color_a = new int[] {
					villageNBTtag.getInteger("townColor"),
					villageNBTtag.getInteger("townColor2"),
					villageNBTtag.getInteger("townColor3"),
					villageNBTtag.getInteger("townColor4"),
					villageNBTtag.getInteger("townColor5"),
					villageNBTtag.getInteger("townColor6"),
					villageNBTtag.getInteger("townColor7")
					};
		}
		
		return color_a;
	}
	
	// Wooden table (Vanilla is a fence with a pressure plate on top)
	public static Object[][] chooseModWoodenTable(int materialMeta)
	{
		if (MineTradingCards.hasVillageNamesInstalled
				&& astrotibs.villagenames.config.village.VillageGeneratorConfigHandler.newVillageGenerator) {
			return astrotibs.villagenames.integration.ModObjects.chooseModWoodenTable(materialMeta);
		}
		else {
			return new Object[][] {
				new Object[] {Blocks.wooden_pressure_plate, 0},
				new Object[] {Blocks.fence, 0},
				};
		}
	}
}
