package flaxbeard.immersivepetroleum.common.blocks.tileentities;

@Deprecated(forRemoval = true)
public class DerrickTileEntity{ /*extends PoweredMultiblockBlockEntity<DerrickTileEntity, MultiblockRecipe> implements IPCommonTickableTile, ICanSkipGUI, IPMenuProvider<DerrickTileEntity>, IEBlockInterfaces.IBlockBounds{
	public static final int REQUIRED_WATER_AMOUNT = 125;
	public static final int REQUIRED_CONCRETE_AMOUNT = 125;
	
	public enum Inventory{
		/** Item Pipe Input */
		/*INPUT;

		public int id(){
			return ordinal();
		}
	}
	
	public static final FluidTank DUMMY_TANK = new FluidTank(0);
	
	/** Template-Location of the Fluid Input Port. (2 0 4)<br> */
	//public static final BlockPos Fluid_IN = new BlockPos(2, 0, 4);
	
	/** Template-Location of the Fluid Output Port. (4 0 2)<br> */
	//public static final BlockPos Fluid_OUT = new BlockPos(4, 0, 2);
	
	/** Template-Location of the Energy Input Ports.<br><pre>2 1 0</pre><br> */
	//public static final Set<MultiblockFace> Energy_IN = ImmutableSet.of(new MultiblockFace(2, 1, 0, RelativeBlockFace.UP));
	
