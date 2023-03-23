package flaxbeard.immersivepetroleum.common.util;

import java.text.DecimalFormat;
import java.util.function.Consumer;

import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

/**
 * General purpose Utilities
 * 
 * @author TwistedGate
 */
public class Utils{
	static final DecimalFormat FORMATTER = new DecimalFormat("#,###.##");
	
	public static String fDecimal(byte number){
		return FORMATTER.format(number);
	}
	
	public static String fDecimal(short number){
		return FORMATTER.format(number);
	}
	
	public static String fDecimal(int number){
		return FORMATTER.format(number);
	}
	
	public static String fDecimal(long number){
		return FORMATTER.format(number);
	}
	
	public static String fDecimal(float number){
		return FORMATTER.format(number);
	}
	
	public static String fDecimal(double number){
		return FORMATTER.format(number);
	}
	
	/** Copy of {@link blusunrize.immersiveengineering.common.util.Utils#unlockIEAdvancement(Player, String)} */
	public static void unlockIPAdvancement(Player player, String name){
		if(player instanceof ServerPlayer serverPlayer){
			PlayerAdvancements advancements = serverPlayer.getAdvancements();
			ServerAdvancementManager manager = ((ServerLevel) serverPlayer.getCommandSenderWorld()).getServer().getAdvancements();
			Advancement advancement = manager.getAdvancement(ResourceUtils.ip(name));
			if(advancement != null)
				advancements.award(advancement, "code_trigger");
		}
	}
	
	/** Copy of {@link blusunrize.immersiveengineering.common.util.Utils#isFluidRelatedItemStack(ItemStack)} */
	public static boolean isFluidRelatedItemStack(ItemStack stack){
		if(stack.isEmpty())
			return false;
		return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).isPresent();
	}
	
	public static void dropItem(Level level, BlockPos pos, ItemStack stack){
		dropItem(level, pos, stack, ItemEntity::setDefaultPickUpDelay);
	}
	
	public static void dropItemNoDelay(Level level, BlockPos pos, ItemStack stack){
		dropItem(level, pos, stack, ItemEntity::setNoPickUpDelay);
	}
	
	public static void dropItem(Level level, BlockPos pos, ItemStack stack, Consumer<ItemEntity> func){
		if(!level.isClientSide && !stack.isEmpty() && !level.restoringBlockSnapshots){
			double f = EntityType.ITEM.getHeight() / 2.0D;
			double x = pos.getX() + 0.5D;
			double y = pos.getY() + 0.5D - f;
			double z = pos.getZ() + 0.5D;
			
			ItemEntity entity = new ItemEntity(level, x, y, z, stack);
			func.accept(entity);
			level.addFreshEntity(entity);
		}
	}
}
