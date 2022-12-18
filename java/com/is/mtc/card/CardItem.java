package com.is.mtc.card;

import java.util.List;

import com.is.mtc.MineTradingCards;
import com.is.mtc.data_manager.CardStructure;
import com.is.mtc.data_manager.Databank;
import com.is.mtc.handler.GuiHandler;
import com.is.mtc.root.Logs;
import com.is.mtc.root.Rarity;
import com.is.mtc.root.Tools;
import com.is.mtc.util.Reference;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class CardItem extends Item {
	
	private static final String PREFIX = "item_card_";
	private static final int MAX_DESC_LENGTH = 42;

	private int rarity;

	public CardItem(int r) {
		setUnlocalizedName(PREFIX + Rarity.toString(r).toLowerCase());
		setTextureName(Reference.MODID + ":" + PREFIX + Rarity.toString(r).toLowerCase());
		setCreativeTab(MineTradingCards.MODTAB);

		rarity = r;
	}

	public int getCardRarity() {
		return rarity;
	}
	
	public static ItemStack applyCDWDtoStack(ItemStack stack, CardStructure cStruct) {
		stack.stackTagCompound.setString("cdwd", cStruct.getCDWD());
		if (cStruct.getAssetPath().size() > 0) {
			stack.stackTagCompound.setInteger("assetnumber", Tools.randInt(0, cStruct.getAssetPath().size()));
		}
		return stack;
	}
	
	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String cdwd = Tools.hasCDWD(stack) ? stack.stackTagCompound.getString("cdwd") : null;
		CardStructure cStruct = cdwd != null ? Databank.getCardByCDWD(cdwd) : null;

		if (cdwd != null) {
			if (cStruct == null) { // Card not registered ? Display cdwd
				return cdwd;
			} else {
				return cStruct.getName();
			}
		} else
			return super.getItemStackDisplayName(stack);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if (world.isRemote) {
			if (Tools.hasCDWD(stack)) {
				player.openGui(MineTradingCards.INSTANCE, GuiHandler.GUI_CARD, world, (int) player.posX, (int) player.posY, (int) player.posZ);
			}
			return stack;
		}

		if (!stack.hasTagCompound()) {
			stack.stackTagCompound = new NBTTagCompound();
		}
		
		if (!Tools.hasCDWD(stack)) {
			CardStructure cStruct = Databank.generateACard(rarity);
			
			if (cStruct != null) {
				if (stack.stackSize!=1) { // Generate a single card from the stack and drop it into inventory
					ItemStack popoffStack = stack.copy();
					if (!popoffStack.hasTagCompound()) {
						popoffStack.stackTagCompound = new NBTTagCompound();
					}
					popoffStack.stackSize=1;
					popoffStack = applyCDWDtoStack(popoffStack, cStruct);
					
					EntityItem dropped_card = player.entityDropItem(popoffStack, 1);
					dropped_card.delayBeforeCanPickup = 0;
					
					if (!player.capabilities.isCreativeMode) {
						stack.stackSize--;
					}
				}
				else { // Add data to the singleton "empty" card 
					stack = applyCDWDtoStack(stack, cStruct);
				}
				
			} else
				Logs.errLog("Unable to generate a card of this rarity: " + Rarity.toString(rarity));
		}

		if (!stack.stackTagCompound.hasKey("assetnumber")) {
			CardStructure cStruct = Databank.getCardByCDWD(stack.stackTagCompound.getString("cdwd"));
			if (cStruct != null) {
				if (cStruct.getAssetPath().size() > 0) {
					stack.stackTagCompound.setInteger("assetnumber", Tools.randInt(0, cStruct.getAssetPath().size()));
				}
			}
		}

		return stack;
	}


	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List infos, boolean par_4) {
		CardStructure cStruct;
		NBTTagCompound nbt;

		if (!stack.hasTagCompound() || !Tools.hasCDWD(stack)) {
			return;
		}

		nbt = stack.stackTagCompound;
		cStruct = Databank.getCardByCDWD(nbt.getString("cdwd"));

		if (cStruct == null) {
			infos.add(EnumChatFormatting.RED + "/!\\ Missing client-side data");
			infos.add(EnumChatFormatting.GRAY + nbt.getString("cdwd"));
			return;
		}

		infos.add("");
		infos.add("Edition: " + Rarity.toColor(rarity) + Databank.getEditionWithId(cStruct.getEdition()).getName());

		if (!cStruct.getCategory().isEmpty()) {
			infos.add("Category: " + EnumChatFormatting.WHITE + cStruct.getCategory());
		}

		if (!cStruct.getDescription().isEmpty()) {
			String[] words = cStruct.getDescription().split(" ");
			String line = "";

			infos.add("");
			for (String word : words) {
				line = line + " " + word;
				line = Tools.clean(line);
				if (line.length() >= MAX_DESC_LENGTH) {
					infos.add(EnumChatFormatting.ITALIC + line);
					line = "";
				}
			}
			if (!line.isEmpty()) {
				infos.add(line);
			}
		}

		infos.add("");
		infos.add(cStruct.numeral + "/" + Databank.getEditionWithId(cStruct.getEdition()).cCount);
	}
}
