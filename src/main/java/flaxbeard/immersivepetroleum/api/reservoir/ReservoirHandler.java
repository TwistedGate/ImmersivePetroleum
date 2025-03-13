package flaxbeard.immersivepetroleum.api.reservoir;

import com.google.common.collect.ImmutableList;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.ReservoirRegionDataStorage;
import flaxbeard.immersivepetroleum.common.util.RegistryUtils;
import flaxbeard.immersivepetroleum.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.SingleThreadedRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.PerlinSimplexNoise;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;

/**
 * This takes care of dealing with generating, storing and caching (Faster access for regulary queried positions) reservoir islands.
 * 
 * @author TwistedGate
 */
public class ReservoirHandler{
	private static final Map<Pair<ResourceKey<Level>, ColumnPos>, ReservoirIsland> CACHE = new HashMap<>();
	
	private static Map<ResourceLocation, Map<ResourceLocation, Integer>> totalWeightMap = new HashMap<>();
	
	private static long lastSeed;
	private static PerlinSimplexNoise generator;
	
	public static void scanChunkForNewReservoirs(ServerLevel world, ChunkPos chunkPos, RandomSource randomSource){
		int chunkX = chunkPos.getMinBlockX();
		int chunkZ = chunkPos.getMinBlockZ();
		
		ResourceKey<Level> dimensionKey = world.dimension();
		ResourceLocation dimensionRL = dimensionKey.location();
		
		final ReservoirRegionDataStorage storage = ReservoirRegionDataStorage.get();
		
		for(int j = 0;j < 16;j++){
			for(int i = 0;i < 16;i++){
				int x = chunkX + i;
				int z = chunkZ + j;
				
				// This is visually more pleasing to me
				if(ReservoirHandler.getValueOf(world, x, z) <= -1)
					continue;
				
				
				// Getting the biome now to prevent lockups
				ResourceLocation biomeRL = RegistryUtils.getRegistryNameOf(world.getBiome(new BlockPos(x, 64, z)).value());
				
				final ColumnPos current = new ColumnPos(x, z);
				if(storage.existsAt(current)){
					return;
				}
				
				ReservoirType reservoir = null;
				int totalWeight = getTotalWeight(dimensionRL, biomeRL);
				if(totalWeight > 0){
					int weight = Math.abs(randomSource.nextInt() % totalWeight);
					for(ReservoirType res:ReservoirType.map.values()){
						if(res.getDimensions().valid(dimensionRL) && res.getBiomes().valid(biomeRL)){
							weight -= res.weight;
							if(weight < 0){
								reservoir = res;
								break;
							}
						}
					}
					
					if(reservoir != null){
						Set<ColumnPos> pol = new HashSet<>();
						next(world, pol, x, z);
						List<ColumnPos> poly = optimizeIsland(world, new ArrayList<>(pol));
						
						if(!poly.isEmpty()){
							int amount = (int) Mth.lerp(randomSource.nextFloat(), reservoir.minSize, reservoir.maxSize);
							
							ReservoirIsland island = new ReservoirIsland(poly, reservoir, amount);
							storage.addIsland(dimensionKey, island);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Gets the total weight of reservoir types for the given dimension ID and biome type
	 *
	 * @param dimension The dimension to check
	 * @param biome     The biome to check
	 * @return The total weight associated with the dimension/biome pair
	 */
	public static int getTotalWeight(ResourceLocation dimension, ResourceLocation biome){
		Map<ResourceLocation, Integer> map = totalWeightMap.computeIfAbsent(dimension, k -> new HashMap<>());
		
		Integer totalWeight = map.get(biome);
		if(totalWeight == null){
			totalWeight = 0;
			
			for(ReservoirType reservoir:ReservoirType.map.values()){
				if(reservoir.getDimensions().valid(dimension) && reservoir.getBiomes().valid(biome)){
					totalWeight += reservoir.weight;
				}
			}
			
			map.put(biome, totalWeight);
		}
		
		return totalWeight;
	}
	
	/** May only be called on the server-side. Returns null on client-side. */
	public static ReservoirIsland getIsland(Level world, BlockPos pos){
		return getIsland(world, Utils.toColumnPos(pos));
	}
	
	/** May only be called on the server-side. Returns null on client-side. */
	public static ReservoirIsland getIsland(Level world, ColumnPos pos){
		if(world.isClientSide){
			return null;
		}
		
		ResourceKey<Level> dimension = world.dimension();
		Pair<ResourceKey<Level>, ColumnPos> cacheKey = Pair.of(dimension, pos);
		synchronized(CACHE){
			ReservoirIsland ret = CACHE.get(cacheKey);
			
			if(ret == null){
				ReservoirIsland island = ReservoirRegionDataStorage.get().getIsland(world, pos);
				CACHE.put(cacheKey, island);
				return island;
			}
			
			return ret;
		}
	}
	
	/** <i>This should not be called too much.</i> May only be called on the server-side, returns null on client-side. */
	public static ReservoirIsland getIslandNoCache(Level world, BlockPos pos){
		return getIslandNoCache(world, Utils.toColumnPos(pos));
	}
	
	/** <i>This should not be called too much.</i> May only be called on the server-side, returns null on client-side. */
	public static ReservoirIsland getIslandNoCache(Level world, ColumnPos pos){
		if(world.isClientSide){
			return null;
		}
		
		ReservoirIsland island = ReservoirRegionDataStorage.get().getIsland(world, pos);
		return island;
	}
	
	/**
	 * Adds a reservoir type to the pool of valid reservoirs
	 * 
	 * @param id        The "recipeId" of the reservoir type
	 * @param reservoir The {@link ReservoirType} type to add
	 * @return The {@link ReservoirType} passed in
	 */
	public static ReservoirType addReservoir(ResourceLocation id, ReservoirType reservoir){
		ReservoirType.map.put(id, reservoir);
		return reservoir;
	}
	
	static final double scale = 0.015625D;
	static final double d0 = 2 / 3D;
	static final double d1 = 1 / 3D;
	
	/**
	 * <i>Only call on server side!</i>
	 * 
	 * @param level {@link Level} to run query on
	 * @param x     Block Position
	 * @param z     Block Position
	 * @return -1 (Nothing/Empty), >=0.0 means there's <i>something</i>
	 */
	public static double getValueOf(@Nonnull Level level, int x, int z){
		if(!level.isClientSide && level instanceof WorldGenLevel worldGen){
			initGenerator(worldGen);
		}
		
		double noise = Math.abs(generator.getValue(x * scale, z * scale, false));
		if(noise > d0){
			return (noise - d0) / d1;
		}
		
		return -1D;
	}
	
	public static void initGenerator(WorldGenLevel world){
		if(generator == null || world.getSeed() != lastSeed){
			lastSeed = world.getSeed();
			generator = new PerlinSimplexNoise(new WorldgenRandom(new LegacyRandomSource(lastSeed)), ImmutableList.of(0));
			//generator = new PerlinSimplexNoise(new WorldgenRandom(new LegacyRandomSource(lastSeed)), ImmutableList.of(0));
		}
	}
	
	public static PerlinSimplexNoise getGenerator(){
		return generator;
	}
	
	/** Recursively discover the whole island */
	static void next(Level world, Set<ColumnPos> list, int x, int z){
		if(ReservoirHandler.getValueOf(world, x, z) > -1 && !list.contains(new ColumnPos(x, z))){
			list.add(new ColumnPos(x, z));
			
			next(world, list, x + 1, z);
			next(world, list, x - 1, z);
			next(world, list, x, z + 1);
			next(world, list, x, z - 1);
		}
	}
	
	public static void clearCache(){
		synchronized(CACHE){
			CACHE.clear();
		}
	}
	
	public static void recalculateChances(){
		totalWeightMap.clear();
	}
	
	// ####################################################
	// Optimization methods below. Warning, overengineered!
	// ####################################################
	
	private static List<ColumnPos> optimizeIsland(Level world, List<ColumnPos> poly){
		poly = keepOutline(world, poly);
		poly = makeDirectional(poly);
		poly = cullLines(poly);
		
		return poly;
	}
	
	/** Keep edges/corners and dump the rest */
	private static List<ColumnPos> keepOutline(Level world, List<ColumnPos> poly){
		final Set<ColumnPos> set = new HashSet<>();
		
		poly.forEach(pos -> {
			for(int z = -1;z <= 1;z++){
				for(int x = -1;x <= 1;x++){
					if(ReservoirHandler.getValueOf(world, pos.x() + 1, pos.z()) == -1){
						ColumnPos p = new ColumnPos(pos.x() + 1, pos.z());
						set.add(p);
					}
					if(ReservoirHandler.getValueOf(world, pos.x() - 1, pos.z()) == -1){
						ColumnPos p = new ColumnPos(pos.x() - 1, pos.z());
						set.add(p);
					}
					if(ReservoirHandler.getValueOf(world, pos.x(), pos.z() + 1) == -1){
						ColumnPos p = new ColumnPos(pos.x(), pos.z() + 1);
						set.add(p);
					}
					if(ReservoirHandler.getValueOf(world, pos.x(), pos.z() - 1) == -1){
						ColumnPos p = new ColumnPos(pos.x(), pos.z() - 1);
						set.add(p);
					}
				}
			}
		});
		
		return new ArrayList<>(set);
	}
	
	/**
	 * Give this some direction. Result can end up being either clockwise or counter-clockwise!
	 */
	private static List<ColumnPos> makeDirectional(List<ColumnPos> poly){
		List<ColumnPos> list = new ArrayList<>();
		list.add(poly.remove(0));
		int a = 0;
		while(poly.size() > 0){
			final ColumnPos col = list.get(a);
			
			if(moveNext(col, poly, list)){
				a++;
			}else{
				ImmersivePetroleum.log.warn("This should not happen, but it did..");
				break;
			}
		}
		
		return list;
	}
	
	/**
	 * <pre>
	 * Straight Line Optimizations (Cut down on number of Points)
	 * to avoid things like #### and turn them into #--#
	 * Where # is a Point, and - is just an imaginary line between.
	 * 
	 * For X and Z
	 * </pre>
	 */
	private static ArrayList<ColumnPos> cullLines(List<ColumnPos> poly){
		ArrayList<ColumnPos> list = new ArrayList<>(poly);
		
		int endIndex = 0;
		ColumnPos startPos = null, endPos = null;
		for(int startIndex = 0;startIndex < list.size();startIndex++){
			startPos = list.get(startIndex);
			
			// Find the end of the current line on X
			{
				for(int j = 1;j < 64;j++){
					int index = (startIndex + j) % list.size();
					ColumnPos pos = list.get(index);
					
					if(startPos.z() != pos.z()){
						break;
					}
					
					endIndex = index;
					endPos = pos;
				}
			}
			
			// Find the end the current line on Z
			{
				for(int j = 1;j < 64;j++){
					int index = (startIndex + j) % list.size();
					ColumnPos pos = list.get(index);
					
					if(startPos.x() != pos.x()){
						break;
					}
					
					endIndex = index;
					endPos = pos;
				}
			}
			
			// Diagonal lines?
			// Causes issues on some occasions, this is why it's "disabled"
			boolean debug = false;
			if(debug){
				for(int j = 1;j < 64;j++){
					int index = (startIndex + j) % list.size();
					ColumnPos pos = list.get(index);
					
					int dx = Math.abs(pos.x() - startPos.x());
					int dz = Math.abs(pos.z() - startPos.z());
					
					if(dx != dz){
						break;
					}
					
					endIndex = index;
					endPos = pos;
				}
			}
			
			// Commence culling
			if(startPos != null && endPos != null){
				int len = (endIndex - startIndex);
				if(len > 1){
					int index = startIndex + 1;
					for(int j = index;j < endIndex;j++){
						list.remove(index % list.size());
					}
				}else if(len < 0){
					// Start and End overlap themselfs
					len = len + list.size() - 1;
					
					if(len > 1){
						int index = startIndex + 1;
						for(int j = 0;j < len;j++){
							list.remove(index % list.size());
						}
					}
				}
				
				startPos = endPos = null;
			}
		}
		
		return list;
	}
	
	private static boolean moveNext(ColumnPos pos, List<ColumnPos> src, List<ColumnPos> dst){
		// X Z axis biased
		ColumnPos p0 = new ColumnPos(pos.x() + 1, pos.z());
		ColumnPos p1 = new ColumnPos(pos.x() - 1, pos.z());
		ColumnPos p2 = new ColumnPos(pos.x(), pos.z() + 1);
		ColumnPos p3 = new ColumnPos(pos.x(), pos.z() - 1);
		
		if((src.remove(p0) && dst.add(p0)) || (src.remove(p1) && dst.add(p1)) || (src.remove(p2) && dst.add(p2)) || (src.remove(p3) && dst.add(p3))){
			return true;
		}
		
		// Diagonals
		ColumnPos p4 = new ColumnPos(pos.x() - 1, pos.z() - 1);
		ColumnPos p5 = new ColumnPos(pos.x() - 1, pos.z() + 1);
		ColumnPos p6 = new ColumnPos(pos.x() + 1, pos.z() - 1);
		ColumnPos p7 = new ColumnPos(pos.x() + 1, pos.z() + 1);
		
		if((src.remove(p4) && dst.add(p4)) || (src.remove(p5) && dst.add(p5)) || (src.remove(p6) && dst.add(p6)) || (src.remove(p7) && dst.add(p7))){
			return true;
		}
		
		return false;
	}
}
