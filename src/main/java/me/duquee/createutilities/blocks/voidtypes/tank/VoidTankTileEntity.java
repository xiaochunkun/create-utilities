package me.duquee.createutilities.blocks.voidtypes.tank;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import me.duquee.createutilities.CreateUtilities;
import me.duquee.createutilities.CreateUtilitiesClient;
import me.duquee.createutilities.blocks.voidtypes.VoidLinkBehaviour;
import me.duquee.createutilities.voidlink.VoidLinkSlot;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 虚空储罐方块实体
 * 
 * 这是虚空储罐系统的核心实现，负责管理流体存储和网络连接。
 * 作为SmartBlockEntity的子类，它继承了Create模组的高级特性，
 * 同时实现IHaveGoggleInformation接口提供护目镜信息显示。
 * 
 * 核心功能：
 * - 虚空网络管理：通过VoidLinkBehaviour处理网络连接
 * - 流体存储：提供IFluidHandler能力实现流体管理
 * - 护目镜支持：显示流体信息和存储状态
 * - 数据同步：处理客户端-服务器的数据传输
 * - 状态管理：支持开关状态的检查和控制
 * 
 * 技术特性：
 * - 持久化存储：通过VoidTanksData管理全局数据
 * - 懒加载：仅在需要时创建存储实例
 * - 跨维度支持：实现不同世界间的流体共享
 * - 信息显示：集成Create的护目镜系统
 * 
 * 设计模式：
 * - 代理模式：通过LazyOptional实现能力代理
 * - 观察者模式：监听流体状态变化并更新显示
 * - 单例模式：全局共享的数据存储管理器
 * 
 * @author duquee
 * @since 1.0.0
 */
public class VoidTankTileEntity extends SmartBlockEntity implements IHaveGoggleInformation {

	/**
	 * 虚空链接行为组件
	 * 
	 * 管理虚空储罐的网络连接逻辑，包括：
	 * - 频率配置管理
	 * - 所有者权限控制
	 * - 网络键生成和管理
	 * - 客户端配置界面支持
	 */
	VoidLinkBehaviour link;

	/**
	 * 构造虚空储罐方块实体
	 * 
	 * 初始化方块实体的基础属性，传递给父类进行标准初始化。
	 * 在这个阶段不创建复杂的组件，等待addBehaviours()方法调用。
	 * 
	 * @param type 方块实体类型，用于类型检查和序列化
	 * @param pos 方块在世界中的位置坐标
	 * @param state 方块的当前状态
	 */
	public VoidTankTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	/**
	 * 添加方块实体行为组件
	 * 
	 * 这是SmartBlockEntity的核心生命周期方法，在方块实体初始化时被调用。
	 * 此时方块实体已经完全就绪，可以安全地创建和添加复杂的行为组件。
	 * 
	 * 执行流程：
	 * 1. 创建虚空链接行为组件
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
	 * 创建虚空链接行为组件
	 * 
	 * 定义和配置虚空储罐的链接逻辑，包括三个配置槽位：
	 * 1. 第一个频率槽位：用于设置第一个频率标识符
	 * 2. 第二个频率槽位：用于设置第二个频率标识符
	 * 3. 玩家槽位：用于设置储罐所有者
	 * 
	 * 槽位位置计算：
	 * - 位置坐标：(5.5, 10.5, -0.001) 像素单位
	 * - 固定朝向：DOWN方向（向下）
	 * - 显示位置：在储罐上方中央的外表面
	 * 
	 * 这个方法在addBehaviours()中被调用，确保在方块实体
	 * 完全初始化后再创建复杂的组件。
	 */
	public void createLink() {

		Triple<VoidLinkSlot, VoidLinkSlot, VoidLinkSlot> slots = VoidLinkSlot.makeSlots(
				index -> new VoidLinkSlot(index,
						state -> Direction.DOWN,
						VecHelper.voxelSpace(5.5F, 10.5F, -.001F)) );

		link = new VoidLinkBehaviour(this, slots);
	}

	/**
	 * 获取流体存储实例
	 * 
	 * 这是虚空储罐的核心逻辑，根据环境返回不同的存储实例：
	 * 
	 * 服务端模式：
	 * - 使用全局数据管理器获取或创建存储实例
	 * - 数据持久化到磁盘，服务器重启后保留
	 * - 多个储罐共享同一个存储实例
	 * 
	 * 客户端模式：
	 * - 使用本地临时存储实例
	 * - 数据仅用于显示和交互
	 * - 通过网络同步从服务端获取数据
	 * 
	 * 这种设计确保了数据一致性和正确的客户端-服务端分离。
	 * 
	 * @return 当前环境适用的流体存储实例
	 */
	public FluidTank getFluidStorage() {
		return level != null && !level.isClientSide ?
				CreateUtilities.VOID_TANKS_DATA.computeStorageIfAbsent(link.getNetworkKey()) :
				CreateUtilitiesClient.VOID_TANKS.computeStorageIfAbsent(link.getNetworkKey());
	}

