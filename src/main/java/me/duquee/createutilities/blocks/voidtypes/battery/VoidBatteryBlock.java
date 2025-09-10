package me.duquee.createutilities.blocks.voidtypes.battery;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;

import me.duquee.createutilities.blocks.CUTileEntities;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import org.jetbrains.annotations.Nullable;

/**
 * 虚空电池方块
 * 
 * 一个具有无限电能容量的虚空储电设备，能够存储和提供任意数量的电能。
 * 该方块是虚空存储系统的能源组件，为自动化系统提供持续稳定的电力供应。
 * 
 * 核心特性：
 * - 无限电能存储容量，彻底解决了电能储存限制问题
 * - 虚空链接系统，支持跨维度的电能共享和分配
 * - 方向性设计，支持4个水平方向的放置和连接
 * - Create扳手兼容，支持便捷的旋转和配置操作
 * - 自动化友好，与Create模组电力系统无缝集成
 * 
 * 技术实现：
 * - 继承HorizontalDirectionalBlock，提供方向性功能
 * - 实现IWrenchable接口，支持Create扳手操作
 * - 实现IBE接口，管理方块实体生命周期
 * - 使用FACING状态属性管理方向，确保连接的准确性
 * - 基于BlockPlaceContext自动检测合适的放置方向
 * 
 * 设计理念：
 * - 简化电力管理，让玩家专注于创造而非资源限制
 * - 提供可靠的电力基础设施，支持大型自动化项目
 * - 保持与原版和Create模组风格的一致性
 * - 通过虚空技术实现理论上无限的能源供应
 * 
 * 应用场景：
 * - 大型工厂的中央电源
 * - 跨基地的电力共享网络
 * - 备用电源系统
 * - 电力密集型设备的专用供电
 * 
 * @author duquee
 */
public class VoidBatteryBlock extends HorizontalDirectionalBlock implements IWrenchable, IBE<VoidBatteryTileEntity> {

	/**
	 * 构造函数
	 * 
	 * 初始化虚空电池方块，设置默认状态和基础属性。
	 * 默认朝向设置为北方，提供一致的初始状态。
	 * 
	 * @param properties 方块属性，定义硬度、抗性、工具需求等物理特性
	 */
	public VoidBatteryBlock(Properties properties) {
		super(properties);
		// 设置默认状态：朝向北方
		this.registerDefaultState(this.defaultBlockState()
				.setValue(FACING, Direction.NORTH));
	}

	/**
	 * 创建方块状态定义
	 * 
	 * 定义此方块的状态属性，主要是朝向属性的注册。
	 * FACING属性决定了电池的输出面方向和连接逻辑。
	 * 
	 * @param builder 状态定义构建器，用于注册方块的所有可能状态
	 */
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	/**
	 * 获取放置时的方块状态
	 * 
	 * 根据玩家的放置上下文确定方块的初始朝向。
	 * 自动设置为玩家面向方向的相反方向，确保电池面向玩家。
	 * 
	 * 放置逻辑：
	 * - 检测玩家的水平朝向
	 * - 设置为相反方向，使电池"面向"玩家
	 * - 这样的设计便于玩家理解电池的输出方向
	 * 
	 * @param context 方块放置上下文，包含玩家位置、朝向等信息
	 * @return 根据放置上下文确定的方块状态，如果无法确定则返回null
	 */
	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState()
				.setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	/**
	 * 获取方块实体类型
	 * 
	 * 返回与此方块关联的方块实体类，用于Create模组的IBE系统。
	 * 这个方法告诉系统该方块应该使用哪个方块实体类来管理数据和逻辑。
	 * 
	 * @return VoidBatteryTileEntity类，处理电能存储和传输逻辑
	 */
	@Override
	public Class<VoidBatteryTileEntity> getBlockEntityClass() {
		return VoidBatteryTileEntity.class;
	}

	/**
	 * 获取方块实体类型定义
	 * 
	 * 返回注册的方块实体类型，用于系统级别的实例化和类型检查。
	 * 这个类型定义在CUTileEntities中注册，确保正确的序列化和网络同步。
	 * 
	 * @return 虚空电池方块实体的注册类型
	 */
	@Override
	public BlockEntityType<? extends VoidBatteryTileEntity> getBlockEntityType() {
		return CUTileEntities.VOID_BATTERY.get();
	}
}