	/** Template-Location of the Redstone Input Port. (0 1 1)<br> */
	/*public static final Set<BlockPos> Redstone_IN = ImmutableSet.of(new BlockPos(0, 1, 1));
	
	public int timer = 0;
	public int rotation = 0;
	public boolean drilling;
	public boolean spilling;
	public final FluidTank tank = new FluidTank(8000, this::acceptsFluid);
	public final NonNullList<ItemStack> inventory = NonNullList.withSize(1, ItemStack.EMPTY);
	
	/** Stores the current derrick configuration. */
	/*@Nullable
	public PipeConfig.Grid gridStorage;
	
	private Fluid fluidSpilled = Fluids.EMPTY;
	private int clientFlow;
	
	public DerrickTileEntity(BlockEntityType<? extends DerrickTileEntity> type, BlockPos pos, BlockState state){
		super(DerrickMultiblock.INSTANCE, 16000, true, type, pos, state);
	}
	
	@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket){
		super.readCustomNBT(nbt, descPacket);
		
		this.drilling = nbt.getBoolean("drilling");
		this.spilling = nbt.getBoolean("spilling");
		this.clientFlow = nbt.getInt("spillflow");
		
		try{
			this.fluidSpilled = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(nbt.getString("spillingfluid")));
		}catch(ResourceLocationException rle){
			this.fluidSpilled = Fluids.EMPTY;
		}
		
		this.tank.readFromNBT(nbt.getCompound("tank"));
		
		if(nbt.contains("grid", Tag.TAG_COMPOUND)){
			this.gridStorage = PipeConfig.Grid.fromCompound(nbt.getCompound("grid"));
		}
		
		if(!descPacket && !this.isDummy()){
			ContainerHelper.loadAllItems(nbt, this.inventory);
			this.setChanged();
		}
	}
	
	@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket){
		super.writeCustomNBT(nbt, descPacket);
		
		nbt.putBoolean("drilling", this.drilling);
		nbt.putBoolean("spilling", this.spilling);
		nbt.putInt("spillflow", getReservoirFlow());
		nbt.putString("spillingfluid", RegistryUtils.getRegistryNameOf(this.fluidSpilled).toString());
		
		nbt.put("tank", this.tank.writeToNBT(new CompoundTag()));
		
		if(this.gridStorage != null){
			nbt.put("grid", this.gridStorage.toCompound());
		}
		
		if(!descPacket && !this.isDummy()){
			ContainerHelper.saveAllItems(nbt, this.inventory);
		}
	}
	
	private int getReservoirFlow(){
		ReservoirIsland island = ReservoirHandler.getIsland(getLevelNonnull(), getBlockPos());
		if(island == null || this.worldPosition.getY() < getLevelNonnull().getSeaLevel())
			return 10;

		return island.getFlowFromPressure(getLevelNonnull(), getBlockPos());
	}
	
	// Only accept as much Concrete and Water as needed
	private boolean acceptsFluid(FluidStack fs){
		if(fs.isEmpty())
			return false;
		
		WellTileEntity well = createAndGetWell(false);
		if(well == null){
			return false;
		}
		
		final Fluid inFluid = fs.getFluid();
		final boolean isConcrete = inFluid == ExternalModContent.getIEFluid_Concrete();
		final boolean isWater = inFluid == Fluids.WATER;
		
		if(!isConcrete && !isWater)
			return false;
		
		int realPipeLength = (getBlockPos().getY() - 1) - well.getBlockPos().getY();
		int concreteNeeded = (REQUIRED_CONCRETE_AMOUNT * (realPipeLength - well.wellPipeLength));
		if(concreteNeeded > 0 && isConcrete){
			FluidStack tankFluidStack = this.tank.getFluid();
			
			if((!tankFluidStack.isEmpty() && inFluid != tankFluidStack.getFluid()) || tankFluidStack.getAmount() >= concreteNeeded){
				return false;
			}
			
			return concreteNeeded >= fs.getAmount();
		}
		
		if(concreteNeeded <= 0){
			int waterNeeded = REQUIRED_WATER_AMOUNT * (well.getMaxPipeLength() - well.wellPipeLength);
			if(waterNeeded > 0 && isWater){
				FluidStack tankFluidStack = this.tank.getFluid();
				
				if((!tankFluidStack.isEmpty() && inFluid != tankFluidStack.getFluid()) || tankFluidStack.getAmount() >= waterNeeded){
					return false;
				}
				
				return waterNeeded >= fs.getAmount();
			}
		}
		
		return false;
	}
	
	/** Used as a list of blocks that should be used for the drill particle effect */
	/*private static final BlockState[] PARTICLESTATES = new BlockState[]{
			Blocks.STONE.defaultBlockState(),
			Blocks.GRANITE.defaultBlockState(),
			Blocks.GRAVEL.defaultBlockState(),
			Blocks.DEEPSLATE.defaultBlockState(),
			Blocks.DIORITE.defaultBlockState(),
			Blocks.SAND.defaultBlockState(),
			Blocks.ANDESITE.defaultBlockState(),
	};
	
	@Override
	public void tickClient(){
		if(isDummy())
			return;
		
		if(this.drilling){
			this.rotation += 10;
			this.rotation %= 2160; // 360 * 6
			
			double x = (this.worldPosition.getX() + 0.5);
			double y = (this.worldPosition.getY() + 1.0);
			double z = (this.worldPosition.getZ() + 0.5);
			int r = this.level.random.nextInt(PARTICLESTATES.length);
			for(int i = 0;i < 5;i++){
				float xa = (this.level.random.nextFloat() - 0.5F) * 10.0F;
				float ya = 5.0F;
				float za = (this.level.random.nextFloat() - 0.5F) * 10.0F;
				
				this.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, PARTICLESTATES[r]), x, y, z, xa, ya, za);
			}
		}
		
		if(this.spilling){
			ClientProxy.spawnSpillParticles(this.level, this.worldPosition, this.fluidSpilled, 5, 1.25F, this.clientFlow);
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void tickServer(){
		if(isDummy())
			return;

		if(!this.level.isAreaLoaded(getBlockPos(), 2))
			return;
		
		boolean forceUpdate = false; // Should only be set to true if absolutely nessesary
		boolean lastDrilling = this.drilling;
		boolean lastSpilling = this.spilling;
		this.drilling = this.spilling = false;
		
		if(this.worldPosition.getY() < this.level.getSeaLevel()){
			if(this.fluidSpilled == Fluids.EMPTY){
				this.fluidSpilled = Fluids.WATER;
			}
			this.spilling = true;
		}else{
			WellTileEntity well = createAndGetWell(getInventory(Inventory.INPUT) != ItemStack.EMPTY);
			if(!isRSDisabled()){
				if(this.energyStorage.extractEnergy(IPServerConfig.EXTRACTION.derrick_consumption.get(), true) >= IPServerConfig.EXTRACTION.derrick_consumption.get()){
					
					if(well != null){
						if(well.wellPipeLength < well.getMaxPipeLength()){
							if(well.pipes <= 0 && getInventory(Inventory.INPUT) != ItemStack.EMPTY){
								ItemStack stack = getInventory(Inventory.INPUT);
								if(stack.getCount() > 0){
									stack.shrink(1);
									well.pipes = WellTileEntity.PIPE_WORTH;
									
									if(stack.getCount() <= 0){
										setInventory(Inventory.INPUT, ItemStack.EMPTY);
									}
									
									well.setChanged();
									forceUpdate = true;
								}
							}
							
							if(well.pipes > 0){
								final BlockPos dPos = getBlockPos();
								final BlockPos wPos = well.getBlockPos();
								int realPipeLength = ((dPos.getY() - 1) - wPos.getY());
								
								if(well.phyiscalPipesList.size() < realPipeLength && well.wellPipeLength < realPipeLength){
									if(this.tank.drain(REQUIRED_CONCRETE_AMOUNT, FluidAction.SIMULATE).getAmount() >= REQUIRED_CONCRETE_AMOUNT){
										this.energyStorage.extractEnergy(IPServerConfig.EXTRACTION.derrick_consumption.get(), false);
										
										if(advanceTimer()){
											Level world = getLevelNonnull();
											int y = dPos.getY() - 1;
											for(;y > wPos.getY();y--){
												BlockPos current = new BlockPos(dPos.getX(), y, dPos.getZ());
												BlockState state = world.getBlockState(current);
												
												if(state.getBlock() == Blocks.BEDROCK || state.getBlock() == IPContent.Blocks.WELL.get()){
													break;
												}else if(!(state.getBlock() == IPContent.Blocks.WELL_PIPE.get() && !state.getValue(WellPipeBlock.BROKEN))){
													world.destroyBlock(current, false);
													world.setBlockAndUpdate(current, IPContent.Blocks.WELL_PIPE.get().defaultBlockState());
													
													well.phyiscalPipesList.add(y);
													
													this.tank.drain(REQUIRED_CONCRETE_AMOUNT, FluidAction.EXECUTE);
													
													well.usePipe();
													break;
												}
											}
											
											if(well.phyiscalPipesList.size() >= realPipeLength && well.wellPipeLength >= realPipeLength){
												well.pastPhysicalPart = true;
												well.setChanged();
											}
										}
										
										forceUpdate = true;
										this.drilling = true;
									}
								}else{
									if(!this.tank.getFluid().isEmpty() && this.tank.getFluid().getFluid() == ExternalModContent.getIEFluid_Concrete()){
										// FIXME ! This happens every now and then, and i have not yet nailed down HOW this happens.
										// Void excess concrete.
										this.tank.drain(this.tank.getFluidAmount(), FluidAction.EXECUTE);
										forceUpdate = true;
									}
									if(this.tank.drain(REQUIRED_WATER_AMOUNT, FluidAction.SIMULATE).getAmount() >= REQUIRED_WATER_AMOUNT){
										this.energyStorage.extractEnergy(IPServerConfig.EXTRACTION.derrick_consumption.get(), false);
										
										if(advanceTimer()){
											restorePhysicalPipeProgress(well, dPos, realPipeLength);
											
											this.tank.drain(REQUIRED_WATER_AMOUNT, FluidAction.EXECUTE);
											well.usePipe();
										}
										
										forceUpdate = true;
										this.drilling = true;
									}
								}
							}
						}
					}
				}
			}
			if(well != null && well.wellPipeLength == well.getMaxPipeLength()) outputReservoirFluid();
		}
		
		if(this.spilling && this.fluidSpilled == Fluids.EMPTY){
			this.fluidSpilled = IPContent.Fluids.CRUDEOIL.get();
		}
		if(!this.spilling && this.fluidSpilled != Fluids.EMPTY){
			this.fluidSpilled = Fluids.EMPTY;
		}
		
		if(forceUpdate || lastDrilling != this.drilling || lastSpilling != this.spilling){
			updateMasterBlock(null, true);
			setChanged();
		}
	}
	
	private WellTileEntity wellCache = null;
	/**
	 * Create and Get the {@link WellTileEntity}.
	 * 
	 * @param popList Set to true, to try and populate the {@link WellTileEntity#tappedIslands} list.
	 * @return WellTileEntity or possibly null
	 */
	/*public WellTileEntity createAndGetWell(boolean popList){
		if(this.wellCache != null && this.wellCache.isRemoved()){
			this.wellCache = null;
		}
		
		if(this.wellCache == null){
			Level world = this.getLevelNonnull();
			WellTileEntity well = null;
			
			for(int y = getBlockPos().getY() - 1;y >= world.getMinBuildHeight();y--){
				BlockPos current = new BlockPos(this.getBlockPos().getX(), y, this.getBlockPos().getZ());
				BlockState state = world.getBlockState(current);
				
				if(state.getBlock() == IPContent.Blocks.WELL.get()){
					well = (WellTileEntity) world.getBlockEntity(current);
					break;
				}else if(state.getBlock() == Blocks.BEDROCK){
					world.setBlockAndUpdate(current, IPContent.Blocks.WELL.get().defaultBlockState());
					well = (WellTileEntity) world.getBlockEntity(current);
					break;
				}
			}
			
			this.wellCache = well;
		}
		
		if(popList && this.wellCache != null && this.wellCache.tappedIslands.isEmpty()){
			if(this.gridStorage != null){
				transferGridDataToWell(this.wellCache);
			}else{
				this.wellCache.tappedIslands.add(Utils.toColumnPos(this.worldPosition));
				this.wellCache.setChanged();
			}
		}
		
		if(this.wellCache != null){
			this.wellCache.abortSelfDestructSequence();
		}
		
		return this.wellCache;
	}
	
	/** Only gets the well if it exists, does not attempt to create it. May return null. */
	/*@Nullable
	public WellTileEntity getWell(){
		if(this.wellCache != null && this.wellCache.isRemoved()){
			this.wellCache = null;
		}
		
		if(this.wellCache == null){
			Level world = this.getLevelNonnull();
			WellTileEntity well = null;
			
			for(int y = getBlockPos().getY() - 1;y >= world.getMinBuildHeight();y--){
				BlockPos current = new BlockPos(this.getBlockPos().getX(), y, this.getBlockPos().getZ());
				BlockState state = world.getBlockState(current);
				
				if(state.getBlock() == IPContent.Blocks.WELL.get()){
					well = (WellTileEntity) world.getBlockEntity(current);
					break;
				}
			}
			
			this.wellCache = well;
		}
		
		return this.wellCache;
	}
	
	public void transferGridDataToWell(@Nullable WellTileEntity well){
		if(well != null){
			int additionalPipes = 0;
			List<ColumnPos> list = new ArrayList<>();
			PipeConfig.Grid grid = this.gridStorage;
			for(int j = 0;j < grid.getHeight();j++){
				for(int i = 0;i < grid.getWidth();i++){
					int type = grid.get(i, j);
					
					if(type > 0){
						switch(type){
							case PipeConfig.PIPE_PERFORATED:
							case PipeConfig.PIPE_PERFORATED_FIXED:{
								int x = i - (grid.getWidth() / 2);
								int z = j - (grid.getHeight() / 2);
								ColumnPos pos = new ColumnPos(this.worldPosition.getX() + x, this.worldPosition.getZ() + z);
								list.add(pos);
							}
							case PipeConfig.PIPE_NORMAL:{
								additionalPipes++;
							}
						}
					}
				}
			}
			
			well.tappedIslands = list;
			well.additionalPipes = additionalPipes;
			well.setChanged();
		}
	}
	
	/** Only returns true if the timer reached zero */
	/*private boolean advanceTimer(){
		if(this.timer-- <= 0){
			this.timer = 10;
			return true;
		}
		return false;
	}
	
	public void restorePhysicalPipeProgress(@Nonnull WellTileEntity well, BlockPos dPos, int realPipeLength){
		int min = Math.min(well.wellPipeLength, realPipeLength);
		for(int i = 1;i < min;i++){
			BlockPos current = new BlockPos(dPos.getX(), dPos.getY() - i, dPos.getZ());
			BlockState state = getLevelNonnull().getBlockState(current);
			if(!(state.getBlock() instanceof WellPipeBlock)){
				getLevelNonnull().destroyBlock(current, false);
				getLevelNonnull().setBlockAndUpdate(current, IPContent.Blocks.WELL_PIPE.get().defaultBlockState());
			}
		}
	}
	
	private void outputReservoirFluid(){
		WellTileEntity well = createAndGetWell(true);
		if(well == null){
			return;
		}
		
		FluidStack extracted = getExtractedFluidStack(well);
		if(!extracted.isEmpty()){
			Direction facing = getIsMirrored() ? getFacing().getCounterClockWise() : getFacing().getClockWise();
			BlockPos outPos = getBlockPosForPos(Fluid_OUT).relative(facing, 1);
			IFluidHandler output = FluidUtil.getFluidHandler(this.level, outPos, facing.getOpposite()).orElse(null);
			if(output != null){
				FluidStack fluid = FluidHelper.copyFluid(extracted, extracted.getAmount());
				
				int accepted = output.fill(fluid, FluidAction.SIMULATE);
				if(accepted > 0){
					boolean iePipe = this.level.getBlockEntity(outPos) instanceof IFluidPipe;
					
					int drained = output.fill(FluidHelper.copyFluid(fluid, Math.min(fluid.getAmount(), accepted), iePipe), FluidAction.EXECUTE);
					if(fluid.getAmount() - drained > 0){
						this.spilling = true;
					}
				}else{
					this.spilling = true;
				}
			}else{
				this.spilling = true;
			}
		}
		
		if(this.spilling && !extracted.isEmpty() && this.fluidSpilled != extracted.getFluid()){
			this.fluidSpilled = extracted.getFluid();
		}
		if(!this.spilling && this.fluidSpilled != Fluids.EMPTY){
			this.fluidSpilled = Fluids.EMPTY;
		}
	}
	
	private FluidStack getExtractedFluidStack(@Nonnull WellTileEntity well){
		Fluid extractedFluid = Fluids.EMPTY;
		int extractedAmount = 0;
		for(ColumnPos cPos:well.tappedIslands){
			ReservoirIsland island = ReservoirHandler.getIsland(this.level, cPos);
			if(island != null){
				if(extractedFluid == Fluids.EMPTY){
					extractedFluid = island.getFluid();
				}else if(island.getFluid() != extractedFluid){
					continue;
				}
				
				extractedAmount += island.extractWithPressure(getLevelNonnull(), cPos.x(), cPos.z());
			}
		}
		
		return new FluidStack(extractedFluid, extractedAmount);
	}
	
	@Override
	public void disassemble(){
		if(this.formed && !this.level.isClientSide && this.offsetToMaster.equals(Vec3i.ZERO)){
			WellTileEntity well = getWell();
			if(well != null && !well.drillingCompleted){
				if(well.wellPipeLength > 0){
					well.startSelfDestructSequence();
				}else{
					this.level.setBlockAndUpdate(well.getBlockPos(), Blocks.BEDROCK.defaultBlockState());
				}
			}
		}
		
		super.disassemble();
	}
	
	private final MultiblockCapability<IFluidHandler> fluidInputHandler = MultiblockCapability.make(
			this, be -> be.fluidInputHandler, DerrickTileEntity::master, registerFluidInput(this.tank)
	);
	private final ResettableCapability<IFluidHandler> dummyTank = registerFluidOutput(DUMMY_TANK);
	
	@Override
	public <C> LazyOptional<C> getCapability(Capability<C> capability, Direction side){
		if(capability == ForgeCapabilities.FLUID_HANDLER){
			if(this.posInMultiblock.equals(Fluid_IN) && (side == null || side == getFacing().getOpposite())){
				return this.fluidInputHandler.getAndCast();
			}
			if(this.posInMultiblock.equals(Fluid_OUT)){
				if(side == null || (getIsMirrored() ? side == getFacing().getCounterClockWise() : side == getFacing().getClockWise())){
					return this.dummyTank.cast();
				}
			}
		}
		return super.getCapability(capability, side);
	}
	
	@Override
	public boolean skipGui(Direction hitFace){
		Direction facing = getFacing();
		
		// Power input
		if(getEnergyPos().stream().anyMatch((t) -> t.posInMultiblock().equals(this.posInMultiblock)) && hitFace == Direction.UP){
			return true;
		}
		
		// Redstone controller input
		if(getRedstonePos().contains(this.posInMultiblock) && hitFace == (getIsMirrored() ? facing.getClockWise() : facing.getCounterClockWise())){
			return true;
		}
		
		// Fluid I/O Ports
		if(this.posInMultiblock.equals(Fluid_IN) && hitFace == getFacing().getOpposite()){
			return true;
		}
		if(this.posInMultiblock.equals(Fluid_OUT) && hitFace == (getIsMirrored() ? getFacing().getCounterClockWise() : getFacing().getClockWise())){
			return true;
		}
		
		return false;
	}
	
	public ItemStack getInventory(Inventory inv){
		return this.inventory.get(inv.id());
	}
	
	public ItemStack setInventory(Inventory inv, ItemStack stack){
		return this.inventory.set(inv.id(), stack);
	}
	
	@Override
	public DerrickTileEntity getGuiMaster(){
		return master();
	}
	
	@Override
	public BEContainerIP<? super DerrickTileEntity, ?> getContainerType(){
		return IPMenuTypes.DERRICK;
	}
	
	@Override
	public boolean canUseGui(Player player){
		return this.formed;
	}
	
	@Override
	public NonNullList<ItemStack> getInventory(){
		return this.inventory;
	}
	
	@Override
	public boolean isStackValid(int slot, ItemStack stack){
		return true;
	}
	
	@Override
	public int getSlotLimit(int slot){
		return 64;
	}
	
	@Override
	public void doGraphicalUpdates(){
		updateMasterBlock(null, true);
	}
	
	@Override
	protected MultiblockRecipe getRecipeForId(Level level, ResourceLocation id){
		return null;
	}
	
	@Override
	public Set<BlockPos> getRedstonePos(){
		return Redstone_IN;
	}
	
	@Override
	public Set<MultiblockFace> getEnergyPos(){
		return Energy_IN;
	}
	
	@Override
	public IFluidTank[] getInternalTanks(){
		return new IFluidTank[]{this.tank};
	}
	
	@Override
	public MultiblockRecipe findRecipeForInsertion(ItemStack inserting){
		return null;
	}
	
	@Override
	public int[] getOutputSlots(){
		return new int[0];
	}
	
	@Override
	public int[] getOutputTanks(){
		return new int[0];
	}
	
	@Override
	public boolean additionalCanProcessCheck(MultiblockProcess<MultiblockRecipe> process){
		return false;
	}
	
	@Override
	public void doProcessOutput(ItemStack output){
	}
	
	@Override
	public void doProcessFluidOutput(FluidStack output){
	}
	
	@Override
	public void onProcessFinish(MultiblockProcess<MultiblockRecipe> process){
	}
	
	@Override
	public int getMaxProcessPerTick(){
		return 1;
	}
	
	@Override
	public int getProcessQueueMaxLength(){
		return 1;
	}
	
	@Override
	public float getMinProcessDistance(MultiblockProcess<MultiblockRecipe> process){
		return 0;
	}
	
	@Override
	public boolean isInWorldProcessingMachine(){
		return false;
	}
	
	public boolean isLadder(){
		int x = this.posInMultiblock.getX();
		int y = this.posInMultiblock.getY();
		int z = this.posInMultiblock.getZ();
		
		return y >= 0 && y <= 2 && x == 0 && z == 2;
	}
	
	private static final CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> SHAPES = CachedShapesWithTransform.createForMultiblock(DerrickTileEntity::getShape);
	@Override
	public VoxelShape getBlockBounds(CollisionContext ctx){
		return SHAPES.get(this.posInMultiblock, Pair.of(getFacing(), getIsMirrored()));
	}
	
	private static List<AABB> getShape(BlockPos posInMultiblock){
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
	}*/
}
