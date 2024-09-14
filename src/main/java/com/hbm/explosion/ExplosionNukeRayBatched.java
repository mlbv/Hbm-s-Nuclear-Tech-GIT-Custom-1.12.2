package com.hbm.explosion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
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

	public HashMap<ChunkPos, CompressedBlockSet> perChunk = new HashMap<ChunkPos, CompressedBlockSet>();
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


	public void addPos(int x, int y, int z) {
		ChunkPos chunk = new ChunkPos(x >> 4, z >> 4);
		CompressedBlockSet hitPositions = (CompressedBlockSet) perChunk.get(chunk);
	
		if (hitPositions == null) {
			hitPositions = new CompressedBlockSet();
			perChunk.put(chunk, hitPositions);
		}
	
		int index = ((255 - y) << 8) + ((x & 15) << 4) + (z & 15);
		hitPositions.add(index);
	}

	int age = 0;
	public void collectTip(int time) {
		if (!CompatibilityConfig.isWarDim(world)) {
			isAusf3Complete = true;
			return;
		}
	
		long start = System.currentTimeMillis();
		long raysProcessed = 0;
		try {
			// 如果异步计算还未完成，返回等待
			if (!precomputedGspCoordinates.isDone()) {
				return;
			}
	
			if (gspCoordinates == null) {
				gspCoordinates = precomputedGspCoordinates.get();
			}
	
			while (this.gspNumMax >= this.gspNum) {
				// 获取预生成的Gsp坐标
				Vec3 vec = gspCoordinates.get(gspNum - 1);
	
				int x0 = posX;
				int y0 = posY;
				int z0 = posZ;
				int x1 = posX + (int) (vec.xCoord * radius);
				int y1 = posY + (int) (vec.yCoord * radius);
				int z1 = posZ + (int) (vec.zCoord * radius);
	
				// 使用 Bresenham 的线算法来处理射线追踪
				List<BlockPos> rayBlocks = getLineBlocks(x0, y0, z0, x1, y1, z1);
				float rayStrength = strength * 0.3F;
	
				for (BlockPos pos : rayBlocks) {
					int iX = pos.getX();
					int iY = pos.getY();
					int iZ = pos.getZ();
	
					if (iY < minY || iY > maxY) {
						isContained = false;
						break;
					}
	
					IBlockState blockState = world.getBlockState(pos);
					Block block = blockState.getBlock();
					if (block.getExplosionResistance(null) >= 2_000_000) {
						break;
					}
	
					rayStrength -= Math.pow(getNukeResistance(blockState, block) + 1, 3 * pos.distanceSq(posX, posY, posZ) / (radius * radius)) - 1;
	
					if (rayStrength > 0) {
						if (block != Blocks.AIR) {
							addPos(iX, iY, iZ); // 将方块位置保存到 `CompressedBlockSet`
						}
						if (pos.distanceSq(posX, posY, posZ) >= radius * radius) {
							isContained = false;
						}
					} else {
						break;
					}
				}
	
				raysProcessed++;
				if (raysProcessed % rayCheckInterval == 0 && System.currentTimeMillis() > start + time) {
					return; // 超过指定时间，等待下一次调用继续处理
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

	CompressedBlockSet hitArray;
	ChunkPos chunk;
	boolean needsNewHitArray = true;
	Iterator<Integer> indexIterator;
	
	public void processChunkBlocks(long start, int time) {
		if (!CompatibilityConfig.isWarDim(world)) {
			this.perChunk.clear();
		}
		if (this.perChunk.isEmpty()) return;
		if (needsNewHitArray) {
			chunk = orderedChunks.get(0);
			hitArray = perChunk.get(chunk);
			indexIterator = hitArray.getIndices().iterator();
			needsNewHitArray = false;
		}
	
		int chunkX = chunk.getXStart();
		int chunkZ = chunk.getZStart();
	
		MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int blocksRemoved = 0;
		while (indexIterator.hasNext()) {
			int index = indexIterator.next();
			pos.setPos(((index >> 4) % 16) + chunkX, 255 - (index >> 8), (index % 16) + chunkZ);
			world.setBlockToAir(pos);
			blocksRemoved++;
			if (blocksRemoved % 256 == 0 && System.currentTimeMillis() > start + time) {
				break;
			}
		}
	
		if (!indexIterator.hasNext()) {
			perChunk.remove(chunk);
			orderedChunks.remove(0);
			needsNewHitArray = true;
		}
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
			while (nbt.hasKey("chunks" + i)) {
				NBTTagCompound c = (NBTTagCompound) nbt.getTag("chunks" + i);
				ChunkPos chunkPos = new ChunkPos(c.getInteger("cX"), c.getInteger("cZ"));
				CompressedBlockSet blockSet = new CompressedBlockSet();
				
				long[] longArray = getLongArray((NBTTagLongArray) c.getTag("cB"));
				for (long value : longArray) {
					// 将long数组转换回相应的blockSet (假设long存储索引)
					int index = (int) value;  // 这里需要根据你具体的long到index的转换逻辑
					blockSet.add(index);
				}
				perChunk.put(chunkPos, blockSet);
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
			for (Entry<ChunkPos, CompressedBlockSet> e : perChunk.entrySet()) {
				NBTTagCompound c = new NBTTagCompound();
				c.setInteger("cX", e.getKey().x);
				c.setInteger("cZ", e.getKey().z);
				
				List<Integer> indices = new ArrayList<>();
				for (int index : e.getValue().getIndices()) {
					indices.add(index);  // 将压缩的blockSet转换为index列表
				}
				long[] longArray = new long[indices.size()];
				for (int j = 0; j < indices.size(); j++) {
					longArray[j] = indices.get(j);  // 这里转换为long值存储，具体实现取决于你对index的逻辑
				}
				c.setTag("cB", new NBTTagLongArray(longArray));
				nbt.setTag("chunks" + i, c.copy());
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
	public class CompressedBlockSet {
		private TreeMap<Integer, Integer> ranges = new TreeMap<>();

		public void add(int index) {
			Integer lowerKey = ranges.floorKey(index);
			if (lowerKey != null && ranges.get(lowerKey) >= index - 1) {
				ranges.put(lowerKey, Math.max(ranges.get(lowerKey), index));
			} else {
				ranges.put(index, index);
			}
		}

		public Iterable<Integer> getIndices() {
			List<Integer> indices = new ArrayList<>();
			for (Entry<Integer, Integer> entry : ranges.entrySet()) {
				for (int i = entry.getKey(); i <= entry.getValue(); i++) {
					indices.add(i);
				}
			}
			return indices;
		}
	}

	private List<BlockPos> getLineBlocks(int x0, int y0, int z0, int x1, int y1, int z1) {
		List<BlockPos> result = new ArrayList<>();
	
		int dx = Math.abs(x1 - x0);
		int dy = Math.abs(y1 - y0);
		int dz = Math.abs(z1 - z0);
	
		int xs = x0 < x1 ? 1 : -1;
		int ys = y0 < y1 ? 1 : -1;
		int zs = z0 < z1 ? 1 : -1;
	
		if (dx >= dy && dx >= dz) {
			int p1 = 2 * dy - dx;
			int p2 = 2 * dz - dx;
	
			while (x0 != x1) {
				x0 += xs;
				if (p1 >= 0) {
					y0 += ys;
					p1 -= 2 * dx;
				}
				if (p2 >= 0) {
					z0 += zs;
					p2 -= 2 * dx;
				}
				p1 += 2 * dy;
				p2 += 2 * dz;
	
				result.add(new BlockPos(x0, y0, z0));
			}
		} else if (dy >= dx && dy >= dz) {
			int p1 = 2 * dx - dy;
			int p2 = 2 * dz - dy;
	
			while (y0 != y1) {
				y0 += ys;
				if (p1 >= 0) {
					x0 += xs;
					p1 -= 2 * dy;
				}
				if (p2 >= 0) {
					z0 += zs;
					p2 -= 2 * dy;
				}
				p1 += 2 * dx;
				p2 += 2 * dz;
	
				result.add(new BlockPos(x0, y0, z0));
			}
		} else {
			int p1 = 2 * dy - dz;
			int p2 = 2 * dx - dz;
	
			while (z0 != z1) {
				z0 += zs;
				if (p1 >= 0) {
					y0 += ys;
					p1 -= 2 * dz;
				}
				if (p2 >= 0) {
					x0 += xs;
					p2 -= 2 * dz;
				}
				p1 += 2 * dy;
				p2 += 2 * dx;
	
				result.add(new BlockPos(x0, y0, z0));
			}
		}
	
		return result;
	}
}
