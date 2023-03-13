package flaxbeard.immersivepetroleum.common;

import java.util.ArrayList;
import java.util.List;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderLayer;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.IShaderRegistryMethod;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderRegistryEntry;
import flaxbeard.immersivepetroleum.common.shaderscases.ShaderCaseProjector;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.Ingredient;

public class IPToolShaders{
	
	public static void preInit(){
		final String warnings = "Do not touch the operational end of the device.\n" +
								"Do not look directly at the operational end of the device.";
		
		addProjectorShader("blue", Rarity.COMMON, 0xFF007FFF, 0xFF000000, 0xFFFFFFFF, false, true, (primary, secondary, background, layers) -> {
			layers.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_portal"), -1));
			layers.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_1_0"), primary));
		}).setInfo("Aperture", "Portal", warnings);
		
		addProjectorShader("orange", Rarity.UNCOMMON, 0xFFFF7F00, 0xFF000000, 0xFFFFFFFF, false, true, (primary, secondary, background, layers) -> {
			layers.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_portal"), -1));
			layers.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_1_0"), primary));
		}).setInfo("Aperture", "Portal", warnings);
		
		addProjectorShader("cube0", Rarity.COMMON, 0xFF3AF1FF, 0xFF000000, 0xFFFFFFFF, false, true, (primary, secondary, background, layers) -> {
			layers.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_cube"), -1));
			layers.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_1_1"), primary));
		}).setInfo("Aperture", "Portal", "Designed to be used on the 1500 Megawatt Aperture Science Heavy Duty Super-Colliding Super Button");
		
		addProjectorShader("cube1", Rarity.EPIC, 0xFFFF66AE, 0xFF000000, 0xFFFFFFFF, false, true, (primary, secondary, background, layers) -> {
			layers.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_cube"), -1));
			layers.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_1_1"), primary));
		}).setInfo("Aperture", "Portal", "Can not speak");
	}
	
	// I lack ideas and this may end up never being used...
	public static ShaderRegistryEntry addProjectorShader(String name, Rarity rarity, int colorPrimary, int colorSecondary, int colorBackground, boolean loot, boolean bags){
		return addProjectorShader(name, rarity, colorPrimary, colorSecondary, colorBackground, loot, bags, (primary, secondary, background, layers) -> {
			layers.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_0_0"), 0xFF000000 | primary));
			layers.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_0_1"), 0xFF000000 | secondary));
			layers.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_0_2"), 0xFF000000 | background));
		});
	}
	
	public static ShaderRegistryEntry addProjectorShader(String name, Rarity rarity, int colorPrimary, int colorSecondary, int colorBackground, boolean loot, boolean bags, LayerAdder<Integer, Integer, Integer, List<ShaderLayer>> extraLayers){
		ResourceLocation rlName = ResourceUtils.ip(name);
		
		ShaderRegistry.registerShader_Item(rlName, rarity, colorBackground, colorPrimary, colorSecondary);
		
		List<ShaderLayer> list = new ArrayList<>();
		extraLayers.accept(colorPrimary, colorSecondary, colorBackground, list);
		list.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_uncolored"), -1));
		
		ShaderCaseProjector shader = new ShaderCaseProjector(list);
		return registerCase(rlName, shader, rarity, colorPrimary, colorSecondary, colorBackground, loot, bags);
	}
	
	private static <S extends ShaderCase> ShaderRegistryEntry registerCase(ResourceLocation name, S shader, Rarity rarity, int colorPrimary, int colorSecondary, int colorBackground, boolean loot, boolean bags){
		ShaderRegistry.registerShaderCase(name, shader, rarity);
		
		for(IShaderRegistryMethod<?> method:ShaderRegistry.shaderRegistrationMethods){
			method.apply(name, "0", rarity, colorBackground, colorPrimary, colorSecondary, 0xFFFFFFFF, null, 0xFFFFFFFF);
		}
		
		return ShaderRegistry.shaderRegistry.get(name)
				.setCrateLoot(loot)
				.setBagLoot(bags)
				.setReplicationCost(() -> new IngredientWithSize(Ingredient.of(ShaderRegistry.defaultReplicationCost), 10 - ShaderRegistry.rarityWeightMap.get(rarity)));
	}
	
	@FunctionalInterface
	private interface LayerAdder<P, S, B, L>{
		void accept(P colorPrimary, S colorSecondary, B colorBackground, L list);
	}
}
