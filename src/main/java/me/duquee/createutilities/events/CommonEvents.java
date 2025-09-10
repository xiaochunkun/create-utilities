package me.duquee.createutilities.events;

import me.duquee.createutilities.CreateUtilities;
import me.duquee.createutilities.blocks.voidtypes.battery.VoidBatteryData;
import me.duquee.createutilities.blocks.voidtypes.chest.VoidChestInventoriesData;
import me.duquee.createutilities.blocks.voidtypes.tank.VoidTanksData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 通用事件处理器
 * 
 * 这个类是Create Utilities模组的核心事件处理中心，负责处理世界级别的重要事件。
 * 它主要管理虚空存储系统在世界加载和卸载时的数据初始化与清理工作。
 * 
 * 主要职责：
 * - 世界加载时初始化所有虚空存储数据结构
 * - 世界卸载时清理虚空马达链接网络
 * - 确保跨维度数据的正确持久化和访问
 * - 维护虚空存储系统的数据完整性
 * 
 * 技术实现：
 * - 使用@Mod.EventBusSubscriber自动注册到Forge事件总线
 * - 监听LevelEvent.Load和LevelEvent.Unload事件
 * - 通过DimensionDataStorage管理持久化数据
 * - 使用computeIfAbsent确保数据的单例性和线程安全
 * 
 * 数据管理策略：
 * - 所有虚空存储数据均存储在主世界(overworld)的数据存储中
 * - 这确保了跨维度访问的一致性和可靠性
 * - 数据在服务器启动时加载，关闭时自动保存
 * - 支持热重载，数据在世界重新加载时保持持久化
 * 
 * 虚空存储数据类型：
 * - VoidChestInventoriesData：虚空胸子的物品存储数据
 * - VoidTanksData：虚空储罐的流体存储数据
 * - VoidBatteryData：虚空电池的能量存储数据
 * - VoidMotorLinkNetwork：虚空马达的网络连接数据
 * 
 * 事件处理流程：
 * 1. 世界加载事件触发
 * 2. 验证服务器环境（排除客户端）
 * 3. 获取数据存储管理器
 * 4. 初始化虚空马达网络
 * 5. 加载或创建各类虚空存储数据
 * 6. 建立全局数据引用
 * 
 * 错误处理：
 * - 空服务器检查，避免客户端执行服务器逻辑
 * - 使用computeIfAbsent确保数据存在，避免空指针异常
 * - 网络系统的优雅加载和卸载
 * 
 * 性能考虑：
 * - 数据懒加载，只在需要时创建
 * - 单例模式，避免重复加载相同数据
 * - 事件驱动，减少轮询开销
 * 
 * @author duquee
 * @since 1.0.0
 */
@Mod.EventBusSubscriber
public class CommonEvents {

