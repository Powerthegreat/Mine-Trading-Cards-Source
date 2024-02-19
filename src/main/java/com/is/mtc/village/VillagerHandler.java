package com.is.mtc.village;

import com.is.mtc.MineTradingCards;
import com.is.mtc.card.CardItem;
import com.is.mtc.data_manager.CardStructure;
import com.is.mtc.data_manager.Databank;
import com.is.mtc.root.Logs;
import com.is.mtc.root.Rarity;
import com.is.mtc.util.Reference;
import cpw.mods.fml.common.registry.VillagerRegistry;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;

import javax.annotation.Nullable;
import java.util.Random;

public class VillagerHandler
		implements VillagerRegistry.IVillageTradeHandler {
	public static final String[] CARD_MASTER_TRADE_LIST_DEFAULT = new String[]{
			// Trades that existed in previous versions
//			"1.0|common_pack|1-3|emerald|1",
//			"1.0|standard_pack|1-2|emerald|1",
//			"1.0|edition_pack|1-2|emerald|1",
//			"1.0|uncommon_pack|1|emerald|1-2",
//			"1.0|rare_pack|1|emerald|3-6",
//			"1.0|ancient_pack|1|emerald|5-10",

			// Sell either packs for emeralds,
			// or a single pre-generated card for something that may or may not be emeralds.
			"1.0|common_pack|1|emerald|1",
			"1.0|standard_pack|1|emerald|2",
			"1.0|common_card_random|1|iron_ingot|2-4",
			"0.9|edition_pack|1|emerald|2|iron_ingot|0-1",
			"1.0|uncommon_pack|1|emerald|2|gold_ingot|0-1",
			"0.8|uncommon_card_random|1|gold_ingot|2-3",
			"1.0|rare_pack|1|emerald|3-6",
			"0.7|rare_card_random|1|emerald|2-3",
			"1.0|ancient_pack|1|emerald|16-21",
			"0.6|ancient_card_random|1|emerald|18-24",
			"0.5|legendary_pack|1|emerald|53-64",
			"0.5|legendary_card_random|1|diamond|16-21",
	};
	public static final String[] CARD_TRADER_TRADE_LIST_DEFAULT = new String[]{
			// Trade either a specific card for an arbitrary card of that same level,
			// or a non-generated card for two arbitrary cards of that level.
			"1.0|common_card_random|1|common_card|1",
			"0.5|common_card|1|common_card|1|common_card|1",
			"1.0|uncommon_card_random|1|uncommon_card|1",
			"0.4|uncommon_card|1|uncommon_card|1|uncommon_card|1",
			"1.0|rare_card_random|1|rare_card|1",
			"0.3|rare_card|1|rare_card|1|rare_card|1",
			"1.0|ancient_card_random|1|ancient_card|1",
			"0.2|ancient_card|1|ancient_card|1|ancient_card|1",
			"0.5|legendary_card_random|1|legendary_card|1",
	};
	// Indices used to disassemble trade list
	private static final int INDEX_TRADECHANCE = 0;
	private static final int INDEX_SELLITEM = 1;
	private static final int INDEX_SELLITEM_AMOUNT = 2;
	private static final int INDEX_BUYITEM1 = 3;
	private static final int INDEX_BUYITEM1_AMOUNT = 4;
	private static final int INDEX_BUYITEM2 = 5;
	private static final int INDEX_BUYITEM2_AMOUNT = 6;
	public static int ID_CARD_MASTER = 7117;
	public static int ID_CARD_TRADER = 7118;
	public static String[] CARD_MASTER_TRADE_LIST = CARD_MASTER_TRADE_LIST_DEFAULT;
	public static String[] CARD_TRADER_TRADE_LIST = CARD_TRADER_TRADE_LIST_DEFAULT;
	public VillagerHandler(int id) {

		VillagerRegistry vr = VillagerRegistry.instance();
		vr.registerVillagerId(id);
	}

	private static final void warnAboutTradeDiscriminationBug(String config_entry, String specificconfigvalue) {
		Logs.errLog("WARNING: \"_random\" is not supported for " + specificconfigvalue + " in " + config_entry + ", because requested items can't discriminate between meta or NBT values in 1.7!");
	}

	@Nullable
	private static ItemStack getItemStackFromKeyName(String item_key, int count, Random random) {

		item_key = item_key.toLowerCase().trim();

		boolean is_random = item_key.endsWith("_random");
		Item item = null;
		int rarity = -1;

		// Vanilla stuff
		if (item_key.equals("iron_ingot")) {
			item = Items.iron_ingot;
		} else if (item_key.equals("gold_ingot")) {
			item = Items.gold_ingot;
		} else if (item_key.equals("emerald")) {
			item = Items.emerald;
		} else if (item_key.equals("diamond")) {
			item = Items.diamond;
		}
		// Packs
		else if (item_key.equals(Reference.KEY_PACK_COM)) {
			item = MineTradingCards.packCommon;
		} else if (item_key.equals(Reference.KEY_PACK_UNC)) {
			item = MineTradingCards.packUncommon;
		} else if (item_key.equals(Reference.KEY_PACK_RAR)) {
			item = MineTradingCards.packRare;
		} else if (item_key.equals(Reference.KEY_PACK_ANC)) {
			item = MineTradingCards.packAncient;
		} else if (item_key.equals(Reference.KEY_PACK_LEG)) {
			item = MineTradingCards.packLegendary;
		} else if (item_key.equals(Reference.KEY_PACK_STD)) {
			item = MineTradingCards.packStandard;
		} else if (item_key.equals(Reference.KEY_PACK_EDT)) {
			item = MineTradingCards.packEdition;
		} else if (item_key.equals(Reference.KEY_PACK_CUS)) {
			item = MineTradingCards.packCustom;
		}
		// Cards
		else if (item_key.equals(Reference.KEY_CARD_COM) || item_key.equals(Reference.KEY_CARD_COM + "_random")) {
			item = MineTradingCards.cardCommon;
			rarity = Rarity.COMMON;
		} else if (item_key.equals(Reference.KEY_CARD_UNC) || item_key.equals(Reference.KEY_CARD_UNC + "_random")) {
			item = MineTradingCards.cardUncommon;
			rarity = Rarity.UNCOMMON;
		} else if (item_key.equals(Reference.KEY_CARD_RAR) || item_key.equals(Reference.KEY_CARD_RAR + "_random")) {
			item = MineTradingCards.cardRare;
			rarity = Rarity.RARE;
		} else if (item_key.equals(Reference.KEY_CARD_ANC) || item_key.equals(Reference.KEY_CARD_ANC + "_random")) {
			item = MineTradingCards.cardAncient;
			rarity = Rarity.ANCIENT;
		} else if (item_key.equals(Reference.KEY_CARD_LEG) || item_key.equals(Reference.KEY_CARD_LEG + "_random")) {
			item = MineTradingCards.cardLegendary;
			rarity = Rarity.LEGENDARY;
		}

		if (item == null) {
			return null;
		}

		ItemStack returnstack = new ItemStack(item, count);

		// Turn card into specific type
		if (is_random) {
			CardStructure cStruct = Databank.generateACard(rarity, new Random()); // Using new Random() because world random can cause issues generating cards
			if (cStruct != null) {
				returnstack.stackTagCompound = new NBTTagCompound();
				returnstack = CardItem.applyCDWDtoStack(returnstack, cStruct, random);
			}
		}

		return returnstack;
	}

	private static MerchantRecipe generateTradeFromConfigEntry(String config_entry, Random random) {

		try {
			String[] split_config_entry = config_entry.toLowerCase().trim().split("\\|");

			// Skip this trade probabilistically
			float trade_probability = MathHelper.clamp_float(Float.valueOf(split_config_entry[INDEX_TRADECHANCE]), 0F, 1F);
			if (random.nextFloat() >= trade_probability) {
				if (trade_probability == 0F) {
					Logs.errLog("tradechance is 0 for " + config_entry + ", so trade will never generate!");
				}
				return null;
			}

			boolean use_second_buy_item = split_config_entry.length > INDEX_BUYITEM2;

			// Selling item stuff
			String sellitem = split_config_entry[INDEX_SELLITEM];
			String[] sellitem_range = split_config_entry[INDEX_SELLITEM_AMOUNT].trim().split("-");
			int sellamt_low = Integer.valueOf(sellitem_range[0]);
			int sellamt_high = Integer.valueOf(sellitem_range[sellitem_range.length > 1 ? 1 : 0]);
			int sellamt_randomized = MathHelper.clamp_int(sellamt_low + (sellamt_high > sellamt_low ? random.nextInt(sellamt_high - sellamt_low + 1) : 0), 0, 64);
			ItemStack sellitem_stack = sellamt_randomized == 0 ? null : getItemStackFromKeyName(sellitem, sellamt_randomized, random);

			// Buying item (1) stuff
			String buyitem1 = split_config_entry[INDEX_BUYITEM1];
			int buyitem1_random_ind = buyitem1.indexOf("_random");
			if (buyitem1_random_ind != -1) {
				warnAboutTradeDiscriminationBug(config_entry, "buyitem1");
				buyitem1 = buyitem1.substring(0, buyitem1_random_ind);
			}
			String[] buyitem1_range = split_config_entry[INDEX_BUYITEM1_AMOUNT].trim().split("-");
			int buyamt1_low = Integer.valueOf(buyitem1_range[0]);
			int buyamt1_high = Integer.valueOf(buyitem1_range[buyitem1_range.length > 1 ? 1 : 0]);
			int buyamt1_randomized = MathHelper.clamp_int(buyamt1_low + (buyamt1_high > buyamt1_low ? random.nextInt(buyamt1_high - buyamt1_low + 1) : 0), 0, 64);
			ItemStack buyitem1_stack = buyamt1_randomized == 0 ? null : getItemStackFromKeyName(buyitem1, buyamt1_randomized, random);

			// Buying item (2) stuff
			ItemStack buyitem2_stack = null;
			String buyitem2 = "";
			if (use_second_buy_item) {
				buyitem2 = split_config_entry[INDEX_BUYITEM2];
				int buyitem2_random_ind = buyitem2.indexOf("_random");
				if (buyitem2_random_ind != -1) {
					warnAboutTradeDiscriminationBug(config_entry, "buyitem2");
					buyitem2 = buyitem2.substring(0, buyitem2_random_ind);
				}
				String[] buyitem2_range = split_config_entry[INDEX_BUYITEM2_AMOUNT].trim().split("-");
				int buyamt2_low = Integer.valueOf(buyitem2_range[0]);
				int buyamt2_high = Integer.valueOf(buyitem2_range[buyitem2_range.length > 1 ? 1 : 0]);
				int buyamt2_randomized = MathHelper.clamp_int(buyamt2_low + (buyamt2_high > buyamt2_low ? random.nextInt(buyamt2_high - buyamt2_low + 1) : 0), 0, 64);
				buyitem2_stack = buyamt2_randomized == 0 ? null : getItemStackFromKeyName(buyitem2, buyamt2_randomized, random);
			}

			// Return null if there are malformations
			if (sellamt_randomized == 0) {
				Logs.errLog("Skipping registering villager trade " + config_entry + " because the sellitem stack size rolled a zero!");
				return null;
			}
			if (sellitem_stack == null) {
				Logs.errLog("Skipping registering villager trade " + config_entry + " because sellitem is invalid!");
				return null;
			}
			if (buyitem1_stack == null) {
				if (buyitem2_stack == null) {
					Logs.errLog("Skipping registering villager trade " + config_entry + " because both buyitems are invalid and/or rolled a stack size of zero!");
					return null;
				} else {
					Logs.errLog("buyitem1 " + buyitem1 + " is invalid in " + config_entry + ", so using " + buyitem2_stack + " instead.");
					return new MerchantRecipe(buyitem2_stack, sellitem_stack);
				}
			}

			// Return the proper MerchantRecipe
			if (buyitem2_stack == null) {
				return new MerchantRecipe(buyitem1_stack, sellitem_stack);
			} else {
				return new MerchantRecipe(buyitem1_stack, buyitem2_stack, sellitem_stack);
			}

		} catch (Exception e) {
			Logs.errLog("Skipping Item buy1, EntityVillager.PriceInfo buy1Number, Item sell, EntityVillager.PriceInfo sellNumbervillager trade " + config_entry + " because something went wrong! Check your formatting.");
			return null;
		}
	}

	@Override
	public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList recipeList, Random random) {
		if (villager.getProfession() == ID_CARD_MASTER) {

			// Iterate through config entries and add them as trades
			for (int i = 0; i < CARD_MASTER_TRADE_LIST.length; i++) {
				MerchantRecipe card_villager_trade = generateTradeFromConfigEntry(CARD_MASTER_TRADE_LIST[i], random);
				if (card_villager_trade != null) {
					recipeList.addToListWithCheck(card_villager_trade);
				}
			}
		} else if (villager.getProfession() == ID_CARD_TRADER) {

			// Iterate through config entries and add them as trades
			for (int i = 0; i < CARD_TRADER_TRADE_LIST.length; i++) {
				MerchantRecipe card_villager_trade = generateTradeFromConfigEntry(CARD_TRADER_TRADE_LIST[i], random);
				if (card_villager_trade != null) {
					recipeList.addToListWithCheck(card_villager_trade);
				}
			}
		}
	}
}

