package com.is.mtc.version;

import com.is.mtc.util.Reference;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class DevVersionWarning {
	public static DevVersionWarning instance = new DevVersionWarning();

	@SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
	public void onPlayerTickEvent(PlayerTickEvent event) {
		if (event.player.worldObj.isRemote) {
			event.player.addChatComponentMessage(
					new ChatComponentText(
							"You're using a "
									+ EnumChatFormatting.RED + "development version" + EnumChatFormatting.RESET + " of " + Reference.NAME + "."
					));

			event.player.addChatComponentMessage(
					new ChatComponentText(
							EnumChatFormatting.RED + "This version is not meant for public use."
					));

			FMLCommonHandler.instance().bus().unregister(instance);
		}
	}
}