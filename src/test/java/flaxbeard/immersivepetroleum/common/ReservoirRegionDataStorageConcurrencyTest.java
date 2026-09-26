package flaxbeard.immersivepetroleum.common;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import flaxbeard.immersivepetroleum.MinecraftTestBootstrap;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.storage.DimensionDataStorage;

public class ReservoirRegionDataStorageConcurrencyTest{
	private static final int THREAD_COUNT = 32;
	private static final int ROUND_COUNT = 64;

	@Test(timeout = 30000)
	public void concurrentRegionCreationDoesNotModifyRegionMapWhileItIsBeingUsed() throws Exception{
		MinecraftTestBootstrap.initialize();

		for(int round = 0;round < ROUND_COUNT;round++){
			Path dataDirectory = Files.createTempDirectory("immersive-petroleum-regions-");
			DimensionDataStorage dimensionData = new DimensionDataStorage(dataDirectory.toFile(), DataFixers.getDataFixer());
			ReservoirRegionDataStorage storage = new ReservoirRegionDataStorage(dimensionData);
			ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
			CountDownLatch ready = new CountDownLatch(THREAD_COUNT);
			CountDownLatch start = new CountDownLatch(1);
			List<Future<ReservoirRegionDataStorage.RegionData>> futures = new ArrayList<>();

			for(int thread = 0;thread < THREAD_COUNT;thread++){
				final int key = round * THREAD_COUNT + thread;
				futures.add(executor.submit(() -> {
					ready.countDown();
					if(!start.await(10, TimeUnit.SECONDS)){
						throw new AssertionError("Timed out waiting for concurrent start");
					}

					return storage.getOrCreateRegionData(new ColumnPos(key, 0));
				}));
			}

			if(!ready.await(10, TimeUnit.SECONDS)){
				fail("Timed out waiting for worker threads");
			}
			start.countDown();

			try{
				for(Future<ReservoirRegionDataStorage.RegionData> future:futures){
					assertNotNull(future.get(10, TimeUnit.SECONDS));
				}
			}finally{
				executor.shutdownNow();
			}
		}
	}
}
