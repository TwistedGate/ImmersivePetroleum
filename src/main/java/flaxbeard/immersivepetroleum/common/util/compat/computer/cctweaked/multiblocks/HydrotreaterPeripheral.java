package flaxbeard.immersivepetroleum.common.util.compat.computer.cctweaked.multiblocks;

public class HydrotreaterPeripheral //extends PoweredMultiblockPeripheral
{
	/*HydrotreaterTileEntity master;
	public HydrotreaterPeripheral(HydrotreaterTileEntity treater){
		super(treater);
		this.master = treater.master();
	}
	
	@Override
	public String getType(){
		return "ip_hydrotreater";
	}
	
	@LuaFunction
	public final MethodResult getTankSize(int tank){
		switch(tank){
			case 1:
			case 2:
			case 3:
				return MethodResult.of(this.master.tanks[tank - 1].getCapacity());
			default:
				return MethodResult.of(null, "Index " + tank + " out of Bounds.");
		}
	}
	
	@LuaFunction
	public final MethodResult getInputTank(int tank){
		switch(tank){
			case 1:
			case 2:
				return MethodResult.of(CCTUtils.fluidToMap(this.master.tanks[tank - 1].getFluid()));
			default:
				return MethodResult.of(null, "Index " + tank + " out of Bounds.");
		}
	}
	
	@LuaFunction
	public final Map<String, Object> getOutputTank(){
		return CCTUtils.fluidToMap(this.master.tanks[HydrotreaterTileEntity.TANK_OUTPUT].getFluid());
	}*/
}