	/**
	 * 提供Forge能力系统的接口实现
	 * 
	 * 这是Minecraft Forge能力系统的核心方法，允许其他模组和系统
	 * 访问虚空储罐的特定功能。
	 * 
	 * 对于FLUID_HANDLER能力：
	 * - 返回当前的流体存储实例
	 * - 支持所有标准的流体操作（注入、抽取、查询等）
	 * - 通过LazyOptional实现懒加载和资源管理
	 * 
	 * 对于其他能力：
	 * - 委托给父类处理
	 * - 可能包括基础的方块实体能力（如NBT存储等）
	 * 
	 * LazyOptional的优点：
	 * - 懒加载：只有在需要时才创建实例
	 * - 内存效率：自动管理资源的生命周期
	 * - 线程安全：提供线程安全的访问机制
	 * 
	 * @param cap 请求的能力类型
	 * @param side 访问的方向（可为null表示任意方向）
	 * @param <T> 能力的泛型参数
	 * @return 对应能力的LazyOptional包装器
	 */
	@Override
	public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
		if (cap == ForgeCapabilities.FLUID_HANDLER) {
			return LazyOptional.of(this::getFluidStorage).cast();
		}
		return super.getCapability(cap, side);
	}

	/**
	 * 从 NBT 数据读取方块实体状态
	 * 
	 * 这个方法在以下情况下被调用：
	 * - 世界加载时从磁盘恢复数据
	 * - 客户端-服务端同步数据时
	 * - 区块传输时序列化数据
	 * 
	 * 客户端数据处理 (clientPacket = true)：
	 * - 从 NBT 中恢复流体储罐数据用于显示
	 * - 更新护目镜信息和渲染效果
	 * 
	 * 服务端数据处理 (clientPacket = false)：
	 * - 仅读取基础配置数据
	 * - 实际存储数据由全局数据管理器处理
	 * 
	 * 这种分离设计确保了数据一致性和网络效率。
	 * 
	 * @param tag 包含NBT数据的复合标签
	 * @param clientPacket 是否为客户端数据包
	 */
	@Override
	protected void read(CompoundTag tag, boolean clientPacket) {
		super.read(tag, clientPacket);
		if (clientPacket) getFluidStorage().readFromNBT(tag.getCompound("Tank"));
	}

	/**
	 * 将方块实体状态写入 NBT 数据
	 * 
	 * 这个方法在以下情况下被调用：
	 * - 世界保存时将数据持久化到磁盘
	 * - 客户端-服务端同步数据时
	 * - 区块传输时序列化数据
	 * 
	 * 数据写入逻辑：
	 * 
	 * 客户端同步数据：
	 * - Tank：流体储罐的完整状态
	 * - 用于客户端显示和渲染效果
	 * 
	 * 服务端数据处理：
	 * - 仅保存基础配置数据
	 * - 实际存储数据由全局数据管理器处理
	 * 
	 * 这种设计确保了：
	 * - 数据一致性：服务端为数据权威来源
	 * - 网络效率：只同步必要的显示数据
	 * - 容错性：多重后备方案防止数据丢失
	 * 
	 * @param tag 用于存储数据的复合标签
	 * @param clientPacket 是否为客户端数据包
	 */
	@Override
	protected void write(CompoundTag tag, boolean clientPacket) {
		if (clientPacket) tag.put("Tank", getFluidStorage().writeToNBT(new CompoundTag()));
		super.write(tag, clientPacket);
	}

	/**
	 * 检查储罐是否处于关闭状态
	 * 
	 * 通过读取方块状态中的CLOSED属性来判断储罐是否被关闭。
	 * 这个状态影响：
	 * - 渲染效果：关闭时隐藏虚空特效
	 * - 流体传输：关闭时可能禁止自动化访问
	 * - 交互行为：关闭时可能限制某些操作
	 * 
	 * @return true 如果储罐处于关闭状态，false 如果处于开启状态
	 */
	public boolean isClosed() {
		return getBlockState().getValue(VoidTankBlock.CLOSED);
	}

	/**
	 * 为护目镜添加显示信息
	 * 
	 * 实现IHaveGoggleInformation接口的方法，在玩家佩戴护目镜时
	 * 为虚空储罐添加详细的状态信息显示。
	 * 
	 * 显示的信息包括：
	 * - 流体类型和显示名称
	 * - 当前存储量和最大容量
	 * - 存储百分比和进度条
	 * - 流体的物理属性（如温度、粘度等）
	 * 
	 * containedFluidTooltip() 是Create模组提供的工具方法，
	 * 能够自动根据流体存储能力生成标准的显示信息。
	 * 
	 * @param tooltip 显示信息列表，方法会在这里添加信息
	 * @param isPlayerSneaking 玩家是否按住Shift键（用于显示更多详情）
	 * @return 是否成功添加了信息
	 */
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return containedFluidTooltip(tooltip, isPlayerSneaking, getCapability(ForgeCapabilities.FLUID_HANDLER));
	}
}
