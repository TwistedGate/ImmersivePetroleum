package flaxbeard.immersivepetroleum.common.util.compat.computer.cctweaked.multiblocks;

public class DerrickPeripheral //extends PoweredMultiblockPeripheral
{
	/*DerrickTileEntity master;
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
	/*@LuaFunction
	public final Map<String, Object> getInputTank(){
		return CCTUtils.fluidToMap(this.master.tank.getFluid());
	}
	
	/** Returns the currently expected fluid to be supplied to the derrick, or nil */
	/*@LuaFunction
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
	/*@LuaFunction
	public final float getPressure(){
		final WellTileEntity well;
		if((well = this.master.getWell()) == null){
			return 0.0F;
		}
		
		float highest = 0.0F;
		for(ColumnPos cPos:well.tappedIslands){
			ReservoirIsland island = ReservoirHandler.getIsland(this.master.getLevelNonnull(), cPos);
			if(island != null){
				float pressure = island.getPressure(this.master.getLevelNonnull(), cPos.x(), cPos.z());
				if(highest < pressure){
					highest = pressure;
				}
			}
		}
		
		return highest;
	}
	
	/** Returns the combined flowrate of all tapped islands */
	/*@LuaFunction
	public final int getFlowrate(){
		final WellTileEntity well;
		if((well = this.master.getWell()) == null){
			return 0;
		}
		
		int totalFlowrate = 0;
		for(ColumnPos cPos:well.tappedIslands){
			ReservoirIsland island = ReservoirHandler.getIsland(this.master.getLevelNonnull(), cPos);
			if(island != null){
				float pressure = island.getPressure(this.master.getLevelNonnull(), cPos.x(), cPos.z());
				totalFlowrate += ReservoirIsland.getFlow(pressure);
			}
		}
		
		return totalFlowrate;
	}*/
}
