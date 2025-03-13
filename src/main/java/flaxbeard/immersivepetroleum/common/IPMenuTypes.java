package flaxbeard.immersivepetroleum.common;

import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.CokerUnitLogic;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.DerrickLogic;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.distillation_tower.DistillationTowerLogic;
import flaxbeard.immersivepetroleum.common.blocks.multiblocks.logic.hydro_treater.HydroTreaterLogic;
import flaxbeard.immersivepetroleum.common.gui.CokerUnitContainer;
import flaxbeard.immersivepetroleum.common.gui.DerrickContainer;
import flaxbeard.immersivepetroleum.common.gui.DistillationTowerContainer;
import flaxbeard.immersivepetroleum.common.gui.HydrotreaterContainer;

public class IPMenuTypes{
	/*public static final BEContainerIP<DistillationTowerTileEntity, DistillationTowerContainer> DISTILLATION_TOWER =
			register("distillation_tower", DistillationTowerContainer::new);
	public static final BEContainerIP<DerrickTileEntity, DerrickContainer> DERRICK =
			register("derrick", DerrickContainer::new);
	public static final BEContainerIP<CokerUnitTileEntity, CokerUnitContainer> COKER =
			register("coker", CokerUnitContainer::new);
	public static final BEContainerIP<HydrotreaterTileEntity, HydrotreaterContainer> HYDROTREATER =
			register("hydrotreater", HydrotreaterContainer::new);
	*/
	public static void forceClassLoad(){}

	public static final IEMenuTypes.MultiblockContainer<DistillationTowerLogic.State, DistillationTowerContainer> DISTILLATION_TOWER = IEMenuTypes.registerMultiblock(
			"distillation_tower", DistillationTowerContainer :: makeServer, DistillationTowerContainer :: makeClient);
	public static final IEMenuTypes.MultiblockContainer<DerrickLogic.State, DerrickContainer> DERRICK = IEMenuTypes.registerMultiblock(
			"derrick", DerrickContainer :: makeServer, DerrickContainer :: makeClient);
	public static final IEMenuTypes.MultiblockContainer<CokerUnitLogic.State, CokerUnitContainer> COKER = IEMenuTypes.registerMultiblock(
			"coker", CokerUnitContainer :: makeServer, CokerUnitContainer :: makeClient);
	public static final IEMenuTypes.MultiblockContainer<HydroTreaterLogic.State, HydrotreaterContainer> HYDROTREATER = IEMenuTypes.registerMultiblock(
			"hydrotreater", HydrotreaterContainer :: makeServer, HydrotreaterContainer :: makeClient);


	/*public static <S extends IMultiblockState, C extends IEContainerMenu>
	MultiblockContainer<S, C> registerMultiblock(
			String name,
			IEMenuTypes.ArgContainerConstructor<IEContainerMenu.MultiblockMenuContext<S>, C> container,
			IEMenuTypes.ClientContainerConstructor<C> client
	)
	{
		RegistryObject<MenuType<C>> typeRef = registerType(name, client);
		return new MultiblockContainer<>(typeRef, container);
	}

	private static <C extends IEContainerMenu>
	RegistryObject<MenuType<C>> registerType(String name, IEMenuTypes.ClientContainerConstructor<C> client)
	{
		return IPRegisters.registerMenu(
				name, () -> {
					Mutable<MenuType<C>> typeBox = new MutableObject<>();
					MenuType<C> type = new MenuType<>((id, inv) -> client.construct(typeBox.getValue(), id, inv), FeatureFlagSet.of());
					typeBox.setValue(type);
					return type;
				}
		);
	}
	*/
	/*public static class MultiblockContainer<S extends IMultiblockState, C extends IEContainerMenu> extends
			ArgContainer<IEContainerMenu.MultiblockMenuContext<S>, C>
	{
		private MultiblockContainer(
				RegistryObject<MenuType<C>> type,
				IEMenuTypes.ArgContainerConstructor<IEContainerMenu.MultiblockMenuContext<S>, C> factory
		)
		{
			super(type, factory);
		}

		public MenuProvider provide(IMultiblockContext<S> ctx, BlockPos relativeClicked)
		{
			return provide(new IEContainerMenu.MultiblockMenuContext<>(ctx, ctx.getLevel().toAbsolute(relativeClicked)));
		}
	}

	public static class ArgContainer<T, C extends IEContainerMenu>
	{
		private final RegistryObject<MenuType<C>> type;
		private final IEMenuTypes.ArgContainerConstructor<T, C> factory;

		private ArgContainer(RegistryObject<MenuType<C>> type, IEMenuTypes.ArgContainerConstructor<T, C> factory)
		{
			this.type = type;
			this.factory = factory;
		}

		public C create(int windowId, Inventory playerInv, T tile)
		{
			return factory.construct(getType(), windowId, playerInv, tile);
		}

		public MenuProvider provide(T arg)
		{
			return new MenuProvider()
			{
				@Nonnull
				@Override
				public Component getDisplayName()
				{
					return Component.empty();
				}

				@Nullable
				@Override
				public AbstractContainerMenu createMenu(
						int containerId, @Nonnull Inventory inventory, @Nonnull Player player
				)
				{
					return create(containerId, inventory, arg);
				}
			};
		}

		public MenuType<C> getType()
		{
			return type.get();
		}
	}
	*/
	/*public static <T extends BlockEntity, C extends IEContainerMenu> BEContainerIP<T, C> register(String name, IEMenuTypes.BEContainerConstructor<T, C> container){
		RegistryObject<MenuType<C>> typeRef = IPRegisters.registerMenu(name, () -> {
			Mutable<MenuType<C>> typeBox = new MutableObject<>();
			MenuType<C> type = new MenuType<>((IContainerFactory<C>) (windowId, inv, data) -> {
				Level world = ImmersivePetroleum.proxy.getClientWorld();
				BlockPos pos = data.readBlockPos();
				BlockEntity te = world.getBlockEntity(pos);
				return container.construct(typeBox.getValue(), windowId, inv, (T) te);
			});
			typeBox.setValue(type);
			return type;
		});
		return new BEContainerIP<>(typeRef, container);
	}*/
}
