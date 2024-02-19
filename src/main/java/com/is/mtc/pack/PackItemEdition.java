package com.is.mtc.pack;

import com.is.mtc.card.CardItem;
import com.is.mtc.data_manager.CardStructure;
import com.is.mtc.data_manager.Databank;
import com.is.mtc.data_manager.EditionStructure;
import com.is.mtc.root.Logs;
import com.is.mtc.util.Functions;
import com.is.mtc.util.Reference;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PackItemEdition extends PackItemBase {

	private static final String EDITION_ID_KEY = "edition_id";
	public static String[] EDITION_PACK_CONTENT = PackItemStandard.STANDARD_PACK_CONTENT_DEFAULT;
	@SideOnly(Side.CLIENT)
	private IIcon overlayIcon;

	public PackItemEdition() {
		setUnlocalizedName("item_pack_edition");
		setTextureName(Reference.MODID + Reference.ITEM_PACK_GRAYSCALE);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity player, int par_4, boolean par_5) {
		Random random = world.rand;

		if (!stack.hasTagCompound())
			stack.stackTagCompound = new NBTTagCompound();

		if (!stack.stackTagCompound.hasKey(EDITION_ID_KEY) && Databank.getEditionsCount() > 0) {
			int i = random.nextInt(Databank.getEditionsCount());

			stack.stackTagCompound.setString(EDITION_ID_KEY, Databank.getEditionWithNumeralId(i).getId());
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String eid = stack.hasTagCompound() && stack.stackTagCompound.hasKey(EDITION_ID_KEY) ? stack.stackTagCompound.getString(EDITION_ID_KEY) : null;
		EditionStructure eStruct = eid != null ? Databank.getEditionWithId(eid) : null;

		if (eid != null) {
			if (eStruct == null) // Pack was created earlier, but edition was removed in the mean time
				return "edition_pack_" + eid;
			else
				return eStruct.getName() + " Pack";
		} else
			return super.getItemStackDisplayName(stack);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List infos, boolean par_4) {
		EditionStructure eStruct;
		NBTTagCompound nbt;

		if (!stack.hasTagCompound() || !stack.stackTagCompound.hasKey(EDITION_ID_KEY))
			return;

		nbt = stack.stackTagCompound;
		eStruct = Databank.getEditionWithId(stack.stackTagCompound.getString(EDITION_ID_KEY));

		if (eStruct == null) {
			infos.add(EnumChatFormatting.RED + "/!\\ Missing client-side edition");
			infos.add(EnumChatFormatting.GRAY + nbt.getString(EDITION_ID_KEY));
			return;
		}

		infos.add("Contains cards from the edition '" + eStruct.getName() + "'");
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (world.isRemote) {
			return stack;
		} // Don't do this on the client side

		if (!stack.hasTagCompound() || !stack.stackTagCompound.hasKey(EDITION_ID_KEY)) {
			Logs.errLog("PackItemEdition: Missing NBT or NBTTag");
			return stack;
		}

		ArrayList<String> created;
		Random random = world.rand;
		NBTTagCompound nbt;
		nbt = stack.stackTagCompound;
		EditionStructure eStruct;
		eStruct = Databank.getEditionWithId(stack.stackTagCompound.getString(EDITION_ID_KEY));

		if (eStruct == null) {
			Logs.chatMessage(player, "The edition this pack is linked to does not exist, thus zero cards were generated");
			Logs.errLog("PackItemEdition: Edition is missing");
			return stack;
		}

		// Figure out how many of each card rarity to create

		int[] card_set_to_create = new int[]{0, 0, 0, 0, 0}; // Set of cards that will come out of the pack

		for (String entry : EDITION_PACK_CONTENT) {
			try {
				double[] card_weighted_dist = new double[]{0, 0, 0, 0, 0}; // Distribution used when a card is randomized

				// Split entry
				String[] split_entry = entry.toLowerCase().trim().split("x");

				float count = MathHelper.clamp_float(Float.parseFloat(split_entry[0]), 0F, 64F);
				int drop_count_characteristic = (int) count;
				float drop_count_mantissa = count % 1;

				String[] distribution_split = split_entry[1].split(":");

				for (int i = 0; i < distribution_split.length; i++) {
					card_weighted_dist[i] = Integer.parseInt(distribution_split[i].trim());
				}

				// Repeat for the number of cards prescribed
				for (int i = 0; i < drop_count_characteristic + (random.nextFloat() < drop_count_mantissa ? 1 : 0); i++) {
					Object chosen_rarity = Functions.weightedRandom(CardItem.CARD_RARITY_ARRAY, card_weighted_dist, random);

					if (chosen_rarity != null) {
						card_set_to_create[(Integer) chosen_rarity]++;
					}
				}
			} catch (Exception e) {
				Logs.errLog("Something went wrong parsing edition_pack_contents line: " + entry);
			}
		}

		// Actually create the cards

		created = new ArrayList<String>();

		for (int rarity : CardItem.CARD_RARITY_ARRAY) {
			createCards(eStruct.getId(), rarity, card_set_to_create[rarity], created, world.rand);
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

	private void createCards(String edition, int cardRarity, int count, ArrayList<String> created, Random random) {

		for (int x = 0; x < count; ++x) { // Generate x cards
			CardStructure cStruct = null;

			for (int y = 0; y < RETRY; ++y) { // Retry x times until...
				cStruct = Databank.generateACardFromEdition(cardRarity, edition, new Random()); // Using new Random() because world random can cause issues generating cards

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
			String eid = stack.hasTagCompound() && stack.stackTagCompound.hasKey(EDITION_ID_KEY) ? stack.stackTagCompound.getString(EDITION_ID_KEY) : null;
			return eid != null && Databank.getEditionWithId(eid) != null ? Databank.getEditionWithId(eid).getColor() : Reference.COLOR_GRAY;
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
