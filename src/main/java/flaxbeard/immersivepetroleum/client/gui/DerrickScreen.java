package flaxbeard.immersivepetroleum.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.vertex.PoseStack;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.gui.info.EnergyInfoArea;
import blusunrize.immersiveengineering.client.gui.info.FluidInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import flaxbeard.immersivepetroleum.client.utils.MCUtil;
import flaxbeard.immersivepetroleum.common.ExternalModContent;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.DerrickTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.WellTileEntity;
import flaxbeard.immersivepetroleum.common.gui.DerrickContainer;
import flaxbeard.immersivepetroleum.common.util.ResourceUtils;
import flaxbeard.immersivepetroleum.common.util.Utils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

public class DerrickScreen extends AbstractContainerScreen<DerrickContainer>{
	static final ResourceLocation GUI_TEXTURE = ResourceUtils.ip("textures/gui/derrick.png");
	
	Button cfgButton;
	
	final DerrickTileEntity tile;
	private List<InfoArea> areas;
	
	public DerrickScreen(DerrickContainer inventorySlotsIn, Inventory inv, Component title){
		super(inventorySlotsIn, inv, title);
		this.tile = menu.tile;
		
		// TODO GUI may either get bigger or smaller as i figure this out
		this.imageWidth = 200;
		this.imageHeight = 164;
	}
	
	@Override
	protected void init(){
		this.leftPos = (this.width - this.imageWidth) / 2;
		this.topPos = (this.height - this.imageHeight) / 2;
		
		this.cfgButton = new Button(this.leftPos + 125, this.topPos + 52, 50, 20, new TextComponent(I18n.get("gui.immersivepetroleum.derrick.msg.config")), button -> this.minecraft.setScreen(new DerrickSettingsScreen(this)), (button, matrix, mx, my) -> {
			if(!button.active){
				renderTooltip(matrix, List.of(new TextComponent(I18n.get("gui.immersivepetroleum.derrick.msg.set_in_stone"))), Optional.empty(), mx, my);
			}
		});
		addRenderableWidget(this.cfgButton);
		this.areas = List.of(
			new FluidInfoArea(tile.tank, new Rect2i(leftPos + 11, topPos + 16, 16, 47), 200, 0, 20, 51, GUI_TEXTURE),
			new EnergyInfoArea(leftPos + 185, topPos + 19, tile.energyStorage)
		);
	}
	
	@Override
	public void render(@Nonnull PoseStack matrix, int mx, int my, float partialTicks){
		this.inventoryLabelY = this.imageHeight - 40;
		this.renderBackground(matrix);
		super.render(matrix, mx, my, partialTicks);
		this.renderTooltip(matrix, mx, my);
		
		List<Component> tooltip = new ArrayList<>();
		
		for(InfoArea area:areas){
			area.fillTooltip(mx, my, tooltip);
		}
		
		if(!tooltip.isEmpty()){
			renderTooltip(matrix, tooltip, Optional.empty(), mx, my);
		}
	}
	
	@Override
	protected void renderLabels(@Nonnull PoseStack matrix, int x, int y){
		if(this.tile.getBlockPos().getY() <= 62){
			drawInfoTextCenteredMultiLine(matrix, I18n.get("gui.immersivepetroleum.derrick.msg.water_table"), 0xEF0000);
			return;
		}
		
		WellTileEntity well = this.tile.getWell();
		if(well != null){
			if(this.cfgButton.active && well.wellPipeLength > 0){
				this.cfgButton.active = false;
			}
			
			// Possible display prototypes
			if(well.wellPipeLength < well.getMaxPipeLength()){
				if(this.tile.isRSDisabled()){
					drawInfoTextCentered(matrix, new TranslatableComponent("gui.immersivepetroleum.derrick.msg.disabled"), 0, 0xEF0000);
					return;
				}
				
				if(this.tile.drilling){
					String str = String.format(Locale.ROOT, "(%d%%)", (int) (100 * well.wellPipeLength / (float) well.getMaxPipeLength()));
					drawInfoText(matrix, new TranslatableComponent("gui.immersivepetroleum.derrick.msg.drilling", str), 0);
					return;
				}else if(well.pipes <= 0 && !this.menu.getSlot(0).hasItem()){
					drawInfoTextCentered(matrix, new TranslatableComponent("gui.immersivepetroleum.derrick.msg.out_of_pipes"), 3, 0xEF0000);
					return;
				}
				
				if(this.tile.tank.isEmpty()){
					int realPipeLength = (this.tile.getBlockPos().getY() - 1) - well.getBlockPos().getY();
					int concreteNeeded = (DerrickTileEntity.REQUIRED_CONCRETE_AMOUNT * (realPipeLength - well.wellPipeLength));
					if(concreteNeeded > 0){
						drawInfoText(matrix, new TranslatableComponent("gui.immersivepetroleum.derrick.msg.missing", Utils.fDecimal(concreteNeeded) + "mB"), 0, 0xEF0000);
						drawInfoText(matrix, ExternalModContent.getIEFluid_Concrete(1).getDisplayName(), 1, 0xEF0000);
						return;
					}
					
					int waterNeeded = DerrickTileEntity.REQUIRED_WATER_AMOUNT * (well.getMaxPipeLength() - well.wellPipeLength);
					if(waterNeeded > 0){
						drawInfoText(matrix, new TranslatableComponent("gui.immersivepetroleum.derrick.msg.missing", Utils.fDecimal(waterNeeded) + "mB"), 0, 0xEF0000);
						drawInfoText(matrix, new FluidStack(Fluids.WATER, 1).getDisplayName(), 1, 0xEF0000);
						return;
					}
				}
			}else{
				if(this.tile.spilling){
					drawInfoTextCenteredMultiLine(matrix, I18n.get("gui.immersivepetroleum.derrick.msg.safety_valve"), 0xEF0000);
				}else{
					drawInfoTextCenteredMultiLine(matrix, I18n.get("gui.immersivepetroleum.derrick.msg.completed"), Lib.colour_nixieTubeText);
				}
			}
		}
	}
	
	private void drawInfoText(PoseStack matrix, Component text, int line){
		drawInfoText(matrix, text, line, Lib.colour_nixieTubeText);
	}
	
	private void drawInfoText(PoseStack matrix, Component text, int line, int color){
		this.font.draw(matrix, text, 60, 10 + (9 * line), color);
	}
	
	@SuppressWarnings("unused")
	private void drawInfoTextCentered(PoseStack matrix, Component text, int line){
		drawInfoTextCentered(matrix, text, line, Lib.colour_nixieTubeText);
	}
	
	private void drawInfoTextCentered(PoseStack matrix, Component text, int line, int color){
		int strWidth = this.font.width(text.getString());
		this.font.draw(matrix, text, 118.5F - (strWidth / 2F), 10 + (9 * line), color);
	}
	
	private void drawInfoTextCenteredMultiLine(PoseStack matrix, String text, int color){
		String[] lines = text.split("<br>");
		for(int i = 0;i < Math.min(lines.length, 4);i++){
			drawInfoTextCentered(matrix, new TextComponent(lines[i].length() > 25 ? lines[i].substring(0, 25) : lines[i]), i, color);
		}
	}
	
	@Override
	protected void renderBg(@Nonnull PoseStack matrix, float partialTicks, int mx, int my){
		MCUtil.bindTexture(GUI_TEXTURE);
		this.blit(matrix, leftPos, topPos, 0, 0, imageWidth, imageHeight);
		for(InfoArea area:areas){
			area.draw(matrix);
		}
	}
}
