package me.duquee.createutilities.blocks.voidtypes.motor;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import me.duquee.createutilities.voidlink.VoidLinkSlot;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

/**
 * 虚空马达方块实体
 * 
 * 这是虚空马达系统的核心实现，负责管理动力传输和网络连接。
 * 作为KineticBlockEntity的子类，它继承了Create模组的完整动力传输功能，
 * 同时集成虚空网络技术实现跨维度的动力共享。
 * 
 * 核心功能：
 * - 虚空网络管理：通过VoidMotorLinkBehaviour处理网络连接
 * - 动力传输：作为Create动力系统的动力源提供无限转速
 * - 网络传播：实现虚空马达间的动力网络连接
 * - 连接管理：处理虚空网络的连接和断开事件
 * 
 * 技术特性：
 * - 动力网络：通过网络键连接的马达共享相同的动力输出
 * - 智能传播：自动识别并连接网络中的其他马达
 * - 静音运行：不产生机械噪音，保持环境安静
 * - 事件响应：响应网络连接状态变化并调整动力传输
 * 
 * 设计模式：
 * - 观察者模式：监听网络连接状态变化
 * - 代理模式：通过链接行为代理网络操作
 * - 策略模式：根据连接状态采用不同的传播策略
 * 
 * @author duquee
 * @since 1.0.0
 */
public class VoidMotorTileEntity extends KineticBlockEntity {

	/**
	 * 虚空马达链接行为组件
	 * 
	 * 管理虚空马达的网络连接逻辑，包括：
	 * - 频率配置管理
	 * - 所有者权限控制
	 * - 网络键生成和管理
	 * - 马达间的网络通信
	 */
	VoidMotorLinkBehaviour link;

