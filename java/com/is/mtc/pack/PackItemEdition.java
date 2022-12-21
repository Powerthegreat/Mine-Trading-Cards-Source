package com.is.mtc.pack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.is.mtc.data_manager.CardStructure;
import com.is.mtc.data_manager.Databank;
import com.is.mtc.data_manager.EditionStructure;
import com.is.mtc.root.Logs;
import com.is.mtc.root.Rarity;
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
import net.minecraft.world.World;

public class PackItemEdition extends PackItemBase {

	private static final int[] cCount = {7, 2, 1};
	private static final int[] rWeight = {25, 29, 30};
	private static final int rtWeight = rWeight[2];
	
	private static final String EDITION_ID_KEY = "edition_id";
	
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
		ArrayList<String> created;
		EditionStructure eStruct;
		NBTTagCompound nbt;
		Random random = world.rand;
		int i;

		if (world.isRemote)
			return stack;

		if (!stack.hasTagCompound() || !stack.stackTagCompound.hasKey(EDITION_ID_KEY)) {
			Logs.errLog("PackItemEdition: Missing NBT or NBTTag");
			return stack;
		}

		nbt = stack.stackTagCompound;
		eStruct = Databank.getEditionWithId(stack.stackTagCompound.getString(EDITION_ID_KEY));

		if (eStruct == null) {
			Logs.chatMessage(player, "The edition this pack is linked to does not exist, thus zero cards were generated");
			Logs.errLog("PackItemEdition: Edition is missing");
			return stack;
		}

		created = new ArrayList<String>();
		createCards(eStruct.getId(), Rarity.COMMON, cCount[Rarity.COMMON], created, random);
		createCards(eStruct.getId(), Rarity.UNCOMMON, cCount[Rarity.UNCOMMON], created, random);

		i = random.nextInt(rtWeight);
		if (i < rWeight[0])
			createCards(eStruct.getId(), Rarity.RARE, cCount[Rarity.RARE], created, random);
		else if (i < rWeight[1])
			createCards(eStruct.getId(), Rarity.ANCIENT, cCount[Rarity.RARE], created, random);
		else if (i < rWeight[2])
			createCards(eStruct.getId(), Rarity.LEGENDARY, cCount[Rarity.RARE], created, random);

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

	private void createCards(String edition, int cardRarity, int count, ArrayList<String> created, Random random) {

		for (int x = 0; x < count; ++x) { // Generate x cards
			CardStructure cStruct = null;

			for (int y = 0; y < RETRY; ++y) { // Retry x times until...
				cStruct = Databank.generatedACardFromEdition(cardRarity, edition, random);

				if (cStruct != null) {
					if (!created.contains(cStruct.getCDWD())) { // ... cards was not already created. Duplicate prevention
						created.add(cStruct.getCDWD());
						break;
					}
				}
			}
		}
	}
	
	// === ICON LAYERING AND COLORIZATION === //
	
    @SideOnly(Side.CLIENT)
    private IIcon overlayIcon;
    
    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses()
    {
        return true;
    }
    
    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int pass)
    {
    	if (pass==0)
    	{
    		String eid = stack.hasTagCompound() && stack.stackTagCompound.hasKey(EDITION_ID_KEY) ? stack.stackTagCompound.getString(EDITION_ID_KEY) : null;
    		return eid != null && Databank.getEditionWithId(eid) != null ? Databank.getEditionWithId(eid).getColor() : Reference.COLOR_GRAY;
    	}
    	
        return -1;
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iiconRegister)
    {
        super.registerIcons(iiconRegister);
        
        this.overlayIcon = iiconRegister.registerIcon(Reference.MODID + Reference.ITEM_PACK_OVERLAY);
    }
    
    /**
     * Gets an icon index based on an item's damage value and the given render pass
     */
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamageForRenderPass(int damage, int pass)
    {
        return pass == 1 ? this.overlayIcon : this.itemIcon;
    }
}
