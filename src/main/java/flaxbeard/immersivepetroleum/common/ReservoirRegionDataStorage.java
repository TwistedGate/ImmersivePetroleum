package flaxbeard.immersivepetroleum.common;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirIsland;
import flaxbeard.immersivepetroleum.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Manager for {@link RegionData}s
 * 
 * @author TwistedGate
 */
public class ReservoirRegionDataStorage extends SavedData{
	private static final Logger log = LogManager.getLogger(ImmersivePetroleum.MODID + "/RegionDataStorage");
	
	private static final String DATA_NAME = "ImmersivePetroleum-ReservoirRegions";
	
	private static ReservoirRegionDataStorage active_instance;
	public static ReservoirRegionDataStorage get(){
		return active_instance;
	}
	
	public static final void init(final DimensionDataStorage dimData){
		active_instance = dimData.computeIfAbsent(t -> new ReservoirRegionDataStorage(dimData, t), () -> {
			log.debug("Creating new ReservoirRegionDataStorage instance.");
			return new ReservoirRegionDataStorage(dimData);
		}, DATA_NAME);
	}
	
	// -----------------------------------------------------------------------------
	
	/** Contains existing reservoir-region files */
	final Map<RegionPos, RegionData> regions = new HashMap<>();
	final DimensionDataStorage dimData;
	public ReservoirRegionDataStorage(DimensionDataStorage dimData){
		this.dimData = dimData;
	}
	public ReservoirRegionDataStorage(DimensionDataStorage dimData, CompoundTag nbt){
		this.dimData = dimData;
		load(nbt);
	}
	
	@Override
	public CompoundTag save(CompoundTag nbt){
		ListTag list = new ListTag();
		this.regions.forEach((key, entry) -> {
			CompoundTag tag = new CompoundTag();
			tag.putInt("x", key.x());
			tag.putInt("z", key.z());
			list.add(tag);
		});
		nbt.put("regions", list);
		
		log.debug("Saved regions file.");
		return nbt;
	}
	
	private void load(CompoundTag nbt){
		ListTag regions = nbt.getList("regions", Tag.TAG_COMPOUND);
		for(int i = 0;i < regions.size();i++){
			CompoundTag tag = regions.getCompound(i);
			int x = tag.getInt("x");
			int z = tag.getInt("z");
			
			RegionPos rPos = new RegionPos(x, z);
			RegionData rData = getOrCreateRegionData(rPos);
			this.regions.put(rPos, rData);
		}
		
		log.debug("Loaded regions file.");
	}
	
	/** Marks itself and all regions as dirty. (Only to be used by {@link CommonEventHandler#onUnload(net.minecraftforge.event.world.WorldEvent.Unload)}) */
	public void markAllDirty(){
		setDirty();
		this.regions.values().forEach(RegionData::setDirty);
	}
	
	public void addIsland(ResourceKey<Level> dimensionKey, ReservoirIsland island){
		RegionPos regionPos = new RegionPos(island.getBoundingBox().getCenter());
		
		RegionData regionData = getOrCreateRegionData(regionPos);
		synchronized(regionData.reservoirlist){
			if(!regionData.reservoirlist.containsEntry(dimensionKey, island)){
				regionData.reservoirlist.put(dimensionKey, island);
				island.setRegion(regionData);
				regionData.setDirty();
			}
		}
	}
	
	/** May only be called on the server-side. Returns null on client-side. */
	@Nullable
	public ReservoirIsland getIsland(Level world, BlockPos pos){
		return getIsland(world, Utils.toColumnPos(pos));
	}
	
	/** May only be called on the server-side. Returns null on client-side. */
	@Nullable
	public ReservoirIsland getIsland(Level world, ColumnPos pos){
		if(world.isClientSide){
			return null;
		}
		
		final ResourceKey<Level> dimKey = world.dimension();
		
		ReservoirIsland ret = getIsland(dimKey, pos, 0, 0);
		if((ret = getIsland(dimKey, pos, 1, -1)) == null){
			if((ret = getIsland(dimKey, pos, 1, 1)) == null){
				if((ret = getIsland(dimKey, pos, -1, -1)) == null){
					ret = getIsland(dimKey, pos, -1, 1);
				}
			}
		}
		
		return ret;
	}
	
	private ReservoirIsland getIsland(ResourceKey<Level> dimKey, ColumnPos pos, int regionXOff, int regionZOff){
		RegionData regionData = getRegionData(new RegionPos(pos, regionXOff, regionZOff));
		return regionData != null ? regionData.get(dimKey, pos) : null;
	}
	
	public boolean existsAt(ColumnPos pos){
		boolean ret = false;
		if(!(ret = existsAt(pos, 1, -1))){
			if(!(ret = existsAt(pos, 1, 1))){
				if(!(ret = existsAt(pos, -1, -1))){
					ret = existsAt(pos, -1, 1);
				}
			}
		}
		return ret;
	}
	
	private boolean existsAt(ColumnPos pos, int regionXOff, int regionZOff){
		RegionData regionData = getRegionData(new RegionPos(pos, regionXOff, regionZOff));
		if(regionData == null)
			return false;
		
		boolean ret = false;
		synchronized(regionData.reservoirlist){
			ret = regionData.reservoirlist.values().stream().anyMatch(island -> island.contains(pos));
		}
		return ret;
	}
	
	@Nullable
	public RegionData getRegionData(BlockPos pos){
		return getRegionData(new RegionPos(pos));
	}
	
	@Nullable
	public RegionData getRegionData(RegionPos regionPos){
		RegionData ret = this.regions.getOrDefault(regionPos, null);
		return ret;
	}
	
