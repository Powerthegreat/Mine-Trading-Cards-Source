package com.is.mtc;

import java.io.File;

import com.is.mtc.binder.BinderItem;
import com.is.mtc.card.CardItem;
import com.is.mtc.data_manager.DataLoader;
import com.is.mtc.data_manager.Databank;
import com.is.mtc.displayer.DisplayerBlock;
import com.is.mtc.displayer.DisplayerBlockTileEntity;
import com.is.mtc.displayer_mono.MonoDisplayerBlock;
import com.is.mtc.displayer_mono.MonoDisplayerBlockTileEntity;
import com.is.mtc.handler.DropHandler;
import com.is.mtc.handler.GuiHandler;
import com.is.mtc.pack.PackItemBase;
import com.is.mtc.pack.PackItemCustom;
import com.is.mtc.pack.PackItemEdition;
import com.is.mtc.pack.PackItemRarity;
import com.is.mtc.pack.PackItemStandard;
import com.is.mtc.packet.MTCMessage;
import com.is.mtc.packet.MTCMessageHandler;
import com.is.mtc.proxy.CommonProxy;
import com.is.mtc.root.CC_CreateCard;
import com.is.mtc.root.CC_ForceCreateCard;
import com.is.mtc.root.Injector;
import com.is.mtc.root.Logs;
import com.is.mtc.root.Rarity;
import com.is.mtc.util.Functions;
import com.is.mtc.util.Reference;
import com.is.mtc.version.DevVersionWarning;
import com.is.mtc.version.VersionChecker;
import com.is.mtc.village.CardMasterHome;
import com.is.mtc.village.CardMasterHomeHandler;
import com.is.mtc.village.VillagerHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.ShapedOreRecipe;

@Mod(modid = Reference.MODID, version = Reference.MOD_VERSION, name = Reference.NAME)
public class MineTradingCards {
	// The instance of the mod class that forge uses
	@Instance(Reference.MODID)
	public static MineTradingCards INSTANCE;

	// Whether the proxy is remote
	public static boolean PROXY_IS_REMOTE = false;
	
	// Cards, packs, binders and display blocks to be registered
	public static CardItem cardCommon, cardUncommon, cardRare, cardAncient, cardLegendary;
	public static PackItemBase packCommon, packUncommon, packRare, packAncient, packLegendary, packStandard, packEdition, packCustom; // Common (com), unccommon (unc), rare (rar), ancient (anc), legendary (leg), standard (std), edition (edt)

	public static BinderItem binder;
	public static DisplayerBlock displayerBlock;
	public static MonoDisplayerBlock monoDisplayerBlock;

	// The directories that MTC works with
	private static String DATA_DIR = "";
	private static String CONF_DIR = "";
	
	// Configuration stuff
	public static final String CONFIG_CAT_COLORS = "colors";
	public static final String CONFIG_CAT_DROPS = "drops";
	public static final String CONFIG_CAT_LOGS = "logs";
	public static final String CONFIG_CAT_RECIPES = "recipes";
	public static final String CONFIG_CAT_UPDATES = "updates";
	public static final String CONFIG_CAT_VILLAGERS = "villagers";
	
	public static boolean ENABLE_CARD_RECIPES = true;
	public static int CARD_COLOR_COMMON = Reference.COLOR_GREEN;
	public static int CARD_COLOR_UNCOMMON = Reference.COLOR_GOLD;
	public static int CARD_COLOR_RARE = Reference.COLOR_RED;
	public static int CARD_COLOR_ANCIENT = Reference.COLOR_AQUA;
	public static int CARD_COLOR_LEGENDARY = Reference.COLOR_LIGHT_PURPLE;
	public static String CARD_TOOLTIP_COLOR_COMMON = "green";
	public static String CARD_TOOLTIP_COLOR_UNCOMMON = "gold";
	public static String CARD_TOOLTIP_COLOR_RARE = "red";
	public static String CARD_TOOLTIP_COLOR_ANCIENT = "aqua";
	public static String CARD_TOOLTIP_COLOR_LEGENDARY = "light_purple";
	public static int PACK_COLOR_COMMON = Reference.COLOR_GREEN;
	public static int PACK_COLOR_UNCOMMON = Reference.COLOR_GOLD;
	public static int PACK_COLOR_RARE = Reference.COLOR_RED;
	public static int PACK_COLOR_ANCIENT = Reference.COLOR_AQUA;
	public static int PACK_COLOR_LEGENDARY = Reference.COLOR_LIGHT_PURPLE;
	public static int PACK_COLOR_STANDARD = Reference.COLOR_BLUE;
	public static boolean ENABLE_UPDATE_CHECKER = true;
	
