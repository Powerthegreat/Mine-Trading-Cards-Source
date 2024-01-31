package com.is.mtc.pack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.is.mtc.data_manager.CardStructure;
import com.is.mtc.data_manager.CustomPackStructure;
import com.is.mtc.data_manager.Databank;
import com.is.mtc.root.Logs;
import com.is.mtc.util.Reference;
import com.mojang.realmsclient.gui.ChatFormatting;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class PackItemCustom extends PackItemBase {

	private static final String CUSTOM_PACK_ID_KEY = "custom_pack_id";
	@SideOnly(Side.CLIENT)
	private IIcon overlayIcon;

	public PackItemCustom() {
		setUnlocalizedName("item_pack_custom");
		setTextureName(Reference.MODID + Reference.ITEM_PACK_GRAYSCALE);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity player, int itemSlot, boolean isSelected) {

		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		if (!stack.getTagCompound().hasKey(CUSTOM_PACK_ID_KEY) && Databank.getCustomPacksCount() > 0) {
			int i = world.rand.nextInt(Databank.getCustomPacksCount());

			NBTTagCompound nbtTag = stack.getTagCompound();
			nbtTag.setString(CUSTOM_PACK_ID_KEY, Databank.getCustomPackWithNumeralId(i).getId());
			stack.setTagCompound(nbtTag);
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String cpid = stack.hasTagCompound() && stack.getTagCompound().hasKey(CUSTOM_PACK_ID_KEY) ? stack.getTagCompound().getString(CUSTOM_PACK_ID_KEY) : null;
		CustomPackStructure packStructure = cpid != null ? Databank.getCustomPackWithId(cpid) : null;

		if (cpid != null) {
			if (packStructure == null) { // Pack was created earlier, but edition was removed in the meantime
				return "custom_pack_" + cpid;
			} else {
				return packStructure.getName();
			}
		} else {
			return super.getItemStackDisplayName(stack);
		}
	}

	public void addInformation(ItemStack stack, @Nullable World world, List<String> infos, boolean flag) {
		CustomPackStructure packStructure;
		NBTTagCompound nbt;

		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey(CUSTOM_PACK_ID_KEY)) {
			return;
		}

		nbt = stack.getTagCompound();
		packStructure = Databank.getCustomPackWithId(stack.getTagCompound().getString(CUSTOM_PACK_ID_KEY));

		if (packStructure == null) {
			infos.add(ChatFormatting.RED + "/!\\ Missing client-side custom pack");
			infos.add(ChatFormatting.GRAY + nbt.getString(CUSTOM_PACK_ID_KEY));
			return;
		}

		infos.add("Contains cards from the custom pack '" + packStructure.getName() + "'");
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		ArrayList<String> created;
		CustomPackStructure packStructure;
		NBTTagCompound nbt;

		if (world.isRemote) {
			return stack;
		}
		if (!stack.hasTagCompound() || !stack.getTagCompound().hasKey(CUSTOM_PACK_ID_KEY)) {
			Logs.errLog("PackItemCustom: Missing NBT or NBTTag");
			return stack;
		}

		nbt = stack.getTagCompound();
		packStructure = Databank.getCustomPackWithId(stack.getTagCompound().getString(CUSTOM_PACK_ID_KEY));

		if (packStructure == null) {
			Logs.chatMessage(player, "The custom pack this pack is linked to does not exist, thus zero cards were generated");
			Logs.errLog("PackItemCustom: Custom pack is missing");
			return stack;
		}

		created = new ArrayList<String>();

//		packStructure.categoryQuantities.forEach((category, categoryInfo) -> createCards(category, categoryInfo[1], categoryInfo[0], created));
		for (String category : packStructure.categoryQuantities.keySet()) {
//			packStructure.categoryQuantities.forEach((category, categoryInfo) -> createCards(category, categoryInfo[1], categoryInfo[0], created));
			int[] categoryInfo = packStructure.categoryQuantities.get(category);
			createCards(category, categoryInfo[1], categoryInfo[0], created, world.rand);
		}

		if (created.size() > 0) {
			for (String cdwd : created) {
				spawnCard(player, world, cdwd);
			}
			stack.stackSize -= 1;
		} else {
			Logs.chatMessage(player, "Zero cards were registered, thus zero cards were generated");
			Logs.errLog("Zero cards were registered, thus zero cards were generated");
		}

		return stack;
	}

	// === ICON LAYERING AND COLORIZATION === //

	private void createCards(String category, int cardRarity, int count, ArrayList<String> created, Random random) {

		for (int x = 0; x < count; ++x) { // Generate x cards
			CardStructure cStruct = null;

			for (int y = 0; y < RETRY; ++y) { // Retry x times until...
				cStruct = Databank.generatedACardFromCategory(cardRarity, category, new Random()); // Using new Random() because world random can cause issues generating cards

				if (cStruct != null) {
					if (!created.contains(cStruct.getCDWD())) { // ... card was not already created. Duplicate prevention
						created.add(cStruct.getCDWD());
						break;
					}
				}
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int pass) {
		if (pass == 0) {
			String eid = stack.hasTagCompound() && stack.stackTagCompound.hasKey(CUSTOM_PACK_ID_KEY) ? stack.stackTagCompound.getString(CUSTOM_PACK_ID_KEY) : null;
			return eid != null && Databank.getCustomPackWithId(eid) != null ? Databank.getCustomPackWithId(eid).getColor() : Reference.COLOR_GRAY;
		}

		return -1;
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iiconRegister) {
		super.registerIcons(iiconRegister);

		this.overlayIcon = iiconRegister.registerIcon(Reference.MODID + Reference.ITEM_PACK_OVERLAY);
	}

	/**
	 * Gets an icon index based on an item's damage value and the given render pass
	 */
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamageForRenderPass(int damage, int pass) {
		return pass == 1 ? this.overlayIcon : this.itemIcon;
	}
}
