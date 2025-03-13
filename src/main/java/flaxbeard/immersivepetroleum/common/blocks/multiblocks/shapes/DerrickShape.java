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

public class DerrickShape extends GenericShape{
    public static final DerrickShape GETTER = new DerrickShape();

    private DerrickShape(){
    }

    @Nonnull
    protected List<AABB> getShape(BlockPos posInMultiblock){
        int x = posInMultiblock.getX();
        int y = posInMultiblock.getY();
        int z = posInMultiblock.getZ();

        List<AABB> main = new ArrayList<>();

        if(y == 0){
            if(!(x == 2 && z == 4 || x == 4 && z == 2 || x == 2 && z == 0 || x == 2 && z == 2)){
                AABBUtils.box16(main, 0, 0, 0, 16, 8, 16);
            }

            if(z == 4){
                // to left and right of fluid input
                if(x == 1) AABBUtils.box16(main, 8, 8, 0, 16, 16, 16);
                if(x == 3) AABBUtils.box16(main, 0, 8, 0, 8, 16, 16);
            }

            // Drill Base
            {
                // Corners
                if(x == 1 && z == 1) AABBUtils.box16(main, 12, 8, 12, 16, 16, 16);
                if(x == 3 && z == 1) AABBUtils.box16(main, 0, 8, 12, 4, 16, 16);
                if(x == 1 && z == 3) AABBUtils.box16(main, 12, 8, 0, 16, 16, 4);
                if(x == 3 && z == 3) AABBUtils.box16(main, 0, 8, 0, 4, 16, 4);

                // Edges
                if(x == 2 && z == 1) AABBUtils.box16(main, 0, 8, 12, 16, 16, 16);
                if(x == 2 && z == 3) AABBUtils.box16(main, 0, 8, 0, 16, 16, 4);
                if(x == 1 && z == 2) AABBUtils.box16(main, 12, 8, 0, 16, 16, 16);
                if(x == 3 && z == 2) AABBUtils.box16(main, 0, 8, 0, 4, 16, 16);
            }
        }

        if(y == 0 || y == 1){
            // Power Box
            if(z == 0){
                if(x == 1){
                    AABBUtils.box16(main, 4, 0, 0, 16, 16, 16);
                }else if(x == 3){
                    AABBUtils.box16(main, 0, 0, 0, 12, 16, 16);
                }
            }
        }

        // E-Motor
        if(x == 1 && z == 2){
            if(y == 0) AABBUtils.box16(main, 0, 8, 3, 12, 16, 13);
            if(y == 1) AABBUtils.box16(main, 0, 0, 3, 12, 2, 13);
        }

        // Corner Posts
        if(x == 1 && z == 1) AABBUtils.box16(main, 0, 0, 0, 4, 16, 4);
        if(x == 3 && z == 1) AABBUtils.box16(main, 12, 0, 0, 16, 16, 4);
        if(x == 1 && z == 3) AABBUtils.box16(main, 0, 0, 12, 4, 16, 16);
        if(x == 3 && z == 3) AABBUtils.box16(main, 12, 0, 12, 16, 16, 16);

        // Ladder
        if(y >= 0 && y <= 2 && x == 0 && z == 2){
            AABBUtils.box16(main, 15, 0, 2, 16, 16, 14);
        }

        // Upper Platform
        if(y == 2 && x >= 1 && x <= 3 && z >= 1 && z <= 3){
            AABBUtils.box16(main, 0, 8, 0, 16, 16, 16);
        }

        // Railing
        if(y == 3){
            // Near ladder
            if(x == 1 && z == 2){
                AABBUtils.box16(main, 0, 0, 0, 1, 16, 1);
                AABBUtils.box16(main, 0, 0, 15, 1, 16, 16);
            }
            if(x >= 1 && x <= 3){
                if(z == 1) AABBUtils.box16(main, 0, 0, 0, 16, 16, 1);
                if(z == 3) AABBUtils.box16(main, 0, 0, 15, 16, 16, 16);
            }
            if(x == 1 && (z == 1 || z == 3)) AABBUtils.box16(main, 0, 0, 0, 1, 16, 16);
            if(z >= 1 && z <= 3){
                if(x == 3) AABBUtils.box16(main, 15, 0, 0, 16, 16, 16);
            }
        }

        // Drill Pipe
        if(y >= 1){
            if(x == 2 && z == 2){
                if(y == 7){
                    AABBUtils.box16(main, 4, 0, 4, 12, 4, 12);
                }else{
                    AABBUtils.box16(main, 4, 0, 4, 12, 16, 12);
                }
            }
        }

        // Redstone Controller
        if(x == 0 && z == 1){
            if(y == 0){
                AABBUtils.box16(main, 2, 0, 2, 6, 16, 4);
                AABBUtils.box16(main, 2, 0, 12, 6, 16, 14);
            }
            if(y == 1) AABBUtils.box16(main, 0, 0, 0, 8, 16, 16);
        }

        // Use default cube shape if nessesary
        if(main.isEmpty()){
            main.add(AABBUtils.FULL);
        }
        return main;
    }
}