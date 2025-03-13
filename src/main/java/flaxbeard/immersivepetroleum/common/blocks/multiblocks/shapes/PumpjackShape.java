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

public class PumpjackShape extends GenericShape{
    public static final PumpjackShape GETTER = new PumpjackShape();

    private PumpjackShape(){
    }

    @Nonnull
    protected List<AABB> getShape(BlockPos posInMultiblock){
        final int x = posInMultiblock.getX();
        final int y = posInMultiblock.getY();
        final int z = posInMultiblock.getZ();

        List<AABB> main = new ArrayList<>();

        // Most of the arm doesnt need collision. No point because it's a moving piece.
        if((y == 3 && x == 1 && z != 2) || (x == 1 && y == 2 && z == 0)){
            return main;
        }

        // Motor
        if(y < 3 && x == 1 && z == 4){
            if(y == 2){
                AABBUtils.box16(main, 4, 0, 0, 12, 4, 16);
            }else{
                AABBUtils.box16(main, 4, 0, 0, 12, 16, 16);
            }
            if(y == 0){
                AABBUtils.box16(main, 0, 0, 0, 16, 8, 16);
            }
        }

        // Support
        if(z == 2 && y > 0){
            if(x == 0){
                if(y == 1){
                    AABBUtils.box16(main, 11, 0, 0, 16, 16, 4);
                    AABBUtils.box16(main, 11, 0, 12, 16, 16, 16);
                }
                if(y == 2){
                    AABBUtils.box16(main, 13, 0, 0, 16, 8, 16);
                    AABBUtils.box16(main, 13, 8, 4, 16, 16, 12);
                }
                if(y == 3){
                    AABBUtils.box16(main, 15, 0, 6, 16, 2, 10);
                }
            }
            if(x == 1 && y == 3){
                AABBUtils.box16(main, 0, -2, 6, 16, 2, 10);
            }
            if(x == 2){
                if(y == 1){
                    AABBUtils.box16(main, 0, 0, 0, 5, 16, 4);
                    AABBUtils.box16(main, 0, 0, 12, 5, 16, 16);
                }
                if(y == 2){
                    AABBUtils.box16(main, 0, 0, 0, 3, 8, 16);
                    AABBUtils.box16(main, 0, 8, 4, 3, 16, 12);
                }
                if(y == 3){
                    AABBUtils.box16(main, 0, 0, 6, 1, 2, 10);
                }
            }
        }

        // Redstone Controller
        if(x == 0 && z == 5){
            if(y == 0){ // Bottom
                AABBUtils.box16(main, 12, 0, 10, 14, 16, 14);
                AABBUtils.box16(main, 2, 0, 10, 4, 16, 14);
            }
            if(y == 1){ // Top
                AABBUtils.box16(main, 0, 0, 8, 16, 16, 16);
            }
        }

        // Below the power-in block, base height
        if(x == 2 && y == 0 && z == 5){
            AABBUtils.box16(main, 0, 0, 0, 16, 16, 16);
        }

        // Misc
        if(y == 0){

            // Baseplate, ignoring Well and Fluid Ports
            if(!((x == 2 && z == 5) || (z == 2 && (x == 0 || x == 2))) && (x >= 0 && x <= 2 && z >= 1 && z <= 5))
                AABBUtils.box16(main, 0, 0, 0, 16, 8, 16);

            // Legs Bottom Front
            if(z == 1 && (x == 0 || x == 2)){
                AABBUtils.box16(main, 0, 0, 0, 16, 8, 16);

                if(x == 0){
                    AABBUtils.box16(main, 8, 8, 8, 16, 16, 16);
                }
                if(x == 2){
                    AABBUtils.box16(main, 0, 8, 8, 8, 16, 16);
                }
            }

            // Legs Bottom Back
            if(z == 3 && (x == 0 || x == 2)){
                if(x == 0){
                    AABBUtils.box16(main, 8, 8, 0, 16, 16, 8);
                }
                if(x == 2){
                    AABBUtils.box16(main, 0, 8, 0, 8, 16, 8);
                }
            }

            if(x == 1){
                // Well
                if(z == 0){
                    AABBUtils.box16(main, 5, 8, 13, 11, 14, 16);
                    AABBUtils.box16(main, 3, 0, 3, 13, 16, 13);
                }

                // Pipes
                if(z == 1){
                    AABBUtils.box16(main, 5, 8, 0, 11, 14, 16);
                }
                if(z == 2){
                    AABBUtils.box16(main, 5, 8, 0, 11, 14, 11);
                    AABBUtils.box16(main, 0, 8, 5, 16, 14, 11);
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