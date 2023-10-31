package com.is.mtc.proxy;

import com.is.mtc.MineTradingCards;
import com.is.mtc.displayer.DisplayerBlockRenderer;
import com.is.mtc.displayer.DisplayerBlockTileEntity;
import com.is.mtc.displayer_mono.MonoDisplayerBlockRenderer;
import com.is.mtc.displayer_mono.MonoDisplayerBlockTileEntity;
import com.is.mtc.root.Logs;
import com.is.mtc.village.VillagerHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.VillagerRegistry;
import net.minecraft.util.ResourceLocation;

public class ClientProxy extends CommonProxy {
	@Override
	public void preInit(FMLPreInitializationEvent e) {
		MineTradingCards.PROXY_IS_REMOTE = true;
		Logs.devLog("Dectected proxy: Client");

		super.preInit(e);
	}

	@Override
	public void init(FMLInitializationEvent e) {
		super.init(e);
	}

	@Override
	public void postInit(FMLPostInitializationEvent e) {
		super.postInit(e);

		ClientRegistry.bindTileEntitySpecialRenderer(DisplayerBlockTileEntity.class, new DisplayerBlockRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(MonoDisplayerBlockTileEntity.class, new MonoDisplayerBlockRenderer());
		VillagerRegistry.instance().registerVillagerSkin(VillagerHandler.ID_CARD_MASTER, new ResourceLocation("is_mtc", "textures/skins/card_master.png"));
		VillagerRegistry.instance().registerVillagerSkin(VillagerHandler.ID_CARD_TRADER, new ResourceLocation("is_mtc", "textures/skins/card_trader.png"));
	}
}