	/**
	 * 构造虚空马达方块实体
	 * 
	 * 初始化方块实体的基础属性，传递给父类进行标准的动力方块实体初始化。
	 * 在这个阶段不创建复杂的组件，等待addBehaviours()方法调用。
	 * 
	 * @param typeIn 方块实体类型，用于类型检查和序列化
	 * @param pos 方块在世界中的位置坐标
	 * @param state 方块的当前状态
	 */
	public VoidMotorTileEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}

	/**
	 * 添加方块实体行为组件
	 * 
	 * 这是KineticBlockEntity的核心生命周期方法，在方块实体初始化时被调用。
	 * 此时方块实体已经完全就绪，可以安全地创建和添加复杂的行为组件。
	 * 
	 * 执行流程：
	 * 1. 创建虚空马达链接行为组件
	 * 2. 将组件添加到行为列表中
	 * 3. Create模组会自动管理这些行为的生命周期
	 * 
	 * @param behaviours 行为组件列表，方块实体的所有行为都会添加到这里
	 */
	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		createLink();
		behaviours.add(link);
	}

	/**
	 * 创建虚空马达链接行为组件
	 * 
	 * 定义和配置虚空马达的链接逻辑，包括三个配置槽位：
	 * 1. 第一个频率槽位：用于设置第一个频率标识符
	 * 2. 第二个频率槽位：用于设置第二个频率标识符
	 * 3. 玩家槽位：用于设置马达所有者
	 * 
	 * 槽位位置计算：
	 * - 位置坐标：(5.5, 10.5, -0.001) 像素单位
	 * - 动态朝向：根据马达的FACING属性计算
	 * - 显示位置：在马达朝向面的正中央
	 * 
	 * 这个方法在addBehaviours()中被调用，确保在方块实体
	 * 完全初始化后再创建复杂的组件。
	 */
	public void createLink() {

		Triple<VoidLinkSlot, VoidLinkSlot, VoidLinkSlot> slots = VoidLinkSlot.makeSlots(
				index -> new VoidLinkSlot(index,
						state -> state.getValue(VoidMotorBlock.FACING),
						VecHelper.voxelSpace(5.5F, 10.5F, -.001F)));

		link = new VoidMotorLinkBehaviour(this, slots);

	}

	/**
	 * 连接到虚空网络事件处理
	 * 
	 * 当马达成功连接到虚空网络时被调用。此时马达需要激活其动力传输功能，
	 * 开始为网络中的其他马达和连接的机械设备提供动力。
	 * 
	 * 执行操作：
	 * - 附加动力学系统：将马达注册到Create的动力网络中
	 * - 激活动力输出：开始向连接的轴和齿轮传递动力
	 * - 网络同步：与虚空网络中的其他马达建立动力同步
	 * 
	 * 这个方法确保马达在连接到虚空网络后立即开始工作，
	 * 为依赖它的机械装置提供持续稳定的动力。
	 */
	public void onConnectToVoidNetwork() {
		attachKinetics();
	}

	/**
	 * 断开虚空网络连接事件处理
	 * 
	 * 当马达从虚空网络断开时被调用。此时马达需要停止其动力传输功能，
	 * 断开与其他马达的连接，并清理相关的动力状态。
	 * 
	 * 执行操作：
	 * - 分离动力学系统：从Create的动力网络中移除马达
	 * - 停止动力输出：断开向连接轴和齿轮的动力传递
	 * - 清理动力源：移除马达作为动力源的状态
	 * - 网络清理：断开与虚空网络中其他马达的连接
	 * 
	 * 这个方法确保马达在断开连接后完全停止工作，
	 * 避免对依赖它的机械装置造成异常状态。
	 */
	public void onDisconnectFromVoidNetwork() {
		detachKinetics();
		removeSource();
	}

	/**
	 * 添加动力传播位置
	 * 
	 * 扩展Create模组的动力传播机制，将虚空网络中的其他马达位置
	 * 添加到动力传播列表中。这实现了虚空马达间的"虚拟连接"，
	 * 使得网络中的马达能够互相传递动力，就像它们物理相连一样。
	 * 
	 * 传播逻辑：
	 * 1. 获取父类计算的标准邻居列表（物理相邻的机械设备）
	 * 2. 添加虚空网络中所有其他马达的位置
	 * 3. Create模组会将这些位置视为"虚拟邻居"进行动力传播
	 * 
	 * 这种设计实现了：
	 * - 跨距离动力传输：马达间无需物理连接即可共享动力
	 * - 网络效应：一个马达的动力状态会影响整个网络
	 * - 统一管理：所有网络马达表现为一个整体的动力系统
	 * 
	 * @param block 当前马达方块的IRotate接口
	 * @param state 当前马达的方块状态
	 * @param neighbours 现有的邻居位置列表
	 * @return 包含虚空网络位置的扩展邻居列表
	 */
	@Override
	public List<BlockPos> addPropagationLocations(IRotate block, BlockState state, List<BlockPos> neighbours) {
		neighbours.addAll(link.getNetwork());
		return neighbours;
	}

	/**
	 * 向目标传播旋转
	 * 
	 * 确定向目标机械设备传播动力的转速比例。这个方法是Create模组
	 * 动力系统的核心，用于计算不同机械设备间的动力传递关系。
	 * 
	 * 对于虚空马达系统：
	 * - 同网络马达：返回1.0，表示直接传递相同的转速
	 * - 不同网络：返回0，表示没有动力连接
	 * - 非马达设备：返回0，表示不通过虚空连接传递
	 * 
	 * 网络键验证：
	 * 1. 检查目标是否为虚空马达
	 * 2. 获取目标的虚空链接行为
	 * 3. 比较网络键是否相同
	 * 4. 返回相应的传播比例
	 * 
	 * 这种设计确保了：
	 * - 网络隔离：不同网络的马达互不干扰
	 * - 精确控制：只有相同网络的马达才能共享动力
	 * - 兼容性：与Create模组的动力系统完全兼容
	 * 
	 * @param target 目标机械设备的方块实体
	 * @param stateFrom 源马达的方块状态
	 * @param stateTo 目标设备的方块状态
	 * @param diff 位置差向量（虚空连接时可能很大）
	 * @param connectedViaAxes 是否通过轴连接
	 * @param connectedViaCogs 是否通过齿轮连接
	 * @return 动力传播比例，1.0表示直接传递，0表示无连接
	 */
	@Override
	public float propagateRotationTo(KineticBlockEntity target, BlockState stateFrom, BlockState stateTo, BlockPos diff, boolean connectedViaAxes, boolean connectedViaCogs) {
		VoidMotorLinkBehaviour targetLink = (VoidMotorLinkBehaviour) BlockEntityBehaviour.get(target, VoidMotorLinkBehaviour.TYPE);
		if (targetLink != null) return targetLink.getNetworkKey().equals(link.getNetworkKey()) ? 1 : 0;
		return 0;
	}

	/**
	 * 检查马达是否产生噪音
	 * 
	 * 虚空马达采用虚空动力技术，运行时完全静音。
	 * 这与传统的机械马达不同，为用户提供更安静的游戏体验。
	 * 
	 * 静音特性的优点：
	 * - 环境友好：不会产生持续的机械噪音
	 * - 适合住宅区：可以在建筑内部使用而不影响居住体验
	 * - 隐蔽性强：适合建造隐藏的自动化系统
	 * 
	 * @return 总是返回false，表示虚空马达完全静音
	 */
	@Override
	protected boolean isNoisy() {
		return false;
	}

}
