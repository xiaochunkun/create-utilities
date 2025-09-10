package me.duquee.createutilities.blocks.voidtypes.motor;

import com.simibubi.create.AllShapes;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import me.duquee.createutilities.blocks.CUTileEntities;
import me.duquee.createutilities.blocks.voidtypes.VoidLinkBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

/**
 * 虚空马达方块
 * 
 * 一个具有无限动力输出的虚空驱动设备，为Create机械系统提供持续稳定的动力源。
 * 该方块是虚空存储系统的动力组件，解决了大型机械装置的动力供应问题。
 * 
 * 核心特性：
 * - 无限动力输出，提供恒定的转速和扭矩
 * - 方向性传动，支持6个方向的轴传动连接
 * - 智能放置系统，自动检测最佳连接方向
 * - 水下工作兼容，支持水淹状态下的正常运行
 * - 虚空网络集成，通过网络密钥实现跨维度动力共享
 * 
 * 技术实现：
 * - 继承DirectionalKineticBlock，获得完整的Create机械传动功能
 * - 实现SimpleWaterloggedBlock，支持水下放置和工作
 * - 实现IBE接口，提供方块实体的生命周期管理
 * - 使用AllShapes马达外形，保持与Create模组风格的一致性
 * - 集成VoidLinkBehaviour，实现虚空网络连接
 * 
 * 机械特性：
 * - 轴传动接口：仅在朝向方向提供轴连接
 * - 旋转轴：根据朝向自动确定旋转轴线
 * - 动力传递：作为动力源向连接的机械设备提供动力
 * - 智能朝向：优先连接现有的机械装置
 * 
 * 放置行为：
 * - 默认模式：自动检测并连接最近的机械设备
 * - Shift模式：手动指定朝向，用于精确布置
 * - 水下放置：自动处理水淹状态，无需额外配置
 * 
 * 应用场景：
 * - 大型工厂的中央动力源
 * - 远程动力传输网络
 * - 高扭矩设备的专用驱动
 * - 水下机械装置的动力供应
 * 
 * @author duquee
 */
public class VoidMotorBlock extends DirectionalKineticBlock implements SimpleWaterloggedBlock, IBE<VoidMotorTileEntity> {

