package flaxbeard.immersivepetroleum.client.gui;

import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import blusunrize.immersiveengineering.client.gui.info.EnergyInfoArea;
import blusunrize.immersiveengineering.client.gui.info.FluidInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.client.gui.info.MultitankArea;
import flaxbeard.immersivepetroleum.common.gui.DistillationTowerContainer;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import flaxbeard.immersivepetroleum.common.util.inventory.MultiFluidTankFiltered;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.List;

public class DistillationTowerScreen extends IEContainerScreen<DistillationTowerContainer>{
	static final ResourceLocation GUI_TEXTURE = ResourceUtils.ip("textures/gui/distillation.png");
	private final MultiFluidTankFiltered input = new MultiFluidTankFiltered(24000);

	public DistillationTowerScreen(DistillationTowerContainer container, Inventory playerInventory, Component title){
		super(container, playerInventory, title, GUI_TEXTURE);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.render(graphics, mouseX, mouseY, partialTicks);
		this.input.fluids = this.menu.input.get();
	}

	@Override
	protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY){
		// Render no labels
	}
	
	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas(){
		input.fluids = this.menu.input.get();

		return List.of(
				new FluidInfoArea(input, new Rect2i(leftPos + 62, topPos + 21, 16, 47), 177, 31, 20, 51, GUI_TEXTURE),
				new EnergyInfoArea(leftPos + 158, topPos + 22, this.menu.energy),
				new MultitankArea(new Rect2i(leftPos + 112, topPos + 21, 16, 47), 24000, this.menu.output)
		);
	}
}