	public static final String COLOR_ITEM_DESCRIPTION_1 = "Color for ";
	public static final String COLOR_ITEM_DESCRIPTION_2 = "Entered as a decimal integer, or as a hexadecimal by putting # in front.";
	public static final String COLOR_TOOLTIP_1 = "Tooltip color for ";
	public static final String COLOR_TOOLTIP_2 = " cards, using \"friendly\" Minecraft color name";
	
	// Mod intercompatibility stuff
	public static boolean hasVillageNamesInstalled = false;
	
	// The proxy, either a combined client or a dedicated server
	@SidedProxy(clientSide = "com.is.mtc.proxy.ClientProxy", serverSide = "com.is.mtc.proxy.ServerProxy")
	public static CommonProxy PROXY;
	public static SimpleNetworkWrapper simpleNetworkWrapper; // The network wrapper for the mod

	// The creative tab that the mod uses
	public static CreativeTabs MODTAB = new CreativeTabs("tab_mtc") {
		@Override
		public Item getTabIconItem() {
			return MineTradingCards.packStandard;
		}
	};
	//-

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		// Gets the config and reads the cards, and runs the preinitialisation from the proxy
		DATA_DIR = event.getModConfigurationDirectory().getParentFile().getAbsolutePath().replace('\\', '/') + "/mtc/";
		CONF_DIR = event.getModConfigurationDirectory().getAbsolutePath().replace('\\', '/') + '/';
		
        // Version check monitor
        if (Reference.MOD_VERSION.contains("DEV") || Reference.MOD_VERSION.equals("@VERSION@")) {FMLCommonHandler.instance().bus().register(DevVersionWarning.instance);}
        else if (ENABLE_UPDATE_CHECKER) {FMLCommonHandler.instance().bus().register(VersionChecker.instance);}
		
		PROXY.preInit(event);
		readConfig(event);

		Databank.setup();
		DataLoader.readAndLoad();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		// Runs the initialisation from the proxy, then defines the items and blocks
		PROXY.init(event);

		cardCommon = new CardItem(Rarity.COMMON);
		cardUncommon = new CardItem(Rarity.UNCOMMON);
		cardRare = new CardItem(Rarity.RARE);
		cardAncient = new CardItem(Rarity.ANCIENT);
		cardLegendary = new CardItem(Rarity.LEGENDARY);

		packCommon = new PackItemRarity(Rarity.COMMON);
		packUncommon = new PackItemRarity(Rarity.UNCOMMON);
		packRare = new PackItemRarity(Rarity.RARE);
		packAncient = new PackItemRarity(Rarity.ANCIENT);
		packLegendary = new PackItemRarity(Rarity.LEGENDARY);

		packStandard = new PackItemStandard();
		packEdition = new PackItemEdition();
		packCustom = new PackItemCustom();

		binder = new BinderItem();
		displayerBlock = new DisplayerBlock();
		monoDisplayerBlock = new MonoDisplayerBlock();
		
		// Mod intercompat stuff
        if (Loader.isModLoaded(Reference.VILLAGE_NAMES_MODID)) {
        	hasVillageNamesInstalled = true;
        }
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		// Runs the postinitialisation from the proxy, then registers the items and blocks
		PROXY.postInit(event);

		Injector.registerItem(cardCommon);
		Injector.registerItem(cardUncommon);
		Injector.registerItem(cardRare);
		Injector.registerItem(cardAncient);
		Injector.registerItem(cardLegendary);

		Injector.registerItem(packCommon);
		Injector.registerItem(packUncommon);
		Injector.registerItem(packRare);
		Injector.registerItem(packAncient);
		Injector.registerItem(packLegendary);

		Injector.registerItem(packStandard);
		Injector.registerItem(packEdition);
		Injector.registerItem(packCustom);

		Injector.registerItem(binder);
		Injector.registerBlock(displayerBlock);
		Injector.registerBlock(monoDisplayerBlock);

