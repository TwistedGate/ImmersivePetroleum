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

public class CokerShape extends GenericShape{
    public static final CokerShape GETTER = new CokerShape();

    private CokerShape(){
    }

    @Nonnull
    protected List<AABB> getShape(BlockPos posInMultiblock){
        int x = posInMultiblock.getX();
        int y = posInMultiblock.getY();
        int z = posInMultiblock.getZ();

        List<AABB> main = new ArrayList<>();

        if((y >= 15 && y <= 22) && z == 2 && (x == 2 || x == 6)){
            // The two vertical pipes at the very top
            AABBUtils.box16(main, 4, 0, 4, 12, 16, 12);
        }

        { // Catwalk Railings
            if(y == 22 || y == 18){
                if(x == 1){
                    AABBUtils.box16(main, 0, 0, 0, 1, 16, 16); // West Plate
                }
                if(x == 7){
                    AABBUtils.box16(main, 15, 0, 0, 16, 16, 16); // East Plate
                }
                if(z == 1){
                    AABBUtils.box16(main, 0, 0, 0, 16, 16, 1); // South Plate
                }
                if(z == 3){
                    AABBUtils.box16(main, 0, 0, 15, 16, 16, 16); // North Plate
                }
            }
            if(y == 14 || y == 9){
                if(x >= 0 && x <= 8){
                    if(z == 0){
                        AABBUtils.box16(main, 0, 0, 0, 16, 16, 1); // South Plate
                    }
                    if(z == 4){
                        AABBUtils.box16(main, 0, 0, 15, 16, 16, 16); // North Plate
                    }

                    if(x == 8 && (z == 0 || z == 4)){
                        AABBUtils.box16(main, 15, 0, 0, 16, 16, 16); // East Plate
                    }

                    if(x == 0 && (z == 0 || z == 4)){
                        AABBUtils.box16(main, 0, 0, 0, 1, 16, 16); // West Plate
                    }
                }
            }
        }

        // Catwalk
        {
            // Lower 2
            {
                if(y == 8 || y == 13){
                    if(!(x >= 1 && x <= 3 && z >= 1 && z <= 3) && !(x >= 5 && x <= 7 && z >= 1 && z <= 3)){
                        if(!(x == 8 && z == 2)){
                            AABBUtils.box16(main, 0, 8, 0, 16, 16, 16);
                        }
                    }
                }
            }

            // Upper 2
            {
                if(y == 17 || y == 21){
                    if(x >= 1 && x <= 7 && z >= 1 && z <= 3){
                        if(!((x == 2 || x == 6 || x == 7) && z == 2)){
                            AABBUtils.box16(main, 0, 8, 0, 16, 16, 16);
                        }
                    }
                }
            }
        }

        // Support Beams
        {
            // Vertical Beams
            boolean lower = y >= 4 && y <= 13;
            boolean upper = y >= 14 && y <= 21;
            if(lower || upper){
                // Corners
                if((lower && x == 0 && z == 0) || (upper && ((x == 1 && z == 1) || (x == 5 && z == 1)))){
                    AABBUtils.box16(main, 2, 0, 2, 6, 16, 6);
                }
                if((lower && x == 0 && z == 4) || (upper && ((x == 1 && z == 3) || (x == 5 && z == 3)))){
                    AABBUtils.box16(main, 2, 0, 10, 6, 16, 14);
                }
                if((lower && x == 8 && z == 0) || (upper && ((x == 3 && z == 1) || (x == 7 && z == 1)))){
                    AABBUtils.box16(main, 10, 0, 2, 14, 16, 6);
                }
                if((lower && x == 8 && z == 4) || (upper && ((x == 3 && z == 3) || (x == 7 && z == 3)))){
                    AABBUtils.box16(main, 10, 0, 10, 14, 16, 14);
                }

                // Middle
                if(lower && x == 4 && z == 0){
                    AABBUtils.box16(main, 6, 0, 2, 10, 16, 6);
                }
                if(lower && x == 4 && z == 4){
                    AABBUtils.box16(main, 6, 0, 10, 10, 16, 14);
                }
            }

            // Horizontal Beams
            if(y == 6 || y == 11){
                //AABBUtils.box16(main, 0, 2, 0, 16, 6, 16);
                if(x >= 0 && x <= 8){
                    if(z == 0){
                        double xa = (x == 0) ? 2 : 0;
                        double xb = (x == 8) ? 14 : 16;
                        AABBUtils.box16(main, xa, 2, 2, xb, 6, 6);
                    }
                    if(z == 4){
                        double xa = (x == 0) ? 2 : 0;
                        double xb = (x == 8) ? 14 : 16;
                        AABBUtils.box16(main, xa, 2, 10, xb, 6, 14);
                    }
                }
                if(z >= 0 && z <= 4){
                    if(x == 0){
                        double za = (z == 0) ? 2 : 0;
                        double zb = (z == 4) ? 14 : 16;
                        AABBUtils.box16(main, 2, 2, za, 6, 6, zb);
                    }
                    if(x == 8 && z != 2){
                        double za = (z == 0) ? 2 : 0;
                        double zb = (z == 4) ? 14 : 16;
                        AABBUtils.box16(main, 10, 2, za, 14, 6, zb);
                    }
                }
            }
        }

        { // Pipes

            // Up, Down, North, South, East, West Connection
            boolean u = false, d = false, n = false, s = false, e = false, w = false;

            // Straight & T Pipes
            {
                // Vertical, Horizontal-X, Horizontal-Z
                boolean v = false, hX = false, hZ = false;

                if(((y >= 1 && y <= 7 && y != 3) && x == 2 && z == 4) || ((y >= 9 && y <= 13) && x == 0 && z == 2) || (y == 1 && x == 5 && z == 4)){
                    v = true;

                    if(y == 2 || y == 7 || (x == 5 && z == 4)){
                        u = true;
                    }
                    if(y == 1 || y == 4 || y == 9){
                        d = true;
                    }
                }

                if(y == 0){
                    if(x == 3 && z == 3){
                        hZ = n = s = true;
                    }
                }

                if(y == 1){
                    if(x == 4 && z == 2){
                        v = u = d = true;
                    }
                }
                if(y == 2){
                    if(x == 3 && z == 3){
                        hX = w = true;
                    }
                    if(x == 4 && z == 3){
                        hX = e = true;
                    }
                    if((x == 3 || x == 5) && z == 2){
                        hX = e = w = true;
                    }
                    if(x == 4 && z == 2){
                        hX = e = w = d = true;
                        AABBUtils.box16(main, 4, 2, 4, 12, 4, 12);
                    }
                    if(x == 5 && z == 3){
                        hX = e = w = s = true;
                        AABBUtils.box16(main, 4, 4, 12, 12, 12, 14);
                    }
                }
                if(y == 8){
                    if(x == 1 && z == 4){
                        hX = e = w = true;
                    }
                    if(x == 0 && z == 3){
                        hZ = n = s = true;
                    }
                }

                if(v) AABBUtils.box16(main, 4, 0, 4, 12, 16, 12); // Vertical Pipe
                if(hX) AABBUtils.box16(main, 0, 4, 4, 16, 12, 12); // Horizontal-X Pipe
                if(hZ) AABBUtils.box16(main, 4, 4, 0, 12, 12, 16); // Horizontal-Z Pipe
            }

            // 90° Bends
            {
                if(y == 0){
                    if(x == 4 && z == 2){
                        u = w = true;
                        AABBUtils.box16(main, 2, 4, 4, 12, 14, 12);
                    }
                    if(x == 3 && z == 2){
                        e = s = true;
                        AABBUtils.box16(main, 4, 4, 4, 14, 12, 14);
                    }
                }
                if(y == 2){
                    if(x == 2 && z == 3){
                        n = e = true;
                        AABBUtils.box16(main, 4, 4, 2, 14, 12, 12);
                    }
                    if(x == 5 && z == 4){
                        d = n = true;
                        AABBUtils.box16(main, 4, 2, 2, 12, 12, 12);
                    }
                    if(x == 6 && z == 3){
                        w = n = true;
                        AABBUtils.box16(main, 2, 4, 2, 12, 12, 12);
                    }
                }
                if(y == 8){
                    if(x == 0 && z == 2){
                        u = s = true;
                        AABBUtils.box16(main, 4, 4, 4, 12, 14, 14);
                    }
                    if(x == 0 && z == 4){
                        n = e = true;
                        AABBUtils.box16(main, 4, 4, 2, 14, 12, 12);
                    }
                    if(x == 2 && z == 4){
                        d = w = true;
                        AABBUtils.box16(main, 2, 2, 4, 12, 12, 12);
                    }
                }
            }

            if(u) AABBUtils.box16(main, 2, 14, 2, 14, 16, 14); // Top Connection
            if(d) AABBUtils.box16(main, 2, 0, 2, 14, 2, 14); // Bottom Connection
            if(n) AABBUtils.box16(main, 2, 2, 0, 14, 14, 2); // North Connection
            if(s) AABBUtils.box16(main, 2, 2, 14, 14, 14, 16); // South Connection
            if(e) AABBUtils.box16(main, 14, 2, 2, 16, 14, 14); // East Connection
            if(w) AABBUtils.box16(main, 0, 2, 2, 2, 14, 14); // West Connection
        }

        // Redstone Controller
        if(x == 1 && z == 4){
            if(y == 0){ // Bottom
                AABBUtils.box16(main, 0, 0, 0, 16, 8, 16);
                AABBUtils.box16(main, 12, 0, 10, 14, 16, 14);
                AABBUtils.box16(main, 2, 0, 10, 4, 16, 14);
            }
            if(y == 1){ // Top
                AABBUtils.box16(main, 0, 0, 8, 16, 16, 16);
            }
        }

        // Power Sockets
        if(y == 1 && z == 4 && (x == 6 || x == 7)){
            AABBUtils.box16(main, 0, 0, 0, 16, 16, 14);
            AABBUtils.box16(main, 4, 4, 14, 12, 12, 16);
        }

        // Power Cables
        if((x == 6 && y == 2 && z == 4) || (y >= 4 && y <= 13 && x == 4 && z == 2)){
            AABBUtils.box16(main, 5, 0, 5, 11, 16, 11);
        }

        // Bitumen input Sign
        if(y == 1 && x == 3 && z == 4){
            AABBUtils.box16(main, 0, 15, 14, 16, 17, 16);
            AABBUtils.box16(main, 1, 1, 14, 15, 15, 15);
        }

        // Primary Ladder
        if((x == 8 && z == 2) && (y >= 5 && y <= 13)){
            AABBUtils.box16(main, 0, 0, 2, 1, 16, 14); // West Plate
        }

        // Secondary Ladder
        if((x == 7 && z == 2) && (y >= 15 && y <= 21)){
            AABBUtils.box16(main, 0, 0, 2, 1, 16, 14); // West Plate
        }

        // Baseplate
        if(y == 0){
            if(!((z == 0 || z == 4) && (x == 0 || x == 4 || x == 8)) && !(z == 4 && (x == 2 || x == 3 || (x >= 5 && x <= 7)))){
                AABBUtils.box16(main, 0, 0, 0, 16, 8, 16);
            }
        }

        // Chambers
        {
            if(y >= 4 && y <= 13){
                if(x == 1 || x == 5){
                    if(z == 1) List.of(AABBUtils.box16(1, 0, 1, 16, 16, 16));
                    if(z == 2) List.of(AABBUtils.box16(1, 0, 0, 16, 16, 16));
                    if(z == 3) List.of(AABBUtils.box16(1, 0, 0, 16, 16, 15));
                }
                if(x == 2 || x == 6){
                    if(z == 1) List.of(AABBUtils.box16(0, 0, 1, 16, 16, 16));
                    if(z == 3) List.of(AABBUtils.box16(0, 0, 0, 16, 16, 15));
                }
                if(x == 3 || x == 7){
                    if(z == 1) List.of(AABBUtils.box16(0, 0, 1, 15, 16, 16));
                    if(z == 2) List.of(AABBUtils.box16(0, 0, 0, 15, 16, 16));
                    if(z == 3) List.of(AABBUtils.box16(0, 0, 0, 15, 16, 15));
                }
            }
        }

        // Fences
        {
            if(y == 0 || y == 1){
                if(z >= 1 && z <= 3){
                    if(x == 0){
                        AABBUtils.box16(main, 6, 0, 0, 7, 16, 16);
                    }
                    if(x == 8){
                        AABBUtils.box16(main, 9, 0, 0, 10, 16, 16);
                    }
                }
                if(x >= 1 && x <= 3 || x >= 5 && x <= 7){
                    if(z == 0){
                        AABBUtils.box16(main, 0, 0, 6, 16, 16, 7);
                    }
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
