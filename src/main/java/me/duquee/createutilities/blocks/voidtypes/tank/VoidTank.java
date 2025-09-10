package me.duquee.createutilities.blocks.voidtypes.tank;

import com.simibubi.create.infrastructure.config.AllConfigs;
import me.duquee.createutilities.CreateUtilities;
import me.duquee.createutilities.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import me.duquee.createutilities.networking.CUPackets;
import me.duquee.createutilities.networking.packets.VoidTankUpdatePacket;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.network.PacketDistributor;

/**
 * 虚空储罐类
 * 
 * 这个类是虚空储罐系统的核心流体存储实现，继承Forge的FluidTank类。
 * 它为虚空存储系统提供了专门的流体处理能力，包括大容量存储、
 * 网络同步和数据持久化功能。
 * 
 * 核心特性：
 * - 大容量存储：基于Create配置的大容量流体存储
 * - 网络关联：通过NetworkKey与虚空网络系统集成
 * - 实时同步：流体变化时自动同步到所有客户端
 * - 数据持久化：自动触发数据保存机制
 * 
 * 技术实现：
 * - 继承Forge FluidTank：免费获得所有标准流体操作支持
 * - 配置驱动容量：根据Create模组配置动态调整容量
 * - 网络数据包：使用自定义数据包进行客户端同步
 * - 事件驱动：流体变化时自动触发相关操作
 * 
 * 使用场景：
 * - 自动化流体系统中的中继存储
 * - 跨维度流体传输系统
 * - 大规模流体生产和消耗系统
 * - 流体备份和存储管理
 * 
 * @author duquee
 * @since 1.0.0
 */
public class VoidTank extends FluidTank {

	/**
	 * 虚空储罐的最大容量
	 * 
	 * 基于Create模组的流体储罐配置计算得出的容量。
	 * 通过配置系统的fluidTankCapacity参数乘以1000来定义。
	 * 
	 * 计算方式：
	 * - 基础配置：fluidTankCapacity（默认值由Create决定）
	 * - 缩放系数：1000（提供更大的虚空存储容量）
	 * 
	 * 这种设计的优点：
	 * - 用户可配置：通过Create配置文件调整容量
	 * - 平衡性能：提供大容量同时保持系统稳定
	 * - 兼容性：与Create的整体设计保持一致
	 */
	public static final int CAPACITY = AllConfigs.server().fluids.fluidTankCapacity.get() * 1000;

	/**
	 * 虚空网络键
	 * 
	 * 这个键用于标识当前流体存储所属的虚空网络。
	 * 具有相同网络键的虚空储罐会共享这个流体存储实例，
	 * 实现真正的“虚空”存储——流体存在于一个抽象的空间中，
	 * 可以从任何关联的储罐中访问。
	 * 
	 * 网络键的组成通常包括：
	 * - 所有者信息：确保只有授权用户可以访问
	 * - 频率标识：区分不同的虚空网络
	 * - 维度信息：确保跨维度访问的正确性
	 */
	private final NetworkKey key;

	/**
	 * 构造虚空储罐
	 * 
	 * 创建一个与指定网络键关联的虚空流体存储实例。
	 * 使用预定义的大容量初始化存储空间，为流体操作做准备。
	 * 
	 * 初始化过程：
	 * 1. 调用父类构造函数，传递预计算的容量
	 * 2. 保存网络键的引用，用于后续的网络操作
	 * 3. 继承父类的所有流体处理能力
	 * 
	 * @param key 虚空网络键，用于标识这个流体存储所属的网络
	 */
	public VoidTank(NetworkKey key) {
		super(CAPACITY);  // 使用预定义的大容量初始化流体储罐
		this.key = key;
	}

	/**
	 * 处理流体内容变更事件
	 * 
	 * 当储罐中的流体发生变化时（注入、抽取、数量改变等），
	 * 这个方法会被自动调用。它负责触发两个关键操作：
	 * 
	 * 1. 数据持久化标记：
	 *    - 检查全局虚空储罐数据管理器是否存在
	 *    - 调用setDirty()标记数据需要保存
	 *    - 在世界保存时，数据管理器会将变更写入NBT
	 * 
	 * 2. 网络数据包发送：
	 *    - 创建VoidTankUpdatePacket数据包
	 *    - 包含网络键和当前储罐状态
	 *    - 向所有客户端广播更新
	 *    - 确保所有相关的虚空储罐实时同步
	 * 
	 * 这种设计实现了：
	 * - 实时性：每次流体变更都会立即同步
	 * - 可靠性：确保数据不会在意外情况下丢失
	 * - 一致性：所有相关储罐都显示相同的数据
	 * - 性能：只在变更时才触发操作，避免不必要的开销
	 */
	@Override
	protected void onContentsChanged() {
		if (CreateUtilities.VOID_TANKS_DATA != null) CreateUtilities.VOID_TANKS_DATA.setDirty();
		CUPackets.channel.send(PacketDistributor.ALL.noArg(), new VoidTankUpdatePacket(key, this));
	}

}
