package flaxbeard.immersivepetroleum.common.util.compat.computer.cctweaked.multiblocks;

public class DistillationTowerPeripheral //extends PoweredMultiblockPeripheral
{
	/*DistillationTowerTileEntity master;
	public DistillationTowerPeripheral(DistillationTowerTileEntity tower){
		super(tower);
		this.master = tower.master();
	}
	
	@Override
	public String getType(){
		return "ip_distillationtower";
	}
	
	@LuaFunction
	public final MethodResult getTankSize(int tank){
		switch(tank){
			case 1:
			case 2:
				return MethodResult.of(this.master.tanks[tank - 1].getCapacity());
			default:
				return MethodResult.of(null, "Index " + tank + " out of Bounds.");
		}
	}
	
	@LuaFunction
	public final Map<String, Object> getInputTank(){
		return CCTUtils.fluidToMap(this.master.tanks[DistillationTowerTileEntity.TANK_INPUT].getFluid());
	}
	
	@LuaFunction
	public final List<Map<String, Object>> getOutputTank(){
		MultiFluidTank tank = this.master.tanks[DistillationTowerTileEntity.TANK_OUTPUT];
		
		List<Map<String, Object>> list = new ArrayList<>();
		if(!tank.fluids.isEmpty()){
			tank.fluids.forEach(f -> list.add(CCTUtils.fluidToMap(f)));
		}
		
		return list;
	}*/
}
