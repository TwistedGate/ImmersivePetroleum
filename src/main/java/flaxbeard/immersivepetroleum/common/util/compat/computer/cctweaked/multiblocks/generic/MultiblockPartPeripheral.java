package flaxbeard.immersivepetroleum.common.util.compat.computer.cctweaked.multiblocks.generic;

public abstract class MultiblockPartPeripheral //implements IPeripheral
{
	/*MultiblockPartBlockEntity<?> mbPart;
	public MultiblockPartPeripheral(MultiblockPartBlockEntity<?> mbPart){
		this.mbPart = mbPart;
	}
	
	@LuaFunction
	public final void setEnabled(boolean bool){
		this.mbPart.computerControl.setEnabled(bool);
	}
	
	@LuaFunction
	public final boolean isEnabled(){
		return this.mbPart.computerControl.isEnabled();
	}
	
	@Override
	public void attach(IComputerAccess computer){
		this.mbPart.computerControl.addReference();
	}
	
	@Override
	public void detach(IComputerAccess computer){
		this.mbPart.computerControl.removeReference();
	}
	
	@Override
	public boolean equals(IPeripheral other){
		if(other == this)
			return true;
		if(other instanceof MultiblockPartPeripheral part && part.mbPart.getBlockPos().equals(this.mbPart.getBlockPos()))
			return true;
		return false;
	}*/
}
