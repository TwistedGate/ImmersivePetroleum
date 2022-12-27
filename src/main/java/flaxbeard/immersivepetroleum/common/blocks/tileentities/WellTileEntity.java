package flaxbeard.immersivepetroleum.common.blocks.tileentities;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import flaxbeard.immersivepetroleum.client.ClientProxy;
import org.apache.commons.lang3.tuple.Pair;

import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirIsland;
import flaxbeard.immersivepetroleum.common.IPTileTypes;
import flaxbeard.immersivepetroleum.common.blocks.stone.WellPipeBlock;
import flaxbeard.immersivepetroleum.common.blocks.ticking.IPClientTickableTile;
import flaxbeard.immersivepetroleum.common.blocks.ticking.IPServerTickableTile;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.registries.ForgeRegistries;

public class WellTileEntity extends IPTileEntityBase implements IPServerTickableTile, IPClientTickableTile{
	
	static final int PIPE_WORTH = 6;
	static final int DEFAULT_PIPELENGTH = PIPE_WORTH * 64;
	
	/** It's supposed to be never null nor empty. If it is then something's wrong. */
	@Nonnull
	public List<ColumnPos> tappedIslands = new ArrayList<>();
	
	/** Only ever contains the Y component of {@link BlockPos} */
	public final List<Integer> phyiscalPipesList = new ArrayList<>();
	
	/** Amount of pipe left over from 1 IE Pipe */
	public int pipes = 0;
	/** Current length of the virtual well pipe */
	public int wellPipeLength = 0;
	/** Extra pipes needed to make the bend and connect to other "inputs" */
	public int additionalPipes = 0;
	public boolean drillingCompleted;
	
	public boolean pastPhysicalPart;
	
	private boolean selfDestruct;
	private int selfDestructTimer;
	
	private Fluid spillFType = Fluids.EMPTY;
	private int spillHeight = -1;
	
	boolean spill = false;
	int clientFlow = 0;
	public WellTileEntity(BlockPos pWorldPosition, BlockState pBlockState){
		super(IPTileTypes.WELL.get(), pWorldPosition, pBlockState);
	}
	
	@Override
	protected void writeCustom(CompoundTag nbt){
		nbt.putBoolean("spill", this.spill);
		nbt.putInt("flow", ReservoirHandler.getIsland(getWorldNonnull(), getBlockPos()) == null ? 0 :
			ReservoirIsland.getFlow(ReservoirHandler.getIsland(getWorldNonnull(),
				getBlockPos()).getPressure(getWorldNonnull(),
				getBlockPos().getX(), getBlockPos().getZ())));
		
		nbt.putBoolean("drillingcompleted", this.drillingCompleted);
		nbt.putBoolean("pastphyiscalpart", this.pastPhysicalPart);
		
		nbt.putInt("pipes", this.pipes);
		nbt.putInt("wellpipelength", this.wellPipeLength);
		nbt.putInt("additionalpipes", this.additionalPipes);
		
		nbt.putBoolean("selfdestruct", this.selfDestruct);
		nbt.putInt("selfdestructtimer", this.selfDestructTimer);
		
		nbt.putString("spillftype", this.spillFType.getRegistryName().toString());
		nbt.putInt("spillheight", this.spillHeight);
		
		if(!this.tappedIslands.isEmpty()){
			final ListTag list = new ListTag();
			this.tappedIslands.forEach(c -> {
				CompoundTag pos = new CompoundTag();
				pos.putInt("x", c.x);
				pos.putInt("z", c.z);
				list.add(pos);
			});
			nbt.put("tappedislands", list);
		}
		
		if(!this.phyiscalPipesList.isEmpty()){
			final ListTag list = new ListTag();
			this.phyiscalPipesList.forEach(i -> list.add(IntTag.valueOf(i)));
			nbt.put("pipeLoc", list);
		}
	}
	
	@Override
	protected void readCustom(CompoundTag nbt){
		this.spill = nbt.getBoolean("spill");
		this.clientFlow = nbt.getInt("flow");
		this.drillingCompleted = nbt.getBoolean("drillingcompleted");
		this.pastPhysicalPart = nbt.getBoolean("pastphyiscalpart");
		
		this.pipes = nbt.getInt("pipes");
		this.wellPipeLength = nbt.getInt("wellpipelength");
		this.additionalPipes = nbt.getInt("additionalpipes");
		
		this.selfDestruct = nbt.getBoolean("selfdestruct");
		this.selfDestructTimer = nbt.getInt("selfdestructtimer");
		
		try{
			this.spillFType = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(nbt.getString("spillftype")));
		}catch(ResourceLocationException rle){
			this.spillFType = Fluids.EMPTY;
		}
		this.spillHeight = nbt.getInt("spillheight");
		
		if(nbt.contains("tappedislands", Tag.TAG_LIST)){
			ListTag list = nbt.getList("tappedislands", Tag.TAG_COMPOUND);
			final List<ColumnPos> tmp = new ArrayList<>(list.size());
			list.forEach(n -> {
				CompoundTag pos = (CompoundTag) n;
				int x = pos.getInt("x");
				int z = pos.getInt("z");
				tmp.add(new ColumnPos(x, z));
			});
			this.tappedIslands = tmp;
		}
		
