package com.is.mtc.root;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.is.mtc.card.CardItem;

import net.minecraft.item.ItemStack;

public class Tools {

	public static final int SIDE_BOTTOM = 0;
	public static final int SIDE_TOP = 1;

	public static final int SIDE_NORTH = 2;
	public static final int SIDE_SOUTH = 3;

	public static final int SIDE_WEST = 4;
	public static final int SIDE_EAST = 5;
	private static Pattern pattern = Pattern.compile("^[a-z0-9_]*$");

	// -
	public static String clean(String string) {
		if (string == null)
			return "";

		string = string.replaceAll(" +", " ");
		string = string.replace('\t', ' ');
		string = string.trim();

		return string;
	}

	public static boolean isValidID(String string) {
		Matcher matcher;

		if (string == null || string.isEmpty())
			return false;
		matcher = pattern.matcher(string.toLowerCase());

		return matcher.find();
	}

	public static int randInt(int min, int max, Random random) {

		return random.nextInt(max - min) + min;
	}

	public static String generateString(int length, Random random) {
		char[] text = new char[length];
		String characters = "abcdefghijklmnopqrstuvwxyz0123456789";

		for (int i = 0; i < length; i++) {
			text[i] = characters.charAt(random.nextInt(characters.length()));
		}

		return new String(text);
	}

	public static float clamp(float min, float value, float max) {
		return value < min ? min : value > max ? max : value;
	}

	public static boolean isValidCard(ItemStack stack) {
		return stack != null && stack.getItem() instanceof CardItem && hasCDWD(stack);
	}

	public static boolean hasCDWD(ItemStack stack) {
		return stack != null && stack.hasTagCompound() && stack.stackTagCompound.hasKey("cdwd");
	}
}
