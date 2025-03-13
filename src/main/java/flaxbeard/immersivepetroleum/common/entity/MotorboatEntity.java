package flaxbeard.immersivepetroleum.common.entity;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.IESounds;
import com.google.common.collect.Lists;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.energy.FuelHandler;
import flaxbeard.immersivepetroleum.common.IPContent;
import flaxbeard.immersivepetroleum.common.IPContent.BoatUpgrades;
import flaxbeard.immersivepetroleum.common.items.DebugItem;
import flaxbeard.immersivepetroleum.common.items.GasolineBottleItem;
import flaxbeard.immersivepetroleum.common.items.MotorboatItem;
import flaxbeard.immersivepetroleum.common.network.IPPacketHandler;
import flaxbeard.immersivepetroleum.common.network.MessageConsumeBoatFuel;
import flaxbeard.immersivepetroleum.common.util.IPItemStackHandler;
import flaxbeard.immersivepetroleum.common.util.RegistryUtils;
import flaxbeard.immersivepetroleum.common.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.List;

public class MotorboatEntity extends Boat implements IEntityAdditionalSpawnData{
	
	public static EntityDataAccessor<Byte> getFlags(){
		return DATA_SHARED_FLAGS_ID;
	}
	
	/**
	 * Storage for {@link ResourceLocation} using {@link ResourceLocation#toString()}
	 */
	static final EntityDataAccessor<String> TANK_FLUID = SynchedEntityData.defineId(MotorboatEntity.class, EntityDataSerializers.STRING);
	static final EntityDataAccessor<Integer> TANK_AMOUNT = SynchedEntityData.defineId(MotorboatEntity.class, EntityDataSerializers.INT);
	
	static final EntityDataAccessor<ItemStack> UPGRADE_0 = SynchedEntityData.defineId(MotorboatEntity.class, EntityDataSerializers.ITEM_STACK);
	static final EntityDataAccessor<ItemStack> UPGRADE_1 = SynchedEntityData.defineId(MotorboatEntity.class, EntityDataSerializers.ITEM_STACK);
	static final EntityDataAccessor<ItemStack> UPGRADE_2 = SynchedEntityData.defineId(MotorboatEntity.class, EntityDataSerializers.ITEM_STACK);
	static final EntityDataAccessor<ItemStack> UPGRADE_3 = SynchedEntityData.defineId(MotorboatEntity.class, EntityDataSerializers.ITEM_STACK);
	
	public boolean isFireproof = false;
	public boolean hasIcebreaker = false;
	public boolean hasTank = false;
	public boolean hasRudders = false;
	public boolean hasPaddles = false;
	public boolean isBoosting = false;
	public float lastMoving;
	
	public float propellerYRotation = 0.0F;
	public float propellerXRot = 0.0F;
	public float propellerXRotSpeed = 0.0F;
	
	public MotorboatEntity(Level world){
		this(IPEntityTypes.MOTORBOAT.get(), world);
	}
	
	public MotorboatEntity(Level world, double x, double y, double z){
		this(IPEntityTypes.MOTORBOAT.get(), world);
		setPos(x, y, z);
		this.xo = x;
		this.yo = y;
		this.zo = z;
	}
	
	public MotorboatEntity(EntityType<MotorboatEntity> type, Level world){
		super(type, world);
		this.blocksBuilding = true;
	}
	
	@Override
	protected void defineSynchedData(){
		super.defineSynchedData();
		this.entityData.define(TANK_FLUID, "");
		this.entityData.define(TANK_AMOUNT, 0);
		this.entityData.define(UPGRADE_0, ItemStack.EMPTY);
		this.entityData.define(UPGRADE_1, ItemStack.EMPTY);
		this.entityData.define(UPGRADE_2, ItemStack.EMPTY);
		this.entityData.define(UPGRADE_3, ItemStack.EMPTY);
	}
	
	@Override
	protected void readAdditionalSaveData(@Nonnull CompoundTag compound){
		super.readAdditionalSaveData(compound);
		
		String fluid = "";
		int amount = 0;
		ItemStack stack0 = ItemStack.EMPTY;
		ItemStack stack1 = ItemStack.EMPTY;
		ItemStack stack2 = ItemStack.EMPTY;
		ItemStack stack3 = ItemStack.EMPTY;
		
		if(compound.contains("tank")){
			CompoundTag tank = compound.getCompound("tank");
			fluid = tank.getString("fluid");
			amount = tank.getInt("amount");
		}
		
		if(compound.contains("upgrades")){
			CompoundTag upgrades = compound.getCompound("upgrades");
			stack0 = ItemStack.of(upgrades.getCompound("0"));
			stack1 = ItemStack.of(upgrades.getCompound("1"));
			stack2 = ItemStack.of(upgrades.getCompound("2"));
			stack3 = ItemStack.of(upgrades.getCompound("3"));
		}
		
		this.entityData.set(TANK_FLUID, fluid);
		this.entityData.set(TANK_AMOUNT, amount);
		this.entityData.set(UPGRADE_0, stack0);
		this.entityData.set(UPGRADE_1, stack1);
		this.entityData.set(UPGRADE_2, stack2);
		this.entityData.set(UPGRADE_3, stack3);
	}
	
