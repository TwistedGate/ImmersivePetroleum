package flaxbeard.immersivepetroleum.client.gui;

import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import blusunrize.immersiveengineering.client.gui.info.EnergyInfoArea;
import blusunrize.immersiveengineering.client.gui.info.FluidInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import flaxbeard.immersivepetroleum.common.gui.HydrotreaterContainer;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;

public class HydrotreaterScreen extends IEContainerScreen<HydrotreaterContainer>{
	static final ResourceLocation GUI_TEXTURE = ResourceUtils.ip("textures/gui/hydrotreater.png");
	
	public HydrotreaterScreen(HydrotreaterContainer inventorySlotsIn, Inventory inv, Component title){
		super(inventorySlotsIn, inv, title, GUI_TEXTURE);

		this.imageWidth = 140;
		this.imageHeight = 69;
	}
	
	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY){
		// Render no labels
	}
	
	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas(){
		return List.of(
				new FluidInfoArea(
						this.getMenu().tanks.primary(),
						new Rect2i(this.leftPos + 34, this.topPos + 11, 16, 47),
						140, 0, 20, 51,
						GUI_TEXTURE
				),
				new FluidInfoArea(
						this.getMenu().tanks.secondary(),
						new Rect2i(this.leftPos + 11, this.topPos + 11, 16, 47),
						140, 0, 20, 51,
						GUI_TEXTURE
				),
				new FluidInfoArea(
						this.getMenu().tanks.output(),
						new Rect2i(this.leftPos + 92, this.topPos + 11, 16, 47),
						140, 0, 20, 51,
						GUI_TEXTURE
				),
				new EnergyInfoArea(leftPos + 122, topPos + 12, this.getMenu().energy)
		);
	}
}
