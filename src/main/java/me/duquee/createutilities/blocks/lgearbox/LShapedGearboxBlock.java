package me.duquee.createutilities.blocks.lgearbox;

import com.simibubi.create.api.contraption.transformable.TransformableBlock;
import com.simibubi.create.content.contraptions.StructureTransform;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.gearbox.GearboxBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import me.duquee.createutilities.blocks.CUTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * L型齿轮箱方块类
 * <p>
 * 实现了具有两个垂直传动轴的L型齿轮箱功能，允许动力在两个不同方向之间进行90度转换。
 * 与标准齿轮箱不同，L型齿轮箱只有两个传动接口，呈L形排列，适用于紧凑的空间布局。
 * <p>
 * 核心功能：
 * 1. 支持两个方向的动力传输，一个水平方向，一个可以是水平或垂直方向
 * 2. 实现动态的方块状态管理，根据周围环境自动调整朝向
 * 3. 支持结构转换和旋转变换，保持在机械装置中的正确连接
 * 4. 提供智能的放置逻辑，优先连接到已存在的传动轴
 * 
 * 设计理念：
 * - 使用双Direction属性系统管理两个传动轴方向
 * - 实现自适应放置算法，优化用户体验
 * - 支持Create模组的所有变换和旋转机制
 * - 提供紧凑型齿轮传动解决方案
 */

public class LShapedGearboxBlock extends KineticBlock implements IBE<GearboxBlockEntity>, TransformableBlock {

	/**
	 * 第一个传动轴方向属性
	 * <p>
	 * 限制为水平方向（北、南、东、西），作为L型齿轮箱的主要传动轴。
	 * 这个轴始终保持水平，确保L型齿轮箱的基础方向稳定。
	 */
	public static final DirectionProperty FACING_1 = BlockStateProperties.HORIZONTAL_FACING;
	
	/**
	 * 第二个传动轴方向属性
	 * <p>
	 * 支持东、上、西、下四个方向，与第一个轴形成L形布局。
	 * 这个设计允许灵活的动力传输配置，既可以是水平转向，也可以是垂直传动。
	 */
	public static final DirectionProperty FACING_2 = DirectionProperty.create("facing_2",
			Direction.EAST, Direction.UP, Direction.WEST, Direction.DOWN);

	/**
	 * 构造L型齿轮箱方块
	 * <p>
	 * 初始化方块属性并设置默认的方块状态。
	 * 默认配置为：第一轴朝向北方，第二轴朝向东方，形成标准的L形布局。
	 * 
	 * @param properties 方块属性，定义硬度、材质、阻力等基础特性
	 */

	public LShapedGearboxBlock(Properties properties) {
		super(properties);
		// 注册默认状态：第一轴朝北，第二轴朝东
		this.registerDefaultState(this.defaultBlockState()
				.setValue(FACING_1, Direction.NORTH)
				.setValue(FACING_2, Direction.EAST));
	}

