package com.is.mtc.pack;

import java.util.ArrayList;

import com.is.mtc.MineTradingCards;
import com.is.mtc.root.Logs;
import com.is.mtc.root.Rarity;
import com.is.mtc.util.Reference;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class PackItemStandard extends PackItemBase {

	private static final int[] cCount = {7, 2, 1};
	private static final int[] rWeight = {25, 29, 30};
	private static final int rtWeight = rWeight[2];

	public PackItemStandard() {
		setUnlocalizedName("item_pack_standard");
		setTextureName(Reference.MODID + Reference.ITEM_PACK_GRAYSCALE);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		ArrayList<String> created;
		
		if (world.isRemote) {
			return stack;
		}

		created = new ArrayList<String>();
		createCards(Rarity.COMMON, cCount[Rarity.COMMON], created, world.rand);
		createCards(Rarity.UNCOMMON, cCount[Rarity.UNCOMMON], created, world.rand);

		int i = world.rand.nextInt(rtWeight);
		if (i < rWeight[0]) {
			createCards(Rarity.RARE, cCount[Rarity.RARE], created, world.rand);
		}
		else if (i < rWeight[1]) {
			createCards(Rarity.ANCIENT, cCount[Rarity.RARE], created, world.rand);
		}
		else if (i < rWeight[2]) {
			createCards(Rarity.LEGENDARY, cCount[Rarity.RARE], created, world.rand);
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
    		return MineTradingCards.PACK_COLOR_STANDARD;
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
