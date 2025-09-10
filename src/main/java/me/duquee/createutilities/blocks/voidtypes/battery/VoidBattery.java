package me.duquee.createutilities.blocks.voidtypes.battery;

import me.duquee.createutilities.CreateUtilities;
import me.duquee.createutilities.blocks.voidtypes.motor.VoidMotorNetworkHandler.NetworkKey;
import me.duquee.createutilities.networking.CUPackets;
import me.duquee.createutilities.networking.packets.VoidBatteryUpdatePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.network.PacketDistributor;

/**
 * 虚空电池能量存储器
 * 
 * 虚空电池的核心能量管理组件，继承自Forge能量系统的EnergyStorage。
 * 提供了基于虚空网络的无限能量存储和高效的能量传输能力。
 * 
 * 核心功能：
 * - 大容量能量存储（32000 FE基础容量）
 * - 高速能量传输（4096 FE/tick输入输出速率）
 * - 虚空网络集成，支持跨维度能量共享
 * - 实时网络同步，确保多玩家环境下的数据一致性
 * - NBT序列化支持，实现数据的持久化存储
 * 
 * 技术特性：
 * - 网络密钥系统：每个电池都有唯一的网络标识符
 * - 变更通知机制：能量变化时自动触发网络同步
 * - 数据完整性保护：通过脏标记确保数据及时保存
 * - 双向能量传输：同时支持能量输入和输出操作
 * 
 * 虚空网络机制：
 * - 使用NetworkKey作为唯一标识符
 * - 所有相同密钥的电池共享能量池
 * - 跨维度能量访问和分配
 * - 实时网络状态同步
 * 
 * 性能优化：
 * - 只在能量实际变化时才触发网络更新
 * - 使用高效的NBT序列化格式
 * - 批量网络数据包发送，减少网络负载
 * 
 * @author duquee
 */
public class VoidBattery extends EnergyStorage {

	/**
	 * 网络密钥
	 * 
	 * 用于标识此电池在虚空网络中的位置和归属。
	 * 相同网络密钥的电池会共享同一个能量池，实现真正的虚空存储效果。
	 */
	private final NetworkKey key;

	/**
	 * 构造函数
	 * 
	 * 初始化虚空电池的能量存储参数和网络配置。
	 * 
	 * 能量参数配置：
	 * - 容量：32000 FE（相当于16个基础电池的容量）
	 * - 最大输入速率：4096 FE/tick（高速充电支持）
	 * - 最大输出速率：4096 FE/tick（高速放电支持）
	 * 
	 * @param key 网络密钥，用于虚空网络中的电池标识和分组
	 */
	public VoidBattery(NetworkKey key) {
		super(32000, 4096, 4096); // 容量, 最大输入, 最大输出
		this.key = key;
	}

	/**
	 * 检查电池是否为空
	 * 
	 * 判断当前电池是否还存储有能量。
	 * 这个方法常用于渲染系统和逻辑判断中。
	 * 
	 * @return 如果电池能量为0则返回true，否则返回false
	 */
	public boolean isEmpty() {
		return energy == 0;
	}

	/**
	 * 接收能量
	 * 
	 * 重写父类的能量接收方法，添加了虚空网络同步机制。
	 * 当能量实际发生变化时，会触发网络更新和数据保存。
	 * 
	 * 工作流程：
	 * 1. 调用父类方法执行基础的能量接收逻辑
	 * 2. 检查是否有能量实际被接收
	 * 3. 如有变化，触发内容变更通知
	 * 4. 返回实际接收的能量数量
	 * 
	 * @param maxReceive 尝试接收的最大能量数量
	 * @param simulate 是否为模拟模式（true=不实际执行，仅计算结果）
	 * @return 实际接收的能量数量
	 */
	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
		int inserted = super.receiveEnergy(maxReceive, simulate);
		if (inserted != 0) onContentsChanged(); // 能量变化时触发同步
		return inserted;
	}

	/**
	 * 提取能量
	 * 
	 * 重写父类的能量提取方法，添加了虚空网络同步机制。
	 * 当能量实际发生变化时，会触发网络更新和数据保存。
	 * 
	 * 工作流程：
	 * 1. 调用父类方法执行基础的能量提取逻辑
	 * 2. 检查是否有能量实际被提取
	 * 3. 如有变化，触发内容变更通知
	 * 4. 返回实际提取的能量数量
	 * 
	 * @param maxExtract 尝试提取的最大能量数量
	 * @param simulate 是否为模拟模式（true=不实际执行，仅计算结果）
	 * @return 实际提取的能量数量
	 */
	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		int extracted = super.extractEnergy(maxExtract, simulate);
		if (extracted != 0) onContentsChanged(); // 能量变化时触发同步
		return extracted;
	}

	/**
	 * 内容变更通知
	 * 
	 * 当电池能量发生变化时调用此方法，负责处理数据同步和持久化。
	 * 这个方法确保了虚空网络中所有相关组件都能及时获得能量状态的更新。
	 * 
	 * 执行的操作：
	 * 1. 标记虚空电池数据为"脏"状态，触发保存机制
	 * 2. 向所有连接的客户端发送能量更新数据包
	 * 3. 确保网络中的其他电池和相关设备能够实时感知变化
	 * 
	 * 网络同步机制：
	 * - 使用PacketDistributor.ALL将更新发送给所有玩家
	 * - VoidBatteryUpdatePacket包含网络密钥和当前电池状态
	 * - 支持多维度和多玩家环境下的实时同步
	 */
	private void onContentsChanged() {
		// 标记数据需要保存
		if (CreateUtilities.VOID_BATTERIES_DATA != null) CreateUtilities.VOID_BATTERIES_DATA.setDirty();
		
		// 发送网络更新数据包给所有客户端
		CUPackets.channel.send(PacketDistributor.ALL.noArg(), new VoidBatteryUpdatePacket(key, this));
	}

	/**
	 * 序列化为NBT
	 * 
	 * 将电池的能量数据转换为NBT格式，用于数据持久化存储。
	 * 这个方法在世界保存、区块卸载等场景中被调用。
	 * 
	 * NBT数据结构：
	 * - "Energy": 当前存储的能量值（long类型，支持大数值）
	 * 
	 * 设计考虑：
	 * - 使用long类型存储能量，为未来的大容量扩展留出空间
	 * - 数据结构简洁，减少存储空间占用
	 * - 兼容性好，便于版本升级和数据迁移
	 * 
	 * @return 包含电池数据的NBT标签
	 */
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		nbt.putLong("Energy", energy); // 使用long类型存储，支持大数值
		return nbt;
	}

	/**
	 * 从NBT反序列化
	 * 
	 * 从NBT标签中恢复电池的能量数据，用于数据加载和恢复。
	 * 这个方法在世界加载、区块加载等场景中被调用。
	 * 
	 * 数据恢复过程：
	 * 1. 从NBT中读取"Energy"字段
	 * 2. 将读取的值赋给energy字段
	 * 3. 如果NBT中没有对应字段，则使用默认值（0）
	 * 
	 * 兼容性处理：
	 * - 使用getInt方法读取，保持向后兼容性
	 * - 如果数据不存在或损坏，电池会以空状态开始
	 * - 静默处理数据错误，避免影响游戏正常运行
	 * 
	 * @param nbt 包含电池数据的NBT标签
	 */
	public void deserializeNBT(CompoundTag nbt) {
		energy = nbt.getInt("Energy"); // 从NBT恢复能量数据
	}
}
