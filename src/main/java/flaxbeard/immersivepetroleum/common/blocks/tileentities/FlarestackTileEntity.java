package flaxbeard.immersivepetroleum.common.blocks.tileentities;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import flaxbeard.immersivepetroleum.api.crafting.FlarestackHandler;
import flaxbeard.immersivepetroleum.client.particle.IPParticleTypes;
import flaxbeard.immersivepetroleum.common.IPTileTypes;
import flaxbeard.immersivepetroleum.common.blocks.ticking.IPCommonTickableTile;
import flaxbeard.immersivepetroleum.common.util.Utils;
import flaxbeard.immersivepetroleum.common.util.damageSources.IPDamageSources;
import flaxbeard.immersivepetroleum.common.util.sounds.IPSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import java.util.List;

public class FlarestackTileEntity extends IPTileEntityBase implements IPCommonTickableTile, IEBlockInterfaces.ISoundBE{

	
	protected boolean isRedstoneInverted;
	protected boolean isActive;
	protected short drained;
	protected final FluidTank tank = new FluidTank(250, fstack -> (fstack != FluidStack.EMPTY && FlarestackHandler.isBurnable(fstack)));
	
	public FlarestackTileEntity(BlockPos pWorldPosition, BlockState pBlockState){
		super(IPTileTypes.FLARE.get(), pWorldPosition, pBlockState);
	}
	
	public void invertRedstone(){
		this.isRedstoneInverted = !this.isRedstoneInverted;
		this.setChanged();
	}
	
	public boolean isRedstoneInverted(){
		return this.isRedstoneInverted;
	}
	
	public boolean isActive(){
		return this.isActive;
	}
	
	public short getFlow(){
		return this.drained;
	}
	
	@Override
	public void readCustom(CompoundTag nbt){
		this.isRedstoneInverted = nbt.getBoolean("inverted");
		this.isActive = nbt.getBoolean("active");
		this.drained = nbt.getShort("drained");
		this.tank.readFromNBT(nbt.getCompound("tank"));
	}
	
	@Override
	public void writeCustom(CompoundTag nbt){
		nbt.putBoolean("inverted", this.isRedstoneInverted);
		nbt.putBoolean("active", this.isActive);
		nbt.putShort("drained", this.drained);
		
		CompoundTag tank = this.tank.writeToNBT(new CompoundTag());
		nbt.put("tank", tank);
	}
	
	private LazyOptional<IFluidHandler> inputHandler;
	
	@Override
	@Nonnull
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side){
		if(cap == ForgeCapabilities.FLUID_HANDLER){
			if(side == null || side == Direction.DOWN){
				BlockEntity te = this.level.getBlockEntity(getBlockPos());
				if(te instanceof FlarestackTileEntity flare){
					if(this.inputHandler == null){
						this.inputHandler = LazyOptional.of(() -> flare.tank);
					}
				}else{
					return LazyOptional.empty();
				}
				
				return this.inputHandler.cast();
			}
		}
		
		return super.getCapability(cap, side);
	}
	
	@Override
	public void setRemoved(){
		super.setRemoved();
		if(this.inputHandler != null){
			this.inputHandler.invalidate();
		}
	}
	
	@Override
	public void invalidateCaps(){
		super.invalidateCaps();
		if(this.inputHandler != null){
			this.inputHandler.invalidate();
		}
	}
	
	@Override
	public void setChanged(){
		super.setChanged();
		
		BlockState state = this.level.getBlockState(this.worldPosition);
		this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
		this.level.updateNeighborsAt(this.worldPosition, state.getBlock());
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public AABB getRenderBoundingBox(){
		BlockPos pos = getBlockPos();
		return new AABB(pos.offset(-1, -1, -1), pos.offset(1, 2, 1));
	}
	
	@Override
	public void tickClient(){
		ImmersiveEngineering.proxy.handleTileSound(IPSounds.FLARESTACK, this, this.isActive, 1.0F, 0.75F);
		if(this.isActive){
			if(this.level.getGameTime() % 2 == 0){
				float xPos = (this.worldPosition.getX() + 0.50F) + (this.level.random.nextFloat() - 0.5F) * .4375F;
				float zPos = (this.worldPosition.getZ() + 0.50F) + (this.level.random.nextFloat() - 0.5F) * .4375F;
				float yPos = (this.worldPosition.getY() + 1.875F) + (0.2F * this.level.random.nextFloat());
				
				this.level.addParticle(IPParticleTypes.FLARE_FIRE.get(), xPos, yPos, zPos, 0.0, 0.0625F + (this.drained / (float) this.tank.getCapacity() * 0.125F), 0.0);
			}
			
		}else if(this.level.getGameTime() % 5 == 0){
			float xPos = this.worldPosition.getX() + 0.50F + (this.level.random.nextFloat() - 0.5F) * .4375F;
			float zPos = this.worldPosition.getZ() + 0.50F + (this.level.random.nextFloat() - 0.5F) * .4375F;
			float yPos = this.worldPosition.getY() + 1.6F;
			float xa = (this.level.random.nextFloat() - .5F) * .00625F;
			float ya = (this.level.random.nextFloat() - .5F) * .00625F;
			
			this.level.addParticle(ParticleTypes.FLAME, xPos, yPos, zPos, xa, 0.025F, ya);
		}
	}
	
	@Override
	public void tickServer(){
		boolean lastActive = this.isActive;
		this.isActive = false;
		
		int redstone = this.level.getBestNeighborSignal(this.worldPosition);
		if(this.isRedstoneInverted()){
			redstone = 15 - redstone;
		}
		
		if(redstone > 0 && this.tank.getFluidAmount() > 0){
			float signal = redstone / 15F;
			FluidStack fs = this.tank.drain((int) (this.tank.getCapacity() * signal), FluidAction.SIMULATE);
			if(fs.getAmount() > 0){
				this.tank.drain(fs.getAmount(), FluidAction.EXECUTE);
				this.drained = (short) fs.getAmount();
				this.isActive = true;
			}
		}
		
		if(this.isActive && this.level.getGameTime() % 10 == 0){
			// Set *anything* ablaze that's in the danger zone
			List<Entity> list = this.getLevel().getEntitiesOfClass(Entity.class, new AABB(this.worldPosition).inflate(1));
			if(!list.isEmpty()){
				list.forEach(e -> {
					if(!e.fireImmune()){
						e.setSecondsOnFire(15);
						e.hurt(IPDamageSources.flarestack(this.level), 6.0F * (this.drained / (float) this.tank.getCapacity()));
					}
				});
				
				List<Entity> goats = list.stream().filter(e -> e instanceof Goat).toList();
				if(!goats.isEmpty()){
					final List<Player> players = this.getLevel().getEntitiesOfClass(Player.class, new AABB(this.worldPosition).inflate(8));
					for(Entity g:goats){
						if(!g.isAlive()){
							players.forEach(p -> Utils.unlockIPAdvancement(p, "main/flarestack"));
							break;
						}
					}
				}
			}
		}
		
		if(lastActive != this.isActive || (!this.level.isClientSide && this.isActive)){
			setChanged();
		}
	}
	
	@Override
	public boolean shouldPlaySound(@Nonnull String sound){
		return true;
	}
}
