package flaxbeard.immersivepetroleum.common.util.compat.computer.cctweaked;

public class IPPeripheralProvider //implements IPeripheralProvider
{
	/*public static final IPPeripheralProvider INSTANCE = new IPPeripheralProvider();
	
	@Override
	public LazyOptional<IPeripheral> getPeripheral(Level world, BlockPos pos, Direction side){
		BlockEntity be = world.getBlockEntity(pos);

		//&& multiblockBE.getHelper().getContext().getState() instanceof PumpjackLogic.State)

		if(be instanceof IMultiblockBE<?> multiblockBE && multiblockBE.getHelper().getMultiblock().redstoneInputAware())
		{
			RedstoneControl<?> redstoneControl = null;
			for(final MultiblockRegistration.ExtraComponent<?, ?> component : multiblockBE.getHelper().getMultiblock().extraComponents())
				if(component.makeWrapper() instanceof RedstoneControl<?> )
					redstoneControl = (RedstoneControl<?>) component.makeWrapper();

			if (redstoneControl.allowComputerControl())
			{
				if(multiblockBE.getHelper().getState() instanceof HydroTreaterLogic.State hydrotreater){
					return LazyOptional.of(() -> new HydrotreaterPeripheral(hydrotreater));
				}
				if(multiblockBE.getHelper().getState() instanceof DistillationTowerLogic.State tower){
					return LazyOptional.of(() -> new DistillationTowerPeripheral(tower));
				}
				if(multiblockBE.getHelper().getState() instanceof CokerUnitLogic.State coker){
					return LazyOptional.of(() -> new CokerUnitPeripheral(coker));
				}
				if(multiblockBE.getHelper().getState() instanceof PumpjackLogic.State pumpjack){
					return LazyOptional.of(() -> new PumpjackPeripheral(pumpjack));
				}
				if(multiblockBE.getHelper().getState() instanceof OilTankLogic.State oiltank){
					return LazyOptional.of(() -> new OilTankPeripheral(oiltank));
				}
				if(multiblockBE.getHelper().getState() instanceof DerrickLogic.State derrick){
					return LazyOptional.of(() -> new DerrickPeripheral(derrick));
				}
			}
		}
		return LazyOptional.empty();
	}
	
	public static void init(){
		ComputerCraftAPI.registerPeripheralProvider(IPPeripheralProvider.INSTANCE);
	}*/
}
