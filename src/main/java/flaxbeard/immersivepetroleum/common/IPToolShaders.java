package flaxbeard.immersivepetroleum.common;

import java.util.ArrayList;
import java.util.List;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
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
		addProjectorShader("blue", Rarity.COMMON, 0xFF007FFF, 0xFF000000, 0xFFFFFFFF, false, true, (primary, secondary, background, layer) -> {
			layer.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_portal"), -1));
			layer.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_1_0"), -1));
			layer.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_1_1"), primary));
		}).setInfo("Aperture", "Portal", "Blue Portal Gun");
		addProjectorShader("orange", Rarity.UNCOMMON, 0xFFFF7F00, 0xFF000000, 0xFFFFFFFF, false, true, (primary, secondary, background, layer) -> {
			layer.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_portal"), -1));
			layer.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_1_0"), -1));
			layer.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_1_1"), primary));
		}).setInfo("Aperture", "Portal", "Orange Portal Gun");
		
		addProjectorShader("cube0", Rarity.COMMON, 0xFF3AF1FF, 0xFF000000, 0xFFFFFFFF, false, true, (primary, secondary, background, layer) -> {
			layer.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_cube"), -1));
			layer.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_1_2"), primary));
			
		}).setInfo("Aperture", "Portal", "Storage Cube");
		addProjectorShader("cube1", Rarity.EPIC, 0xFFFF66AE, 0xFF000000, 0xFFFFFFFF, false, true, (primary, secondary, background, layer) -> {
			layer.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_cube"), -1));
			layer.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_1_2"), primary));
		}).setInfo("Aperture", "Portal", "Companion Cube");
	}
	
	public static ShaderRegistryEntry addProjectorShader(String name, Rarity rarity, int colorPrimary, int colorSecondary, int colorBackground, boolean loot, boolean bags, LayerAdder<Integer, Integer, Integer, List<ShaderLayer>> extraLayers){
		ResourceLocation rlName = ResourceUtils.ip(name);
		
		ShaderRegistry.registerShader_Item(rlName, rarity, colorBackground, colorPrimary, colorSecondary);
		
		List<ShaderLayer> list = new ArrayList<>();
		extraLayers.accept(colorPrimary, colorSecondary, colorBackground, list);
		list.add(new ShaderLayer(ResourceUtils.ip("projectors/shaders/projector_uncolored"), -1));
		
		ShaderCaseProjector shader = new ShaderCaseProjector(list);
		ShaderRegistry.registerShaderCase(rlName, shader, rarity);
		
		for(IShaderRegistryMethod<?> method:ShaderRegistry.shaderRegistrationMethods){
			method.apply(rlName, "0", rarity, colorBackground, colorPrimary, colorSecondary, 0xFFFFFFFF, null, 0xFFFFFFFF);
		}
		
		return ShaderRegistry.shaderRegistry.get(rlName)
				.setCrateLoot(loot)
				.setBagLoot(bags)
				.setReplicationCost(() -> new IngredientWithSize(Ingredient.of(ShaderRegistry.defaultReplicationCost), 10 - ShaderRegistry.rarityWeightMap.get(rarity)));
	}
	
	@FunctionalInterface
	private interface LayerAdder<P, S, B, L>{
		void accept(P colorPrimary, S colorSecondary, B colorBackground, L list);
	}
}