	private RegionData getOrCreateRegionData(RegionPos regionPos){
		RegionData ret = this.regions.computeIfAbsent(regionPos, p -> {
			String fn = getRegionFileName(p);
			RegionData data = this.dimData.computeIfAbsent(t -> new RegionData(p, t), () -> new RegionData(p), fn);
			setDirty();
			log.debug("Created RegionData[{}, {}]", regionPos.x(), regionPos.z());
			return data;
		});
		return ret;
	}
	
	private String getRegionFileName(RegionPos regionPos){
		return DATA_NAME + File.separatorChar + regionPos.x() + "_" + regionPos.z();
	}
	
	// -----------------------------------------------------------------------------
	
	public static record RegionPos(int x, int z){
		public RegionPos(BlockPos pos, int xOff, int zOff){
			this((pos.getX() + 256 * xOff) >> 9, (pos.getZ() + 256 * zOff) >> 9);
		}
		
		public RegionPos(ColumnPos pos, int xOff, int zOff){
			this((pos.x() + 256 * xOff) >> 9, (pos.z() + 256 * zOff) >> 9);
		}
		
		public RegionPos(BlockPos pos){
			this(pos.getX() >> 9, pos.getZ() >> 9);
		}
		
		public RegionPos(ColumnPos pos){
			this(pos.x() >> 9, pos.z() >> 9);
		}
	}
	
	/**
	 * Contains reservoirs within a particular region.
	 * 
	 * @author TwistedGate
	 */
	public static class RegionData extends SavedData{
		final RegionPos regionPos;
		final Multimap<ResourceKey<Level>, ReservoirIsland> reservoirlist = ArrayListMultimap.create();
		RegionData(RegionPos regionPos){
			this.regionPos = regionPos;
		}
		RegionData(RegionPos regionPos, CompoundTag nbt){
			this.regionPos = regionPos;
			load(nbt);
		}
		
		@Override
		public void save(File pFile){
			if(!pFile.getParentFile().exists()){
				pFile.getParentFile().mkdirs();
			}
			super.save(pFile);
		}
		
		@Override
		public CompoundTag save(CompoundTag nbt){
			ListTag reservoirs = new ListTag();
			synchronized(this.reservoirlist){
				for(ResourceKey<Level> dimension:this.reservoirlist.keySet()){
					CompoundTag dim = new CompoundTag();
					dim.putString("dimension", dimension.location().toString());
					
					ListTag islands = new ListTag();
					for(ReservoirIsland island:this.reservoirlist.get(dimension)){
						islands.add(island.writeToNBT());
					}
					dim.put("islands", islands);
					
					reservoirs.add(dim);
				}
			}
			nbt.put("reservoirs", reservoirs);
			
			log.debug("{} Saved.", this);
			return nbt;
		}
		
		private void load(CompoundTag nbt){
			ListTag reservoirs = nbt.getList("reservoirs", Tag.TAG_COMPOUND);
			if(!reservoirs.isEmpty()){
				synchronized(this.reservoirlist){
					for(int i = 0;i < reservoirs.size();i++){
						CompoundTag dim = reservoirs.getCompound(i);
						ResourceLocation rl = new ResourceLocation(dim.getString("dimension"));
						ResourceKey<Level> dimType = ResourceKey.create(Registries.DIMENSION, rl);
						ListTag islands = dim.getList("islands", Tag.TAG_COMPOUND);
						
						List<ReservoirIsland> list = islands.stream()
								.map(inbt -> ReservoirIsland.readFromNBT((CompoundTag) inbt))
								.filter(Objects::nonNull)
								.collect(Collectors.toList());
						list.forEach(island -> island.setRegion(this));
						this.reservoirlist.putAll(dimType, list);
					}
				}
				log.debug("{} Loaded.", this);
			}
		}
		
		/** Position of this RegionData instance. */
		public RegionPos position(){
			return this.regionPos;
		}
		
		/**
		 * Attempts to find a reservoir.
		 * 
		 * @param dimension The world-dimension to look in.
		 * @param pos The position of the possible reservoir.
		 * @return a {@link ReservoirIsland} instance, or null if none was found at <code>pos</code>.
		 */
		@Nullable
		public ReservoirIsland get(ResourceKey<Level> dimension, ColumnPos pos){
			synchronized(this.reservoirlist){
				for(ReservoirIsland island:this.reservoirlist.get(dimension)){
					if(island.contains(pos)){
						// There's no such thing as overlapping islands, so just return what was found directly
						return island;
					}
				}
				return null;
			}
		}
		
		/**
		 * List of all reservoirs. (Read-Only)
		 * 
		 * @return {@link ImmutableMultimap}<{@link ResourceKey}<{@link Level}>, {@link ReservoirIsland}>
		 */
		public Multimap<ResourceKey<Level>, ReservoirIsland> getReservoirIslandList(){
			synchronized(this.reservoirlist){
				return ImmutableMultimap.copyOf(this.reservoirlist);
			}
		}
		
		@Override
		public int hashCode(){
			return Objects.hash(regionPos);
		}
		
		@Override
		public boolean equals(Object obj){
			if(this == obj){
				return true;
			}
			if(!(obj instanceof RegionData)){
				return false;
			}
			RegionData other = (RegionData) obj;
			return Objects.equals(this.regionPos, other.regionPos);
		}
		
		@Override
		public String toString(){
			return String.format("RegionData[%d, %d]", this.regionPos.x(), this.regionPos.z());
		}
	}
}
