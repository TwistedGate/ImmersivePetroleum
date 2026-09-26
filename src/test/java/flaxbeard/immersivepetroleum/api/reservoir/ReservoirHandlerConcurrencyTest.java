package flaxbeard.immersivepetroleum.api.reservoir;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import flaxbeard.immersivepetroleum.MinecraftTestBootstrap;
import net.minecraft.resources.ResourceLocation;

public class ReservoirHandlerConcurrencyTest{
	private static final int THREAD_COUNT = 32;
	private static final int ROUND_COUNT = 64;

	@Test(timeout = 30000)
	public void concurrentWeightCalculationDoesNotModifyWeightMapWhileItIsBeingUsed() throws Exception{
		MinecraftTestBootstrap.initialize();

		for(int round = 0;round < ROUND_COUNT;round++){
			ReservoirHandler.recalculateChances();
			ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
			CountDownLatch ready = new CountDownLatch(THREAD_COUNT);
			CountDownLatch start = new CountDownLatch(1);
			List<Future<Integer>> futures = new ArrayList<>();

			for(int thread = 0;thread < THREAD_COUNT;thread++){
				final int key = round * THREAD_COUNT + thread;
				futures.add(executor.submit(() -> {
					ready.countDown();
					if(!start.await(10, TimeUnit.SECONDS)){
						throw new AssertionError("Timed out waiting for concurrent start");
					}

					return ReservoirHandler.getTotalWeight(
							new ResourceLocation("immersivepetroleum", "dimension_" + key),
							new ResourceLocation("minecraft", "biome_" + key));
				}));
			}

			if(!ready.await(10, TimeUnit.SECONDS)){
				fail("Timed out waiting for worker threads");
			}
			start.countDown();

			try{
				for(Future<Integer> future:futures){
					future.get(10, TimeUnit.SECONDS);
				}
			}finally{
				executor.shutdownNow();
			}
		}
	}
}
