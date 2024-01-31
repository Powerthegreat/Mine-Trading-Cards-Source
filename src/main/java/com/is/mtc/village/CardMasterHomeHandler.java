package com.is.mtc.village;

import java.util.List;
import java.util.Random;

import cpw.mods.fml.common.registry.VillagerRegistry;
import net.minecraft.world.gen.structure.StructureVillagePieces;

public class CardMasterHomeHandler implements VillagerRegistry.IVillageCreationHandler {

	public static int SHOP_WEIGHT = 5;
	public static int SHOP_MAX_NUMBER = 1;

	@Override
	public StructureVillagePieces.PieceWeight getVillagePieceWeight(Random random, int villageSize) {
		return new StructureVillagePieces.PieceWeight(CardMasterHome.class, SHOP_WEIGHT, SHOP_MAX_NUMBER);
	}

	@Override
	public Class<?> getComponentClass() {
		return CardMasterHome.class;
	}

	@Override
	public Object buildComponent(StructureVillagePieces.PieceWeight villagePiece, StructureVillagePieces.Start startPiece, List pieces, Random random, int x, int y, int z, int horizIndex, int componentType) {
		return CardMasterHome.buildComponent(startPiece, pieces, random, x, y, z, horizIndex, componentType);
	}
}