	@Override
	protected void addAdditionalSaveData(@Nonnull CompoundTag compound){
		super.addAdditionalSaveData(compound);
		
		String fluid = this.entityData.get(TANK_FLUID);
		int amount = this.entityData.get(TANK_AMOUNT);
		ItemStack stack0 = this.entityData.get(UPGRADE_0);
		ItemStack stack1 = this.entityData.get(UPGRADE_1);
		ItemStack stack2 = this.entityData.get(UPGRADE_2);
		ItemStack stack3 = this.entityData.get(UPGRADE_3);
		
		CompoundTag tank = new CompoundTag();
		tank.putString("fluid", fluid);
		tank.putInt("amount", amount);
		compound.put("tank", tank);
		
		CompoundTag upgrades = new CompoundTag();
		upgrades.put("0", stack0.serializeNBT());
		upgrades.put("1", stack1.serializeNBT());
		upgrades.put("2", stack2.serializeNBT());
		upgrades.put("3", stack3.serializeNBT());
		compound.put("upgrades", upgrades);
	}
	
	public void setUpgrades(NonNullList<ItemStack> stacks){
		if(stacks != null && stacks.size() > 0){
			ItemStack o0 = stacks.get(0);
			ItemStack o1 = stacks.get(1);
			ItemStack o2 = stacks.get(2);
			ItemStack o3 = stacks.get(3);
			this.entityData.set(UPGRADE_0, o0);
			this.entityData.set(UPGRADE_1, o1);
			this.entityData.set(UPGRADE_2, o2);
			this.entityData.set(UPGRADE_3, o3);
		}
	}
	
	public boolean isLeftDown(){
		return this.inputLeft;
	}
	
	public boolean isRightDown(){
		return this.inputRight;
	}
	
	public boolean isForwardDown(){
		return this.inputUp;
	}
	
	public boolean isBackDown(){
		return this.inputDown;
	}
	
	@Override
	public void onSyncedDataUpdated(@Nonnull EntityDataAccessor<?> key){
		super.onSyncedDataUpdated(key);
		if(key == UPGRADE_0 || key == UPGRADE_1 || key == UPGRADE_2 || key == UPGRADE_3){
			NonNullList<ItemStack> upgrades = getUpgrades();
			this.isFireproof = false;
			this.hasIcebreaker = false;
			for(ItemStack upgrade:upgrades){
				if(upgrade != null && upgrade != ItemStack.EMPTY){
					Item item = upgrade.getItem();
					if(item == BoatUpgrades.REINFORCED_HULL.get()){
						this.isFireproof = true;
					}else if(item == BoatUpgrades.ICE_BREAKER.get()){
						this.hasIcebreaker = true;
					}else if(item == BoatUpgrades.TANK.get()){
						this.hasTank = true;
					}else if(item == BoatUpgrades.RUDDERS.get()){
						this.hasRudders = true;
					}else if(item == BoatUpgrades.PADDLES.get()){
						this.hasPaddles = true;
					}
				}
			}
		}
	}
	
	public void setContainedFluid(FluidStack stack){
		if(stack == null){
			this.entityData.set(TANK_FLUID, "");
			this.entityData.set(TANK_AMOUNT, 0);
		}else{
			this.entityData.set(TANK_FLUID, stack.getFluid() == null ? "" : RegistryUtils.getRegistryNameOf(stack.getFluid()).toString());
			this.entityData.set(TANK_AMOUNT, stack.getAmount());
		}
	}
	
	public FluidStack getContainedFluid(){
		String fluidName = this.entityData.get(TANK_FLUID);
		int amount = this.entityData.get(TANK_AMOUNT);
		
		if(fluidName == null || fluidName.isEmpty() || amount == 0)
			return FluidStack.EMPTY;
		
		Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(fluidName));
		if(fluid == null)
			return FluidStack.EMPTY;
		
