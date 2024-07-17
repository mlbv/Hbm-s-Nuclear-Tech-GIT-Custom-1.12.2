package com.hbm.tileentity.machine.oil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;

import com.hbm.blocks.ModBlocks;
import com.hbm.forgefluid.FFUtils;
import com.hbm.forgefluid.ModForgeFluids;
import com.hbm.interfaces.ITankPacketAcceptor;
import com.hbm.tileentity.TileEntityLoadedBase;

import api.hbm.energy.IEnergyUser;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public abstract class TileEntityOilDrillBase extends TileEntityLoadedBase implements ITickable, IEnergyUser, IFluidHandler, ITankPacketAcceptor
{
    public ItemStackHandler inventory;

    public long power;
    public int warning;
    public int warning2;
    public int age = 0;
    public int age2 = 0;
    public FluidTank[] tanks;
    public Fluid[] tankTypes;
    public boolean needsUpdate;

    public TileEntityOilDrillBase() {
        this(6);
    }

    public TileEntityOilDrillBase(int slots) {
        inventory = new ItemStackHandler(slots){
            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
                super.onContentsChanged(slot);
            }
        };
        tanks = new FluidTank[3];
        tankTypes = new Fluid[3];

        tanks[0] = new FluidTank(128000);
        tankTypes[0] = ModForgeFluids.oil;
        tanks[1] = new FluidTank(128000);
        tankTypes[1] = ModForgeFluids.gas;
        needsUpdate = false;
    }

    private String customName;


    public boolean hasCustomInventoryName() {
        return this.customName != null && this.customName.length() > 0;
    }

    public void setCustomName(String name) {
        this.customName = name;
    }

    protected String getCustomName() {
        return customName;
    }

    public boolean isUseableByPlayer(EntityPlayer player) {
        if(world.getTileEntity(pos) != this)
        {
            return false;
        }else{
            return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <=128;
        }
    }


    @Override
    public void readFromNBT(NBTTagCompound compound) {
        this.power = compound.getLong("powerTime");
        this.age = compound.getInteger("age");
        tankTypes[0] = ModForgeFluids.oil;
        tankTypes[1] = ModForgeFluids.gas;
        if(compound.hasKey("tanks"))
            FFUtils.deserializeTankArray(compound.getTagList("tanks", 10), tanks);
        if(compound.hasKey("inventory"))
            inventory.deserializeNBT(compound.getCompoundTag("inventory"));
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setLong("powerTime", power);
        compound.setInteger("age", age);
        compound.setTag("tanks", FFUtils.serializeTankArray(tanks));
        compound.setTag("inventory", inventory.serializeNBT());
        return super.writeToNBT(compound);
    }

    public long getPowerScaled(long i) {
        return (power * i) / getMaxPower();
    }

    List<int[]> list = new ArrayList<int[]>();
    HashSet<BlockPos> processed = new HashSet<BlockPos>();

/*
    public byte succ(int x, int y, int z) {
        list.clear();
        processed.clear(); // 清空processed集合
    
        int maxDepth = 3000; // 设置递归深度阈值
    
        succ1(x, y, z, 0, maxDepth);
        succ2(x, y, z, 0, maxDepth);
    
        if (!list.isEmpty()) {
            int i = world.rand.nextInt(list.size());
            int a = list.get(i)[0];
            int b = list.get(i)[1];
            int c = list.get(i)[2];
            BlockPos abc = new BlockPos(a, b, c);
    
            if (world.getBlockState(abc).getBlock() == ModBlocks.ore_oil) {
                world.setBlockState(abc, ModBlocks.ore_oil_empty.getDefaultState());
                return 1;
            } else if (world.getBlockState(abc).getBlock() == ModBlocks.ore_bedrock_oil) {
                return 2;
            }
        }
    
        return 0;
    }
*/

