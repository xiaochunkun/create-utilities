package me.duquee.createutilities.blocks.voidtypes.tank;

import me.duquee.createutilities.blocks.voidtypes.VoidStorageData;
import me.duquee.createutilities.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.minecraft.nbt.CompoundTag;

import org.jetbrains.annotations.NotNull;

/**
 * 虚空储罐数据管理器
 * 
 * 这个类是虚空储罐存储系统的服务端数据管理核心，负责管理所有虚空储罐的流体数据。
 * 它继承自VoidStorageData基类，专门处理VoidTank类型的存储实例。
 * 
 * 核心职责：
 * - 管理虚空储罐网络中所有流体存储实例的映射关系
 * - 提供流体数据的持久化存储和加载功能
 * - 确保相同网络键的虚空储罐共享同一个流体存储空间
 * 
 * 设计特点：
 * - 基于网络键的流体共享机制
 * - 自动创建和缓存流体存储实例
 * - 支持NBT序列化和反序列化
 * - 与Minecraft的SavedData系统集成
 * 
 * 使用场景：
 * 当玩家在不同位置放置具有相同虚空链接键的虚空储罐时，
 * 这个类确保它们访问的是同一个共享流体存储空间。
 * 这使得玩家可以在远距离之间传输和共享流体资源。
 * 
 * 数据持久化：
 * 所有流体存储数据会在世界保存时自动序列化到NBT，
 * 在世界加载时自动反序列化，确保储罐中的流体不会丢失。
 * 
 * 流体管理特性：
 * - 支持大容量流体存储
 * - 实时流体数据同步
 * - 多储罐并联操作
 * 
 * @author duquee
 * @since 1.0.0
 */
public class VoidTanksData extends VoidStorageData<VoidTank> {

	/**
	 * 获取或创建虚空储罐实例
	 * 
	 * 这是虚空储罐系统的核心方法，实现了流体存储实例的懒加载机制。
	 * 当虚空储罐需要访问其流体存储时，会通过这个方法获取对应的存储实例：
	 * 
	 * 工作流程：
	 * 1. 检查缓存中是否已存在指定网络键的流体存储实例
	 * 2. 如果存在则直接返回，实现流体存储共享
	 * 3. 如果不存在则创建新的VoidTank实例并缓存
	 * 
	 * 这种设计确保了：
	 * - 具有相同网络键的虚空储罐共享同一个流体存储
	 * - 避免重复创建存储实例，节省内存
	 * - 流体数据在所有相关储罐间实时同步
	 * - 支持多点流体输入输出操作
	 * 
	 * @param key 虚空网络键，用于标识特定的虚空存储网络
	 * @return 与网络键对应的虚空储罐实例，如果不存在则创建新实例
	 */
	public VoidTank computeStorageIfAbsent(NetworkKey key) {
		return super.computeStorageIfAbsent(key, VoidTank::new);
	}

	/**
	 * 将流体存储数据保存到NBT标签
	 * 
	 * 这个方法负责将所有虚空储罐的流体数据序列化到NBT格式，
	 * 以便在世界保存时持久化存储。只有非空的储罐才会被保存，
	 * 这样可以减少存储文件的大小并提高性能。
	 * 
	 * 保存逻辑：
	 * 1. 遍历所有缓存的流体存储实例
	 * 2. 检查每个储罐是否为空（使用VoidTank::isEmpty）
	 * 3. 将非空储罐的流体数据序列化为NBT并存储
	 * 4. 使用网络键的字符串形式作为NBT中的键名
	 * 
	 * 流体序列化包含：
	 * - 流体类型（水、岩浆、模组流体等）
	 * - 流体数量
	 * - 流体NBT数据（如温度、属性等）
	 * 
	 * 这确保了服务器重启后，所有虚空储罐中的流体都能正确恢复。
	 * 
	 * @param tag 用于存储流体数据的NBT复合标签
	 * @return 包含所有流体存储数据的NBT复合标签
	 */
	@Override
	public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
		return super.save(tag, VoidTank::isEmpty, tank -> tank.writeToNBT(new CompoundTag()));
	}

	/**
	 * 从NBT标签加载流体存储数据
	 * 
	 * 这是一个静态工厂方法，用于从保存的NBT数据中重建虚空储罐数据。
	 * 它会解析NBT中的所有流体存储数据，并创建对应的储罐实例。
	 * 
	 * 加载过程：
	 * 1. 创建新的VoidTanksData实例
	 * 2. 遍历NBT中的所有键值对
	 * 3. 将每个键解析为NetworkKey
	 * 4. 为每个网络键创建VoidTank实例
	 * 5. 从对应的NBT数据反序列化流体内容
	 * 6. 将储罐实例添加到数据管理器中
	 * 
	 * 流体反序列化恢复：
	 * - 流体类型的正确识别和重建
	 * - 精确的流体数量恢复
	 * - 流体属性和NBT数据的完整恢复
	 * 
	 * 这个方法在世界加载时被调用，确保所有虚空储罐的流体数据
	 * 能够从存档中正确恢复，包括复杂的模组流体。
	 * 
	 * @param tag 包含流体存储数据的NBT复合标签
	 * @return 加载了所有流体数据的VoidTanksData实例
	 */
	public static VoidTanksData load(CompoundTag tag) {
		return load(tag, VoidTanksData::new, VoidTank::new, VoidTank::readFromNBT);
	}

}
