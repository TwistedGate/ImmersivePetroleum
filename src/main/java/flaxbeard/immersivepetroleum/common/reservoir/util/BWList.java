package flaxbeard.immersivepetroleum.common.reservoir.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Simple Black/White-List.
 *
 * @author TwistedGate
 */
public abstract class BWList<T, V extends IValidator>{
	protected final Set<V> set;
	protected final Mode mode;
	private final Function<String, V> validatorConstructor;
	
	protected BWList(Set<V> set, Mode mode, Function<String, V> validatorConstructor){
		this.set = set;
		this.mode = mode;
		this.validatorConstructor = validatorConstructor;
	}
	
	public boolean hasEntries(){
		return !this.set.isEmpty();
	}
	
	public int size(){
		return this.set.size();
	}
	
	public abstract boolean isValid(T t);
	
	/** Convenience method */
	protected final boolean isValid(Predicate<V> predicate){
		// An empty set is considered to be "allow anywhere". Regardless of mode value.
		if(this.set.isEmpty())
			return true;
		
		boolean matchFound = this.set.stream().anyMatch(predicate);
		return isBlacklist() != matchFound;
	}
	
	public boolean isBlacklist(){
		return this.mode == Mode.BLACKLIST;
	}
	
	protected Set<V> getSet(){
		return this.set;
	}
	
	public void forEach(Consumer<V> action){
		this.set.forEach(action);
	}
	
	public void readFromNbt(CompoundTag nbt){
		if(nbt.contains("list", Tag.TAG_LIST)){
			ListTag list = nbt.getList("list", Tag.TAG_STRING);
			
			if(!list.isEmpty()){
				this.set.clear();
				list.forEach(t -> {
					if(t instanceof StringTag strTag){
						this.set.add(this.validatorConstructor.apply(strTag.getAsString()));
					}
				});
			}
		}
	}
	
	public CompoundTag writeToNbt(){
		CompoundTag nbt = new CompoundTag();
		nbt.putBoolean("isBlacklist", isBlacklist());
		
		if(hasEntries()){
			ListTag nbtList = new ListTag();
			this.set.forEach(IValidator::getString);
			nbt.put("list", nbtList);
		}
		
		return nbt;
	}
	
	public abstract void encode(FriendlyByteBuf buffer);
	
	public enum Mode{
		BLACKLIST, WHITELIST
	}
}
