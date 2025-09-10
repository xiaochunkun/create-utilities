package me.duquee.createutilities.blocks.voidtypes.battery;

import org.jetbrains.annotations.NotNull;

import me.duquee.createutilities.blocks.voidtypes.VoidStorageData;
import me.duquee.createutilities.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.minecraft.nbt.CompoundTag;

/**
 * 虚空电池数据管理器
 * 
 * 这个类是虚空电池存储系统的服务端数据管理核心，负责管理所有虚空电池的能量数据。
 * 它继承自VoidStorageData基类，专门处理VoidBattery类型的存储实例。
 * 
 * 核心职责：
 * - 管理虚空电池网络中所有能量存储实例的映射关系
 * - 提供能量数据的持久化存储和加载功能
 * - 确保相同网络键的虚空电池共享同一个能量存储空间
 * 
 * 设计特点：
 * - 基于网络键的能量共享机制
 * - 自动创建和缓存能量存储实例
 * - 支持NBT序列化和反序列化
 * - 与Minecraft的SavedData系统集成
 * 
 * 使用场景：
 * 当玩家在不同位置放置具有相同虚空链接键的虚空电池时，
 * 这个类确保它们访问的是同一个共享能量存储空间。
 * 这使得玩家可以建立分布式的能量存储和传输网络。
 * 
 * 数据持久化：
 * 所有能量存储数据会在世界保存时自动序列化到NBT，
 * 在世界加载时自动反序列化，确保电池中的能量不会丢失。
 * 
 * 能量管理特性：
 * - 支持大容量能量存储
 * - 实时能量数据同步
 * - 多电池并联操作
 * - 能量输入输出平衡
 * 
 * @author duquee
 * @since 1.0.0
 */
public class VoidBatteryData extends VoidStorageData<VoidBattery> {

	/**
	 * 获取或创建虚空电池实例
	 * 
	 * 这是虚空电池系统的核心方法，实现了能量存储实例的懒加载机制。
	 * 当虚空电池需要访问其能量存储时，会通过这个方法获取对应的存储实例：
	 * 
	 * 工作流程：
	 * 1. 检查缓存中是否已存在指定网络键的能量存储实例
	 * 2. 如果存在则直接返回，实现能量存储共享
	 * 3. 如果不存在则创建新的VoidBattery实例并缓存
	 * 
	 * 这种设计确保了：
	 * - 具有相同网络键的虚空电池共享同一个能量存储
	 * - 避免重复创建存储实例，节省内存
	 * - 能量数据在所有相关电池间实时同步
	 * - 支持多点能量输入输出操作
	 * - 实现能量的负载均衡和分布式管理
	 * 
	 * @param key 虚空网络键，用于标识特定的虚空存储网络
	 * @return 与网络键对应的虚空电池实例，如果不存在则创建新实例
	 */
	public VoidBattery computeStorageIfAbsent(NetworkKey key) {
		return super.computeStorageIfAbsent(key, VoidBattery::new);
	}

	/**
	 * 将能量存储数据保存到NBT标签
	 * 
	 * 这个方法负责将所有虚空电池的能量数据序列化到NBT格式，
	 * 以便在世界保存时持久化存储。只有非空的电池才会被保存，
	 * 这样可以减少存储文件的大小并提高性能。
	 * 
	 * 保存逻辑：
	 * 1. 遍历所有缓存的能量存储实例
	 * 2. 检查每个电池是否为空（使用VoidBattery::isEmpty）
	 * 3. 将非空电池的能量数据序列化为NBT并存储（使用VoidBattery::serializeNBT）
	 * 4. 使用网络键的字符串形式作为NBT中的键名
	 * 
	 * 能量序列化包含：
	 * - 当前存储的能量数量（以FE为单位）
	 * - 最大能量容量配置
	 * - 能量传输速率设置
	 * - 电池状态信息
	 * 
	 * 这确保了服务器重启后，所有虚空电池中的能量都能正确恢复。
	 * 
	 * @param tag 用于存储能量数据的NBT复合标签
	 * @return 包含所有能量存储数据的NBT复合标签
	 */
	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
		return super.save(tag, VoidBattery::isEmpty, VoidBattery::serializeNBT);
	}

	/**
	 * 从NBT标签加载能量存储数据
	 * 
	 * 这是一个静态工厂方法，用于从保存的NBT数据中重建虚空电池数据。
	 * 它会解析NBT中的所有能量存储数据，并创建对应的电池实例。
	 * 
	 * 加载过程：
	 * 1. 创建新的VoidBatteryData实例
	 * 2. 遍历NBT中的所有键值对
	 * 3. 将每个键解析为NetworkKey
	 * 4. 为每个网络键创建VoidBattery实例
	 * 5. 从对应的NBT数据反序列化能量内容（使用VoidBattery::deserializeNBT）
	 * 6. 将电池实例添加到数据管理器中
	 * 
	 * 能量反序列化恢复：
	 * - 精确的能量数量恢复
	 * - 电池容量和传输速率设置的重建
	 * - 电池状态和配置的完整恢复
	 * - 与能量系统的正确重新连接
	 * 
	 * 这个方法在世界加载时被调用，确保所有虚空电池的能量数据
	 * 能够从存档中正确恢复，维持能量网络的完整性。
	 * 
	 * @param tag 包含能量存储数据的NBT复合标签
	 * @return 加载了所有能量数据的VoidBatteryData实例
	 */
	public static VoidBatteryData load(CompoundTag tag) {
		return load(tag, VoidBatteryData::new, VoidBattery::new, VoidBattery::deserializeNBT);
	}

}
