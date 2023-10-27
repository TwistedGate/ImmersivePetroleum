package flaxbeard.immersivepetroleum.common.util;

import java.util.List;

import net.minecraft.world.phys.AABB;

public class AABBUtils{
	
	public static final AABB FULL = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
	
	/** Creates a 16p-Texture aligned {@link AABB} and adds it to the provided List */
	public static void box16(List<AABB> list, double x0, double y0, double z0, double x1, double y1, double z1){
		list.add(box16(x0, y0, z0, x1, y1, z1));
	}
	
	/** Creates a 16p-Texture aligned {@link AABB} */
	public static AABB box16(double x0, double y0, double z0, double x1, double y1, double z1){
		return new AABB(x0 / 16D, y0 / 16D, z0 / 16D, x1 / 16D, y1 / 16D, z1 / 16D);
	}
	
	/** Creates a {@link AABB} and adds it to the provided List */
	public static void box(List<AABB> list, double x0, double y0, double z0, double x1, double y1, double z1){
		list.add(box(x0, y0, z0, x1, y1, z1));
	}
	
	/** Creates a {@link AABB} */
	public static AABB box(double x0, double y0, double z0, double x1, double y1, double z1){
		return new AABB(x0, y0, z0, x1, y1, z1);
	}
	
	private AABBUtils(){
	}
}