	/**
	 * 构造函数
	 * 
	 * 初始化虚空马达方块，设置默认状态和基础属性。
	 * 默认设置为不被水淹，提供干燥环境下的初始状态。
	 * 
	 * @param properties 方块属性，定义硬度、抗性、工具需求等物理特性
	 */
	public VoidMotorBlock(Properties properties) {
		super(properties);
		// 设置默认状态：不被水淹
		registerDefaultState(defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));
	}

	/**
	 * 获取方块的碰撞形状
	 * 
	 * 根据马达的朝向返回相应的碰撞体积。使用Create模组预定义的马达外形，
	 * 确保与其他机械方块的视觉和功能一致性。
	 * 
	 * @param state 方块状态，包含朝向信息
	 * @param worldIn 世界实例
	 * @param pos 方块位置
	 * @param context 碰撞检测上下文
	 * @return 根据朝向调整的马达外形
	 */
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.MOTOR_BLOCK.get(state.getValue(FACING));
	}

	/**
	 * 获取流体状态
	 * 
	 * 确定方块当前的流体状态，支持水淹功能。
	 * 这允许虚空马达在水下正常工作，扩展了其应用范围。
	 * 
	 * @param state 方块状态，包含水淹信息
	 * @return 如果被水淹则返回水源流体状态，否则返回空流体状态
	 */
	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
	}

	/**
	 * 更新方块形状
	 * 
	 * 处理邻接方块变化对当前方块的影响，主要用于水流更新调度。
	 * 当马达被水淹时，确保水流的正确更新和传播。
	 * 
	 * @param state 当前方块状态
	 * @param direction 变化发生的方向
	 * @param neighbourState 邻接方块的新状态
	 * @param world 世界访问器
	 * @param pos 当前方块位置
	 * @param neighbourPos 邻接方块位置
	 * @return 更新后的方块状态
	 */
	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor world, BlockPos pos, BlockPos neighbourPos) {
		// 如果方块被水淹，调度水流更新
		if (state.getValue(WATERLOGGED)) world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		return state;
	}

	/**
	 * 创建方块状态定义
	 * 
	 * 定义此方块的所有可能状态属性，包括父类的定向属性和水淹属性。
	 * 
	 * @param builder 状态定义构建器
	 */
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(WATERLOGGED));
	}

	/**
	 * 获取放置时的方块状态
	 * 
	 * 根据放置上下文确定马达的初始状态，包括朝向和水淹状态。
	 * 实现智能放置逻辑，自动连接现有的机械装置。
	 * 
	 * 放置逻辑：
	 * 1. 检查是否按下Shift键（手动模式）
	 * 2. 尝试获取首选朝向（自动连接模式）
	 * 3. 检测放置位置的流体状态
	 * 4. 返回相应的方块状态配置
	 * 
	 * 智能连接：
	 * - 扫描周围的机械设备
	 * - 选择最佳的连接方向
	 * - 确保动力传递的连续性
	 * 
	 * @param context 方块放置上下文，包含玩家操作和环境信息
	 * @return 根据放置条件确定的方块状态
	 */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		
		Direction preferred = getPreferredFacing(context);
		FluidState ifluidstate = context.getLevel().getFluidState(context.getClickedPos());

		// 手动模式：玩家按住Shift或没有找到首选方向
		if ((context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) || preferred == null)
			return super.getStateForPlacement(context)
					.setValue(WATERLOGGED, ifluidstate.getType() == Fluids.WATER);

		// 自动模式：使用首选方向
		return defaultBlockState().setValue(FACING, preferred)
				.setValue(WATERLOGGED, ifluidstate.getType() == Fluids.WATER);
	}

	// IRotate 接口实现：

	/**
	 * 检查是否向指定方向提供轴连接
	 * 
	 * 虚空马达仅在其朝向方向提供轴传动接口，这确保了明确的动力输出方向。
	 * 
	 * @param world 世界读取器
	 * @param pos 马达位置
	 * @param state 马达状态
	 * @param face 检查的方向
	 * @return 如果指定方向是马达朝向则返回true
	 */
	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return face == state.getValue(FACING);
	}

	/**
	 * 获取旋转轴线
	 * 
	 * 返回马达的旋转轴线，这决定了动力传递的方向和连接的机械设备的旋转方式。
	 * 旋转轴线与马达朝向一致，确保动力沿正确方向传递。
	 * 
	 * @param state 马达状态，包含朝向信息
	 * @return 马达的旋转轴线
	 */
	@Override
	public Direction.Axis getRotationAxis(BlockState state) {
		return state.getValue(FACING).getAxis();
	}

	/**
	 * 方块放置完成回调
	 * 
	 * 在马达被成功放置到世界中后执行初始化操作。
	 * 主要用于设置虚空链接行为和绑定所有者信息。
	 * 
	 * 初始化流程：
	 * 1. 调用父类方法执行基础初始化
	 * 2. 检查是否为服务器端环境
	 * 3. 获取虚空链接行为实例
	 * 4. 设置放置者为马达所有者
	 * 
	 * @param worldIn 放置的世界
	 * @param pos 放置位置
	 * @param state 放置的状态
	 * @param placer 放置者实体
	 * @param stack 用于放置的物品栈
	 */
	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(worldIn, pos, state, placer, stack);
		if (worldIn.isClientSide()) return; // 只在服务器端执行
		
		VoidLinkBehaviour behaviour = BlockEntityBehaviour.get(worldIn, pos, VoidLinkBehaviour.TYPE);
		if (placer instanceof Player player) behaviour.setOwner(player.getGameProfile());
	}

	/**
	 * 获取方块实体类型
	 * 
	 * 返回与此方块关联的方块实体类，用于Create模组的IBE系统。
	 * 
	 * @return VoidMotorTileEntity类，处理马达的逻辑和数据
	 */
	@Override
	public Class<VoidMotorTileEntity> getBlockEntityClass() {
		return VoidMotorTileEntity.class;
	}

	/**
	 * 获取方块实体类型定义
	 * 
	 * 返回注册的方块实体类型，用于实例化和类型检查。
	 * 
	 * @return 虚空马达方块实体的注册类型
	 */
	@Override
	public BlockEntityType<? extends VoidMotorTileEntity> getBlockEntityType() {
		return CUTileEntities.VOID_MOTOR.get();
	}
}
