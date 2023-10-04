package flaxbeard.immersivepetroleum.common.util.compat.computer.cctweaked.multiblocks;

import java.util.Map;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirHandler;
import flaxbeard.immersivepetroleum.api.reservoir.ReservoirIsland;
import flaxbeard.immersivepetroleum.common.ExternalModContent;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.DerrickTileEntity;
import flaxbeard.immersivepetroleum.common.blocks.tileentities.WellTileEntity;
import flaxbeard.immersivepetroleum.common.util.compat.computer.cctweaked.CCTUtils;
import flaxbeard.immersivepetroleum.common.util.compat.computer.cctweaked.multiblocks.generic.PoweredMultiblockPeripheral;
import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

public class DerrickPeripheral extends PoweredMultiblockPeripheral{
	DerrickTileEntity master;
	public DerrickPeripheral(DerrickTileEntity tower){
		super(tower);
		this.master = tower.master();
	}
	
	@Override
	public String getType(){
		return "ip_derrick";
	}
	
	@LuaFunction
	public final boolean isDrilling(){
		return this.master.drilling;
	}
	
	@LuaFunction
	public final boolean isSpilling(){
		return this.master.spilling;
	}
	
	@LuaFunction
	public final boolean completed(){
		final WellTileEntity well;
		if((well = this.master.getWell()) == null){
			return false;
		}
		
		return well.wellPipeLength >= well.getMaxPipeLength() && !this.master.spilling;
	}
	
	/** Returns the current content of the internal tank */
	@LuaFunction
	public final Map<String, Object> getInputTank(){
		return CCTUtils.fluidToMap(this.master.tank.getFluid());
	}
	
	/** Returns the currently expected fluid to be supplied to the derrick, or nil */
	@LuaFunction
	public final MethodResult getExpectedFluid(){
		final WellTileEntity well;
		if((well = this.master.getWell()) == null){
			return MethodResult.of(null, "Well not found!");
		}
		
		int realPipeLength = (this.master.getBlockPos().getY() - 1) - well.getBlockPos().getY();
		int concreteNeeded = (DerrickTileEntity.REQUIRED_CONCRETE_AMOUNT * (realPipeLength - well.wellPipeLength));
		if(concreteNeeded > 0){
			return MethodResult.of(CCTUtils.fluidToMap(new FluidStack(ExternalModContent.getIEFluid_Concrete(), concreteNeeded)));
		}else{
			int waterNeeded = DerrickTileEntity.REQUIRED_WATER_AMOUNT * (well.getMaxPipeLength() - well.wellPipeLength);
			if(waterNeeded > 0){
				return MethodResult.of(CCTUtils.fluidToMap(new FluidStack(Fluids.WATER, waterNeeded)));
			}
		}
		
		return MethodResult.of();
	}
	
	/** Returns the highest pressure of a tapped islands */
	@LuaFunction
	public final float getPressure(){
		final WellTileEntity well;
		if((well = this.master.getWell()) == null){
			return 0.0F;
		}
		
		float highest = 0.0F;
		for(ColumnPos cPos:well.tappedIslands){
			ReservoirIsland island = ReservoirHandler.getIsland(this.master.getLevelNonnull(), cPos);
			if(island != null){
				float pressure = island.getPressure(this.master.getLevelNonnull(), cPos.x, cPos.z);
				if(highest < pressure){
					highest = pressure;
				}
			}
		}
		
		return highest;
	}
	
	/** Returns the combined flowrate of all tapped islands */
	@LuaFunction
	public final int getFlowrate(){
		final WellTileEntity well;
		if((well = this.master.getWell()) == null){
			return 0;
		}
		
		int totalFlowrate = 0;
		for(ColumnPos cPos:well.tappedIslands){
			ReservoirIsland island = ReservoirHandler.getIsland(this.master.getLevelNonnull(), cPos);
			if(island != null){
				float pressure = island.getPressure(this.master.getLevelNonnull(), cPos.x, cPos.z);
				totalFlowrate += ReservoirIsland.getFlow(pressure);
			}
		}
		
		return totalFlowrate;
	}
}
