package flaxbeard.immersivepetroleum.common.blocks.tileentities;

@Deprecated(forRemoval = true)
public class PumpjackTileEntity// extends PoweredMultiblockBlockEntity<PumpjackTileEntity, MultiblockRecipe> implements IPCommonTickableTile, IEBlockInterfaces.IBlockBounds
{
	/** Template-Location of the Energy Input Port. (0, 1, 5) */
	//public static final Set<BlockPos> Redstone_IN = ImmutableSet.of(new BlockPos(0, 1, 5));
	
	/** Template-Location of the Redstone Input Port. (2, 1, 5) */
	//public static final Set<MultiblockFace> Energy_IN = ImmutableSet.of(new MultiblockFace(2, 1, 5, RelativeBlockFace.UP));
	
	/** Template-Location of the Eastern Fluid Output Port. (2, 0, 2) */
	//public static final BlockPos East_Port = new BlockPos(2, 0, 2);
	
	/** Template-Location of the Western Fluid Output Port. (0, 0, 2) */
	//public static final BlockPos West_Port = new BlockPos(0, 0, 2);
	
	/**
	 * Template-Location of the Bottom Fluid Output Port. (1, 0, 0) <b>(Also Master Block)</b>
	 */
	/*public static final BlockPos Down_Port = new BlockPos(1, 0, 0);
	
	public static final FluidTank FAKE_TANK = new FluidTank(0);
	public boolean wasActive = false;
	public float activeTicks = 0;
	public PumpjackTileEntity(BlockEntityType<?> type, BlockPos pWorldPosition, BlockState pBlockState){
		//super(PumpjackMultiblock.INSTANCE, 16000, true, type, pWorldPosition, pBlockState);
	}
	
	//@Override
	public void readCustomNBT(CompoundTag nbt, boolean descPacket){
		//super.readCustomNBT(nbt, descPacket);
		boolean lastActive = this.wasActive;
		this.wasActive = nbt.getBoolean("wasActive");
		if(!this.wasActive && lastActive){
			this.activeTicks++;
		}
	}
	
	//@Override
	public void writeCustomNBT(CompoundTag nbt, boolean descPacket){
		//super.writeCustomNBT(nbt, descPacket);
		nbt.putBoolean("wasActive", this.wasActive);
	}
	
	@Override
	public void tickClient(){
		if(!isDummy() && this.wasActive){
			this.activeTicks++;
		}
	}
	
	@Override
	public void tickServer(){
		if(isDummy()){
			return;
		}
		
		super.tickServer();
		
		boolean active = false;
		if(!isRSDisabled()){
			BlockEntity teLow = this.getLevelNonnull().getBlockEntity(this.worldPosition.below());
			
			if(teLow instanceof WellPipeTileEntity pipe){
				WellTileEntity well = pipe.getWell();
				
				if(well != null){
					int consumption = IPServerConfig.EXTRACTION.pumpjack_consumption.get();
					int extracted = this.energyStorage.extractEnergy(consumption, true);
					
					if(extracted >= consumption){
						// Does any island still have pressure?
						boolean foundPressurizedIsland = false;
						for(ColumnPos cPos:well.tappedIslands){
							ReservoirIsland island = ReservoirHandler.getIsland(this.level, cPos);
							
							if(island != null && island.getPressure(getLevelNonnull(), cPos.x(), cPos.z()) > 0.0F){
								foundPressurizedIsland = true;
								break;
							}
						}
						
						// Skip if there is (Simulates pumpjack not being able to handle high pressures)
						if(!foundPressurizedIsland){
							int extractSpeed = IPServerConfig.EXTRACTION.pumpjack_speed.get();
							
							Direction portEast_facing = getIsMirrored() ? getFacing().getCounterClockWise() : getFacing().getClockWise();
							Direction portWest_facing = getIsMirrored() ? getFacing().getClockWise() : getFacing().getCounterClockWise();
							
							BlockPos portEast_pos = getBlockPosForPos(East_Port).relative(portEast_facing);
							BlockPos portWest_pos = getBlockPosForPos(West_Port).relative(portWest_facing);
							
							IFluidHandler portEast_output = FluidUtil.getFluidHandler(this.level, portEast_pos, portEast_facing.getOpposite()).orElse(null);
							IFluidHandler portWest_output = FluidUtil.getFluidHandler(this.level, portWest_pos, portWest_facing.getOpposite()).orElse(null);
							
							for(ColumnPos cPos:well.tappedIslands){
								ReservoirIsland island = ReservoirHandler.getIsland(this.level, cPos);
								if(island != null){
									FluidStack fluid = new FluidStack(island.getFluid(), island.extract(extractSpeed, FluidAction.SIMULATE));
									
									if(portEast_output != null){
										int accepted = portEast_output.fill(fluid, FluidAction.SIMULATE);
										if(accepted > 0){
											int drained = portEast_output.fill(FluidHelper.copyFluid(fluid, Math.min(fluid.getAmount(), accepted)), FluidAction.EXECUTE);
											island.extract(drained, FluidAction.EXECUTE);
											fluid = FluidHelper.copyFluid(fluid, fluid.getAmount() - drained);
											active = true;
										}
									}
									
									if(portWest_output != null && fluid.getAmount() > 0){
										int accepted = portWest_output.fill(fluid, FluidAction.SIMULATE);
										if(accepted > 0){
											int drained = portWest_output.fill(FluidHelper.copyFluid(fluid, Math.min(fluid.getAmount(), accepted)), FluidAction.EXECUTE);
											island.extract(drained, FluidAction.EXECUTE);
											active = true;
										}
									}
								}
							}
							
							if(active){
								this.energyStorage.extractEnergy(consumption, false);
								this.activeTicks++;
							}
						}
					}
				}
			}
		}
		
		if(active != this.wasActive){
			this.setChanged();
			this.markContainingBlockForUpdate(null);
		}
		
		this.wasActive = active;
	}
	
	@Override
	public Set<MultiblockFace> getEnergyPos(){
		return Energy_IN;
	}
	
	@Override
	public Set<BlockPos> getRedstonePos(){
		return Redstone_IN;
	}
	
	@Override
	public boolean isInWorldProcessingMachine(){
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
	public boolean additionalCanProcessCheck(MultiblockProcess<MultiblockRecipe> process){
		return false;
	}
	
	@Override
	public float getMinProcessDistance(MultiblockProcess<MultiblockRecipe> process){
		return 0;
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
	public boolean isStackValid(int slot, ItemStack stack){
		return true;
	}
	
	@Override
	public int getSlotLimit(int slot){
		return 64;
	}
	
	@Override
	public int[] getOutputSlots(){
		return null;
	}
	
	@Override
	public int[] getOutputTanks(){
		return new int[]{1};
	}
	
	@Override
	public void doGraphicalUpdates(){
		this.setChanged();
		this.markContainingBlockForUpdate(null);
	}
	
	@Override
	public MultiblockRecipe findRecipeForInsertion(ItemStack inserting){
		return null;
	}
	
	@Override
	protected MultiblockRecipe getRecipeForId(Level level, ResourceLocation id){
		return null;
	}
	
	@Override
	public NonNullList<ItemStack> getInventory(){
		return null;
	}
	
	@Override
	public IFluidTank[] getInternalTanks(){
		return null;
	}
	
	private final ResettableCapability<IFluidHandler> fakeFluidHandler = registerFluidHandler(FAKE_TANK);
	
	@Nonnull
	@Override
	public <C> LazyOptional<C> getCapability(@Nonnull Capability<C> capability, @Nullable Direction side){
		if(capability == ForgeCapabilities.FLUID_HANDLER){
			// East Port
			if(this.posInMultiblock.equals(East_Port)){
				if(side == null || (getIsMirrored() ? (side == getFacing().getCounterClockWise()) : (side == getFacing().getClockWise()))){
					return fakeFluidHandler.cast();
				}
			}
			// West Port
			if(this.posInMultiblock.equals(West_Port)){
				if(side == null || (getIsMirrored() ? (side == getFacing().getClockWise()) : (side == getFacing().getCounterClockWise()))){
					return fakeFluidHandler.cast();
				}
			}
		}
		return super.getCapability(capability, side);
	}
	
	private static final CachedShapesWithTransform<BlockPos, Pair<Direction, Boolean>> SHAPES = CachedShapesWithTransform.createForMultiblock(PumpjackTileEntity::getShape);
	
	@Override
	@Nonnull
	public VoxelShape getBlockBounds(CollisionContext ctx){
		return SHAPES.get(this.posInMultiblock, Pair.of(getFacing(), getIsMirrored()));
	}
	
	private static List<AABB> getShape(BlockPos posInMultiblock){
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
	}*/
}
