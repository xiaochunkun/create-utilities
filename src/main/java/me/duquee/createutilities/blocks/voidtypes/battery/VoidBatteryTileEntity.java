package me.duquee.createutilities.blocks.voidtypes.battery;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import me.duquee.createutilities.CreateUtilities;
import me.duquee.createutilities.CreateUtilitiesClient;
import me.duquee.createutilities.blocks.voidtypes.VoidLinkBehaviour;
import me.duquee.createutilities.voidlink.VoidLinkSlot;
import net.createmod.catnip.lang.LangBuilder;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 虚空电池方块实体
 * 
 * 这是虚空电池系统的核心实现，负责管理电能存储和网络连接。
 * 作为SmartBlockEntity的子类，它继承了Create模组的高级特性，
 * 同时实现IHaveGoggleInformation接口提供护目镜信息显示。
 * 
 * 核心功能：
 * - 虚空网络管理：通过VoidLinkBehaviour处理网络连接
 * - 电能存储：提供IEnergyStorage能力实现电能管理
 * - 护目镜支持：显示电能信息和存储状态
 * - 数据同步：处理客户端-服务器的数据传输
 * - 方向管理：支持方向性电能输入输出
 * 
 * 技术特性：
 * - 持久化存储：通过VoidBatteryData管理全局数据
 * - 懒加载：仅在需要时创建存储实例
 * - 跨维度支持：实现不同世界间的电能共享
 * - 信息显示：集成Create的护目镜系统
 * 
 * 设计模式：
 * - 代理模式：通过LazyOptional实现能力代理
 * - 观察者模式：监听电能状态变化并更新显示
 * - 单例模式：全局共享的数据存储管理器
 * 
 * @author duquee
 * @since 1.0.0
 */
public class VoidBatteryTileEntity extends SmartBlockEntity implements IHaveGoggleInformation {

	/**
	 * 虚空链接行为组件
	 * 
	 * 管理虚空电池的网络连接逻辑，包括：
	 * - 频率配置管理
	 * - 所有者权限控制
	 * - 网络键生成和管理
	 * - 客户端配置界面支持
	 */
	VoidLinkBehaviour link;