public byte succ(int x, int y, int z) {
    list.clear();
    processed.clear(); // 清空processed集合

    bfsCheckOil(x, y, z);

    if (!list.isEmpty()) {
        int i = world.rand.nextInt(list.size());
        int a = list.get(i)[0];
        int b = list.get(i)[1];
        int c = list.get(i)[2];
        BlockPos abc = new BlockPos(a, b, c);

        if (world.getBlockState(abc).getBlock() == ModBlocks.ore_oil) {
            world.setBlockState(abc, ModBlocks.ore_oil_empty.getDefaultState());
            return 1;
        } else if (world.getBlockState(abc).getBlock() == ModBlocks.ore_bedrock_oil) {
            return 2;
        }
    }

    return 0;
}
private void bfsCheckOil(int x, int y, int z) {
    Queue<BlockPos> queue = new LinkedList<>();
    queue.add(new BlockPos(x, y, z));

    while (!queue.isEmpty()) {
        BlockPos currentPos = queue.poll();
        if (!processed.contains(currentPos)) {
            processed.add(currentPos);

            // 检查当前方块是否是目标方块类型
            if (world.getBlockState(currentPos).getBlock() == ModBlocks.ore_oil || 
                world.getBlockState(currentPos).getBlock() == ModBlocks.ore_bedrock_oil) {
                list.add(new int[]{currentPos.getX(), currentPos.getY(), currentPos.getZ()});
            }

            // 如果是空油岩方块，则继续搜索相邻方块
            if (world.getBlockState(currentPos).getBlock() == ModBlocks.ore_oil_empty) {
                queue.add(new BlockPos(currentPos.getX() + 1, currentPos.getY(), currentPos.getZ()));
                queue.add(new BlockPos(currentPos.getX() - 1, currentPos.getY(), currentPos.getZ()));
                queue.add(new BlockPos(currentPos.getX(), currentPos.getY() + 1, currentPos.getZ()));
                queue.add(new BlockPos(currentPos.getX(), currentPos.getY() - 1, currentPos.getZ()));
                queue.add(new BlockPos(currentPos.getX(), currentPos.getY(), currentPos.getZ() + 1));
                queue.add(new BlockPos(currentPos.getX(), currentPos.getY(), currentPos.getZ() - 1));
            }
        }
    }
}
    public void succInit1(int x, int y, int z, int depth, int maxDepth) {
        succ1(x + 1, y, z, depth, maxDepth);
        succ1(x - 1, y, z, depth, maxDepth);
        succ1(x, y + 1, z, depth, maxDepth);
        succ1(x, y - 1, z, depth, maxDepth);
        succ1(x, y, z + 1, depth, maxDepth);
        succ1(x, y, z - 1, depth, maxDepth);
    }
    

    public void succInit2(int x, int y, int z, int depth, int maxDepth) {
        succ2(x + 1, y, z, depth, maxDepth);
        succ2(x - 1, y, z, depth, maxDepth);
        succ2(x, y + 1, z, depth, maxDepth);
        succ2(x, y - 1, z, depth, maxDepth);
        succ2(x, y, z + 1, depth, maxDepth);
        succ2(x, y, z - 1, depth, maxDepth);
    }

    public void succ1(int x, int y, int z, int depth, int maxDepth) {
        if (depth >= maxDepth) {
            return; // 达到递归深度阈值，终止递归
        }
    
        BlockPos newPos = new BlockPos(x, y, z);
        if (world.getBlockState(newPos).getBlock() == ModBlocks.ore_oil_empty && !processed.contains(newPos)) {
            processed.add(newPos);
            succInit1(x, y, z, depth + 1, maxDepth);
        }
    }

    public void succ2(int x, int y, int z, int depth, int maxDepth) {
        if (depth >= maxDepth) {
            return; // 达到递归深度阈值，终止递归
        }
    
        BlockPos newPos = new BlockPos(x, y, z);
        if (world.getBlockState(newPos).getBlock() == ModBlocks.ore_oil_empty && !processed.contains(newPos)) {
            processed.add(newPos);
            succInit2(x, y, z, depth + 1, maxDepth);
        } else if (world.getBlockState(newPos).getBlock() == ModBlocks.ore_oil || world.getBlockState(newPos).getBlock() == ModBlocks.ore_bedrock_oil) {
            list.add(new int[]{x, y, z});
        }
    }

    @Override
    public void setPower(long i) {
        power = i;

    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public long getMaxPower() {
        return 100000L;
    }

    @Override
    public IFluidTankProperties[] getTankProperties() {
        return new IFluidTankProperties[] { tanks[0].getTankProperties()[0], tanks[1].getTankProperties()[0] };
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) { return 0; }



    @Override
    public void recievePacket(NBTTagCompound[] tags) {
        if(tags.length != 2) {
            return;
        } else {
            tanks[0].readFromNBT(tags[0]);
            tanks[1].readFromNBT(tags[1]);
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
            return true;
        } else if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
            return true;
        } else {
            return super.hasCapability(capability, facing);
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        } else if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this);
        } else {
            return super.getCapability(capability, facing);
        }
    }
}