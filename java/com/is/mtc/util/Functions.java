package com.is.mtc.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.is.mtc.root.Logs;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Direction;
import net.minecraft.util.Facing;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public class Functions {
	// Represents the 1.14+ village types
	
	// Indexed by [isLower][isRightHanded][isShut][orientation][horizIndex]
	public static final int[][][][][] DOOR_META_ARRAY = new int[][][][][]
	{
		// --- UPPER HALF --- //
		// Left-hand
		{{{ // Open
		{8,8,9,9},
		{8,8,9,9},
		{8,8,9,9},
		{8,8,9,9}
		},
		{ // Shut
		{8,8,9,9},
		{8,8,9,9},
		{8,8,9,9},
		{8,8,9,9}
		}},
		
		// Right-hand
		{{ // Open
		{9,9,8,8},
		{9,9,8,8},
		{9,9,8,8},
		{9,9,8,8}
		},
		{ // Shut
		{9,9,8,8},
		{9,9,8,8},
		{9,9,8,8},
		{9,9,8,8}
		}}},
		
		// --- LOWER HALF --- //
		// Left-hand
		{{{ // Open
		{7,4,5,6},
		{6,7,6,7},
		{5,6,7,4},
		{4,5,4,5}
		},
		{ // Shut
		{3,0,1,2},
		{2,3,2,3},
		{1,2,3,0},
		{0,1,0,1}
		}},
		
		// Right-hand
		{{ // Open
		{7,4,5,6},
		{6,7,6,7},
		{5,6,7,4},
		{4,5,4,5}
		},
		{ // Shut
		{3,0,1,2},
		{2,3,2,3},
		{1,2,3,0},
		{0,1,0,1}
		}}}
	};
	
	// Indexed by [orientation][horizIndex]
	public static final int[][] FURNACE_META_ARRAY = new int[][]{
		{3,4,2,5},
		{5,3,5,3},
		{2,5,3,4},
		{4,2,4,2},
	};
	
    /**
     * Discover the y coordinate that will serve as the ground level of the supplied BoundingBox.
     * (An ACTUAL median of all the levels in the BB's horizontal rectangle).
     * 
     * Use outlineOnly if you'd like to tally only the boundary values.
     * 
     * If outlineOnly is true, use sideFlag to specify which boundaries:
     * +1: front
     * +2: left (wrt coordbase 0 or 1)
     * +4: back
     * +8: right (wrt coordbase 0 or 1)
     * 
     * horizIndex is the integer that represents the orientation of the structure.
     */
    public static int getMedianGroundLevel(World world, StructureBoundingBox boundingBox, boolean outlineOnly, byte sideFlag, int horizIndex)
    {
    	ArrayList<Integer> i = new ArrayList<Integer>();
    	
        for (int k = boundingBox.minZ; k <= boundingBox.maxZ; ++k)
        {
            for (int l = boundingBox.minX; l <= boundingBox.maxX; ++l)
            {
                if (boundingBox.isVecInside(l, 64, k))
                {
                	if (!outlineOnly || (outlineOnly &&
                			(
                					(k==boundingBox.minZ && (sideFlag&(new int[]{1,2,4,2}[horizIndex]))>0) ||
                					(k==boundingBox.maxZ && (sideFlag&(new int[]{4,8,1,8}[horizIndex]))>0) ||
                					(l==boundingBox.minX && (sideFlag&(new int[]{2,4,2,1}[horizIndex]))>0) ||
                					(l==boundingBox.maxX && (sideFlag&(new int[]{8,1,8,4}[horizIndex]))>0) ||
                					false
                					)
                			))
                	{
                		int aboveTopLevel = getAboveTopmostSolidOrLiquidBlock(world, l, k);
                		if (aboveTopLevel != -1) {i.add(aboveTopLevel);}
                		//if (GeneralConfig.debugMessages) {LogHelper.info("Position [" + l + ", " + k + "] sideFlag: " + sideFlag + ", horizIndex: " + horizIndex);}
                	}
                }
            }
        }
        //if (GeneralConfig.debugMessages) {LogHelper.info("Ground height array for [" + boundingBox.minX + ", " + boundingBox.minZ + "] to [" + boundingBox.maxX + ", " + boundingBox.maxZ + "]: " + i);}
        return medianIntArray(i, true);
    }
    
    
    /**
     * Returns the space above the topmost block that is solid or liquid. Does not count leaves or other foliage
     */
    public static int getAboveTopmostSolidOrLiquidBlock(World world, int posX, int posZ)
    {
        Chunk chunk = world.getChunkFromBlockCoords(posX, posZ);
        int x = posX;
        int z = posZ;
        int k = chunk.getTopFilledSegment() + 15;
        posX &= 15;
        
        // Search downward until you hit the first block that meets the "solid/liquid" requirement
        for (posZ &= 15; k > 0; --k)
        {
            Block block = chunk.getBlock(posX, k, posZ);
            Material material = block.getMaterial();
            
            if (
            		// If it's a solid, full block that isn't one of these particular types
            		(material.blocksMovement()
            		&& material != Material.leaves
    				&& material != Material.plants
					&& material != Material.vine
					&& material != Material.air
            		&& !block.isFoliage(world, x, k, z)
            		&& block.isNormalCube())
            		// If the block is liquid, return the value above it
            		|| material.isLiquid()
            		)
            {
                return k + 1;
            }
        }
        
        return -1;
    }
	
	/**
	 * Returns the median value of an int array.
	 * If the returned value is a halfway point, round up or down depending on if the average value is higher or lower than the median.
	 * If it's the same, specify based on roundup parameter.
	 */
	public static int medianIntArray(ArrayList<Integer> array, boolean roundup)
	{
		if (array.size() <=0) return -1;
		
		Collections.sort(array);
		
		//if (GeneralConfig.debugMessages) {LogHelper.info("array: " + array);}
		
		if (array.size() % 2 == 0)
		{
			// Array is even-length. Find average of the middle two values.
			int totalElements = array.size();
			int sumOfMiddleTwo = array.get(totalElements / 2) + array.get(totalElements / 2 - 1);
			
			if (sumOfMiddleTwo%2==0)
			{
				// Average of middle two values is integer
				//LogHelper.info("Median chosen type A: " + sumOfMiddleTwo/2);
				return sumOfMiddleTwo/2;
			}
			else
			{
				// Average of middle two is half-integer.
				// Round this based on whether the average is higher.
				double median = (double)sumOfMiddleTwo/2;
				
				double average = 0;
				for (int i : array) {average += i;}
				average /= array.size();
				
				if (average < median)
				{
					//LogHelper.info("Median chosen type B: " + MathHelper.floor_double(median) );
					return MathHelper.floor_double(median);
				}
				else if (average > median)
				{
					//LogHelper.info("Median chosen type C: " + MathHelper.ceiling_double_int(median) );
					return MathHelper.ceiling_double_int(median);
				}
				else
				{
					//LogHelper.info("Median chosen type D: " + (roundup ? MathHelper.ceiling_double_int(median) : MathHelper.floor_double(median)));
					return roundup ? MathHelper.ceiling_double_int(median) : MathHelper.floor_double(median);
				}
			}
		}
		else
		{
			// Array is odd-length. Take the middle value.
			//LogHelper.info("Median chosen type E: " + array.get(array.size()/2));
			return array.get(array.size()/2);
		}
	}
	
    
    /**
     * Creates a villager of the specified profession/career/age.
     * A profession of -1 means return a random one. Whether this is vanilla only or from all registered will be determined by the configs.
     * Any career greater than 0 requires the career system to be true.
     */
    public static EntityVillager makeVillagerWithProfession(World world, Random random, int profession, int age)
    {
		EntityVillager entityvillager = new EntityVillager(world);
		
		entityvillager.setProfession(profession);
		
		// Set age
		entityvillager.setGrowingAge(age);
		
		return entityvillager;
    }
    
    
    
	/**
	 * Deletes EntityItems within a given structure bounding box
	 */
	public static void cleanEntityItems(World world, StructureBoundingBox boundingBox)
	{
		// selectEntitiesWithinAABB is an AABB method
		AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(
				// Modified to center onto front of house
				boundingBox.minX, boundingBox.minY, boundingBox.minZ,
				boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).expand(3, 8, 3);
        
        List<EntityItem> list = world.selectEntitiesWithinAABB(EntityItem.class, aabb, null);
        
		if (!list.isEmpty())
        {
			Iterator iterator = list.iterator();
					
			while (iterator.hasNext())
			{
				EntityItem entityitem = (EntityItem) iterator.next();
				entityitem.setDead();
			}
			
			if (Logs.ENABLE_DEV_LOGS) {Logs.devLog("Cleaned "+list.size()+" Entity items within " + aabb.toString());}
        }
	}
    
    /**
     * Give this method the orientation of a hanging torch and the base mode of the structure it's in,
     * and it'll give you back the required meta value for construction.
     * For relative orientations, use:
     * 0=fore-facing (away from you); 1=right-facing; 2=back-facing (toward you); 3=left-facing
     */
    public static int getTorchRotationMeta(int relativeOrientation, int coordBaseMode)
    {
		switch (relativeOrientation)
		{
		case 0: // Facing away
			return new int[]{3,2,4,1}[coordBaseMode];
		case 1: // Facing right
			return new int[]{1,3,1,3}[coordBaseMode];
		case 2: // Facing you
			return new int[]{4,1,3,2}[coordBaseMode];
		case 3: // Facing left
			return new int[]{2,4,2,4}[coordBaseMode];
		default: // Torch will be standing upright, hopefully
			return 0;
		}
    }
	
    /**
     * Returns meta values for lower and upper halves of a door
     * 
	 * orientation - Direction the outside of the door faces when closed:
	 * 0=fore-facing (away from you); 1=right-facing; 2=back-facing (toward you); 3=left-facing
	 * 
	 * isShut - doors are "shut" by default when placed by a player
	 * rightHandRule - whether the door opens counterclockwise when viewed from above. This is default state when placed by a player
	 */
	public static int[] getDoorMetas(int orientation, int horizIndex, boolean isShut, boolean isRightHanded)
	{
		return new int[] {
				DOOR_META_ARRAY[1][isRightHanded?1:0][isShut?1:0][orientation][horizIndex],
				DOOR_META_ARRAY[0][isRightHanded?1:0][isShut?1:0][orientation][horizIndex]
						};
	}
    /**
	 * furnaceOrientation:
	 * 0=fore-facing (away from you); 1=right-facing; 2=back-facing (toward you); 3=left-facing
	 * -X: returns the value X - used for things like upright barrels
	 */
	public static int chooseFurnaceMeta(int orientation, int horizIndex)
	{
		if (orientation<0) {return -orientation;}
		return FURNACE_META_ARRAY[orientation][horizIndex];
	}
    
    
    /**
     * Returns the direction-shifted metadata for blocks that require orientation, e.g. doors, stairs, ladders.
     */
    public static int getMetadataWithOffset(Block blockIn, int metaIn, int coordBaseMode)
    {
    	// Rotate rails
        if (blockIn == Blocks.rail)
        {
            if (coordBaseMode == 1 || coordBaseMode == 3)
            {
                if (metaIn == 1) {return 0;}
                return 1;
            }
        }
        else if (blockIn != Blocks.wooden_door && blockIn != Blocks.iron_door)
        {
            if (blockIn != Blocks.stone_stairs && blockIn != Blocks.oak_stairs && blockIn != Blocks.nether_brick_stairs && blockIn != Blocks.stone_brick_stairs && blockIn != Blocks.sandstone_stairs)
            {
                if (blockIn == Blocks.ladder)
                {
                    if (coordBaseMode == 0)
                    {
                    	switch (metaIn)
                    	{
                    	case 2: return 3;
                    	case 3: return 2;
                    	default:
                    	}
                    }
                    else if (coordBaseMode == 1)
                    {
                    	switch (metaIn)
                    	{
                    	case 2: return 4;
                    	case 3: return 5;
                    	case 4: return 2;
                    	case 5: return 3;
                    	default:
                    	}
                    }
                    else if (coordBaseMode == 3)
                    {
                    	switch (metaIn)
                    	{
                    	case 2: return 5;
                    	case 3: return 4;
                    	case 4: return 2;
                    	case 5: return 3;
                    	default:
                    	}
                    }
                }
                else if (blockIn == Blocks.stone_button)
                {
                    if (coordBaseMode == 0)
                    {
                    	switch (metaIn)
                    	{
                    	case 3: return 4;
                    	case 4: return 3;
                    	default:
                    	}
                    }
                    else if (coordBaseMode == 1)
                    {
                    	switch (metaIn)
                    	{
                    	case 1: return 4;
                    	case 2: return 3;
                    	case 3: return 1;
                    	case 4: return 2;
                    	default:
                    	}
                    }
                    else if (coordBaseMode == 3)
                    {
                    	switch (metaIn)
                    	{
                    	case 1: return 4;
                    	case 2: return 3;
                    	case 3: return 2;
                    	case 4: return 1;
                    	default:
                    	}
                    }
                }
                else if (blockIn != Blocks.tripwire_hook && !(blockIn instanceof BlockDirectional))
                {
                    if (blockIn == Blocks.piston || blockIn == Blocks.sticky_piston || blockIn == Blocks.lever || blockIn == Blocks.dispenser)
                    {
                        if (coordBaseMode == 0)
                        {
                            if (metaIn == 2 || metaIn == 3)
                            {
                                return Facing.oppositeSide[metaIn];
                            }
                        }
                        else if (coordBaseMode == 1)
                        {
                        	switch (metaIn)
                        	{
                        	case 2: return 4;
                        	case 3: return 5;
                        	case 4: return 2;
                        	case 5: return 3;
                        	default:
                        	}
                        }
                        else if (coordBaseMode == 3)
                        {
                        	switch (metaIn)
                        	{
                        	case 2: return 5;
                        	case 3: return 4;
                        	case 4: return 2;
                        	case 5: return 3;
                        	default:
                        	}
                        }
                    }
                }
                else if (coordBaseMode == 0)
                {
                    if (metaIn == 0 || metaIn == 2)
                    {
                        return Direction.rotateOpposite[metaIn];
                    }
                }
                else if (coordBaseMode == 1)
                {
                	switch (metaIn)
                	{
                	case 0: return 3;
                	case 1: return 2;
                	case 2: return 1;
                	case 3: return 0;
                	default:
                	}
                }
                else if (coordBaseMode == 3)
                {
                	switch (metaIn)
                	{
                	case 0: return 1;
                	case 1: return 2;
                	case 2: return 3;
                	case 3: return 0;
                	default:
                	}
                }
            }
            else if (coordBaseMode == 0)
            {
            	switch (metaIn)
            	{
            	case 2: return 3;
            	case 3: return 2;
            	default:
            	}
            }
            else if (coordBaseMode == 1)
            {
            	switch (metaIn)
            	{
            	case 0: return 2;
            	case 1: return 3;
            	case 2: return 0;
            	case 3: return 1;
            	default:
            	}
            }
            else if (coordBaseMode == 3)
            {
            	switch (metaIn)
            	{
            	case 0: return 2;
            	case 1: return 3;
            	case 2: return 1;
            	case 3: return 0;
            	default:
            	}
            }
        }
        else if (coordBaseMode == 0)
        {
        	switch (metaIn)
        	{
        	case 0: return 2;
        	case 2: return 0;
        	default:
        	}
        }
        else
        {
        	switch (metaIn)
        	{
        	case 1: return metaIn + 1 & 3;
        	case 3: return metaIn + 3 & 3;
        	default:
        	}
        }

        return metaIn;
    }
    
    /**
     * Inputs a color string, either as a decimal integer or a hex integer
     * as signified by # in the front, and returns it as the proper integer.
     */
    public static int parseColorInteger(String colorstring, int defaultcolor) {
		boolean ishexformat;
		try {
			ishexformat=colorstring.indexOf("#")==0;
			String colorstring_substring=ishexformat?colorstring.substring(1):colorstring;
			return Integer.parseInt(colorstring_substring, ishexformat ? 16 : 10);
		}
		catch (Exception e) {
			Logs.errLog("Color integer " + colorstring + " is not properly formatted!");
			return defaultcolor;
		}
    }
    
    private static final int MINECRAFT_ID_HASH = "minecraft".hashCode();
    private static final int MINECRAFT_ID_TARGET_HASH = 0x5e8f51;
    // base 16777216 plus target color int, minus "minecraft" hash 7207341
    private static final int HASH_COLOR_OFFSET = MINECRAFT_ID_TARGET_HASH - MINECRAFT_ID_HASH + (MINECRAFT_ID_TARGET_HASH - MINECRAFT_ID_HASH >= 0 ? 0 : Reference.COLOR_WHITE);
    
    /**
     * Uses a hash conversion to set a name into a color int
     * @return
     */
    public static int string_to_color_code(String id) {
    	// Convert into a "safe" int (positive and within 0xFFFFFF)
    	int safe_int = MathHelper.abs_int(id.hashCode() + HASH_COLOR_OFFSET)%Reference.COLOR_WHITE;
    	
//    	// Decompose into r, g, b
//    	int r = safe_int>>16;
//		int g = (safe_int>>8)&255;
//		int b = safe_int&255;
    			
		return safe_int;
    }
}