	/**
	 * 构造虚空电池方块实体
	 * 
	 * 初始化方块实体的基础属性，传递给父类进行标准初始化。
	 * 在这个阶段不创建复杂的组件，等待addBehaviours()方法调用。
	 * 
	 * @param type 方块实体类型，用于类型检查和序列化
	 * @param pos 方块在世界中的位置坐标
	 * @param blockState 方块的当前状态
	 */
	public VoidBatteryTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
		super(type, pos, blockState);
	}

	/**
	 * 创建虚空链接行为组件
	 * 
	 * 定义和配置虚空电池的链接逻辑，包括三个配置槽位：
	 * 1. 第一个频率槽位：用于设置第一个频率标识符
	 * 2. 第二个频率槽位：用于设置第二个频率标识符
	 * 3. 玩家槽位：用于设置电池所有者
	 * 
	 * 槽位位置计算：
	 * - 位置坐标：(5.5, 10.5, -0.001) 像素单位
	 * - 动态朝向：根据电池的FACING属性计算
	 * - 显示位置：在电池正面的上方中央
	 * 
	 * 这个方法在addBehaviours()中被调用，确保在方块实体
	 * 完全初始化后再创建复杂的组件。
	 */
	public void createLink() {

		Triple<VoidLinkSlot, VoidLinkSlot, VoidLinkSlot> slots = VoidLinkSlot.makeSlots(
				index -> new VoidLinkSlot(index,
						state -> state.getValue(VoidBatteryBlock.FACING),
						VecHelper.voxelSpace(5.5F, 10.5F, -.001F)));

		link = new VoidLinkBehaviour(this, slots);
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
	 * 检查是否有持久化数据
	 * 
	 * 只有在服务端环境中才允许访问全局持久化数据。
	 * 客户端使用临时的本地存储实例进行显示和交互。
	 * 
	 * 判断条件：
	 * - level != null：确保方块实体已经被加载到世界中
	 * - !level.isClientSide：确保当前环境是服务端
	 * 
	 * @return 是否可以访问持久化存储数据
	 */
	private boolean hasPersistentData() {
		return level != null && !level.isClientSide;
	}

	/**
	 * 获取全局持久化存储数据管理器
	 * 
	 * 返回全局单例的虚空电池存储数据管理器。
	 * 这个管理器在模组初始化时创建，负责管理所有虚空电池的存储数据。
	 * 
	 * 使用静态方法确保：
	 * - 全局访问：任何地方都可以获取同一个实例
	 * - 线程安全：静态变量的访问是安全的
	 * - 内存效率：避免重复创建实例
	 * 
	 * @return 全局虚空电池存储数据管理器
	 */
	private static VoidBatteryData getPersistentStorageData() {
		return CreateUtilities.VOID_BATTERIES_DATA;
	}

	/**
	 * 获取电能存储实例
	 * 
	 * 这是虚空电池的核心逻辑，根据环境返回不同的存储实例：
	 * 
	 * 服务端模式：
	 * - 使用全局数据管理器获取或创建存储实例
	 * - 数据持久化到磁盘，服务器重启后保留
	 * - 多个电池共享同一个存储实例
	 * 
	 * 客户端模式：
	 * - 使用本地临时存储实例
	 * - 数据仅用于显示和交互
	 * - 通过网络同步从服务端获取数据
	 * 
	 * 这种设计确保了数据一致性和正确的客户端-服务端分离。
	 * 
	 * @return 当前环境适用的电能存储实例
	 */
	public VoidBattery getBattery() {
		return hasPersistentData() ?
				getPersistentStorageData().computeStorageIfAbsent(link.getNetworkKey()) :
				CreateUtilitiesClient.VOID_BATTERIES.computeStorageIfAbsent(link.getNetworkKey());
	}

	/**
	 * 提供Forge能力系统的接口实现
	 * 
	 * 这是Minecraft Forge能力系统的核心方法，允许其他模组和系统
	 * 访问虚空电池的特定功能。
	 * 
	 * 对于ENERGY能力：
	 * - 返回当前的电能存储实例
	 * - 支持所有标准的电能操作（存储、提取、查询等）
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
		if (cap == ForgeCapabilities.ENERGY) {
			return LazyOptional.of(this::getBattery).cast();
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
	 * - 从 NBT 中恢复电池数据用于显示
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
		if (clientPacket) getBattery().deserializeNBT(tag.getCompound("Battery"));
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
	 * - Battery：电池的完整状态
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
		if (clientPacket) tag.put("Battery", getBattery().serializeNBT());
		super.write(tag, clientPacket);
	}

	/**
	 * 为护目镜添加显示信息
	 * 
	 * 实现IHaveGoggleInformation接口的方法，在玩家佩戴护目镜时
	 * 为虚空电池添加详细的状态信息显示。
	 * 
	 * 显示的信息包括：
	 * 1. 标题信息：虚空电池状态概览
	 * 2. 电能标签：显示“电能”文字
	 * 3. 电能数值：当前/最大 电能显示
	 * 
	 * 使用LangBuilder构建本地化的显示文本：
	 * - tooltip.void_battery.header: 标题信息
	 * - tooltip.void_battery.energy: 电能标签
	 * - 动态数值显示：当前电能/最大电能
	 * 
	 * 颜色编码：
	 * - GRAY: 灰色的标签文字
	 * - GOLD: 金色的当前电能值
	 * - DARK_GRAY: 深灰色的最大电能值
	 * 
	 * @param tooltip 显示信息列表，方法会在这里添加信息
	 * @param isPlayerSneaking 玩家是否按住Shift键（用于显示更多详情）
	 * @return 总是返回true，表示成功添加了信息
	 */
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {

		VoidBattery battery = getBattery();

		new LangBuilder(CreateUtilities.ID)
				.translate("tooltip.void_battery.header")
				.forGoggles(tooltip);

		new LangBuilder(CreateUtilities.ID)
				.translate("tooltip.void_battery.energy")
				.style(ChatFormatting.GRAY)
				.forGoggles(tooltip, 1);

		new LangBuilder(CreateUtilities.ID)
				.add(new LangBuilder(CreateUtilities.ID)
						.text(battery.getEnergyStored() + "fe")
						.style(ChatFormatting.GOLD))
				.add(new LangBuilder(CreateUtilities.ID)
						.text(" / ")
						.style(ChatFormatting.GRAY))
				.add(new LangBuilder(CreateUtilities.ID)
						.text(battery.getMaxEnergyStored() + "fe")
						.style(ChatFormatting.DARK_GRAY))
				.forGoggles(tooltip, 1);

		return true;
	}

}
