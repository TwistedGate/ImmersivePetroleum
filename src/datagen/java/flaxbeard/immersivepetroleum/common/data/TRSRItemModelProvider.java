package flaxbeard.immersivepetroleum.common.data;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public abstract class TRSRItemModelProvider extends ModelProvider<TRSRModelBuilder>{
	public TRSRItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper){
		super(generator.getPackOutput(), ImmersivePetroleum.MODID, ITEM_FOLDER, TRSRModelBuilder::new, existingFileHelper);
	}
}
