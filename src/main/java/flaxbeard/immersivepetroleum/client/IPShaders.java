package flaxbeard.immersivepetroleum.client;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import java.io.IOException;

// See ShaderUtil
@EventBusSubscriber(value = Dist.CLIENT, modid = ImmersivePetroleum.MODID, bus = Bus.MOD)
public class IPShaders{
	
	private static ShaderInstance shader_line;
	private static ShaderInstance shader_projection;
	private static ShaderInstance shader_translucent_full;
	private static ShaderInstance shader_translucent_postion_color;
	
	private static AbstractUniform projection_alpha;
	private static AbstractUniform projection_time;
	public static void projNoise(float alpha, float time){
		IPShaders.projection_alpha.set(alpha);
		IPShaders.projection_time.set(time);
	}
	
	@SubscribeEvent
	public static void registerShaders(RegisterShadersEvent event) throws IOException{
		event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceUtils.ip("rendertype_line"), DefaultVertexFormat.POSITION_COLOR), s -> {
			ImmersivePetroleum.log.debug("rendertype_line shader loaded.");
			shader_line = s;
		});
		
		event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceUtils.ip("rendertype_projection"), DefaultVertexFormat.POSITION_COLOR_TEX), s -> {
			ImmersivePetroleum.log.debug("rendertype_projection shader loaded.");
			shader_projection = s;
			
			projection_alpha = shader_projection.safeGetUniform("Alpha");
			projection_time = shader_projection.safeGetUniform("Time");
		});
		
		event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceUtils.ip("rendertype_translucent_postion_color"), DefaultVertexFormat.POSITION_COLOR), s -> {
			ImmersivePetroleum.log.debug("rendertype_translucent_postion_color shader loaded.");
			shader_translucent_postion_color = s;
		});
		
		event.registerShader(new ShaderInstance(event.getResourceProvider(), ResourceUtils.ip("rendertype_translucent"), DefaultVertexFormat.BLOCK), s -> {
			ImmersivePetroleum.log.debug("rendertype_translucent shader loaded.");
			shader_translucent_full = s;
		});
	}
	
	public static ShaderInstance getTranslucentLineShader(){
		return shader_line;
	}
	
	public static ShaderInstance getProjectionStaticShader(){
		return shader_projection;
	}
	
	public static ShaderInstance getTranslucentShader(){
		return shader_translucent_full;
	}
	
	public static ShaderInstance getTranslucentPostionColorShader(){
		return shader_translucent_postion_color;
	}
}
