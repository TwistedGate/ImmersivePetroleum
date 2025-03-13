package flaxbeard.immersivepetroleum.client.gui.elements;

import blusunrize.immersiveengineering.client.gui.info.FluidInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import flaxbeard.immersivepetroleum.client.gui.CokerUnitScreen;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.CokerUnitLogic;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import java.util.List;

public class CokerChamberInfoArea extends InfoArea{
	private final CokerUnitLogic.CokingChamber chamber;
	private final FluidInfoArea fluidDisplay;
	
	public CokerChamberInfoArea(CokerUnitLogic.CokingChamber chamber, Rect2i area){
		super(area);
		this.chamber = chamber;
		this.fluidDisplay = new FluidInfoArea(
				chamber.getTank(),
				new Rect2i(area.getX(), area.getY(), 6, 38),
				0, 0, 0, 0,
				CokerUnitScreen.GUI_TEXTURE
		);
	}
	
	@Override
	protected void fillTooltipOverArea(int mouseX, int mouseY, List<Component> tooltip){
		fluidDisplay.fillTooltipOverArea(mouseX, mouseY, tooltip);
	}
	
	@Override
	public void draw(GuiGraphics guiGraphics){
		//ClientUtils.bindTexture(CokerUnitScreen.GUI_TEXTURE);
		int scale = 38;
		int off = (int) (chamber.getTotalAmount() / (float) chamber.getCapacity() * scale);
		guiGraphics.blit(CokerUnitScreen.GUI_TEXTURE, area.getX(), area.getY() + scale - off, 200, 51, 6, off);
		
		// Vertical Overlay to visualize progress
		off = (int) (chamber.getTotalAmount() > 0 ? scale * (chamber.getOutputAmount() / (float) chamber.getCapacity()) : 0);
		guiGraphics.blit(CokerUnitScreen.GUI_TEXTURE, area.getX(), area.getY() + scale - off, 206, 51 + (scale - off), 6, off);
		fluidDisplay.draw(guiGraphics);
	}
}
