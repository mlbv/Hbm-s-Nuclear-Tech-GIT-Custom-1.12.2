package com.hbm.tileentity.machine;

import com.hbm.util.Tuple.Pair;

import api.hbm.tile.IHeatSource;
import com.hbm.tileentity.IGUIProvider;

import com.hbm.forgefluid.FFUtils;
import com.hbm.inventory.CombinationRecipes;

import com.hbm.inventory.container.ContainerFurnaceCombo;

import com.hbm.inventory.gui.GUIFurnaceCombo;

//import com.hbm.lib.ForgeDirection;
//import com.hbm.items.ModItems;
import com.hbm.tileentity.TileEntityMachineBase;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
//import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
//import net.minecraftforge.fluids.capability.IFluidTankProperties;
//import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

//import java.util.ArrayList;
import java.util.List;

public class TileEntityFurnaceCombination extends TileEntityMachineBase implements ITickable, IGUIProvider{

    public boolean wasOn;
    public int progress;
    public static int processTime = 20000;

    public int heat;
    public static int maxHeat = 100000;
    public static double diffusion = 0.25D;

    public FluidTank tank;
    public Fluid[] tankTypes;

    public TileEntityFurnaceCombination() {
        super(4);  // 4 slots
        this.tank = new FluidTank(24000);
        tankTypes = new Fluid[]{};
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            this.tryPullHeat();

            if (this.world.getTotalWorldTime() % 20 == 0) {
                this.sendFluidAndSmoke();
            }

            this.wasOn = false;

            if (canSmelt()) {
                int burn = heat / 100;

                if (burn > 0) {
                    this.wasOn = true;
                    this.progress += burn;
                    this.heat -= burn;

                    if (progress >= processTime) {
                        this.markDirty();
                        progress -= processTime;

                        Pair<ItemStack, FluidStack> pair = CombinationRecipes.getOutput(inventory.getStackInSlot(0));
                        ItemStack out = pair.getKey();
                        FluidStack fluid = pair.getValue();

                        if (out != null) {
                            if (inventory.getStackInSlot(1).isEmpty()) {
                                inventory.setStackInSlot(1, out.copy());
                            } else {
                                inventory.getStackInSlot(1).grow(out.getCount());
                            }
                        }

                        if (fluid != null) {
                            tank.fill(fluid, true);
                        }

                        inventory.getStackInSlot(0).shrink(1);
                    }

                    List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.up()).expand(1.5, 4, 1.5));
                    for (Entity e : entities) e.setFire(5);

                    //TODO: 实现污染系统
                    //if (world.getTotalWorldTime() % 20 == 0) {
                    //    // Emit pollution
                    //}
                }
                //System.out.println("Can Smelt! Heat=" + this.heat + "Burn=" + burn);
            } else {
                this.progress = 0;
                //System.out.println("Cannot Smelt! Heat=" + this.heat);
            }

            this.networkPack(new NBTTagCompound(), 50);
        }
        // else {
        //     if (this.wasOn) {
        //         world.spawnParticle(EnumParticleTypes.LAVA, pos.getX() + 0.5 + world.rand.nextGaussian() * 0.5, pos.getY() + 2, pos.getZ() + 0.5 + world.rand.nextGaussian() * 0.5, 0, 0, 0);
        //     }
        // }
    }

    public boolean canSmelt() {
        if (inventory.getStackInSlot(0).isEmpty()){
            //System.out.println("Slot(0) is Empty!");
            return false;
        }
        Pair<ItemStack, FluidStack> pair = CombinationRecipes.getOutput(inventory.getStackInSlot(0));

        if (pair == null){
            //System.out.println("Unable to get output pair!");
            return false;
        }
        ItemStack out = pair.getKey();
        FluidStack fluid = pair.getValue();

        if (!out.isEmpty()) {
            if (!inventory.getStackInSlot(1).isEmpty() && (!out.isItemEqual(inventory.getStackInSlot(1)) || out.getCount() + inventory.getStackInSlot(1).getCount() > inventory.getStackInSlot(1).getMaxStackSize())) {
                //System.out.println("Output incompatible!");
                return false;
            }
        }

        if (fluid != null) {
            if (tank.getFluidAmount() > 0 && !tank.getFluid().isFluidEqual(fluid)) {
                //System.out.println("Fluid type mismatch!");
                return false;
            }
            if (tank.getFluidAmount() + fluid.amount > tank.getCapacity()) {
                //System.out.println("Fluid tank is full!");
                return false;
            }
        }

        return true;
    }

    protected void tryPullHeat() {

        if(this.heat >= this.maxHeat) return;
        BlockPos blockBelow = pos.down();
        TileEntity con = world.getTileEntity(blockBelow);

        if(con instanceof IHeatSource) {
            IHeatSource source = (IHeatSource) con;
            int diff = source.getHeatStored() - this.heat;

            if(diff == 0) {
                return;
            }

            if(diff > 0) {
                diff = (int) Math.ceil(diff * diffusion);
                source.useUpHeat(diff);
                this.heat += diff;
                if(this.heat > this.maxHeat)
                    this.heat = this.maxHeat;
                return;
            }
        }

        this.heat = Math.max(this.heat - Math.max(this.heat / 1000, 1), 0);
    }
    
    private void sendFluidAndSmoke() {
        // 遍历东南西北四个方向
        world.spawnParticle(EnumParticleTypes.LAVA, pos.getX() + 0.5 + world.rand.nextGaussian() * 0.5, pos.getY() + 2, pos.getZ() + 0.5 + world.rand.nextGaussian() * 0.5, 0, 0, 0);
        for (EnumFacing dir : EnumFacing.HORIZONTALS) {
            // 获取与当前方向垂直的方向
            EnumFacing rot = dir.rotateY();

            // 在当前层和上一层遍历三个位置
            for (int y = pos.getY(); y <= pos.getY() + 1; y++) {
                for (int j = -1; j <= 1; j++) {
                    BlockPos targetPos = pos.offset(dir, 2).offset(rot, j).up(y - pos.getY());
                        
                    // 发送流体
                    if (tank.getFluidAmount() > 0) {
                        FFUtils.fillFluid(this, tank, world, targetPos, tank.getFluidAmount());
                    }
                }
            }

            // 处理顶部的烟雾和流体扩散
            for (int x = pos.getX() - 1; x <= pos.getX() + 1; x++) {
                for (int z = pos.getZ() - 1; z <= pos.getZ() + 1; z++) {
                    BlockPos targetPos = new BlockPos(x, pos.getY() + 2, z);

                    // 发送流体
                    if (tank.getFluidAmount() > 0) {
                        FFUtils.fillFluid(this, tank, world, targetPos, tank.getFluidAmount());
                    }

                    // 生成烟雾粒子效果
                    if(this.wasOn && world.rand.nextInt(15) == 0)
                        spawnSmokeParticles(targetPos, EnumFacing.UP);
                }
            }
        }
    }
    private void spawnSmokeParticles(BlockPos pos, EnumFacing direction) {
        World world = this.getWorld();
        double x = pos.getX() + 0.5 + direction.getFrontOffsetX() * 0.5;
        double y = pos.getY() + 1;
        double z = pos.getZ() + 0.5 + direction.getFrontOffsetZ() * 0.5;

        world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, x, y, z, 0.0D, 0.0D, 0.0D);
    }