		// Sets up the network wrapper
		simpleNetworkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MODID);
		simpleNetworkWrapper.registerMessage(MTCMessageHandler.class, MTCMessage.class, 0, Side.SERVER);

		// Sets up the gui and drop handlers
		MinecraftForge.EVENT_BUS.register(new DropHandler());
		NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new GuiHandler());

		// Registers tile entities
		GameRegistry.registerTileEntity(DisplayerBlockTileEntity.class, "tile_entity_displayer");
		GameRegistry.registerTileEntity(MonoDisplayerBlockTileEntity.class, "tile_entity_monodisplayer");

		// Adds recipes
		GameRegistry.addRecipe(new ItemStack(displayerBlock), "IGI", "GgG", "IGI", 'I', Items.iron_ingot, 'G', Blocks.glass, 'g', Blocks.glowstone);

		GameRegistry.addRecipe(new ItemStack(monoDisplayerBlock, 4), "IWI", "WgW", "IGI", 'I', Items.iron_ingot, 'G', Blocks.glass, 'g', Blocks.glowstone, 'W', Blocks.planks);

		GameRegistry.addShapelessRecipe(new ItemStack(binder), Items.book, cardCommon);
		
		if (ENABLE_CARD_RECIPES) {
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cardCommon), "mmm", "ppp", "bbb", 'm', "dyeWhite", 'p', Items.paper, 'b', "dyeBlack"));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cardUncommon), "mmm", "pip", "bbb", 'm', "dyeWhite", 'p', Items.paper, 'b', "dyeBlack", 'i', "ingotIron"));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cardRare), "mmm", "pgp", "bbb", 'm', "dyeWhite", 'p', Items.paper, 'b', "dyeBlack", 'g', "ingotGold"));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cardAncient), "mmm", "pdp", "bbb", 'm', "dyeWhite", 'p', Items.paper, 'b', "dyeBlack", 'd', "gemDiamond"));
			GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(cardLegendary), "mmm", "pDp", "bbb", 'm', "dyeWhite", 'p', Items.paper, 'b', "dyeBlack", 'D', "blockDiamond"));	
		}

		MapGenStructureIO.func_143031_a(CardMasterHome.class, "Mtc_Cm_House"); // Register the house to the generator with a typed id
		// Registers the Card Master villager's trades, and the creation handler for its home
		VillagerRegistry.instance().registerVillageTradeHandler(VillagerHandler.ID_CARD_MASTER, new VillagerHandler(VillagerHandler.ID_CARD_MASTER));
		VillagerRegistry.instance().registerVillageTradeHandler(VillagerHandler.ID_CARD_TRADER, new VillagerHandler(VillagerHandler.ID_CARD_TRADER));
		VillagerRegistry.instance().registerVillageCreationHandler(new CardMasterHomeHandler());
	}

	@EventHandler
	public void serverLoad(FMLServerStartingEvent event) {
		// Registers the cards on a server
		event.registerServerCommand(new CC_CreateCard());
		event.registerServerCommand(new CC_ForceCreateCard());
	}
	
	private void readConfig(FMLPreInitializationEvent event) {
		// Loads from the configuration file
		Configuration config = new Configuration(new File(CONF_DIR, "Mine Trading Cards.cfg"), Reference.CONFIG_VERSION, false);
		config.load();
		
		// Colors
		// Cards
		CARD_COLOR_COMMON = Functions.parseColorInteger(config.getString("card_color_common", CONFIG_CAT_COLORS, "#55ff55", COLOR_ITEM_DESCRIPTION_1+"common cards. "+COLOR_ITEM_DESCRIPTION_2).trim(), Reference.COLOR_GREEN);
		CARD_COLOR_UNCOMMON = Functions.parseColorInteger(config.getString("card_color_uncommon", CONFIG_CAT_COLORS, "#ffaa00", COLOR_ITEM_DESCRIPTION_1+"uncommon cards. "+COLOR_ITEM_DESCRIPTION_2).trim(), Reference.COLOR_GOLD);
		CARD_COLOR_RARE = Functions.parseColorInteger(config.getString("card_color_rare", CONFIG_CAT_COLORS, "#ff5555", COLOR_ITEM_DESCRIPTION_1+"rare cards. "+COLOR_ITEM_DESCRIPTION_2).trim(), Reference.COLOR_RED);
		CARD_COLOR_ANCIENT = Functions.parseColorInteger(config.getString("card_color_ancient", CONFIG_CAT_COLORS, "#55ffff", COLOR_ITEM_DESCRIPTION_1+"ancient cards. "+COLOR_ITEM_DESCRIPTION_2).trim(), Reference.COLOR_AQUA);
		CARD_COLOR_LEGENDARY = Functions.parseColorInteger(config.getString("card_color_legendary", CONFIG_CAT_COLORS, "#ff55ff", COLOR_ITEM_DESCRIPTION_1+"legendary cards. "+COLOR_ITEM_DESCRIPTION_2).trim(), Reference.COLOR_LIGHT_PURPLE);
		// Tooltips
		CARD_TOOLTIP_COLOR_COMMON = config.getString("card_tooltip_color_common", CONFIG_CAT_COLORS, "green", COLOR_TOOLTIP_1+"common"+COLOR_TOOLTIP_2);
		CARD_TOOLTIP_COLOR_UNCOMMON = config.getString("card_tooltip_color_uncommon", CONFIG_CAT_COLORS, "gold", COLOR_TOOLTIP_1+"uncommon"+COLOR_TOOLTIP_2);
		CARD_TOOLTIP_COLOR_RARE = config.getString("card_tooltip_color_rare", CONFIG_CAT_COLORS, "red", COLOR_TOOLTIP_1+"rare"+COLOR_TOOLTIP_2);
		CARD_TOOLTIP_COLOR_ANCIENT = config.getString("card_tooltip_color_ancient", CONFIG_CAT_COLORS, "aqua", COLOR_TOOLTIP_1+"ancient"+COLOR_TOOLTIP_2);
		CARD_TOOLTIP_COLOR_LEGENDARY = config.getString("card_tooltip_color_legendary", CONFIG_CAT_COLORS, "light_purple", COLOR_TOOLTIP_1+"legendary"+COLOR_TOOLTIP_2);
		// Packs
		PACK_COLOR_COMMON = Functions.parseColorInteger(config.getString("pack_color_common", CONFIG_CAT_COLORS, "#55ff55", COLOR_ITEM_DESCRIPTION_1+"common packs. "+COLOR_ITEM_DESCRIPTION_2).trim(), Reference.COLOR_GREEN);
		PACK_COLOR_UNCOMMON = Functions.parseColorInteger(config.getString("pack_color_uncommon", CONFIG_CAT_COLORS, "#ffaa00", COLOR_ITEM_DESCRIPTION_1+"uncommon packs. "+COLOR_ITEM_DESCRIPTION_2).trim(), Reference.COLOR_GOLD);
		PACK_COLOR_RARE = Functions.parseColorInteger(config.getString("pack_color_rare", CONFIG_CAT_COLORS, "#ff5555", COLOR_ITEM_DESCRIPTION_1+"rare packs. "+COLOR_ITEM_DESCRIPTION_2).trim(), Reference.COLOR_RED);
		PACK_COLOR_ANCIENT = Functions.parseColorInteger(config.getString("pack_color_ancient", CONFIG_CAT_COLORS, "#55ffff", COLOR_ITEM_DESCRIPTION_1+"ancient packs. "+COLOR_ITEM_DESCRIPTION_2).trim(), Reference.COLOR_AQUA);
		PACK_COLOR_LEGENDARY = Functions.parseColorInteger(config.getString("pack_color_legendary", CONFIG_CAT_COLORS, "#ff55ff", COLOR_ITEM_DESCRIPTION_1+"legendary packs. "+COLOR_ITEM_DESCRIPTION_2).trim(), Reference.COLOR_LIGHT_PURPLE);
		PACK_COLOR_STANDARD = Functions.parseColorInteger(config.getString("pack_color_standard", CONFIG_CAT_COLORS, "#5555ff", COLOR_ITEM_DESCRIPTION_1+"standard packs. "+COLOR_ITEM_DESCRIPTION_2).trim(), Reference.COLOR_BLUE);
		
		// Drops toggle
		DropHandler.CAN_DROP_MOB = config.getBoolean("mobs_can_drop", CONFIG_CAT_DROPS, true, "Can mobs drop packs on death");
		DropHandler.CAN_DROP_ANIMAL = config.getBoolean("animals_can_drop", CONFIG_CAT_DROPS, false, "Can animals drop packs on death");
		DropHandler.CAN_DROP_PLAYER = config.getBoolean("players_can_drop", CONFIG_CAT_DROPS, false, "Can players drop packs on death");
		// Tiered pack drop rates
		DropHandler.DROP_RATE_COM = config.getInt("pack_drop_rate_common", CONFIG_CAT_DROPS, 16, 0, Integer.MAX_VALUE, "Chance out of X to drop common packs");
		DropHandler.DROP_RATE_UNC = config.getInt("pack_drop_rate_uncommon", CONFIG_CAT_DROPS, 32, 0, Integer.MAX_VALUE, "Chance out of X to drop uncommon packs");
		DropHandler.DROP_RATE_RAR = config.getInt("pack_drop_rate_rare", CONFIG_CAT_DROPS, 48, 0, Integer.MAX_VALUE, "Chance out of X to drop rare packs");
		DropHandler.DROP_RATE_ANC = config.getInt("pack_drop_rate_ancient", CONFIG_CAT_DROPS, 64, 0, Integer.MAX_VALUE, "Chance out of X to drop ancient packs");
		DropHandler.DROP_RATE_LEG = config.getInt("pack_drop_rate_legendary", CONFIG_CAT_DROPS, 256, 0, Integer.MAX_VALUE, "Chance out of X to drop legendary packs");
		// Non-tiered pack drop rates
		DropHandler.DROP_RATE_STD = config.getInt("pack_drop_rate_standard", CONFIG_CAT_DROPS, 40, 0, Integer.MAX_VALUE, "Chance out of X to drop standard packs");
		DropHandler.DROP_RATE_EDT = config.getInt("pack_drop_rate_edition", CONFIG_CAT_DROPS, 40, 0, Integer.MAX_VALUE, "Chance out of X to drop set-specific (edition) packs");
		DropHandler.DROP_RATE_CUSTOM = config.getInt("pack_drop_rate_custom", CONFIG_CAT_DROPS, 40, 0, Integer.MAX_VALUE, "Chance out of X to drop custom packs");
		
		// Logging
		Logs.ENABLE_DEV_LOGS = config.getBoolean("devlog_enabled", CONFIG_CAT_LOGS, false, "Enable developer logs");
		
		// Recipes
		ENABLE_CARD_RECIPES = config.getBoolean("enable_card_recipes", CONFIG_CAT_RECIPES, true, "Enable recipes for crafting individual cards");
		
		// Villager
		VillagerHandler.ID_CARD_MASTER = config.getInt("card_master_id", CONFIG_CAT_VILLAGERS, 7117, 6, Integer.MAX_VALUE, "Profession ID for the card master villager");
		VillagerHandler.ID_CARD_TRADER = config.getInt("card_trader_id", CONFIG_CAT_VILLAGERS, 7118, 6, Integer.MAX_VALUE, "Profession ID for the card trader villager");
		VillagerHandler.CARD_MASTER_TRADE_LIST = config.getStringList("card_master_trades", CONFIG_CAT_VILLAGERS, VillagerHandler.CARD_MASTER_TRADE_LIST_DEFAULT,
				"List of possible Card Master trades. Entries are of the form:"
				+ "\ntradechance|sellitem|amount|buyitem1|amount|buyitem2|amount"
				+ "\ntradechance is a float from 0 to 1 representing the chance the trade will be considered when adding new trades to a villager."
				+ "\nPossible sellitem and buyitem values are:"
				+ "\niron_ingot, gold_ingot, emerald, diamond, [common/uncommon/rare/ancient/legendary/standard/edition/custom]_pack or [common/uncommon/rare/ancient/legendary]_card."
				+ "\nYou also append \"_random\" at the end of a _card sellitem entry (e.g. common_card_random) in order to generate a randomly-generated card for sale."
				+ "\n\"amount\" is either an integer, or a range like 1-3."
				+ "\nbuyitem2|amount is optional."
				);
		VillagerHandler.CARD_TRADER_TRADE_LIST = config.getStringList("card_trader_trades", CONFIG_CAT_VILLAGERS, VillagerHandler.CARD_TRADER_TRADE_LIST_DEFAULT,
				"List of possible Card Trader trades. Entries are of the form:"
				+ "\ntradechance|sellitem|amount|buyitem1|amount|buyitem2|amount"
				+ "\ntradechance is a float from 0 to 1 representing the chance the trade will be considered when adding new trades to a villager."
				+ "\nPossible sellitem and buyitem values are:"
				+ "\niron_ingot, gold_ingot, emerald, diamond, [common/uncommon/rare/ancient/legendary/standard/edition/custom]_pack or [common/uncommon/rare/ancient/legendary]_card."
				+ "\nYou also append \"_random\" at the end of a _card sellitem entry (e.g. common_card_random) in order to generate a randomly-generated card for sale."
				+ "\n\"amount\" is either an integer, or a range like 1-3."
				+ "\nbuyitem2|amount is optional."
				);
		CardMasterHomeHandler.SHOP_WEIGHT = config.getInt("card_shop_weight", CONFIG_CAT_VILLAGERS, 5, 0, 100, "Weighting for selection when villages generate. Farms and wood huts are 3, church is 20.");
		CardMasterHomeHandler.SHOP_MAX_NUMBER = config.getInt("card_shop_max_number", CONFIG_CAT_VILLAGERS, 1, 0, 32, "Maximum number of card master shops that can spawn per village");
		
		// Update Checker
		ENABLE_UPDATE_CHECKER = config.getBoolean("enable_update_checker", CONFIG_CAT_UPDATES, true, "Displays a client-side chat message on login if there's an update available.");
		
		
		config.save();
	}

	public static String getDataDir() {
		return DATA_DIR;
	}
}
