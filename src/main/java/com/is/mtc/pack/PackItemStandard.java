package com.is.mtc.pack;

import com.is.mtc.MineTradingCards;
import com.is.mtc.card.CardItem;
import com.is.mtc.root.Logs;
import com.is.mtc.util.Functions;
import com.is.mtc.util.Reference;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Random;

public class PackItemStandard extends PackItemBase {

	public static final String[] STANDARD_PACK_CONTENT_DEFAULT = new String[]{
			"7x1:0:0:0:0",
			"2x0:1:0:0:0",
			"1x0:0:25:4:1",
	};
	public static String[] STANDARD_PACK_CONTENT = STANDARD_PACK_CONTENT_DEFAULT;
	@SideOnly(Side.CLIENT)
	private IIcon overlayIcon;

	public PackItemStandard() {
		setUnlocalizedName("item_pack_standard");
		setTextureName(Reference.MODID + Reference.ITEM_PACK_GRAYSCALE);
	}

	// === ICON LAYERING AND COLORIZATION === //

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if (world.isRemote) {
			return stack;
		} // Don't do this on the client side

		ArrayList<String> created;
		Random random = world.rand;

		// Figure out how many of each card rarity to create

		int[] card_set_to_create = new int[]{0, 0, 0, 0, 0}; // Set of cards that will come out of the pack

		for (String entry : STANDARD_PACK_CONTENT) {
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
				Logs.errLog("Something went wrong parsing standard_pack_contents line: " + entry);
			}
		}

		// Actually create the cards

		created = new ArrayList<String>();

		for (int rarity : CardItem.CARD_RARITY_ARRAY) {
			createCards(rarity, card_set_to_create[rarity], created, world.rand);
		}

		if (created.size() > 0) {
			for (String cdwd : created) {
				spawnCard(player, world, cdwd);
			}
			stack.stackSize -= 1;
		} else {
			Logs.chatMessage(player, "Zero cards were registered, thus zero cards were generated");
			Logs.errLog("Zero cards were registered, thus zero cards can be generated");
		}

		return stack;
	}

	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@SideOnly(Side.CLIENT)
	public int getColorFromItemStack(ItemStack stack, int pass) {
		if (pass == 0) {
			return MineTradingCards.PACK_COLOR_STANDARD;
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
