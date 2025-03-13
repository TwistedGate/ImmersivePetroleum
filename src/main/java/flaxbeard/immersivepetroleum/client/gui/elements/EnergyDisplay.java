package flaxbeard.immersivepetroleum.client.gui.elements;

import blusunrize.immersiveengineering.client.gui.info.EnergyInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;

/**
 * Overengineered version of {@link EnergyInfoArea}
 * 
 * @author TwistedGate
 */
public class EnergyDisplay extends InfoArea{
	private final IEnergyStorage storage;
	public EnergyDisplay(int x, int y, int width, int height, IEnergyStorage storage){
		super(new Rect2i(x, y, width, height));
		this.storage = storage;
	}
	
	@Override
	protected void fillTooltipOverArea(int mouseX, int mouseY, List<Component> tooltip){
		tooltip.add(Component.literal(this.storage.getEnergyStored() + "/" + this.storage.getMaxEnergyStored() + " IF"));
	}
	
	@Override
	public void draw(GuiGraphics guiGraphics){
		final int height = this.area.getHeight();
		int stored = (int) (height * (this.storage.getEnergyStored() / (float) this.storage.getMaxEnergyStored()));
		guiGraphics.fillGradient(this.area.getX(), this.area.getY() + (height - stored),
			this.area.getX() + this.area.getWidth(), this.area.getY() + this.area.getHeight(),
			0xFFB51500, 0xFF600B00
		);
	}
}
