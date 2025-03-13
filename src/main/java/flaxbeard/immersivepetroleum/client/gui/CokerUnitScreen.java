package flaxbeard.immersivepetroleum.client.gui;

import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.client.gui.IEContainerScreen;
import blusunrize.immersiveengineering.client.gui.info.FluidInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import flaxbeard.immersivepetroleum.client.gui.elements.CokerChamberInfoArea;
import flaxbeard.immersivepetroleum.client.gui.elements.EnergyDisplay;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.CokerUnitLogic;
import flaxbeard.immersivepetroleum.common.gui.CokerUnitContainer;
import flaxbeard.immersivepetroleum.common.gui.DerrickContainer;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nonnull;
import java.util.List;

public class CokerUnitScreen extends IEContainerScreen<CokerUnitContainer>{
	public static final ResourceLocation GUI_TEXTURE = ResourceUtils.ip("textures/gui/coker.png");
	
	public CokerUnitScreen(CokerUnitContainer inventorySlotsIn, Inventory inv, Component title){
		super(inventorySlotsIn, inv, title, GUI_TEXTURE);

		this.imageWidth = 200;
		this.imageHeight = 187;
	}
	
	@Override
	protected void renderLabels(GuiGraphics transform, int mouseX, int mouseY){
		// Render no labels
	}
	
	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		BlockEntity masterCoker = this.menu.level.getBlockEntity(DerrickContainer.getPos(this.menu.pos.get()));

		if(masterCoker instanceof IMultiblockBE<?> multiblockBE && multiblockBE.getHelper().getContext().getState() instanceof CokerUnitLogic.State state)
		{
			CokerUnitLogic.Chambers chambers = state.chambers;

			return List.of(
					new FluidInfoArea(
							this.getMenu().tanks.input(),
							new Rect2i(this.leftPos + 32, this.topPos + 14, 16, 47),
							202, 2, 16, 47,
							GUI_TEXTURE
					),
					new FluidInfoArea(
							this.getMenu().tanks.output(),
							new Rect2i(this.leftPos + 152, this.topPos + 14, 16, 47),
							202, 2, 16, 47,
							GUI_TEXTURE
					),
					new EnergyDisplay(this.leftPos + 168, this.topPos + 67, 7, 21, this.getMenu().energy),
					new CokerChamberInfoArea(chambers.primary(), new Rect2i(this.leftPos + 74, this.topPos + 24, 6, 38)),
					new CokerChamberInfoArea(chambers.secondary(), new Rect2i(this.leftPos + 120, this.topPos + 24, 6, 38))
			);
		}
		return List.of();
	}
}
