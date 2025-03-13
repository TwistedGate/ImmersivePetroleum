package flaxbeard.immersivepetroleum.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import flaxbeard.immersivepetroleum.api.crafting.LubricantHandler;
import flaxbeard.immersivepetroleum.api.crafting.LubricatedHandler;
import flaxbeard.immersivepetroleum.common.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public class OilCanItem extends IPItemBase{
	public OilCanItem(){
		super(new Item.Properties().stacksTo(1));
	}
	
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt){
		if(!stack.isEmpty()){
			return new FluidHandlerItemStack(stack, 8000);
		}
		
		return null;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(@Nonnull ItemStack stack, Level worldIn, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn){
		if(ForgeCapabilities.FLUID_HANDLER_ITEM == null)
			return;
		
		FluidUtil.getFluidContained(stack).ifPresent(fluid -> {
			if(!fluid.isEmpty() && fluid.getAmount() > 0){
				Component out = ((MutableComponent) fluid.getDisplayName())
						.append(Component.literal(": " + fluid.getAmount() + "/8000mB")).withStyle(ChatFormatting.GRAY);
				tooltip.add(out);
			}else{
				tooltip.add(Component.literal(I18n.get(Lib.DESC_FLAVOUR + "drill.empty")));
			}
		});
	}
	
	@Override
	@Nonnull
	public InteractionResult useOn(UseOnContext context){
		ItemStack stack = context.getItemInHand();
		Player player = context.getPlayer();
		InteractionHand hand = context.getHand();
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		
		if(!world.isClientSide){
			BlockEntity tileEntity = world.getBlockEntity(pos);
			if(tileEntity != null){
				IFluidHandler cap = tileEntity.getCapability(ForgeCapabilities.FLUID_HANDLER).orElse(null);
				
				if(cap != null && FluidUtil.interactWithFluidHandler(player, hand, cap)){
					return InteractionResult.SUCCESS;
				}else{
					InteractionResult ret = FluidUtil.getFluidHandler(stack).map(handler -> {
						if(handler instanceof FluidHandlerItemStack can){
							FluidStack fs = can.getFluid();
							
							if(!fs.isEmpty() && LubricantHandler.isValidLube(fs.getFluid())){
								int amountNeeded = (LubricantHandler.getLubeAmount(fs.getFluid()) * 5 * 20);
								if(fs.getAmount() >= amountNeeded && LubricatedHandler.lubricateTile(world.getBlockEntity(pos), fs.getFluid(), 600)){ // 30 Seconds
									player.playSound(SoundEvents.BUCKET_EMPTY, 1F, 1F);
									if(!player.isCreative()){
										can.drain(amountNeeded, FluidAction.EXECUTE);
									}
									Utils.unlockIPAdvancement(player, "main/oil_can");
									return InteractionResult.SUCCESS;
								}
							}
						}
						
						return InteractionResult.PASS;
					}).orElse(InteractionResult.PASS);
					
					return ret;
				}
			}
		}
		
		return InteractionResult.PASS;
	}
	
	@Override
	public boolean hurtEnemy(@Nonnull ItemStack stack, @Nonnull LivingEntity target, @Nonnull LivingEntity attacker){
		this.interactLivingEntity(stack, null, target, InteractionHand.MAIN_HAND);
		return true;
	}
	
	@Override
	@Nonnull
	public InteractionResult interactLivingEntity(@Nonnull ItemStack stack, @Nonnull Player player, @Nonnull LivingEntity target, @Nonnull InteractionHand hand){
		if(target instanceof IronGolem golem){
			
			FluidUtil.getFluidHandler(stack).ifPresent(con -> {
				if(con instanceof FluidHandlerItemStack handler){
					
					if(!handler.getFluid().isEmpty() && LubricantHandler.isValidLube(handler.getFluid().getFluid())){
						int amountNeeded = (LubricantHandler.getLubeAmount(handler.getFluid().getFluid()) * 5 * 20);
						if(handler.getFluid().getAmount() >= amountNeeded){
							player.playSound(SoundEvents.BUCKET_EMPTY, 1F, 1F);
							golem.setHealth(Math.max(golem.getHealth() + 2F, golem.getMaxHealth()));
							golem.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1200, 1)); // 1 Minute
							golem.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 1)); // 1 Minute
							if(!player.isCreative()){
								handler.drain(amountNeeded, FluidAction.EXECUTE);
							}
						}
					}
				}
			});
			
			return InteractionResult.SUCCESS;
		}else
			return InteractionResult.FAIL;
	}
	
	// TODO Where'd container item stuff go?!
	//@Override
	public boolean hasContainerItem(ItemStack stack){
		return ItemNBTHelper.hasKey(stack, "jerrycanDrain") || FluidUtil.getFluidContained(stack).isPresent();
	}
	
	//@Override
	public ItemStack getContainerItem(ItemStack stack){
		if(ItemNBTHelper.hasKey(stack, "jerrycanDrain")){
			ItemStack ret = stack.copy();
			FluidUtil.getFluidHandler(ret).ifPresent(handler -> {
				handler.drain(ItemNBTHelper.getInt(ret, "jerrycanDrain"), FluidAction.EXECUTE);
				ItemNBTHelper.remove(ret, "jerrycanDrain");
			});
			return ret;
		}else if(FluidUtil.getFluidContained(stack).isPresent()){
			ItemStack ret = stack.copy();
			FluidUtil.getFluidHandler(ret).ifPresent(handler -> handler.drain(1000, FluidAction.EXECUTE));
			return ret;
		}
		return stack;
	}
}
