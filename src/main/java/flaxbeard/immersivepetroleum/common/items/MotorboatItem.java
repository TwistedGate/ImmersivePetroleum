package flaxbeard.immersivepetroleum.common.items;

import blusunrize.immersiveengineering.api.tool.IUpgrade;
import blusunrize.immersiveengineering.api.tool.IUpgradeableTool;
import blusunrize.immersiveengineering.api.utils.ItemUtils;
import blusunrize.immersiveengineering.common.gui.IESlot;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.entity.MotorboatEntity;
import flaxbeard.immersivepetroleum.common.util.IPItemStackHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class MotorboatItem extends IPItemBase implements IUpgradeableTool{
	public static final String UPGRADE_TYPE = "MOTORBOAT";
	
	public MotorboatItem(){
		super(new Item.Properties().stacksTo(1));
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt){
		return new IPItemStackHandler(4);
	}
	
	@Override
	public CompoundTag getUpgrades(ItemStack stack){
		return stack.hasTag() ? stack.getOrCreateTag().getCompound("upgrades") : new CompoundTag();
	}
	
	@Override
	public void clearUpgrades(ItemStack stack){
		ItemUtils.removeTag(stack, "upgrades");
	}
	
	protected NonNullList<ItemStack> getContainedItems(ItemStack stack){
		IItemHandler handler = stack.getCapability(ForgeCapabilities.ITEM_HANDLER).orElse(null);
		
		if(handler == null){
			ImmersivePetroleum.log.debug("No valid inventory handler found for " + stack);
			return NonNullList.create();
		}
		
		if(handler instanceof IPItemStackHandler ipStackHandler){
			return ipStackHandler.getContainedItems();
		}
		
		ImmersivePetroleum.log.warn("Inefficiently getting contained items. Why does " + stack + " have a non-IP IItemHandler?");
		NonNullList<ItemStack> inv = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
		for(int i = 0;i < handler.getSlots();++i){
			inv.set(i, handler.getStackInSlot(i));
		}
		
		return inv;
	}
	
	@Override
	public boolean canTakeFromWorkbench(ItemStack stack){
		return true;
	}
	
	@Override
	public boolean canModify(ItemStack stack){
		return true;
	}
	
	@Override
	public void recalculateUpgrades(ItemStack stack, Level w, Player player){
		if(w.isClientSide){
			return;
		}
		
		clearUpgrades(stack);
		
		LazyOptional<IItemHandler> lazy = stack.getCapability(ForgeCapabilities.ITEM_HANDLER);
		lazy.ifPresent(handler -> {
			CompoundTag nbt = new CompoundTag();
			
			for(int i = 0;i < handler.getSlots();i++){
				ItemStack u = handler.getStackInSlot(i);
				if(u.getItem() instanceof IUpgrade upg){
					if(upg.getUpgradeTypes(u).contains(UPGRADE_TYPE) && upg.canApplyUpgrades(stack, u)){
						upg.applyUpgrades(stack, u, nbt);
					}
				}
			}
			
			stack.getOrCreateTag().put("upgrades", nbt);
			finishUpgradeRecalculation(stack);
		});
	}
	
	@Override
	public void removeFromWorkbench(Player player, ItemStack stack){
	}
	
	@Override
	public void finishUpgradeRecalculation(ItemStack stack){
	}
	
	@Override
	public Slot[] getWorkbenchSlots(AbstractContainerMenu container, ItemStack stack, Level world, Supplier<Player> getPlayer, IItemHandler inv){
		if(inv != null){
			return new Slot[]{
					new IESlot.Upgrades(container, inv, 0, 78, 35 - 5, UPGRADE_TYPE, stack, true, world, getPlayer),
					new IESlot.Upgrades(container, inv, 1, 98, 35 + 5, UPGRADE_TYPE, stack, true, world, getPlayer),
					new IESlot.Upgrades(container, inv, 2, 118, 35 - 5, UPGRADE_TYPE, stack, true, world, getPlayer)
			};
		}else{
			return new Slot[0];
		}
	}
	
	@Override
	@Nonnull
	public Component getName(@Nonnull ItemStack stack){
		boolean hasUpgrades = getContainedItems(stack).stream().anyMatch(s -> s != ItemStack.EMPTY);
		
		Component c = super.getName(stack);
		if(hasUpgrades){
			c = Component.translatable("desc.immersivepetroleum.flavour.speedboat.prefix").append(c).withStyle(ChatFormatting.GOLD);
		}
		return c;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void appendHoverText(ItemStack stack, Level worldIn, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn){
		if(stack.hasTag()){
			CompoundTag tag = stack.getTag();
			
			if(tag.contains("tank")){
				FluidStack fs = FluidStack.loadFluidStackFromNBT(tag.getCompound("tank"));
				if(fs != null){
					tooltip.add(((MutableComponent) fs.getDisplayName()).append(": " + fs.getAmount() + "mB").withStyle(ChatFormatting.GRAY));
				}
			}
		}
		
		stack.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
			for(int i = 0;i < handler.getSlots();i++){
				if(handler.getStackInSlot(i).isEmpty())
					continue;
				
				tooltip.add(Component.translatable("desc.immersivepetroleum.flavour.speedboat.upgrade", i + 1).append(handler.getStackInSlot(i).getHoverName()));
			}
		});
		
		super.appendHoverText(stack, worldIn, tooltip, flagIn);
	}
	
	@Override
	@Nonnull
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, @Nonnull InteractionHand handIn){
		ItemStack itemstack = playerIn.getItemInHand(handIn);
		float f1 = playerIn.xRotO + (playerIn.getXRot() - playerIn.xRotO);
		float f2 = playerIn.yRotO + (playerIn.getYRot() - playerIn.yRotO);
		double d0 = playerIn.xo + (playerIn.getX() - playerIn.xo);
		double d1 = playerIn.yo + (playerIn.getY() - playerIn.yo) + (double) playerIn.getEyeHeight();
		double d2 = playerIn.zo + (playerIn.getZ() - playerIn.zo);
		Vec3 vec3d = new Vec3(d0, d1, d2);
		float f3 = Mth.cos(-f2 * 0.017453292F - (float) Math.PI);
		float f4 = Mth.sin(-f2 * 0.017453292F - (float) Math.PI);
		float f5 = -Mth.cos(-f1 * 0.017453292F);
		float f6 = Mth.sin(-f1 * 0.017453292F);
		float f7 = f4 * f5;
		float f8 = f3 * f5;
		
		Vec3 vec3d1 = vec3d.add((double) f7 * 5.0D, (double) f6 * 5.0D, (double) f8 * 5.0D);
		HitResult raytraceresult = worldIn.clip(new ClipContext(vec3d, vec3d1, ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, playerIn));
		
		Vec3 vec3d2 = playerIn.getViewVector(1.0F);
		boolean flag = false;
		AABB bb = playerIn.getBoundingBox();
		
		List<Entity> list = worldIn.getEntities(playerIn, bb.expandTowards(vec3d2.x * 5.0D, vec3d2.y * 5.0D, vec3d2.z * 5.0D).inflate(1.0D));
		for(Entity entity:list){
			if(entity.isPickable()){
				AABB axisalignedbb = entity.getBoundingBox();
				if(axisalignedbb.inflate(entity.getPickRadius()).contains(vec3d)){
					flag = true;
				}
			}
		}
		
		if(flag){
			return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
		}else if(raytraceresult.getType() != HitResult.Type.BLOCK){
			return new InteractionResultHolder<>(InteractionResult.PASS, itemstack);
		}else{
			Vec3 hit = raytraceresult.getLocation();
			Block block = worldIn.getBlockState(BlockPos.containing(hit.add(0, .5, 0))).getBlock();
			boolean flag1 = block == Blocks.WATER;
			MotorboatEntity entityboat = new MotorboatEntity(worldIn, hit.x, flag1 ? hit.y - 0.12D : hit.y, hit.z);
			{
				entityboat.setYRot(playerIn.yRotO);
				entityboat.setUpgrades(getContainedItems(itemstack));
				entityboat.readTank(itemstack.getTag());
			}
			
			if(worldIn.getBlockCollisions(entityboat, entityboat.getBoundingBox().inflate(-0.1D)).iterator().hasNext()){
				return new InteractionResultHolder<>(InteractionResult.FAIL, itemstack);
			}else{
				if(!worldIn.isClientSide){
					worldIn.addFreshEntity(entityboat);
				}
				
				if(!playerIn.isCreative()){
					itemstack.shrink(1);
				}
				
				// playerIn.addStat(net.minecraft.stats.Stats.CUSTOM.get(getRegistryName()));
				return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
			}
		}
	}
}
