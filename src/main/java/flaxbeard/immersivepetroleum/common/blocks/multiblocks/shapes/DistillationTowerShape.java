/**
 * @author ArcAnc
 * Created at: 18.04.2024
 * Copyright (c) 2023
 * <p>
 * This code is licensed under "Ancient's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package flaxbeard.immersivepetroleum.common.blocks.multiblocks.shapes;

import flaxbeard.immersivepetroleum.common.util.AABBUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class DistillationTowerShape extends GenericShape{
    public static final DistillationTowerShape GETTER = new DistillationTowerShape();

    private DistillationTowerShape(){
    }

    @Nonnull
    protected List<AABB> getShape(BlockPos posInMultiblock){
        final int x = posInMultiblock.getX();
        final int y = posInMultiblock.getY();
        final int z = posInMultiblock.getZ();

        List<AABB> main = new ArrayList<>();

        // Redstone Input
        if(y < 2){
            if(x == 0 && z == 3){
                if(y == 1){ // Actual Input
                    AABBUtils.box16(main, 0, 0, 0, 8, 16, 16);
                }else{ // Input Legs
                    AABBUtils.box16(main, 0, 0, 0, 16, 8, 16);
                    AABBUtils.box16(main, 2, 0, 12, 6, 16, 14);
                    AABBUtils.box16(main, 2, 0, 2, 6, 16, 4);
                }
            }
        }

        // Pipe over Furnace
        if(y == 2 && x == 3 && z == 2){
            AABBUtils.box16(main, -1, 6, 2, 1, 18, 14);
            AABBUtils.box16(main, 2, 0, 2, 14, 2, 14);
            AABBUtils.box16(main, 4, 0, 4, 12, 16, 12);
            AABBUtils.box16(main, 0, 8, 4, 12, 16, 12);
        }

        // Long Pipe
        if(y > 0 && x == 1 && z == 3){
            if(y != 15){
                AABBUtils.box16(main, 3, 0, 3, 13, 16, 13);
                if(y > 0 && y % 4 == 0){ // For pipe passing a platform
                    AABBUtils.box16(main, 0, 8, 0, 16, 16, 16);
                }
            }else{ // Pipe Top Bend
                AABBUtils.box16(main, 3, 0, -1, 13, 10, 13);
            }
        }

        // Ladder
        if(y > 0 && x == 2 && z == 0){
            AABBUtils.box16(main, 1, y == 1 ? 2 : 0, 14, 15, 16, 17);
            if(y > 0 && y % 4 == 0){
                AABBUtils.box16(main, 0, 8, 14, 16, 16, 17);
                AABBUtils.box16(main, 0, 8, 0, 16, 16, 1);
            }
        }

        // Center
        if(x > 0 && x < 3 && z > 0 && z < 3){
            if(y > 0){
                // Distillation Column
                AABB bb = AABBUtils.box16(1, 0, 1, 15, 16, 15);
                if(z == 1){
                    if(x == 1) bb = AABBUtils.box16(1, 0, 1, 16, 16, 16);
                    if(x == 2) bb = AABBUtils.box16(0, 0, 1, 15, 16, 16);
                }else if(z == 2){
                    if(x == 1) bb = AABBUtils.box16(1, 0, 0, 16, 16, 15);
                    if(x == 2) bb = AABBUtils.box16(0, 0, 0, 15, 16, 15);
                }
                main.add(bb);
            }else{
                // Distillation Column Base
                AABBUtils.box16(main, -2, 8, -2, 18, 18, 18);
                AABBUtils.box16(main, 0, 0, 0, 16, 8, 16);
            }
        }

        // Platforms
        if((y > 0 && y % 4 == 0) && !(x == 2 && z == 0) && !(x >= 1 && x <= 2 && z >= 1 && z <= 2)){
            AABBUtils.box16(main, 0, 8, 0, 16, 16, 16);
        }

        // Base
        if(y == 0){
            if(!((x == 0 && z == 1) || (x == 1 && z == 3) || (x == 3 && z == 2) || (x == 3 && z == 3))){
                AABBUtils.box16(main, 0, 0, 0, 16, 8, 16);
            }
        }

        // Use default cube shape if nessesary
        if(main.isEmpty()){
            main.add(AABBUtils.FULL);
        }
        return main;
    }
}