		return new FluidStack(fluid, amount);
	}
	
	@Override
	public double getPassengersRidingOffset(){
		return 0.05D;
	}
	
	@Override
	@Nonnull
	public Vec3 getDismountLocationForPassenger(LivingEntity pLivingEntity){
		Vec3 vec3 = getCollisionHorizontalEscapeVector(this.getBbWidth() * Mth.SQRT_OF_TWO, pLivingEntity.getBbWidth(), pLivingEntity.getYRot());
		double d0 = this.getX() + vec3.x;
		double d1 = this.getZ() + vec3.z;
		BlockPos blockpos = BlockPos.containing(d0, this.getBoundingBox().maxY, d1);
		BlockPos blockpos1 = blockpos.below();
		if(!this.level().isWaterAt(blockpos1) && !this.level().getFluidState(blockpos1).is(FluidTags.LAVA)){
			List<Vec3> list = Lists.newArrayList();
			double d2 = this.level().getBlockFloorHeight(blockpos);
			if(DismountHelper.isBlockFloorValid(d2)){
				list.add(new Vec3(d0, (double) blockpos.getY() + d2, d1));
			}
			
			double d3 = this.level().getBlockFloorHeight(blockpos1);
			if(DismountHelper.isBlockFloorValid(d3)){
				list.add(new Vec3(d0, (double) blockpos1.getY() + d3, d1));
			}
			
			for(Pose pose:pLivingEntity.getDismountPoses()){
				for(Vec3 vec31:list){
					if(DismountHelper.canDismountTo(this.level(), vec31, pLivingEntity, pose)){
						pLivingEntity.setPose(pose);
						return vec31;
					}
				}
			}
		}
		
		return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
	}
	
	@Override
	public boolean hurt(@Nonnull DamageSource source, float amount){
		if(isInvulnerableTo(source) || (this.isFireproof && source.is(DamageTypeTags.IS_FIRE))){
			return false;
		}else if(!this.level().isClientSide && isAlive()){
			if(source.isIndirect() && source.getDirectEntity() != null && hasPassenger(source.getDirectEntity())){
				return false;
			}else{
				setHurtDir(-getHurtDir());
				setHurtTime(10);
				setDamage(getDamage() + amount * 10.0F);
				markHurt();
				boolean isPlayer = source.getDirectEntity() instanceof Player;
				boolean isCreativePlayer = isPlayer && ((Player) source.getDirectEntity()).getAbilities().instabuild;
				if((isCreativePlayer || getDamage() > 40.0F) && (!this.isFireproof || isPlayer) || (getDamage() > 240.0F)){
					if(!isCreativePlayer && this.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)){
						MotorboatItem item = (MotorboatItem) getDropItem();
						ItemStack stack = new ItemStack(item, 1);
						
						LazyOptional<IItemHandler> handler = stack.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
						handler.ifPresent(itemHandler ->
						{
							if(itemHandler instanceof IPItemStackHandler){
								NonNullList<ItemStack> upgrades = getUpgrades();
								for(int i = 0;i < itemHandler.getSlots();i++){
									itemHandler.insertItem(i, upgrades.get(i), false);
								}
							}
						});
						writeTank(stack.getOrCreateTag(), true);
						
						if(isPlayer){
							Player player = (Player) source.getDirectEntity();
							if(!player.addItem(stack)){
								ItemEntity itemEntity = new ItemEntity(this.level(), player.getX(), player.getY(), player.getZ(), stack);
								itemEntity.setNoPickUpDelay();
								this.level().addFreshEntity(itemEntity);
							}
						}else{
							spawnAtLocation(stack, 0F);
						}
					}
					
					remove(RemovalReason.DISCARDED);
				}
				
				return true;
			}
		}else{
			return true;
		}
	}
	
	public void readTank(CompoundTag nbt){
		FluidTank tank = new FluidTank(getMaxFuel());
		if(nbt != null)
			tank.readFromNBT(nbt.getCompound("tank"));
		
		setContainedFluid(tank.getFluid());
	}
	
	public void writeTank(CompoundTag nbt, boolean toItem){
		FluidTank tank = new FluidTank(getMaxFuel());
		tank.setFluid(getContainedFluid());
		
		boolean write = tank.getFluidAmount() > 0;
		if(!toItem || write)
			nbt.put("tank", tank.writeToNBT(new CompoundTag()));
	}
	
	@Override
	@Nonnull
	public InteractionResult interact(Player player, @Nonnull InteractionHand hand){
		ItemStack stack = player.getItemInHand(hand);
		
		if(stack != ItemStack.EMPTY && stack.getItem() instanceof DebugItem debugItem){
			debugItem.onSpeedboatClick(this, player, stack);
			return InteractionResult.SUCCESS;
		}
		
		if(Utils.isFluidRelatedItemStack(stack)){
			FluidStack fstack = FluidUtil.getFluidContained(stack).orElse(null);
			if(fstack != null){
				FluidTank tank = getInternalTank();
				
				if(FluidUtil.interactWithFluidHandler(player, hand, tank)){
					setContainedFluid(tank.getFluid());
					advancement(tank, fstack, player);
				}
			}
			return InteractionResult.SUCCESS;
		}
		
		if(stack.getItem() instanceof GasolineBottleItem gasbottle){
			FluidTank tank = getInternalTank();
			FluidStack fstack = new FluidStack(IPContent.Fluids.GASOLINE.get(), GasolineBottleItem.FILLED_AMOUNT);
			
			if(tank.fill(fstack, FluidAction.SIMULATE) >= GasolineBottleItem.FILLED_AMOUNT){
				tank.fill(fstack, FluidAction.EXECUTE);
				
				gasbottle.toEmptyBottle(player, stack);
				
				setContainedFluid(tank.getFluid());
				advancement(tank, fstack, player);
			}
			
			return InteractionResult.SUCCESS;
		}
		
		if(!this.level().isClientSide && !player.isShiftKeyDown() && this.outOfControlTicks < 60.0F && !player.isPassengerOfSameVehicle(this)){
			player.startRiding(this);
			if(this.level().dimension().equals(Level.NETHER) && this.isFireproof){
				Utils.unlockIPAdvancement(player, "main/reinforced_hull");
			}
			return InteractionResult.SUCCESS;
		}
		
		return InteractionResult.FAIL;
	}
	
	private FluidTank getInternalTank(){
		FluidTank tank = new FluidTank(getMaxFuel(), e -> FuelHandler.isValidBoatFuel(e.getFluid()));
		FluidStack fs = getContainedFluid();
		tank.setFluid(fs);
		return tank;
	}
	
	private void advancement(FluidTank tank, FluidStack fstack, Player player){
		if(tank.isFluidValid(fstack)){
			Utils.unlockIPAdvancement(player, "main/motorboat");
			if(this.hasTank && tank.getFluidAmount() == tank.getCapacity()){
				Utils.unlockIPAdvancement(player, "main/tank");
			}
		}
	}
	
	@Override
	public void setInput(boolean pLeftInputDown, boolean pRightInputDown, boolean pForwardInputDown, boolean pBackInputDown){
		super.setInput(pLeftInputDown, pRightInputDown, pForwardInputDown, pBackInputDown);
		this.isBoosting = !isEmergency() && (pForwardInputDown && Minecraft.getInstance().options.keyJump.isDown());
	}
	
	/* Only needed and used on Server side */
	protected float oYRot;
	protected boolean fastEnough;
	protected int oFuelAmount;
	
	/** Does not change client-side */
	public boolean isSpinningFastEnough(){
		return this.fastEnough;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void tick(){
		// Advancement Stuff
		
		if(!this.level().isClientSide){
			// Spin
			{
				float diff = this.getYRot() - this.oYRot;
				this.fastEnough = diff <= -5.0F || diff >= 5.0F;
				this.oYRot = this.getYRot();
			}
			// Fuel
			{
				int current = this.entityData.get(TANK_AMOUNT);
				int diff = current - this.oFuelAmount;
				if(diff != 0 && current == 0){
					if(this.getFirstPassenger() instanceof Player player && this.hasPaddles){
						Utils.unlockIPAdvancement(player, "main/paddles");
					}
				}
				this.oFuelAmount = current;
			}
		}
		
		// -----------------------------------------------------
		
		this.oldStatus = this.status;
		this.status = this.getStatus();
		if(this.status != Boat.Status.UNDER_WATER && this.status != Boat.Status.UNDER_FLOWING_WATER){
			this.outOfControlTicks = 0.0F;
		}else{
			++this.outOfControlTicks;
		}
		
		if(!this.level().isClientSide && this.outOfControlTicks >= 60.0F){
			this.ejectPassengers();
		}
		
		if(this.getHurtTime() > 0){
			this.setHurtTime(this.getHurtTime() - 1);
		}
		
		if(this.getDamage() > 0.0F){
			this.setDamage(this.getDamage() - 1.0F);
		}
		
		this.xo = this.getX();
		this.yo = this.getY();
		this.zo = this.getZ();
		
		{ // From Entity.tick()
			this.baseTick();
		}
		
		this.tickLerp();
		
		if(this.isControlledByLocalInstance()){
			if(!(this.getFirstPassenger() instanceof Player)){
				this.setPaddleState(false, false);
			}
			
			this.floatBoat();
			if(this.level().isClientSide){
				this.controlBoat();
				this.level().sendPacketToServer(new ServerboundPaddleBoatPacket(this.getPaddleState(0), this.getPaddleState(1)));
			}
			
			this.move(MoverType.SELF, this.getDeltaMovement());
		}else{
			this.setDeltaMovement(Vec3.ZERO);
		}
		
		this.tickBubbleColumn();
		
		if(this.level().isClientSide){
			if(!isEmergency()){
				float moving = (this.inputUp || this.inputDown) ? (this.isBoosting ? .9F : .7F) : 0.5F;
				if(this.lastMoving != moving){
					this.lastMoving = moving;
					ImmersivePetroleum.proxy.handleEntitySound(IESounds.dieselGenerator.get(), this, false, .5f, 0.5F);
				}
				ImmersivePetroleum.proxy.handleEntitySound(IESounds.dieselGenerator.get(), this, this.isVehicle() && this.getContainedFluid() != FluidStack.EMPTY && this.getContainedFluid().getAmount() > 0, this.inputUp || this.inputDown ? .5f : .3f, moving);
				
				if(this.inputUp && this.level().random.nextInt(2) == 0){
					if(isInLava()){
						if(this.level().random.nextInt(3) == 0){
							float xO = Mth.sin(-this.getYRot() * 0.017453292F) + (this.level().random.nextFloat() - .5F) * .3F;
							float zO = Mth.cos(this.getYRot() * 0.017453292F) + (this.level().random.nextFloat() - .5F) * .3F;
							float yO = .4F + (this.level().random.nextFloat() - .5F) * .3F;
							Vec3 motion = getDeltaMovement();
							this.level().addParticle(ParticleTypes.LAVA, getX() - xO * 1.5F, getY() + yO, getZ() - zO * 1.5F, -2 * motion.x(), 0, -2 * motion.z());
						}
					}else{
						float xO = Mth.sin(-this.getYRot() * 0.017453292F) + (this.level().random.nextFloat() - .5F) * .3F;
						float zO = Mth.cos(this.getYRot() * 0.017453292F) + (this.level().random.nextFloat() - .5F) * .3F;
						float yO = .1F + (this.level().random.nextFloat() - .5F) * .3F;
						this.level().addParticle(ParticleTypes.BUBBLE, getX() - xO * 1.5F, getY() + yO, getZ() - zO * 1.5F, 0, 0, 0);
					}
				}
				if(this.isBoosting && this.level().random.nextInt(2) == 0){
					float xO = Mth.sin(-this.getYRot() * 0.017453292F) + (this.level().random.nextFloat() - .5F) * .3F;
					float zO = Mth.cos(this.getYRot() * 0.017453292F) + (this.level().random.nextFloat() - .5F) * .3F;
					float yO = .8F + (this.level().random.nextFloat() - .5F) * .3F;
					this.level().addParticle(ParticleTypes.SMOKE, getX() - xO * 1.3F, getY() + yO, getZ() - zO * 1.3F, 0, 0, 0);
				}
			}
		}
		
		if(this.isEmergency()){
			for(int i = 0;i <= 1;++i){
				if(this.getPaddleState(i)){
					if(!this.isSilent() && (double) (this.paddlePositions[i] % ((float) Math.PI * 2F)) <= (double) ((float) Math.PI / 4F) && (double) ((this.paddlePositions[i] + ((float) Math.PI / 8F)) % ((float) Math.PI * 2F)) >= (double) ((float) Math.PI / 4F)){
						SoundEvent soundevent = this.getPaddleSound();
						if(soundevent != null){
							Vec3 vec3 = this.getViewVector(1.0F);
							double d0 = i == 1 ? -vec3.z : vec3.z;
							double d1 = i == 1 ? vec3.x : -vec3.x;
							this.level().playSound((Player) null, this.getX() + d0, this.getY(), this.getZ() + d1, soundevent, this.getSoundSource(), 1.0F, 0.8F + 0.4F * this.random.nextFloat());
							this.level().gameEvent(this.getControllingPassenger(), GameEvent.SPLASH, BlockPos.containing(this.getX() + d0, this.getY(), this.getZ() + d1));
						}
					}
					
					this.paddlePositions[i] += (float) Math.PI / 8F;
				}else{
					this.paddlePositions[i] = 0.0F;
				}
			}
		}else{
			if(this.getPaddleState(0)){
				this.paddlePositions[0] += (this.isBoosting ? 0.02D : 0.01D);
			}else if(this.getPaddleState(1)){
				this.paddlePositions[0] -= 0.01D;
			}
		}
		
		float xO = Mth.sin(-this.getYRot() * 0.017453292F);
		float zO = Mth.cos(this.getYRot() * 0.017453292F);
		Vector3f vec = new Vector3f(xO, zO, 0.0F);
		vec.normalize();
		
		if(!this.level().isClientSide && this.hasIcebreaker && !isEmergency()){
			AABB bb = getBoundingBox().inflate(0.1);
			BlockPos.MutableBlockPos mutableBlockPos0 = new BlockPos.MutableBlockPos(bb.minX + 0.001D, bb.minY + 0.001D, bb.minZ + 0.001D);
			BlockPos.MutableBlockPos mutableBlockPos1 = new BlockPos.MutableBlockPos(bb.maxX - 0.001D, bb.maxY - 0.001D, bb.maxZ - 0.001D);
			BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
			
			if(this.level().hasChunksAt(mutableBlockPos0, mutableBlockPos1)){
				boolean brokeIce = false;
				for(int i = mutableBlockPos0.getX();i <= mutableBlockPos1.getX();++i){
					for(int j = mutableBlockPos0.getY();j <= mutableBlockPos1.getY();++j){
						for(int k = mutableBlockPos0.getZ();k <= mutableBlockPos1.getZ();++k){
							mutableBlockPos2.set(i, j, k);
							BlockState BlockState = this.level().getBlockState(mutableBlockPos2);
							
							Vector3f vec2 = new Vector3f((float) (i + 0.5f - getX()), (float) (k + 0.5f - getZ()), 0.0F);
							vec2.normalize();
							
							float sim = vec2.dot(vec);
							if(BlockState.getBlock() == Blocks.ICE && sim > .3f){
								this.level().destroyBlock(mutableBlockPos2, false);
								this.level().setBlockAndUpdate(mutableBlockPos2, Blocks.WATER.defaultBlockState());
								brokeIce = true;
							}
						}
					}
				}
				
				if(brokeIce && this.getFirstPassenger() instanceof Player player){
					Utils.unlockIPAdvancement(player, "main/ice_breaker");
				}
			}
		}
		
		this.checkInsideBlocks();
		
		if(!this.level().isClientSide){
			List<Entity> list = this.level().getEntities(this, this.getBoundingBox().inflate(0.2F, -0.01F, 0.2F), EntitySelector.pushableBy(this));
			if(!list.isEmpty()){
				boolean flag = !(this.getControllingPassenger() instanceof Player);
				
				for(Entity entity:list){
					if(!entity.hasPassenger(this)){
						if(flag && this.getPassengers().size() < 2 && !entity.isPassenger() && entity.getBbWidth() < this.getBbWidth() && entity instanceof LivingEntity && !(entity instanceof WaterAnimal) && !(entity instanceof Player)){
							entity.startRiding(this);
						}else{
							this.push(entity);
							
							if(this.hasIcebreaker){
								if(entity instanceof LivingEntity && !(entity instanceof Player) && this.getControllingPassenger() instanceof Player player){
									Vector3f vec2 = new Vector3f((float) (entity.getX() - getX()), (float) (entity.getZ() - getZ()), 0.0F);
									vec2.normalize();
									
									float sim = vec2.dot(vec);
									if(sim > .5f){
										Vec3 motion = entity.getDeltaMovement();
										entity.hurt(this.level().damageSources().playerAttack(player), 4);
										entity.setDeltaMovement(new Vec3(motion.x + (vec2.x() * .75F), motion.y, motion.z + (vec2.y() * .75F)));
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	protected void controlBoat(){
		if(isVehicle()){
			float f = 0.0F;
			
			if(isEmergency()){
				if(this.inputLeft){
					--this.deltaRotation;
				}
				
				if(this.inputRight){
					++this.deltaRotation;
				}
				
				if(this.inputRight != this.inputLeft && !this.inputUp && !this.inputDown){
					f += 0.005F;
				}
				
				this.setYRot(this.getYRot() + this.deltaRotation);
				if(this.inputUp){
					f += 0.04F;
				}
				
				if(this.inputDown){
					f -= 0.005F;
				}
				
				double xa = Mth.sin(-this.getYRot() * Mth.DEG_TO_RAD) * f;
				double za = Mth.cos(this.getYRot() * Mth.DEG_TO_RAD) * f;
				Vec3 motion = this.getDeltaMovement().add(xa, 0.0F, za);
				this.setDeltaMovement(motion);
				this.setPaddleState(this.inputRight && !this.inputLeft || this.inputUp, this.inputLeft && !this.inputRight || this.inputUp);
			}else{
				FluidStack fluid = getContainedFluid();
				int consumeAmount = 0;
				if(fluid != FluidStack.EMPTY){
					consumeAmount = FuelHandler.getBoatFuelUse(fluid.getFluid());
				}
				
				if(fluid != FluidStack.EMPTY && fluid.getAmount() >= consumeAmount && (this.inputUp || this.inputDown)){
					int toConsume = consumeAmount;
					if(this.inputUp){
						f += 0.05F;
						if(this.isBoosting && fluid.getAmount() >= 3 * consumeAmount){
							f *= 1.6;
							toConsume *= 3;
						}
					}
					
					if(this.inputDown){
						f -= 0.01F;
					}
					
					fluid.setAmount(Math.max(0, fluid.getAmount() - toConsume));
					setContainedFluid(fluid);
					
					IPPacketHandler.sendToServer(new MessageConsumeBoatFuel(toConsume));
					
					setPaddleState(this.inputUp, this.inputDown);
				}else{
					setPaddleState(false, false);
				}
				
				double xa = Mth.sin(-this.getYRot() * Mth.DEG_TO_RAD) * f;
				double za = Mth.cos(this.getYRot() * Mth.DEG_TO_RAD) * f;
				Vec3 motion = this.getDeltaMovement().add(xa, 0.0F, za);
				this.setDeltaMovement(motion);
				
				if(this.inputLeft || this.inputRight){
					float speed = Mth.sqrt((float) (motion.x * motion.x + motion.z * motion.z));
					
					if(this.inputRight){
						this.deltaRotation += 1.1F * speed * (this.hasRudders ? 1.5F : 1F) * (this.isBoosting ? 0.5F : 1) * (this.inputDown && !this.inputUp ? 2F : 1F);
						
						this.propellerYRotation = Mth.clamp(this.propellerYRotation - 0.2F, -1.0F, 1.0F);
					}
					
					if(this.inputLeft){
						this.deltaRotation -= 1.1F * speed * (this.hasRudders ? 1.5F : 1F) * (this.isBoosting ? 0.5F : 1) * (this.inputDown && !this.inputUp ? 2F : 1F);
						
						this.propellerYRotation = Mth.clamp(this.propellerYRotation + 0.2F, -1.0F, 1.0F);
					}
				}
				
				if((!this.inputLeft && !this.inputRight) && this.propellerYRotation != 0.0F){
					this.propellerYRotation *= 0.7F;
					if(this.propellerYRotation > -1.0E-2F && this.propellerYRotation < 1.0E-2F){
						this.propellerYRotation = 0;
					}
				}
				
				this.setYRot(this.getYRot() + this.deltaRotation);
				this.setPaddleState((this.inputRight && !this.inputLeft || this.inputUp), (this.inputLeft && !this.inputRight || this.inputUp));
			}
		}
	}
	
	public int getMaxFuel(){
		return this.hasTank ? 16000 : 8000;
	}
	
	@Override
	@Nonnull
	public Item getDropItem(){
		return IPContent.Items.SPEEDBOAT.get();
	}
	
	@Override
	public boolean isOnFire(){
		if(this.isFireproof)
			return false;
		
		return super.isOnFire();
	}
	
	public boolean isEmergency(){
		FluidStack fluid = getContainedFluid();
		if(fluid != FluidStack.EMPTY){
			int consumeAmount = FuelHandler.getBoatFuelUse(fluid.getFluid());
			return fluid.getAmount() < consumeAmount && this.hasPaddles;
		}
		
		return this.hasPaddles;
	}
	
	public NonNullList<ItemStack> getUpgrades(){
		NonNullList<ItemStack> stackList = NonNullList.withSize(4, ItemStack.EMPTY);
		stackList.set(0, this.entityData.get(UPGRADE_0));
		stackList.set(1, this.entityData.get(UPGRADE_1));
		stackList.set(2, this.entityData.get(UPGRADE_2));
		stackList.set(3, this.entityData.get(UPGRADE_3));
		return stackList;
	}
	
	public String[] getOverlayText(Player player, HitResult mop){
		if(Utils.isFluidRelatedItemStack(player.getItemInHand(InteractionHand.MAIN_HAND))){
			String s = null;
			FluidStack stack = getContainedFluid();
			if(stack != FluidStack.EMPTY){
				s = stack.getDisplayName().getString() + ": " + stack.getAmount() + "mB";
			}else{
				s = I18n.get(Lib.GUI + "empty");
			}
			return new String[]{s};
			
		}
		return null;
	}
	
	@Override
	public float getWaterLevelAbove(){
		AABB aabb = this.getBoundingBox();
		int i = Mth.floor(aabb.minX);
		int j = Mth.ceil(aabb.maxX);
		int k = Mth.floor(aabb.maxY);
		int l = Mth.ceil(aabb.maxY - this.lastYd);
		int i1 = Mth.floor(aabb.minZ);
		int j1 = Mth.ceil(aabb.maxZ);
		BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos();
		
		label39: for(int k1 = k;k1 < l;++k1){
			float f = 0.0F;
			
			for(int l1 = i;l1 < j;++l1){
				for(int i2 = i1;i2 < j1;++i2){
					blockpos.set(l1, k1, i2);
					FluidState fluidstate = this.level().getFluidState(blockpos);
					if(fluidstate.is(FluidTags.WATER) || (this.isFireproof && fluidstate.is(FluidTags.LAVA))){
						f = Math.max(f, fluidstate.getHeight(this.level(), blockpos));
					}
					
					if(f >= 1.0F){
						continue label39;
					}
				}
			}
			
			if(f < 1.0F){
				return (float) blockpos.getY() + f;
			}
		}
		
		return (float) (l + 1);
	}
	
	@Override
	protected boolean checkInWater(){
		AABB aabb = this.getBoundingBox();
		int i = Mth.floor(aabb.minX);
		int j = Mth.ceil(aabb.maxX);
		int k = Mth.floor(aabb.minY);
		int l = Mth.ceil(aabb.minY + 0.001D);
		int i1 = Mth.floor(aabb.minZ);
		int j1 = Mth.ceil(aabb.maxZ);
		boolean flag = false;
		this.waterLevel = -Double.MAX_VALUE;
		BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos();
		
		for(int k1 = i;k1 < j;++k1){
			for(int l1 = k;l1 < l;++l1){
				for(int i2 = i1;i2 < j1;++i2){
					blockpos.set(k1, l1, i2);
					FluidState fluidstate = this.level().getFluidState(blockpos);
					if(fluidstate.is(FluidTags.WATER) || (this.isFireproof && fluidstate.is(FluidTags.LAVA))){
						float f = (float) l1 + fluidstate.getHeight(this.level(), blockpos);
						this.waterLevel = Math.max(f, this.waterLevel);
						flag |= aabb.minY < (double) f;
					}
				}
			}
		}
		
		return flag;
	}
	
	@Override
	protected Status isUnderwater(){
		AABB aabb = this.getBoundingBox();
		double d0 = aabb.maxY + 0.001D;
		int i = Mth.floor(aabb.minX);
		int j = Mth.ceil(aabb.maxX);
		int k = Mth.floor(aabb.maxY);
		int l = Mth.ceil(d0);
		int i1 = Mth.floor(aabb.minZ);
		int j1 = Mth.ceil(aabb.maxZ);
		boolean flag = false;
		BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos();
		
		for(int k1 = i;k1 < j;++k1){
			for(int l1 = k;l1 < l;++l1){
				for(int i2 = i1;i2 < j1;++i2){
					blockpos.set(k1, l1, i2);
					FluidState fluidstate = this.level().getFluidState(blockpos);
					if((fluidstate.is(FluidTags.WATER) || ((this.isFireproof && fluidstate.is(FluidTags.LAVA)))) && d0 < (double) ((float) blockpos.getY() + fluidstate.getHeight(this.level(), blockpos))){
						if(!fluidstate.isSource()){
							return Boat.Status.UNDER_FLOWING_WATER;
						}
						
						flag = true;
					}
				}
			}
		}
		
		return flag ? Boat.Status.UNDER_WATER : null;
	}
	
	public boolean isLeftInDown(){
		return this.inputLeft;
	}
	
	public boolean isRightInDown(){
		return this.inputRight;
	}
	
	public boolean isForwardInDown(){
		return this.inputUp;
	}
	
	public boolean isBackInDown(){
		return this.inputDown;
	}
	
	@Override
	public boolean getSharedFlag(int flag){
		return super.getSharedFlag(flag);
	}
	
	@Override
	public void setSharedFlag(int flag, boolean set){
		super.setSharedFlag(flag, set);
	}
	
	@Override
	@Nonnull
	public Packet<ClientGamePacketListener> getAddEntityPacket(){
		return NetworkHooks.getEntitySpawningPacket(this);
	}
	
	@Override
	public void readSpawnData(FriendlyByteBuf buffer){
		String fluid = buffer.readUtf();
		int amount = buffer.readInt();
		ItemStack stack0 = buffer.readItem();
		ItemStack stack1 = buffer.readItem();
		ItemStack stack2 = buffer.readItem();
		ItemStack stack3 = buffer.readItem();
		
		this.entityData.set(TANK_FLUID, fluid);
		this.entityData.set(TANK_AMOUNT, amount);
		this.entityData.set(UPGRADE_0, stack0);
		this.entityData.set(UPGRADE_1, stack1);
		this.entityData.set(UPGRADE_2, stack2);
		this.entityData.set(UPGRADE_3, stack3);
	}
	
	@Override
	public void writeSpawnData(FriendlyByteBuf buffer){
		String fluid = this.entityData.get(TANK_FLUID);
		int amount = this.entityData.get(TANK_AMOUNT);
		ItemStack stack0 = this.entityData.get(UPGRADE_0);
		ItemStack stack1 = this.entityData.get(UPGRADE_1);
		ItemStack stack2 = this.entityData.get(UPGRADE_2);
		ItemStack stack3 = this.entityData.get(UPGRADE_3);
		
		buffer.writeUtf(fluid);
		buffer.writeInt(amount);
		buffer.writeItem(stack0);
		buffer.writeItem(stack1);
		buffer.writeItem(stack2);
		buffer.writeItem(stack3);
	}
}
