/**
 * @author ArcAnc
 * Created at: 27.04.2024
 * Copyright (c) 2023
 * <p>
 * This code is licensed under "Ancient's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package flaxbeard.immersivepetroleum.common.util.damageSources;

import flaxbeard.immersivepetroleum.ImmersivePetroleum;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

public class IPDamageSources
{
    static final ResourceKey<DamageType> FLARESTACK = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(ImmersivePetroleum.MODID, "flarestack"));

    private static Holder<DamageType> type(RegistryAccess access, ResourceKey<DamageType> type)
    {
        return access.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(type);
    }

    public static DamageSource flarestack(Level level)
    {
        return new DamageSource(type(level.registryAccess(), FLARESTACK));
    }

    //public static final DamageSource FLARESTACK = new DamageSource("ipFlarestack").bypassArmor().setIsFire();
    public static void forceClassLoad()
    {

    }
}