/*
    private void sendFluidAndSmoke() {
        // Simulate sending fluid and smoke, specific implementation details should be added
        for (Pair<BlockPos, EnumFacing> pos : getConPos()) {
            // 发送流体
            if (tank.getFluidAmount() > 0) {
                FFUtils.fillFluid(this, tank, world, pos.getKey(), tank.getFluidAmount());
            }

            // 生成烟雾粒子效果
            spawnSmokeParticles(pos.getKey(), pos.getValue());
        }
    }



    List<Pair<BlockPos, EnumFacing>> conPos;

    protected List<Pair<BlockPos, EnumFacing>> getConPos() {
        if (conPos != null && !conPos.isEmpty()) {
            return conPos;
        }
    
        conPos = new ArrayList<>();
        EnumFacing dir = EnumFacing.getFront(this.getBlockMetadata());
    
        // 检查并过滤非法的方向
        if (dir == EnumFacing.DOWN || dir == EnumFacing.UP) {
            return conPos; // 返回空列表或处理其他情况
        }
    
        EnumFacing rot = dir.rotateY();
    
        // 获取不同方向的连接点
        for (int i = 0; i < 4; i++) {
            conPos.add(Pair.of(pos.offset(dir, 2).offset(rot, i - 1), dir));
            conPos.add(Pair.of(pos.up(2).offset(rot, i - 1), EnumFacing.UP));
        }
    
        return conPos;
    }
    
*/
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setBoolean("wasOn", wasOn);
        compound.setInteger("heat", heat);
        compound.setInteger("progress", progress);
        compound.setTag("tank", tank.writeToNBT(new NBTTagCompound()));
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        wasOn = compound.getBoolean("wasOn");
        heat = compound.getInteger("heat");
        progress = compound.getInteger("progress");
        tank.readFromNBT(compound.getCompoundTag("tank"));
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return new int[]{0, 1};
    }

    @Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
        return i == 0 && CombinationRecipes.getOutput(stack) != null;
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack itemStack, int amount) {
        return isItemValidForSlot(slot, itemStack);
    }

    @Override
	public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
        return slot == 1;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.tank);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    //@Override
    //public Container createContainer(EntityPlayer player) {
    //    // Return the container for the GUI, implement as needed
    //    return null;
    //}

    @Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerFurnaceCombo(player.inventory, this);
	}

    @Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIFurnaceCombo(player.inventory, this);
	}

    @Override
    public String getName() {
        return "container.furnaceCombination";
    }

    @Override
	public boolean hasCustomInventoryName() {
        return false;
    }
}
