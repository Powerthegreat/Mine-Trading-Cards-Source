package com.is.mtc.displayer_mono;

import com.is.mtc.displayer.DisplayerBlockTileEntity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;

public class MonoDisplayerBlockTileEntity extends DisplayerBlockTileEntity {
	public static final int INVENTORY_SIZE = 1;

	/*-*/

	public MonoDisplayerBlockTileEntity() {
		content = new ItemStack[INVENTORY_SIZE];
	}

	@Override
	public int getSizeInventory() {
		return INVENTORY_SIZE;
	}

	/*-*/

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		NBTTagList nbttaglist = nbt.getTagList("Items", 10);
		content = new ItemStack[getSizeInventory()];

		for (int i = 0; i < nbttaglist.tagCount(); ++i) {
			NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
			int j = nbttagcompound1.getByte("Slot") & 255;

			if (j >= 0 && j < content.length)
				content[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < content.length; ++i) {
			if (content[i] != null) {
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte) i);
				content[i].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}

		nbt.setTag("Items", nbttaglist);
		readFromNBT(nbt);
	}

	@Override
	public Packet getDescriptionPacket() {
		NBTTagCompound syncData = new NBTTagCompound();
		this.writeToNBT(syncData);

		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, syncData);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
		readFromNBT(pkt.func_148857_g());
	}

	/*@Override
	public void updateContainingBlockInfo() { // Allow data being sync on game loading

		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord); // Makes the server call getDescriptionPacket for a full data sync
		markDirty();

		super.updateContainingBlockInfo();
	}*/
}