		if(nbt.contains("pipeLoc", Tag.TAG_LIST)){
			ListTag list = nbt.getList("pipeLoc", Tag.TAG_INT);
			final List<Integer> ints = new ArrayList<>(list.size());
			list.forEach(n -> ints.add(((IntTag) n).getAsInt()));
			this.phyiscalPipesList.clear();
			this.phyiscalPipesList.addAll(ints);
		}
	}
	
	@Override
	public void tickClient(){
		if(this.spill && this.spillFType != Fluids.EMPTY){
			BlockPos pPos = this.spillHeight > -1 ? new BlockPos(this.worldPosition.getX(), this.spillHeight, this.worldPosition.getZ()) : this.worldPosition.above();
			ClientProxy.spawnSpillParticles(this.level, pPos, this.spillFType, 10, -0.25F, clientFlow);
		}
	}
	
	@Override
	public void tickServer(){
		if(this.drillingCompleted){
			if(this.tappedIslands.size() > 0){
				if(this.level.getGameTime() % 5 == 0){
					boolean spill = false;
					
					int height = -1;
					Fluid fType = Fluids.EMPTY;
					
					BlockEntity teHigh = getWorldNonnull().getBlockEntity(getBlockPos().above());
					if(teHigh instanceof WellPipeTileEntity well){
						Pair<Boolean, BlockPos> result = well.hasValidConnection();
						
						// Don't stop spilling even if the pumpjack is ontop, because it is "not designed" to handle the high pressure
						if(!result.getLeft() || getWorldNonnull().getBlockEntity(result.getRight()) instanceof PumpjackTileEntity){
							for(ColumnPos cPos:this.tappedIslands){
								ReservoirIsland island = ReservoirHandler.getIsland(getWorldNonnull(), cPos);
								
								// One is enough to trigger spilling
								if(island != null && island.getPressure(getWorldNonnull(), cPos.x, cPos.z) > 0.0){
									fType = island.getFluid();
									height = result.getRight().getY();
									spill = true;
									break;
								}
							}
						}
						
					}else{
						ColumnPos cPos = this.tappedIslands.get(0);
						ReservoirIsland island = ReservoirHandler.getIsland(getWorldNonnull(), cPos);
						
						if(island != null && island.getPressure(getWorldNonnull(), cPos.x, cPos.z) > 0.0){
							spill = true;
							fType = island.getFluid();
							height = this.worldPosition.getY() + 1;
						}
					}
					
					if(spill != this.spill){
						this.spill = spill;
						
						if(this.spill){
							this.spillHeight = height;
							this.spillFType = fType;
						}else{
							this.spillHeight = -1;
							this.spillFType = Fluids.EMPTY;
						}
						
						setChanged();
					}
				}
				
				if(this.spill){
					for(ColumnPos cPos:this.tappedIslands){
						ReservoirIsland island = ReservoirHandler.getIsland(getWorldNonnull(), cPos);
						
						if(island != null){
							// Already unpressurized islands are left alone by default
							island.extractWithPressure(getWorldNonnull(), cPos.x, cPos.z);
						}
					}
				}
			}
		}else{
			if(this.selfDestruct && advanceTimer()){
				// Sucks to be you if this happens =P
				if(!this.phyiscalPipesList.isEmpty()){
					for(Integer integer:this.phyiscalPipesList){
						BlockPos pos = getBlockPos();
						pos = new BlockPos(pos.getX(), integer, pos.getZ());
						
						BlockState state = getWorldNonnull().getBlockState(pos);
						
						if(state.getBlock() instanceof WellPipeBlock){
							getWorldNonnull().setBlockAndUpdate(pos, state.setValue(WellPipeBlock.BROKEN, true));
						}
					}
				}
				
				getWorldNonnull().setBlockAndUpdate(getBlockPos(), Blocks.BEDROCK.defaultBlockState());
			}
		}
	}
	
	public void usePipe(){
		if(this.drillingCompleted){
			return;
		}
		
		this.pipes -= 1;
		this.wellPipeLength += 1;
		
		if(this.wellPipeLength >= getMaxPipeLength()){
			this.drillingCompleted = true;
		}
		
		setChanged();
	}
	
	public int getMaxPipeLength(){
		return DEFAULT_PIPELENGTH + this.additionalPipes;
	}
	
	public void startSelfDestructSequence(){
		if(this.drillingCompleted){
			return;
		}
		
		this.selfDestruct = true;
		this.selfDestructTimer = 100;
		//this.selfDestructTimer = 6000; // 5 Minutes
	}
	
	public void abortSelfDestructSequence(){
		if(this.selfDestruct){
			this.selfDestruct = false;
		}
	}
	
	public boolean advanceTimer(){
		return this.selfDestruct && this.selfDestructTimer-- <= 0;
	}
	
	@Override
	public void setChanged(){
		super.setChanged();
		
		BlockState state = level.getBlockState(worldPosition);
		level.sendBlockUpdated(worldPosition, state, state, 3);
		level.updateNeighborsAt(worldPosition, state.getBlock());
	}
}
