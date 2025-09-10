package me.duquee.createutilities.networking.packets;

import com.simibubi.create.foundation.networking.SimplePacketBase;

import me.duquee.createutilities.CreateUtilitiesClient;
import me.duquee.createutilities.blocks.voidtypes.battery.VoidBattery;
import me.duquee.createutilities.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

/**
 * 虚空电池更新数据包
 * 
 * 这个数据包类负责将服务器端的虚空电池状态同步到所有客户端。
 * 当虚空电池中的电能发生变化时（如充电、放电、初始化等），
 * 服务器会向所有连接的客户端广播这个数据包，
 * 确保所有玩家都能看到一致的电池状态。
 * 
 * 核心功能：
 * - 电池状态同步：传输完整的电池状态信息
 * - 网络键匹配：通过网络键标识特定的虚空电池网络
 * - 客户端处理：在客户端更新本地电池数据存储
 * - 线程安全：在正确的线程中处理数据更新
 * 
 * 数据包结构：
 * - 网络键：NetworkKey，由所有者+频率1+频率2组成
 * - 电池数据：序列化的VoidBattery对象，包含存储电量等
 * 
 * 传输方向：服务器 → 客户端（广播所有玩家）
 * 触发条件：虚空电池内容发生变化时自动发送
 * 
 * 技术实现：
 * - 继承SimplePacketBase，获得基础的网络数据包功能
 * - 使用NBT序列化保证数据完整性和兼容性
 * - 采用分布式执行确保客户端安全性
 * - 集成客户端数据管理器，自动更新本地缓存
 * 
 * 使用场景：
 * - 电池充电后同步新的电量状态
 * - 电池放电后同步剩余电量
 * - 新玩家加入服务器时同步现有电池状态
 * - 跨维度电池网络状态保持一致
 * 
 * @author duquee
 * @since 1.0.0
 */
public class VoidBatteryUpdatePacket extends SimplePacketBase {

	/**
	 * 网络键
	 * 
	 * 标识特定的虚空电池网络。这个键由三部分组成：
	 * - 所有者：电池网络的所有者玩家
	 * - 频率1：第一个频率标识符
	 * - 频率2：第二个频率标识符
	 * 
	 * 相同网络键的电池共享同一个电能存储。
	 */
	private final NetworkKey key;
	
	/**
	 * 虚空电池对象
	 * 
	 * 包含完整的电池状态信息，包括：
	 * - 当前存储的电能数量
	 * - 最大存储容量
	 * - 电池的其他属性和状态
	 * 
	 * 这个对象会被序列化后在网络中传输。
	 */
	private final VoidBattery battery;

	/**
	 * 构造发送用数据包
	 * 
	 * 这个构造函数用于在服务器端创建数据包。
	 * 当虚空电池的状态发生变化时，服务器会使用
	 * 这个构造函数创建数据包并广播到所有客户端。
	 * 
	 * @param key 网络键，标识特定的虚空电池网络
	 * @param battery 要同步的虚空电池对象
	 */
	public VoidBatteryUpdatePacket(NetworkKey key, VoidBattery battery) {
		this.key = key;
		this.battery = battery;
	}

	/**
	 * 构造接收用数据包
	 * 
	 * 这个构造函数用于在客户端从网络数据流中
	 * 反序列化数据包。它会从缓冲区中读取数据并
	 * 重建电池对象。
	 * 
	 * 反序列化流程：
	 * 1. 从缓冲区读取网络键
	 * 2. 使用网络键创建新的虚空电池对象
	 * 3. 从缓冲区读取NBT数据并应用到电池对象
	 * 
	 * @param buffer 网络数据缓冲区，包含序列化的数据
	 */
	public VoidBatteryUpdatePacket(FriendlyByteBuf buffer) {
		key = NetworkKey.fromBuffer(buffer);     // 读取网络键
		battery = new VoidBattery(key);          // 创建新的电池对象
		battery.deserializeNBT(buffer.readNbt());  // 从 NBT 数据恢复电池状态
	}

	/**
	 * 将数据包序列化到网络缓冲区
	 * 
	 * 这个方法在发送数据包时被调用，负责将电池对象
	 * 序列化为可以在网络中传输的字节流。
	 * 
	 * 序列化流程：
	 * 1. 将网络键写入缓冲区
	 * 2. 将电池对象序列化为NBT并写入缓冲区
	 * 
	 * 使用NBT的优点：
	 * - 数据完整性：保证所有电池属性都被保存
	 * - 版本兼容：支持属性新增和修改
	 * - 类型安全：保持数据类型信息
	 * 
	 * @param buffer 目标网络缓冲区
	 */
	@Override
	public void write(FriendlyByteBuf buffer) {
		key.writeToBuffer(buffer);                  // 写入网络键
		buffer.writeNbt(battery.serializeNBT());     // 写入电池NBT数据
	}

	/**
	 * 处理数据包
	 * 
	 * 这个方法在客户端接收到数据包时被调用。
	 * 它的主要任务是将从服务器同步的电池状态
	 * 更新到客户端的本地数据存储中。
	 * 
	 * 处理流程：
	 * 1. 将任务入队到主线程（确保线程安全）
	 * 2. 使用DistExecutor确保只在客户端执行
	 * 3. 将电池对象更新到客户端的虚空电池数据管理器
	 * 
	 * 线程安全考虑：
	 * - enqueueWork()：确保在主线程中执行，避免并发问题
	 * - DistExecutor：防止在服务器端错误执行客户端代码
	 * 
	 * 数据更新：
	 * - 将新的电池对象放入客户端存储映射中
	 * - 覆盖旧的电池数据（如果存在）
	 * - 触发相关的UI更新和渲染刷新
	 * 
	 * @param context 网络事件上下文，提供线程安全的执行环境
	 * @return 总是返回true，表示数据包处理成功
	 */
	@Override
	public boolean handle(NetworkEvent.Context context) {
		// 将任务入队到主线程以确保线程安全
		context.enqueueWork(() -> DistExecutor.runWhenOn(Dist.CLIENT, () -> () ->
			// 在客户端更新虚空电池数据存储
			CreateUtilitiesClient.VOID_BATTERIES.storages.put(key, battery)
		));
		return true;  // 表示数据包处理成功
	}

}
