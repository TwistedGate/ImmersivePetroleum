package flaxbeard.immersivepetroleum.common.world;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import flaxbeard.immersivepetroleum.MinecraftTestBootstrap;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public class FeatureReservoirConcurrencyTest{
	private static final int THREAD_COUNT = 32;

	@Before
	public void setUp(){
		MinecraftTestBootstrap.initialize();
		FeatureReservoir.generatedReservoirChunks.clear();
	}

	@After
	public void tearDown(){
		FeatureReservoir.generatedReservoirChunks.clear();
	}

	@Test(timeout = 30000)
	public void onlyOneConcurrentCallerClaimsAChunk() throws Exception{
		ResourceKey<Level> dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation("minecraft", "overworld"));
		ChunkPos chunk = new ChunkPos(28, -17);
		ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
		CountDownLatch ready = new CountDownLatch(THREAD_COUNT);
		CountDownLatch start = new CountDownLatch(1);
		List<Future<Boolean>> futures = new ArrayList<>();

		for(int thread = 0;thread < THREAD_COUNT;thread++){
			futures.add(executor.submit(() -> {
				ready.countDown();
				if(!start.await(10, TimeUnit.SECONDS)){
					throw new AssertionError("Timed out waiting for concurrent start");
				}
				return FeatureReservoir.markChunkAsGenerated(dimension, chunk);
			}));
		}

		if(!ready.await(10, TimeUnit.SECONDS)){
			fail("Timed out waiting for worker threads");
		}
		start.countDown();

		int claimCount = 0;
		try{
			for(Future<Boolean> future:futures){
				if(future.get(10, TimeUnit.SECONDS)){
					claimCount++;
				}
			}
		}finally{
			executor.shutdownNow();
		}

		assertEquals(1, claimCount);
	}
}
