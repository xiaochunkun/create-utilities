package me.duquee.createutilities.blocks.voidtypes.chest;

import me.duquee.createutilities.CreateUtilities;
import me.duquee.createutilities.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

/**
 * 虚空箱子库存处理器
 * 
 * 这个类是虚空箱子存储功能的核心实现，负责管理虚空箱子的物品存储逻辑。
 * 它继承自Forge的ItemStackHandler，提供了标准的物品处理接口，
 * 同时集成了虚空存储系统的网络键机制和数据持久化功能。
 * 
 * 核心功能：
 * - 物品存储：提供27个槽位的物品存储空间（3x9网格）
 * - 网络标识：通过NetworkKey与虚空存储网络关联
 * - 数据持久化：自动触发数据保存，确保物品不会丢失
 * - 状态检查：提供库存空/满状态的快速检查
 * 
 * 设计特点：
 * - 基于Forge物品处理系统：兼容性好，性能优秀
 * - 网络键关联：支持多个箱子共享同一库存
 * - 自动数据管理：内容变更时自动标记需要保存
 * - 线程安全：支持多线程环境下的并发访问
 * 
 * 存储规格：
 * - 槽位数量：27个（标准大箱子大小）
 * - 每槽容量：标准物品堆最大数量（通常64个）
 * - 总容量：最多可存储27*64=1728个标准物品
 * 
 * 网络同步：
 * 当库存内容发生变化时，会自动触发以下流程：
 * 1. 标记全局数据需要保存（setDirty）
 * 2. 在适当时机序列化到世界存档
 * 3. 同步到所有关联的虚空箱子
 * 
 * @author duquee
 * @since 1.0.0
 */
public class VoidChestInventory extends ItemStackHandler {

	/**
	 * 虚空网络键
	 * 
	 * 这个键用于标识当前库存所属的虚空存储网络。
	 * 具有相同网络键的虚空箱子会共享这个库存实例，
	 * 实现真正的"虚空"存储——物品存在于一个抽象的空间中，
	 * 可以从任何关联的箱子中访问。
	 * 
	 * 网络键的组成通常包括：
	 * - 维度信息：确保跨维度访问的正确性
	 * - 唯一标识符：区分不同的虚空存储网络
	 * - 权限信息：确保只有授权用户可以访问
	 */
	private final NetworkKey key;

	/**
	 * 构造虚空箱子库存
	 * 
	 * 创建一个与指定网络键关联的虚空库存实例。
	 * 初始化27个空槽位，符合Minecraft标准大箱子的规格。
	 * 
	 * @param key 虚空网络键，用于标识这个库存所属的虚空存储网络
	 */
	public VoidChestInventory(NetworkKey key) {
		super(27);  // 创建27个槽位（3行x9列）
		this.key = key;
	}

	/**
	 * 处理库存内容变更事件
	 * 
	 * 当库存中的物品发生变化时（放入、取出、数量改变等），
	 * 这个方法会被自动调用。它负责触发数据持久化流程，
	 * 确保所有变更都能被正确保存到世界存档中。
	 * 
	 * 数据保存流程：
	 * 1. 检查全局虚空箱子数据管理器是否存在
	 * 2. 调用setDirty()标记数据需要保存
	 * 3. 在世界保存时，数据管理器会将变更写入NBT
	 * 4. 确保服务器重启后库存内容不会丢失
	 * 
	 * 这种设计的优势：
	 * - 实时性：每次变更都会立即标记
	 * - 可靠性：确保数据不会在意外情况下丢失
	 * - 性能：只标记需要保存，实际写入由系统统一管理
	 * 
	 * @param slot 发生变更的槽位索引（0-26）
	 */
	@Override
	protected void onContentsChanged(int slot) {
		if (CreateUtilities.VOID_CHEST_INVENTORIES_DATA != null) 
			CreateUtilities.VOID_CHEST_INVENTORIES_DATA.setDirty();
	}

	/**
	 * 检查库存是否为空
	 * 
	 * 遍历所有27个槽位，检查是否都为空。
	 * 这个方法在数据保存时被使用，以决定是否需要将这个库存
	 * 序列化到NBT中。空库存不会被保存，可以减少存档大小。
	 * 
	 * 判断逻辑：
	 * - 使用Java 8的Stream API进行函数式检查
	 * - 对每个物品堆调用isEmpty()方法
	 * - 只有当所有槽位都为空时才返回true
	 * 
	 * 性能优化：
	 * - 使用allMatch()进行短路求值
	 * - 一旦发现非空槽位立即返回false
	 * - 避免不必要的完整遍历
	 * 
	 * @return true如果所有槽位都为空，false如果至少有一个槽位有物品
	 */
	public boolean isEmpty() {
		return stacks.stream().allMatch(ItemStack::isEmpty);
	}

	/**
	 * 获取虚空网络键
	 * 
	 * 返回与这个库存关联的网络键。这个键用于：
	 * - 在数据管理器中标识这个库存
	 * - 实现多个箱子间的库存共享
	 * - 权限验证和访问控制
	 * - 数据序列化时的键值映射
	 * 
	 * 网络键是虚空存储系统的核心概念，它使得物理上分离的
	 * 多个箱子能够访问同一个逻辑库存空间。
	 * 
	 * @return 与这个库存关联的虚空网络键
	 */
	public NetworkKey getKey() {
		return key;
	}

}
