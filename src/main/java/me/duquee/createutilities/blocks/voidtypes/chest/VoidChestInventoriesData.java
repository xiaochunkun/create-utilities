package me.duquee.createutilities.blocks.voidtypes.chest;

import me.duquee.createutilities.blocks.voidtypes.VoidStorageData;
import me.duquee.createutilities.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.minecraft.nbt.CompoundTag;

import org.jetbrains.annotations.NotNull;

/**
 * 虚空箱子库存数据管理器
 * 
 * 这个类是虚空箱子存储系统的服务端数据管理核心，负责管理所有虚空箱子的库存数据。
 * 它继承自VoidStorageData基类，专门处理VoidChestInventory类型的存储实例。
 * 
 * 核心职责：
 * - 管理虚空箱子网络中所有库存实例的映射关系
 * - 提供库存数据的持久化存储和加载功能
 * - 确保相同网络键的虚空箱子共享同一个库存空间
 * 
 * 设计特点：
 * - 基于网络键的库存共享机制
 * - 自动创建和缓存库存实例
 * - 支持NBT序列化和反序列化
 * - 与Minecraft的SavedData系统集成
 * 
 * 使用场景：
 * 当玩家在不同位置放置具有相同虚空链接键的虚空箱子时，
 * 这个类确保它们访问的是同一个共享库存空间。
 * 
 * 数据持久化：
 * 所有库存数据会在世界保存时自动序列化到NBT，
 * 在世界加载时自动反序列化，确保库存内容不会丢失。
 * 
 * @author duquee
 * @since 1.0.0
 */
public class VoidChestInventoriesData extends VoidStorageData<VoidChestInventory> {

	/**
	 * 获取或创建虚空箱子库存实例
	 * 
	 * 这是虚空箱子系统的核心方法，实现了库存实例的懒加载机制。
	 * 当虚空箱子需要访问其库存时，会通过这个方法获取对应的库存实例：
	 * 
	 * 工作流程：
	 * 1. 检查缓存中是否已存在指定网络键的库存实例
	 * 2. 如果存在则直接返回，实现库存共享
	 * 3. 如果不存在则创建新的VoidChestInventory实例并缓存
	 * 
	 * 这种设计确保了：
	 * - 具有相同网络键的虚空箱子共享同一个库存
	 * - 避免重复创建库存实例，节省内存
	 * - 库存数据在所有相关箱子间实时同步
	 * 
	 * @param key 虚空网络键，用于标识特定的虚空存储网络
	 * @return 与网络键对应的虚空箱子库存实例，如果不存在则创建新实例
	 */
	public VoidChestInventory computeStorageIfAbsent(NetworkKey key) {
		return super.computeStorageIfAbsent(key, VoidChestInventory::new);
	}

	/**
	 * 将库存数据保存到NBT标签
	 * 
	 * 这个方法负责将所有虚空箱子的库存数据序列化到NBT格式，
	 * 以便在世界保存时持久化存储。只有非空的库存才会被保存，
	 * 这样可以减少存储文件的大小。
	 * 
	 * 保存逻辑：
	 * 1. 遍历所有缓存的库存实例
	 * 2. 检查每个库存是否为空（使用VoidChestInventory::isEmpty）
	 * 3. 将非空库存序列化为NBT并存储（使用VoidChestInventory::serializeNBT）
	 * 4. 使用网络键的字符串形式作为NBT中的键名
	 * 
	 * 这确保了服务器重启后，所有虚空箱子的物品都能正确恢复。
	 * 
	 * @param tag 用于存储库存数据的NBT复合标签
	 * @return 包含所有库存数据的NBT复合标签
	 */
	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
		return super.save(tag, VoidChestInventory::isEmpty, VoidChestInventory::serializeNBT);
	}

	/**
	 * 从NBT标签加载库存数据
	 * 
	 * 这是一个静态工厂方法，用于从保存的NBT数据中重建虚空箱子库存数据。
	 * 它会解析NBT中的所有库存数据，并创建对应的库存实例。
	 * 
	 * 加载过程：
	 * 1. 创建新的VoidChestInventoriesData实例
	 * 2. 遍历NBT中的所有键值对
	 * 3. 将每个键解析为NetworkKey
	 * 4. 为每个网络键创建VoidChestInventory实例
	 * 5. 从对应的NBT数据反序列化库存内容
	 * 6. 将库存实例添加到数据管理器中
	 * 
	 * 这个方法在世界加载时被调用，确保所有虚空箱子的库存数据
	 * 能够从存档中正确恢复。
	 * 
	 * @param tag 包含库存数据的NBT复合标签
	 * @return 加载了所有库存数据的VoidChestInventoriesData实例
	 */
	public static VoidChestInventoriesData load(CompoundTag tag) {
		return load(tag, VoidChestInventoriesData::new, VoidChestInventory::new, VoidChestInventory::deserializeNBT);
	}

}
