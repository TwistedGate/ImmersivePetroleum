package flaxbeard.immersivepetroleum.common;

import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ExternalModContent{
	// Blocks
	private static RegistryObject<Block> IE_REDSTONE_ENGINEERING_BLOCK;
	
	// Items
	private static RegistryObject<Item> IE_ITEM_PIPE;
	private static RegistryObject<Item> IE_ITEM_BUCKSHOT;
	private static RegistryObject<Item> IE_ITEM_EMPTY_SHELL;
	
	// Fluids
	private static RegistryObject<Fluid> IE_FLUID_CONCRETE;
	
	public static void init(){
		IE_REDSTONE_ENGINEERING_BLOCK = RegistryObject.create(ResourceUtils.ie("rs_engineering"), ForgeRegistries.BLOCKS);
		
		IE_ITEM_PIPE = RegistryObject.create(ResourceUtils.ie("fluid_pipe"), ForgeRegistries.ITEMS);
		IE_ITEM_BUCKSHOT = RegistryObject.create(ResourceUtils.ie("buckshot"), ForgeRegistries.ITEMS);
		IE_ITEM_EMPTY_SHELL = RegistryObject.create(ResourceUtils.ie("empty_shell"), ForgeRegistries.ITEMS);
		IE_FLUID_CONCRETE = RegistryObject.create(ResourceUtils.ie("concrete"), ForgeRegistries.FLUIDS);
	}
	
	public static Fluid getIEFluid_Concrete(){
		return IE_FLUID_CONCRETE.get();
	}
	
	public static FluidStack getIEFluid_Concrete(int amount){
		return new FluidStack(IE_FLUID_CONCRETE.get(), amount);
	}
	
	public static Block getIEBlock_RedstoneEngineering(){
		return IE_REDSTONE_ENGINEERING_BLOCK.get();
	}
	
	public static Item getIEItem_Buckshot(){
		return IE_ITEM_BUCKSHOT.get();
	}
	
	public static Item getIEItem_EmptyShell(){
		return IE_ITEM_EMPTY_SHELL.get();
	}
	
	public static Item getIEItem_Pipe(){
		return IE_ITEM_PIPE.get();
	}
	
	public static boolean isIEBlock_RedstoneEngineering(Block block){
		return block.equals(getIEBlock_RedstoneEngineering());
	}
	
	public static boolean isIEItem_Buckshot(ItemStack stack){
		return isIEItem_Buckshot(stack.getItem());
	}
	
	public static boolean isIEItem_Buckshot(Item item){
		return item.equals(getIEItem_Buckshot());
	}
	
	public static boolean isIEItem_EmptyShell(ItemStack stack){
		return isIEItem_EmptyShell(stack.getItem());
	}
	
	public static boolean isIEItem_EmptyShell(Item item){
		return item.equals(getIEItem_EmptyShell());
	}
	
	public static boolean isIEItem_Pipe(ItemStack stack){
		return isIEItem_Pipe(stack.getItem());
	}
	
	public static boolean isIEItem_Pipe(Item item){
		return item.equals(getIEItem_Pipe());
	}
}
