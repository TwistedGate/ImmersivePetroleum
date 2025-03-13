package flaxbeard.immersivepetroleum.api.crafting;

import blusunrize.immersiveengineering.api.crafting.IERecipeTypes.TypeWithClass;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class IPRecipeTypes{
	private static final DeferredRegister<RecipeType<?>> REGISTER = DeferredRegister.create(Registries.RECIPE_TYPE, ImmersivePetroleum.MODID);
	
	public static final TypeWithClass<CokerUnitRecipe> COKER = makeType("cokerunit", CokerUnitRecipe.class);
	public static final TypeWithClass<DistillationTowerRecipe> DISTILLATION = makeType("distillationtower", DistillationTowerRecipe.class);
	public static final TypeWithClass<HighPressureRefineryRecipe> HYDROTREATER = makeType("hydrotreater", HighPressureRefineryRecipe.class);
	
	public static final TypeWithClass<ReservoirType> RESERVOIR = makeType("reservoir", ReservoirType.class);
	
	public static void modConstruction(){
		REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
	private static <T extends Recipe<?>> TypeWithClass<T> makeType(String name, Class<T> type){
		RegistryObject<RecipeType<T>> regObj = REGISTER.register(name, () -> new RecipeType<T>(){
			final String res = ImmersivePetroleum.MODID + ":" + name;
			@Override
			public String toString(){
				// Is this even still needed?
				return this.res;
			}
		});
		return new TypeWithClass<>(regObj, type);
	}
}
