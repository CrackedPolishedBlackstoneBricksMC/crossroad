package agency.highlysuspect.crossroadtest;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelSettings;

//Some nonsensical code, but it refers to things from a bunch of Minecraft versions.
public class CrossroadTest {
	public static LevelSettings serverDemoSettings = MinecraftServer.DEMO_SETTINGS;
	
	public static void main(String[] args) {
		Minecraft minecraft = Minecraft.getInstance();
		
		KeyMapping key = minecraft.options.keyInventory;
		key.consumeClick();
		
		System.out.println(new ItemStack(Items.ANVIL, 32).getOrCreateTag().toString());
		System.out.println(serverDemoSettings);
		
		//tests that type parameters sorta work
		Registry<Item> yeayhhhthth = null;
	}
}
