package flaxbeard.immersivepetroleum.common.items;

import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class GasolineBottleItem extends IPItemBase{
	/** How much gasoline a filled bottle contains in mB. */
	public static final int FILLED_AMOUNT = 250;
	
	public GasolineBottleItem(){
		super(new Item.Properties().stacksTo(16));
	}
	
	@Override
	public int getMaxStackSize(ItemStack stack){
		return 16;
	}
	
	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand){
		ItemStack stack = player.getItemInHand(usedHand);
		
		HitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Block.COLLIDER);
		if(hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult bHit){
			BlockPos pos = bHit.getBlockPos();
			if(!level.mayInteract(player, pos)){
				return InteractionResultHolder.pass(stack);
			}
			
			BlockEntity be = level.getBlockEntity(pos);
			if(be != null){
				IFluidHandler fh = be.getCapability(ForgeCapabilities.FLUID_HANDLER, null).orElse(null);
				if(fh != null){
					FluidStack fs = new FluidStack(IPContent.Fluids.GASOLINE.get(), GasolineBottleItem.FILLED_AMOUNT);
					if(fh.fill(fs, FluidAction.SIMULATE) >= GasolineBottleItem.FILLED_AMOUNT){
						fh.fill(fs, FluidAction.EXECUTE);
						
						toEmptyBottle(player, stack);
						
						return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
					}
				}
				
				return InteractionResultHolder.pass(stack);
			}
		}
		
		return InteractionResultHolder.pass(stack);
	}
	
	public void toEmptyBottle(Player player, ItemStack stack){
		if(player.getAbilities().instabuild)
			return;
		
		stack.shrink(1);
		ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
		if(!player.addItem(bottle)){
			player.drop(bottle, false);
		}
	}
	
	/** Modified copy of {@link Item#getPlayerPOVHitResult(Level, Player, net.minecraft.world.level.ClipContext.Fluid)} */
	protected static BlockHitResult getPlayerPOVHitResult(Level level, Player player, ClipContext.Block blockMode){
		float f = player.getXRot();
		float f1 = player.getYRot();
		Vec3 vec3 = player.getEyePosition();
		float f2 = Mth.cos(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
		float f3 = Mth.sin(-f1 * ((float) Math.PI / 180F) - (float) Math.PI);
		float f4 = -Mth.cos(-f * ((float) Math.PI / 180F));
		float f5 = Mth.sin(-f * ((float) Math.PI / 180F));
		float f6 = f3 * f4;
		float f7 = f2 * f4;
		double d0 = player.getBlockReach();
		Vec3 vec31 = vec3.add((double) f6 * d0, (double) f5 * d0, (double) f7 * d0);
		return level.clip(new ClipContext(vec3, vec31, blockMode, ClipContext.Fluid.NONE, player));
	}
}
