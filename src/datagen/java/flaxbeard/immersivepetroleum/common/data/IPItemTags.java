package flaxbeard.immersivepetroleum.common.data;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.IPTags;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class IPItemTags extends ItemTagsProvider{
	public IPItemTags(DataGenerator dataGen, BlockTagsProvider blockTags, ExistingFileHelper exFileHelper){
		super(dataGen, blockTags, ImmersivePetroleum.MODID, exFileHelper);
	}
	
	@Override
	protected void addTags(){
		IPTags.forAllBlocktags(this::copy);
		
		tag(IPTags.Items.bitumen)
			.add(IPContent.Items.BITUMEN.get());
		
		tag(IPTags.Items.petcoke)
			.add(IPContent.Items.PETCOKE.get());
		
		tag(IPTags.Items.petcokeDust)
			.add(IPContent.Items.PETCOKEDUST.get());
		
		tag(IPTags.Items.petcokeStorage)
			.add(IPContent.Blocks.PETCOKE.get().asItem());
		
		tag(IPTags.Items.paraffinWax)
			.add(IPContent.Items.PARAFFIN_WAX.get());
		
		tag(IPTags.Items.wax)
			.add(IPContent.Items.PARAFFIN_WAX.get());
		
		tag(IPTags.Items.waxBlock)
			.add(IPContent.Blocks.PARAFFIN_WAX.get().asItem());
		
		tag(IPTags.Items.paraffinWaxBlock)
			.add(IPContent.Blocks.PARAFFIN_WAX.get().asItem());
		
		tag(IPTags.Utility.toolboxTools)
			.add(IPContent.Items.PROJECTOR.get().asItem());
	}
}
