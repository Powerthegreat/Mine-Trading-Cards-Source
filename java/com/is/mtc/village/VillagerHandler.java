package com.is.mtc.village;

import java.util.Random;

import com.is.mtc.MineTradingCards;

import cpw.mods.fml.common.registry.VillagerRegistry;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

public class VillagerHandler
		implements VillagerRegistry.IVillageTradeHandler {
	public static int TRADER_ID = 7117;

	public VillagerHandler() {
		VillagerRegistry vr = VillagerRegistry.instance();
		vr.registerVillagerId(TRADER_ID);
	}

	@Override
	public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList recipeList, Random random) {
		if (villager.getProfession() == TRADER_ID) {
			recipeList.clear();
			recipeList.addToListWithCheck(new MerchantRecipe(new ItemStack(Items.emerald, 1), new ItemStack(MineTradingCards.packCommon, random.nextInt(3) + 1)));
			recipeList.addToListWithCheck(new MerchantRecipe(new ItemStack(Items.emerald, random.nextInt(2) + 1), new ItemStack(MineTradingCards.packUncommon)));
			recipeList.addToListWithCheck(new MerchantRecipe(new ItemStack(Items.emerald, random.nextInt(4) + 3), new ItemStack(MineTradingCards.packRare)));
			recipeList.addToListWithCheck(new MerchantRecipe(new ItemStack(Items.emerald, 1), new ItemStack(MineTradingCards.packStandard, random.nextInt(2) + 1)));
			recipeList.addToListWithCheck(new MerchantRecipe(new ItemStack(Items.emerald, 1), new ItemStack(MineTradingCards.packEdition, random.nextInt(2) + 1)));
			recipeList.addToListWithCheck(new MerchantRecipe(new ItemStack(Items.emerald, random.nextInt(6) + 5), new ItemStack(MineTradingCards.packAncient)));
		}
	}
}

