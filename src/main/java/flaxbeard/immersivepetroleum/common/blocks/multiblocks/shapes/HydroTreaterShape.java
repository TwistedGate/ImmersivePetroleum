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

public class HydroTreaterShape extends GenericShape{
    public static final HydroTreaterShape GETTER = new HydroTreaterShape();

    private HydroTreaterShape(){
    }

    @Nonnull
    protected List<AABB> getShape(BlockPos posInMultiblock){
        int x = posInMultiblock.getX();
        int y = posInMultiblock.getY();
        int z = posInMultiblock.getZ();

        List<AABB> main = new ArrayList<>();

        // Baseplate
        if(y == 0 && !(x == 0 && z == 2) && !(z == 3 && (x == 1 || x == 2))){
            AABBUtils.box16(main, 0, 0, 0, 16, 8, 16);
        }

        // Redstone Controller
        if(y == 0 && x == 0 && z == 3){
            AABBUtils.box16(main, 12, 8, 10, 14, 16, 14);
            AABBUtils.box16(main, 2, 8, 10, 4, 16, 14);
        }else if(y == 1 && x == 0 && z == 3){
            AABBUtils.box16(main, 0, 0, 8, 16, 16, 16);
        }

        // Small Tank
        if(x == 0){
            // Bottom half
            if(y == 0){
                if(z == 0){
                    AABBUtils.box16(main, 2, 12, 8, 16, 16, 16);
                    AABBUtils.box16(main, 4, 8, 12, 14, 12, 16);
                }
                if(z == 1){
                    AABBUtils.box16(main, 2, 12, 0, 16, 16, 16);
                }
                if(z == 3){
                    AABBUtils.box16(main, 2, 12, 0, 16, 16, 4);
                }

            }

            // Top half
            if(y == 1){
                if(z == 0){
                    AABBUtils.box16(main, 2, 0, 8, 16, 12, 16);
                }
                if(z == 1){
                    AABBUtils.box16(main, 2, 0, 0, 16, 12, 16);
                }
                if(z == 3){
                    AABBUtils.box16(main, 2, 0, 0, 16, 12, 4);
                }
            }
        }

        // Big tank
        {
            // Support legs
            if(y == 0){
                if(z == 0){
                    if(x == 1){
                        AABBUtils.box16(main, 2, 5, 1, 6, 16, 5);
                    }
                    if(x == 2){
                        AABBUtils.box16(main, 10, 5, 1, 14, 16, 5);
                    }
                }
                if(z == 1){
                    if(x == 1){
                        AABBUtils.box16(main, 2, 5, 16, 6, 16, 16);
                    }
                    if(x == 2){
                        AABBUtils.box16(main, 10, 5, 16, 16, 16, 16);
                    }
                }
                if(x == 2){
                    if(z == 1) AABBUtils.box16(main, 10, 5, 14, 14, 16, 16);
                    if(z == 2) AABBUtils.box16(main, 10, 5, 0, 14, 16, 2);
                }
            }
        }

        // Use default cube shape if nessesary
        if(main.isEmpty()){
            main.add(AABBUtils.FULL);
        }
        return main;
    }
}