	/**
	 * 创建方块状态定义
	 * <p>
	 * 定义此方块可用的所有状态属性，包括两个传动轴的方向属性。
	 * 这些属性组合形成了L型齿轮箱的所有可能配置。
	 * 
	 * @param builder 方块状态定义构建器
	 */

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		// 添加两个传动轴方向属性到状态定义中
		builder.add(FACING_1, FACING_2);
	}

	/**
	 * 获取方块放置时的状态
	 * <p>
	 * 实现智能的自适应放置逻辑，会尝试连接到周围已存在的传动轴。
	 * 放置算法的优先级：
	 * 1. 寻找能连接两个传动轴的配置
	 * 2. 寻找能连接至少一个传动轴的配置
	 * 3. 如果无法连接，则根据玩家朝向设置默认配置
	 * 
	 * @param context 方块放置上下文，包含位置、玩家状态等信息
	 * @return 适合当前环境的方块状态，如果无法连接则返回默认状态
	 */

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();

		BlockState state = defaultBlockState();
		
		// 尝试找到能连接到现有传动轴的最佳配置
		for (Direction facing1 : FACING_1.getPossibleValues()) {
			// 检查第一个传动轴方向是否有效
			if (!isNeighborValid(level, pos, facing1)) continue;
			state = state.setValue(FACING_1, facing1);

			// 寻找第二个传动轴的有效方向
			for (Direction facing2 : FACING_2.getPossibleValues()) {
				if (!isNeighborValid(level, pos, getAbsolute(facing1, facing2))) continue;
				// 找到完美配置：两个传动轴都能连接
				return state.setValue(FACING_2, facing2);
			}

			// 如果只有第一个传动轴能连接，返回该配置
			return state;
		}

		// 无法连接到现有传动轴，使用玩家朝向的默认配置
		Direction facing1 = context.getHorizontalDirection();
		Player player = context.getPlayer();
		// 如果玩家蹲下，使用朝向方向；否则使用相反方向
		return state.setValue(FACING_1,
				player != null && player.isShiftKeyDown() ? facing1 : facing1.getOpposite());
	}

	/**
	 * 检查邻居方块是否为有效的传动连接
	 * <p>
	 * 验证指定方向的邻居方块是否可以与L型齿轮箱建立传动连接。
	 * 只有实现IRotate接口且朝向我们方向有传动轴的方块才被认为是有效邻居。
	 * 
	 * @param level 世界实例
	 * @param pos 当前方块位置
	 * @param direction 要检查的方向
	 * @return 如果邻居是有效的传动连接返回true，否则返回false
	 */

	private boolean isNeighborValid(Level level, BlockPos pos, Direction direction) {
		// 获取相邻方块的位置和状态
		BlockPos neighborPos = pos.relative(direction);
		BlockState neighborState = level.getBlockState(neighborPos);

		// 检查邻居是否为可旋转方块
		if (!(neighborState.getBlock() instanceof IRotate neighbor)) return false;
		// 检查邻居是否朝向我们的方向有传动轴
		return neighbor.hasShaftTowards(level, neighborPos, neighborState, direction.getOpposite());
	}

	/**
	 * 获取旋转轴
	 * <p>
	 * L型齿轮箱始终围绕Y轴旋转，这是水平面的垂直轴。
	 * 这确保了方块在旋转时保持正确的方向关系。
	 * 
	 * @param state 方块状态
	 * @return 始终返回Y轴
	 */

	@Override
	public Direction.Axis getRotationAxis(BlockState state) {
		return Direction.Axis.Y;
	}

	/**
	 * 检查是否朝向指定面有传动轴
	 * <p>
	 * 重载方法，委托给静态的hasShaftTowards方法进行实际检查。
	 * 
	 * @param world 世界实例
	 * @param pos 方块位置
	 * @param state 方块状态
	 * @param face 要检查的面
	 * @return 如果指定面有传动轴返回true
	 */

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return hasShaftTowards(state, face);
	}

	/**
	 * 静态方法：检查方块状态是否朝向指定面有传动轴
	 * <p>
	 * 核心逻辑方法，判断L型齿轮箱的两个传动轴中是否有任何一个朝向指定方向。
	 * 这个方法考虑了相对方向转换，确保正确识别传动轴位置。
	 * 
	 * @param state 方块状态，包含两个传动轴方向信息
	 * @param face 要检查的面方向
	 * @return 如果任一传动轴朝向该面返回true
	 */

	public static boolean hasShaftTowards(BlockState state, Direction face) {
		Direction facing1 = state.getValue(FACING_1);
		// 检查目标面是否与第一个传动轴或第二个传动轴（转换为绝对方向）匹配
		return face == facing1 || face == getAbsolute(facing1, state.getValue(FACING_2));
	}

	/**
	 * 获取旋转后的方块状态
	 * <p>
	 * 当方块被旋转工具操作时调用，循环切换第二个传动轴的方向。
	 * 这允许玩家在不破坏方块的情况下调整L型齿轮箱的配置。
	 * 
	 * @param state 当前方块状态
	 * @param targetedFace 被旋转工具指向的面（未使用，但保持接口一致性）
	 * @return 第二传动轴方向循环后的新状态
	 */

	@Override
	public BlockState getRotatedBlockState(BlockState state, Direction targetedFace) {
		// 循环切换第二个传动轴的方向
		return state.cycle(FACING_2);
	}

	/**
	 * 应用结构变换
	 * <p>
	 * 当L型齿轮箱作为机械装置的一部分被移动或旋转时调用。
	 * 此方法确保在结构变换后，两个传动轴保持正确的相对位置和连接。
	 * 
	 * 变换流程：
	 * 1. 获取当前两个传动轴的绝对方向
	 * 2. 应用变换旋转到两个方向
	 * 3. 处理第一轴变为垂直的特殊情况
	 * 4. 重新计算第二轴的相对方向
	 * 
	 * @param state 原始方块状态
	 * @param transform 要应用的结构变换（包含旋转轴和角度）
	 * @return 变换后的新方块状态
	 */

	@Override
	public BlockState transform(BlockState state, StructureTransform transform) {
		// 获取两个传动轴的当前绝对方向
		Direction facing1 = state.getValue(FACING_1);
		Direction facing2 = getAbsolute(facing1, state.getValue(FACING_2));

		// 应用变换旋转到两个方向
		facing1 = rotate(facing1, transform.rotationAxis, transform.rotation);
		facing2 = rotate(facing2, transform.rotationAxis, transform.rotation);

		// 特殊处理：如果第一轴变为垂直方向，交换两轴
		if (facing1 == Direction.UP || facing1 == Direction.DOWN) {
			Direction placeHolder = facing1;
			facing1 = facing2;
			facing2 = placeHolder;
		}

		// 将第二轴转换回相对于第一轴的相对方向
		facing2 = getRelative(facing1, facing2);

		return state.setValue(FACING_1, facing1).setValue(FACING_2, facing2);
	}

	/**
	 * 旋转方向向量
	 * <p>
	 * 根据指定的旋转轴和旋转角度，计算方向向量旋转后的新方向。
	 * 支持90度、180度、270度旋转，以及不旋转的情况。
	 * 
	 * @param direction 要旋转的原始方向
	 * @param axis 旋转轴
	 * @param rotation 旋转角度（枚举值）
	 * @return 旋转后的新方向
	 */

	public static Direction rotate(Direction direction, Direction.Axis axis, Rotation rotation) {
		return switch (rotation) {
			case CLOCKWISE_90 -> direction.getClockWise(axis);           // 顺时针90度
			case COUNTERCLOCKWISE_90 -> direction.getCounterClockWise(axis); // 逆时针90度
			case CLOCKWISE_180 -> axis == direction.getAxis() ? direction : direction.getOpposite(); // 180度旋转
			case NONE -> direction;  // 不旋转
		};
	}

	/**
	 * 获取绝对方向
	 * <p>
	 * 将相对于第一传动轴的第二传动轴方向转换为世界坐标系中的绝对方向。
	 * 这个转换基于第一传动轴的朝向来计算第二传动轴的实际世界方向。
	 * 
	 * @param direction1 第一传动轴的方向（参考方向）
	 * @param direction2 第二传动轴的相对方向
	 * @return 第二传动轴在世界坐标系中的绝对方向
	 */

	public static Direction getAbsolute(Direction direction1, Direction direction2) {
		// 基于第一方向的旋转来转换第二方向
		return rotate(direction2, Direction.Axis.Y, getRotation(direction1));
	}

	/**
	 * 获取相对方向
	 * <p>
	 * 将绝对世界方向转换为相对于第一传动轴的相对方向。
	 * 这是getAbsolute方法的逆操作，用于将绝对方向转换回相对方向以便存储。
	 * 
	 * @param direction1 第一传动轴的方向（参考方向）
	 * @param direction2 要转换的绝对方向
	 * @return 相对于第一传动轴的相对方向
	 */

	public static Direction getRelative(Direction direction1, Direction direction2) {
		// 应用逆旋转来获取相对方向
		return rotate(direction2, Direction.Axis.Y, getInverse(getRotation(direction1)));
	}

	/**
	 * 根据方向获取对应的旋转量
	 * <p>
	 * 将方向向量转换为相应的旋转量，用于坐标系变换计算。
	 * 以北方（NORTH）为基准方向，计算其他方向相对于北方的旋转角度。
	 * 
	 * @param direction1 要转换的方向
	 * @return 对应的旋转量枚举值
	 */

	public static Rotation getRotation(Direction direction1) {
		return switch (direction1) {
			case EAST -> Rotation.CLOCKWISE_90;        // 东方：顺时针90度
			case SOUTH -> Rotation.CLOCKWISE_180;      // 南方：180度
			case WEST -> Rotation.COUNTERCLOCKWISE_90; // 西方：逆时针90度
			default -> Rotation.NONE;                  // 北方及其他：无旋转
		};
	}

	/**
	 * 获取旋转的逆操作
	 * <p>
	 * 返回给定旋转的逆旋转，用于撤销之前的旋转变换。
	 * 顺时针旋转的逆操作是逆时针旋转，反之亦然。
	 * 
	 * @param rotation 原始旋转量
	 * @return 逆旋转量
	 */

	public static Rotation getInverse(Rotation rotation) {
		return switch (rotation) {
			case CLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;    // 顺时针90度 → 逆时针90度
			case COUNTERCLOCKWISE_90 -> Rotation.CLOCKWISE_90;    // 逆时针90度 → 顺时针90度
			default -> rotation;  // 180度旋转和无旋转的逆操作是自身
		};
	}

	/**
	 * 获取方块实体类
	 * <p>
	 * 返回与此方块关联的方块实体类型。L型齿轮箱使用标准的GearboxBlockEntity，
	 * 继承了Create模组齿轮箱的所有动力传输功能。
	 * 
	 * @return 齿轮箱方块实体的类对象
	 */

	@Override
	public Class<GearboxBlockEntity> getBlockEntityClass() {
		return GearboxBlockEntity.class;
	}

	/**
	 * 获取方块实体类型
	 * <p>
	 * 返回在CU模组中注册的L型齿轮箱方块实体类型。
	 * 这个类型定义了方块实体的创建和管理方式。
	 * 
	 * @return L型齿轮箱的方块实体类型注册对象
	 */
	@Override
	public BlockEntityType<GearboxBlockEntity> getBlockEntityType() {
		return CUTileEntities.LSHAPED_GEARBOX.get();
	}
}