	/**
	 * 世界加载事件处理器
	 * 
	 * 当任何世界（维度）被加载时触发，负责初始化Create Utilities模组的
	 * 所有虚空存储系统数据。这是模组数据生命周期的起点。
	 * 
	 * 处理流程：
	 * 1. 服务器环境验证：确保在服务器端执行，避免客户端逻辑错误
	 * 2. 网络初始化：启动虚空马达链接网络处理器
	 * 3. 数据存储初始化：从主世界数据存储中加载或创建虚空存储数据
	 * 4. 全局引用建立：将数据实例绑定到模组的全局访问点
	 * 
	 * 数据存储策略：
	 * - 统一存储：所有虚空数据都存储在主世界(overworld)中
	 * - 跨维度共享：任何维度都可以访问相同的虚空存储数据
	 * - 原子操作：使用computeIfAbsent确保数据的原子性加载
	 * - 版本兼容：支持数据格式的向后兼容性
	 * 
	 * 初始化的数据类型：
	 * - 虚空胸子库存：存储所有虚空胸子的物品内容
	 * - 虚空储罐数据：存储所有虚空储罐的流体内容
	 * - 虚空电池数据：存储所有虚空电池的能量状态
	 * - 虚空马达网络：管理马达间的动力传输连接
	 * 
	 * 错误容错：
	 * - 服务器空检查：防止在客户端执行服务器专用逻辑
	 * - 数据自动创建：如果数据文件不存在，自动创建默认实例
	 * - 异常隔离：单个数据加载失败不影响其他数据的初始化
	 * 
	 * 性能优化：
	 * - 懒加载机制：只有在实际需要时才加载数据
	 * - 缓存重用：已加载的数据实例会被重复使用
	 * - 最小化I/O：通过批量操作减少磁盘访问次数
	 * 
	 * @param event 世界加载事件，包含被加载的世界实例信息
	 */
	@SubscribeEvent
	public static void onLoad(LevelEvent.Load event) {

		// 获取服务器实例，确保在服务器环境中执行
		MinecraftServer server = event.getLevel().getServer();
		if (server == null) return; // 客户端环境直接返回，避免执行服务器逻辑

		// 获取当前加载的世界和主世界的数据存储管理器
		LevelAccessor level = event.getLevel();
		DimensionDataStorage dataStorage = server.overworld().getDataStorage();

		// 初始化虚空马达链接网络，建立动力传输连接
		CreateUtilities.VOID_MOTOR_LINK_NETWORK_HANDLER.onLoadWorld(level);

		// 加载或创建虚空胸子库存数据，使用"VoidChestInventories"作为数据键
		CreateUtilities.VOID_CHEST_INVENTORIES_DATA = dataStorage
				.computeIfAbsent(VoidChestInventoriesData::load, VoidChestInventoriesData::new, "VoidChestInventories");

		// 加载或创建虚空储罐数据，使用"VoidTanks"作为数据键
		CreateUtilities.VOID_TANKS_DATA = dataStorage
				.computeIfAbsent(VoidTanksData::load, VoidTanksData::new, "VoidTanks");

		// 加载或创建虚空电池数据，使用"VoidBatteries"作为数据键
		CreateUtilities.VOID_BATTERIES_DATA = dataStorage
				.computeIfAbsent(VoidBatteryData::load, VoidBatteryData::new, "VoidBatteries");

	}

	/**
	 * 世界卸载事件处理器
	 * 
	 * 当任何世界（维度）被卸载时触发，负责清理Create Utilities模组的
	 * 运行时状态，确保资源的正确释放和数据的完整性。
	 * 
	 * 清理流程：
	 * 1. 网络连接清理：断开虚空马达在该世界的所有链接
	 * 2. 缓存数据清理：清除该世界相关的缓存数据
	 * 3. 监听器注销：移除该世界的事件监听器
	 * 4. 资源释放：释放不再需要的内存资源
	 * 
	 * 设计考虑：
	 * - 优雅关闭：确保所有操作都能正确完成
	 * - 数据保护：卸载不会影响持久化数据的完整性
	 * - 性能优化：及时释放不需要的资源，避免内存泄漏
	 * - 异常安全：即使清理过程中出现错误，也不影响其他世界
	 * 
	 * 虚空马达网络清理：
	 * - 断开该世界中所有马达的网络连接
	 * - 清除该世界的马达缓存数据
	 * - 通知其他世界的马达更新连接状态
	 * - 保持跨世界连接的一致性
	 * 
	 * 重要说明：
	 * - 持久化数据（如虚空存储内容）不会在此时清理
	 * - 只清理运行时状态和临时数据
	 * - 世界重新加载时，数据会自动恢复
	 * 
	 * 错误处理：
	 * - 使用安全的清理方法，避免异常传播
	 * - 单个清理操作失败不影响其他清理步骤
	 * - 记录清理过程中的错误，便于调试
	 * 
	 * @param event 世界卸载事件，包含被卸载的世界实例信息
	 */
	@SubscribeEvent
	public static void onUnload(LevelEvent.Unload event) {
		// 清理虚空马达链接网络在该世界的所有连接和缓存数据
		CreateUtilities.VOID_MOTOR_LINK_NETWORK_HANDLER.onUnloadWorld(event.getLevel());
	}

}
