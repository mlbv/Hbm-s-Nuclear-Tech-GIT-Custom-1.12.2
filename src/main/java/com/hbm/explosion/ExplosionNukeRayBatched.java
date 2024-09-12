package com.hbm.explosion;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.hbm.config.BombConfig;
import com.hbm.config.CompatibilityConfig;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.util.Tuple.Pair;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class ExplosionNukeRayBatched {

	public HashMap<ChunkPos, BitSet> perChunk = new HashMap<ChunkPos, BitSet>();
	public List<ChunkPos> orderedChunks = new ArrayList();
	private CoordComparator comparator = new CoordComparator();
	public boolean isContained = true;
	int posX;
	int posY;
	int posZ;
	World world;

	int strength;
	int radius;

	int gspNumMax;
	int gspNum;
	double gspX;
	double gspY;

	private int currentChunkX = Integer.MIN_VALUE; // 记录当前处理到的区块X
    private int currentChunkZ = Integer.MIN_VALUE; // 记录当前处理到的区块Z
    private int currentY = minY; // 记录区块中的高度
    private int currentBx = 0; // 记录区块内的X坐标
    private int currentBz = 0; // 记录区块内的Z坐标
    public boolean isVaporizationComplete = false; // 蒸发过程是否完成

	private static final int maxY = 255;
	private static final int minY = 0;

	private Future<List<Vec3>> precomputedGspCoordinates;
    private List<Vec3> gspCoordinates; // 存储异步生成的Gsp坐标
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

	public boolean isAusf3Complete = false;
	public int rayCheckInterval = 100;

	public ExplosionNukeRayBatched(World world, int x, int y, int z, int strength, int radius) {
		this.world = world;
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.strength = strength;
		this.radius = radius;

		// Total number of points
		this.gspNumMax = (int)(2.5 * Math.PI * Math.pow(this.strength, 2));
		this.gspNum = 1;

		// The beginning of the generalized spiral points
		this.gspX = Math.PI;
		this.gspY = 0.0;
		this.rayCheckInterval = 10000/radius;
		precomputedGspCoordinates = precomputeGspCoordinatesAsync();
	}

	private void generateGspUp(){
		if (this.gspNum < this.gspNumMax) {
			int k = this.gspNum + 1;
			double hk = -1.0 + 2.0 * (k - 1.0) / (this.gspNumMax - 1.0);
			this.gspX = Math.acos(hk);

			double prev_lon = this.gspY;
			double lon = prev_lon + 3.6 / Math.sqrt(this.gspNumMax) / Math.sqrt(1.0 - hk * hk);
			this.gspY = lon % (Math.PI * 2);
		} else {
			this.gspX = 0.0;
			this.gspY = 0.0;
		}
		this.gspNum++;
	}

	// Get Cartesian coordinates for spherical coordinates
	// 90 X-Axis rotation for more efficient chunk scanning
	private Vec3 getSpherical2cartesian(){
		double dx = Math.sin(this.gspX) * Math.cos(this.gspY);
		double dy = Math.sin(this.gspX) * Math.sin(this.gspY);
		double dz = Math.cos(this.gspX);
		return Vec3.createVectorHelper(dx, dy, dz);
	}

	public void addPos(int x, int y, int z){
		chunk = new ChunkPos(x >> 4, z >> 4);
		BitSet hitPositions = perChunk.get(chunk);
				
		if(hitPositions == null) {
			hitPositions = new BitSet(65536);
			perChunk.put(chunk, hitPositions); //we re-use the same pos instead of using individualized per-chunk ones to save on RAM
		}
		hitPositions.set(((255-y) << 8) + ((x - chunk.getXStart()) << 4) + (z - chunk.getZStart()));
	}

	int age = 0;
	public void collectTip(int time) {
        if (!CompatibilityConfig.isWarDim(world)) {
            isAusf3Complete = true;
            return;
        }
        MutableBlockPos pos = new BlockPos.MutableBlockPos();
        long raysProcessed = 0;
        long start = System.currentTimeMillis();

        IBlockState blockState;
        Block b;
        int iX, iY, iZ, radius;
        float rayStrength;
        Vec3 vec;
        age++;
        if (age == 120) {
            System.out.println("NTM C " + raysProcessed + " " + Math.round(10000D * 100D * gspNum / (double) gspNumMax) / 10000D + "% " + gspNum + "/" + gspNumMax);
            age = 0;
        }
        
        try {
            // 如果还没完成，等待异步计算完成
            if (!precomputedGspCoordinates.isDone()) {
                return;
            }
            
            // 获取预先计算好的Gsp坐标
            if (gspCoordinates == null) {
                gspCoordinates = precomputedGspCoordinates.get();
            }

            while (this.gspNumMax >= this.gspNum) {
                // 使用预生成的Gsp坐标
                vec = gspCoordinates.get(gspNum - 1);
                radius = (int) Math.ceil(this.radius);
                rayStrength = strength * 0.3F;

                // Finding the end of the ray
                for (int r = 0; r < radius + 1; r++) {
                    iY = (int) Math.floor(posY + (vec.yCoord * r));

                    if (iY < minY || iY > maxY) {
                        isContained = false;
                        break;
                    }

                    iX = (int) Math.floor(posX + (vec.xCoord * r));
                    iZ = (int) Math.floor(posZ + (vec.zCoord * r));

                    pos.setPos(iX, iY, iZ);
                    blockState = world.getBlockState(pos);
                    b = blockState.getBlock();
                    if (b.getExplosionResistance(null) >= 2_000_000)
                        break;

                    rayStrength -= Math.pow(getNukeResistance(blockState, b) + 1, 3 * ((double) r) / ((double) radius)) - 1;

                    // Save block positions in to-destroy-boolean[] until rayStrength is 0
                    if (rayStrength > 0) {
                        if (b != Blocks.AIR) {
                            addPos(iX, iY, iZ);
                        }
                        if (r >= radius) {
                            isContained = false;
                        }
                    } else {
                        break;
                    }
                }

                raysProcessed++;
                if (raysProcessed % rayCheckInterval == 0 && System.currentTimeMillis() + 1 > start + time) {
                    return;
                }
                this.gspNum++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        orderedChunks.addAll(perChunk.keySet());
        orderedChunks.sort(comparator);

        isAusf3Complete = true;
    }
	
	public static float getNukeResistance(IBlockState blockState, Block b) {
		if(blockState.getMaterial().isLiquid()){
			return 0.1F;
		} else {
			if(b == Blocks.SANDSTONE) return 4F;
			if(b == Blocks.OBSIDIAN) return 18F;
			return b.getExplosionResistance(null);
		}
	}
	
	/** little comparator for roughly sorting chunks by distance to the center */
	public class CoordComparator implements Comparator<ChunkPos> {

		@Override
		public int compare(ChunkPos o1, ChunkPos o2) {

			int chunkX = ExplosionNukeRayBatched.this.posX >> 4;
			int chunkZ = ExplosionNukeRayBatched.this.posZ >> 4;

			int diff1 = Math.abs((chunkX - (int) (o1.getXStart() >> 4))) + Math.abs((chunkZ - (int) (o1.getZStart() >> 4)));
			int diff2 = Math.abs((chunkX - (int) (o2.getXStart() >> 4))) + Math.abs((chunkZ - (int) (o2.getZStart() >> 4)));
			
			return diff1 > diff2 ? 1 : diff1 < diff2 ? -1 : 0;
		}
	}

	public void processChunk(int time){
		long start = System.currentTimeMillis();
		while(System.currentTimeMillis() < start + time){
			processChunkBlocks(start, time);
		}
	}

	BitSet hitArray;
	ChunkPos chunk;
	boolean needsNewHitArray = true;
	int index = 0;

	public void processChunkBlocks(long start, int time){
		if(!CompatibilityConfig.isWarDim(world)){
			this.perChunk.clear();
		}
		if(this.perChunk.isEmpty()) return;
		if(needsNewHitArray){
			chunk = orderedChunks.get(0);
			hitArray = perChunk.get(chunk);
			index = hitArray.nextSetBit(0);
			needsNewHitArray = false;
		}
		
		int chunkX = chunk.getXStart();
		int chunkZ = chunk.getZStart();
		
		MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int blocksRemoved = 0;
		while(index > -1) {
			pos.setPos(((index >> 4) % 16) + chunkX, 255 - (index >> 8), (index % 16) + chunkZ);
			world.setBlockToAir(pos);
			index = hitArray.nextSetBit(index+1);
			blocksRemoved++;
			if(blocksRemoved % 256 == 0 && System.currentTimeMillis()+1 > start + time){
				break;
			}
		}

		if(index < 0){
			perChunk.remove(chunk);
			orderedChunks.remove(0);
			needsNewHitArray = true;
		}
	}
	
    public void vaporizeFluids(int radius, int time) {
        if (isVaporizationComplete) {
            return; // 如果已经完成，直接返回
        }

        long start = System.currentTimeMillis();
        MutableBlockPos pos = new BlockPos.MutableBlockPos();

        // 如果是第一次执行，初始化 currentChunkX 和 currentChunkZ
        if (currentChunkX == Integer.MIN_VALUE && currentChunkZ == Integer.MIN_VALUE) {
            currentChunkX = (posX >> 4) - radius;
            currentChunkZ = (posZ >> 4) - radius;
        }

        // 遍历指定半径内的区块
        for (int x = currentChunkX; x <= (posX >> 4) + radius; x++) {
            for (int z = currentChunkZ; z <= (posZ >> 4) + radius; z++) {
                ChunkPos chunkPos = new ChunkPos(x, z);

                // 强制加载区块，确保区块可被处理
                if (!world.isChunkGeneratedAt(chunkPos.x, chunkPos.z)) {
                    continue; // 如果区块未生成，跳过
                }
                Chunk chunk = world.getChunk(chunkPos.x, chunkPos.z);

                // 遍历区块内的所有方块，从上次中断的位置开始
                for (int y = currentY; y <= maxY; y++) {
                    for (int bx = currentBx; bx < 16; bx++) {
                        for (int bz = currentBz; bz < 16; bz++) {
                            pos.setPos(chunkPos.getXStart() + bx, y, chunkPos.getZStart() + bz);
                            IBlockState blockState = chunk.getBlockState(pos);
                            Block block = blockState.getBlock();

                            // 判断是否为液体
                            if (block.getMaterial(blockState).isLiquid()) {
                                // 蒸发液体，将其替换为空气
                                world.setBlockToAir(pos);
                                // 这里可以加入更多蒸发效果，比如粒子效果或声音效果
                            }

                            // 时间限制检查，若超时则中断
                            if (System.currentTimeMillis() > start + time) {
                                // 保存当前的处理位置
                                currentChunkX = x;
                                currentChunkZ = z;
                                currentY = y;
                                currentBx = bx;
                                currentBz = bz + 1; // 下次从下一个bz开始
                                return; // 中断并等待下次继续
                            }
                        }
                        currentBz = 0; // 重置bz，下一次从0开始
                    }
                    currentBx = 0; // 重置bx，下一次从0开始
                }
                currentY = minY; // 重置Y，下一次从最小高度开始
            }
            currentChunkZ = (posZ >> 4) - radius; // 重置Z，下一次从最小区块Z开始
        }

        // 完成所有区块处理，标记为完成
        isVaporizationComplete = true;
    }
	
	public void readEntityFromNBT(NBTTagCompound nbt) {
		radius = nbt.getInteger("radius");
		strength = nbt.getInteger("strength");
		posX = nbt.getInteger("posX");
		posY = nbt.getInteger("posY");
		posZ = nbt.getInteger("posZ");
		gspNumMax = (int)(2.5 * Math.PI * Math.pow(strength, 2));
		rayCheckInterval = 10000/radius;

		if(nbt.hasKey("gspNum")){
			gspNum = nbt.getInteger("gspNum");
			isAusf3Complete = nbt.getBoolean("f3");
			isContained = nbt.getBoolean("isContained");

			int i = 0;
			while(nbt.hasKey("chunks"+i)){
				NBTTagCompound c = (NBTTagCompound)nbt.getTag("chunks"+i);

				perChunk.put(new ChunkPos(c.getInteger("cX"), c.getInteger("cZ")), BitSet.valueOf(getLongArray((NBTTagLongArray)c.getTag("cB"))));
				i++;
			}
			if(isAusf3Complete){
				orderedChunks.addAll(perChunk.keySet());
				orderedChunks.sort(comparator);
			}
		}
	}

	public void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("radius", radius);
		nbt.setInteger("strength", strength);
		nbt.setInteger("posX", posX);
		nbt.setInteger("posY", posY);
		nbt.setInteger("posZ", posZ);
		
		if(BombConfig.enableNukeNBTSaving){
			nbt.setInteger("gspNum", gspNum);
			nbt.setBoolean("f3", isAusf3Complete);
			nbt.setBoolean("isContained", isContained);
		
			int i = 0;
			for(Entry<ChunkPos, BitSet> e : perChunk.entrySet()){
				NBTTagCompound c = new NBTTagCompound();
				c.setInteger("cX", e.getKey().x);
				c.setInteger("cZ", e.getKey().z);
				c.setTag("cB", new NBTTagLongArray(e.getValue().toLongArray()));
				nbt.setTag("chunks"+i, c.copy());
				i++;
			}
		}
	}

	// Who tf forgot to add a way to retrieve the long array from NBTTagLongArray??
	public static long[] getLongArray(NBTTagLongArray nbt) {
		return ObfuscationReflectionHelper.getPrivateValue(NBTTagLongArray.class, nbt, 0);
	}

	private Future<List<Vec3>> precomputeGspCoordinatesAsync() {
		return executorService.submit(() -> {
			List<Vec3> coordinates = new ArrayList<>(gspNumMax);
			double prev_gspY = 0;
			for (int gspNum = 1; gspNum <= gspNumMax; gspNum++) {
				Pair<Vec3, Double> result = generateGspUpLocal(gspNum, gspNumMax, prev_gspY);
				coordinates.add(result.getKey());
				prev_gspY = result.getValue();
			}
			return coordinates;
		});
	}

	public void shutdownExecutor() {
        executorService.shutdownNow();
    }

	private Pair<Vec3, Double> generateGspUpLocal(int gspNum, int gspNumMax, double prev_gspY) {
		double gspX, gspY;
	
		if (gspNum < gspNumMax) {
			double hk = -1.0 + 2.0 * gspNum / (gspNumMax - 1.0);
			gspX = Math.acos(hk);
			hk = Math.max(-1.0, Math.min(1.0, hk));
	
			double prev_lon = prev_gspY;
			double lon = prev_lon + 3.6 / Math.sqrt(gspNumMax) / Math.sqrt(1.0 - hk * hk);
			gspY = Math.abs(hk) == 1.0 ? 0.0 : lon % (Math.PI * 2);
		} else {
			gspX = 0.0;
			gspY = 0.0;
		}
	
		double dx = Math.sin(gspX) * Math.cos(gspY);
		double dy = Math.sin(gspX) * Math.sin(gspY);
		double dz = Math.cos(gspX);
		if (Double.isNaN(dx) || Double.isNaN(dy) || Double.isNaN(dz)) {
			System.out.println("NaN detected in coordinates: gspNum = " + gspNum + ", gspX = " + gspX + ", gspY = " + gspY);
		}
		return Pair.of(Vec3.createVectorHelper(dx, dy, dz), gspY);
	}
	
}
