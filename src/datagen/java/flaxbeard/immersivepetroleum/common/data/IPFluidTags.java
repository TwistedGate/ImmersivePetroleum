package flaxbeard.immersivepetroleum.common.data;

import blusunrize.immersiveengineering.api.IETags;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.IPTags;
import flaxbeard.immersivepetroleum.common.IPContent;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraftforge.common.Tags.Fluids;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class IPFluidTags extends FluidTagsProvider{
	
	public IPFluidTags(DataGenerator gen, CompletableFuture<HolderLookup.Provider> pProvider,  ExistingFileHelper exHelper){
		super(gen.getPackOutput(), pProvider, ImmersivePetroleum.MODID, exHelper);
	}
	
	@Override
	protected void addTags(HolderLookup.Provider pProvider){
		tag(IPTags.Fluids.crudeOil).add(IPContent.Fluids.CRUDEOIL.get());
		
		tag(IPTags.Fluids.diesel)
			.add(IPContent.Fluids.DIESEL.get())
			.add(IPContent.Fluids.DIESEL_SULFUR.get());
		tag(IPTags.Fluids.diesel_sulfur).add(IPContent.Fluids.DIESEL_SULFUR.get());
		
		tag(IPTags.Fluids.gasoline).add(IPContent.Fluids.GASOLINE.get());
		tag(IPTags.Fluids.gasoline_additives).add(IPContent.Fluids.GASOLINE_ADDITIVES.get());
		
		tag(IPTags.Fluids.lubricant).add(IPContent.Fluids.LUBRICANT.get());
		tag(IPTags.Fluids.lubricant_cracked).add(IPContent.Fluids.LUBRICANT_CRACKED.get());
		
		tag(IPTags.Fluids.naphtha).add(IPContent.Fluids.NAPHTHA.get());
		tag(IPTags.Fluids.naphtha_cracked).add(IPContent.Fluids.NAPHTHA_CRACKED.get());
		
		tag(IPTags.Fluids.benzene).add(IPContent.Fluids.BENZENE.get());
		tag(IPTags.Fluids.propylene).add(IPContent.Fluids.PROPYLENE.get());
		tag(IPTags.Fluids.ethylene).add(IPContent.Fluids.ETHYLENE.get());
		tag(IPTags.Fluids.kerosene).add(IPContent.Fluids.KEROSENE.get());
		
		tag(IPTags.Fluids.napalm).add(IPContent.Fluids.NAPALM.get());
		
		tag(IPTags.Utility.burnableInFlarestack)
			.addTag(IPTags.Fluids.lubricant)
			.addTag(IPTags.Fluids.diesel)
			.addTag(IPTags.Fluids.diesel_sulfur)
			.addTag(IPTags.Fluids.gasoline)
			.addTag(IPTags.Fluids.naphtha)
			.addTag(IPTags.Fluids.naphtha_cracked)
			.addTag(IPTags.Fluids.benzene)
			.addTag(IPTags.Fluids.propylene)
			.addTag(IPTags.Fluids.ethylene)
			.addTag(IPTags.Fluids.lubricant_cracked)
			.addTag(IPTags.Fluids.kerosene)
			.addTag(IPTags.Fluids.gasoline_additives)
			.addTag(IETags.fluidPlantoil)
			.addTag(IETags.fluidCreosote)
			.addTag(IETags.fluidEthanol);
		
		tag(IETags.drillFuel)
			.addTag(IPTags.Fluids.diesel)
			.addTag(IPTags.Fluids.kerosene)
			.addTag(IPTags.Fluids.diesel_sulfur);
		
		tag(Fluids.GASEOUS)
			.add(IPContent.Fluids.PROPYLENE.get())
			.add(IPContent.Fluids.ETHYLENE.get());
	}
}
