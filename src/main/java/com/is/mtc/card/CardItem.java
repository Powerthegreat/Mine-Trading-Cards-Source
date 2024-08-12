package com.is.mtc.card;

import java.util.List;
import java.util.Random;

import com.is.mtc.MineTradingCards;
import com.is.mtc.data_manager.CardStructure;
import com.is.mtc.data_manager.Databank;
import com.is.mtc.handler.GuiHandler;
import com.is.mtc.root.Logs;
import com.is.mtc.root.Rarity;
import com.is.mtc.root.Tools;
import com.is.mtc.util.Reference;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class CardItem extends Item {

	public static final int[] CARD_RARITY_ARRAY = new int[]{Rarity.COMMON, Rarity.UNCOMMON, Rarity.RARE, Rarity.ANCIENT, Rarity.LEGENDARY};
	private static final String PREFIX = "item_card_";
	private static final int MAX_DESC_LENGTH = 42;
	private int rarity;
	@SideOnly(Side.CLIENT)
	private IIcon overlayIcon;

	public CardItem(int r) {
		setUnlocalizedName(PREFIX + Rarity.toString(r).toLowerCase());
		setTextureName(Reference.MODID + Reference.ITEM_CARD_GRAYSCALE);
		setCreativeTab(MineTradingCards.MODTAB);

		rarity = r;
	}

	public static ItemStack applyCDWDtoStack(ItemStack stack, CardStructure cStruct, Random random) {
		stack.stackTagCompound.setString("cdwd", cStruct.getCDWD());
		if (cStruct.getResourceLocations() != null && cStruct.getResourceLocations().size() > 1) {
			stack.stackTagCompound.setInteger("assetnumber", Tools.randInt(0, cStruct.getResourceLocations().size(), random));
		}
		return stack;
	}

	public int getCardRarity() {
		return rarity;
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
			CardStructure cStruct = Databank.generateACard(rarity, new Random()); // Using new Random() because world random can cause issues generating cards

			if (cStruct != null) {
				if (stack.stackSize != 1) { // Generate a single card from the stack and drop it into inventory
					ItemStack popoffStack = stack.copy();
					if (!popoffStack.hasTagCompound()) {
						popoffStack.stackTagCompound = new NBTTagCompound();
					}
					popoffStack.stackSize = 1;
					popoffStack = applyCDWDtoStack(popoffStack, cStruct, world.rand);

					EntityItem dropped_card = player.entityDropItem(popoffStack, 1);
					dropped_card.delayBeforeCanPickup = 0;

					if (!player.capabilities.isCreativeMode) {
						stack.stackSize--;
					}
				} else { // Add data to the singleton "empty" card
					stack = applyCDWDtoStack(stack, cStruct, world.rand);
				}

			} else
				Logs.errLog("Unable to generate a card of this rarity: " + Rarity.toString(rarity));
		}

		if (!stack.stackTagCompound.hasKey("assetnumber")) {
			CardStructure cStruct = Databank.getCardByCDWD(stack.stackTagCompound.getString("cdwd"));
			if (cStruct != null) {
				if (cStruct.getResourceLocations() != null && cStruct.getResourceLocations().size() > 1) {
					stack.stackTagCompound.setInteger("assetnumber", Tools.randInt(0, cStruct.getResourceLocations().size(), world.rand));
				}
			}
		}

		return stack;
	}

	// === ICON LAYERING AND COLORIZATION === //

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
			String[] lines = cStruct.getDescription().split("\\\\n");

			infos.add("Description:");
			for (String currentLine : lines) {
				while (currentLine.length() >= MAX_DESC_LENGTH) {
					infos.add(EnumChatFormatting.ITALIC + currentLine.substring(0, MAX_DESC_LENGTH));
					currentLine = currentLine.substring(MAX_DESC_LENGTH);
				}
				infos.add(EnumChatFormatting.ITALIC + currentLine);
			}
		}

		infos.add("");
		infos.add(cStruct.numeral + "/" + Databank.getEditionWithId(cStruct.getEdition()).cCount);
	}

	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int pass) {
		if (pass == 0) {
			switch (this.rarity) {
				case Rarity.COMMON:
					return MineTradingCards.CARD_COLOR_COMMON;
				case Rarity.UNCOMMON:
					return MineTradingCards.CARD_COLOR_UNCOMMON;
				case Rarity.RARE:
					return MineTradingCards.CARD_COLOR_RARE;
				case Rarity.ANCIENT:
					return MineTradingCards.CARD_COLOR_ANCIENT;
				case Rarity.LEGENDARY:
					return MineTradingCards.CARD_COLOR_LEGENDARY;
			}
			return Reference.COLOR_GRAY;
		}

		return -1;
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iiconRegister) {
		super.registerIcons(iiconRegister);

		this.overlayIcon = iiconRegister.registerIcon(Reference.MODID + Reference.ITEM_CARD_OVERLAY);
	}

	/**
	 * Gets an icon index based on an item's damage value and the given render pass
	 */
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamageForRenderPass(int damage, int pass) {
		return pass == 1 ? this.overlayIcon : this.itemIcon;
	}
}
