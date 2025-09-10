package me.duquee.createutilities.blocks.voidtypes.chest;

import com.simibubi.create.content.equipment.wrench.IWrenchable;

import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import me.duquee.createutilities.blocks.CUTileEntities;
import me.duquee.createutilities.blocks.voidtypes.VoidLinkBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

/**
 * 虚空箱子方块
 * 
 * 这是虚空存储系统中的物品存储方块实现，提供了跨维度、远距离的物品存储和共享功能。
 * 虚空箱子通过虚空链接机制实现了多个箱子间的库存同步，使玩家可以在不同位置
 * 访问同一个共享库存空间。
 * 
 * 核心功能特性：
 * - 虚空物品存储：基于虚空链接键的共享库存系统
 * - 多方向放置：支持水平四个方向的朝向设置
 * - 防水淹没：实现SimpleWaterloggedBlock接口，可在水中正常工作
 * - 扳手交互：支持Create模组的扳手工具进行配置和拆除
 * - 玩家所有权：放置时自动绑定玩家身份，确保安全访问
 * 
 * 技术实现：
 * - 继承HorizontalDirectionalBlock实现方向性
 * - 使用VoidLinkBehaviour管理虚空连接逻辑
 * - 集成Forge容器系统提供GUI交互
 * - 自定义碰撞箱提供独特的视觉效果
 * 
 * 设计模式：
 * - 策略模式：通过VoidLinkBehaviour封装连接策略
 * - 观察者模式：与TileEntity协作处理状态变化
 * - 工厂模式：通过IBE接口创建对应的TileEntity
 * 
 * @author duquee
 * @since 1.0.0
 */
public class VoidChestBlock extends HorizontalDirectionalBlock implements IWrenchable, SimpleWaterloggedBlock, IBE<VoidChestTileEntity> {
	
	/**
	 * 虚空箱子的自定义碰撞箱形状
	 * 
	 * 定义了一个稍小于标准方块的形状，从(1,0,1)到(15,14,15)：
	 * - X轴：1-15个像素（留出两侧各1像素的空隙）
	 * - Y轴：0-14个像素（高度比标准方块低2像素）
	 * - Z轴：1-15个像素（留出前后各1像素的空隙）
	 * 
	 * 这种设计让虚空箱子看起来更加精致，同时在视觉上区别于标准箱子。
	 * 较低的高度也暗示了其"虚空"的特殊属性。
	 */
	private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 14, 15);

	/**
	 * 构造虚空箱子方块
	 * 
	 * 初始化方块的基础属性并设置默认状态。
	 * 默认状态包括未被水淹没的状态，确保方块在放置时具有正确的初始属性。
	 * 
	 * @param properties 方块属性，包括材质、硬度、抗爆性等基础特性
	 */
	public VoidChestBlock(Properties properties) {
		super(properties);
		// 设置默认状态：未被水淹没
		registerDefaultState(defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));
	}

	/**
	 * 获取方块的碰撞箱形状
	 * 
	 * 返回虚空箱子的自定义形状，这个形状用于：
	 * - 玩家与方块的碰撞检测
	 * - 实体移动时的路径规划
	 * - 渲染时的边界框显示
	 * - 光照和阴影的计算
	 * 
	 * @param state 方块状态
	 * @param level 世界实例
	 * @param pos 方块位置
	 * @param context 碰撞上下文
	 * @return 虚空箱子的体素形状
	 */
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	/**
	 * 获取方块的流体状态
	 * 
	 * 根据方块状态中的WATERLOGGED属性返回相应的流体状态。
	 * 这是实现防水淹没功能的关键方法：
	 * - 如果被水淹没，返回水的源方块状态
	 * - 如果未被淹没，返回空气状态
	 * 
	 * 这允许虚空箱子在水中正常放置和工作，提高了使用的灵活性。
	 * 
	 * @param state 当前方块状态
	 * @return 对应的流体状态
	 */
	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
	}

	/**
	 * 处理方块形状更新
	 * 
	 * 当相邻方块发生变化时调用此方法，主要用于处理水流的物理特性。
	 * 如果当前方块被水淹没，则安排水的tick更新以保持水流的正确行为。
	 * 
	 * 这确保了虚空箱子在水中的表现与原版水淹没方块保持一致，
	 * 包括水流的传播、压力计算等物理特性。
	 * 
	 * @param state 当前方块状态
	 * @param direction 更新来源方向
	 * @param neighbourState 相邻方块的新状态
	 * @param world 世界访问器
	 * @param pos 当前方块位置
	 * @param neighbourPos 相邻方块位置
	 * @return 更新后的方块状态
	 */
	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor world, BlockPos pos, BlockPos neighbourPos) {
		if (state.getValue(WATERLOGGED)) 
			world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		return state;
	}

	/**
	 * 创建方块状态定义
	 * 
	 * 定义这个方块支持的所有状态属性：
	 * - WATERLOGGED：是否被水淹没
	 * - FACING：方块的朝向（从父类HorizontalDirectionalBlock继承）
	 * 
	 * 这些状态属性决定了方块在世界中的表现和行为，
	 * 包括视觉渲染、物理特性和交互逻辑。
	 * 
	 * @param builder 状态定义构建器
	 */
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder.add(WATERLOGGED).add(FACING));
	}

	/**
	 * 确定方块放置时的状态
	 * 
	 * 根据放置上下文计算方块的初始状态：
	 * 1. 朝向设置：面向玩家放置方向的相反方向（箱子开口面向玩家）
	 * 2. 水淹没状态：检测放置位置是否有水并相应设置WATERLOGGED属性
	 * 
	 * 这种朝向逻辑确保了箱子的开口总是面向放置它的玩家，
	 * 符合玩家的使用习惯和视觉预期。
	 * 
	 * @param context 方块放置上下文，包含放置位置、玩家朝向等信息
	 * @return 确定的方块状态
	 */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		FluidState ifluidstate = context.getLevel()
				.getFluidState(context.getClickedPos());
		return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection()
						.getOpposite())
				.setValue(WATERLOGGED, Boolean.valueOf(ifluidstate.getType() == Fluids.WATER));
	}

	/**
	 * 方块放置完成后的处理
	 * 
	 * 在方块成功放置到世界后执行的逻辑：
	 * 1. 检查是否在客户端（客户端跳过处理避免重复）
	 * 2. 获取VoidLinkBehaviour实例
	 * 3. 如果放置者是玩家，则设置方块的所有者
	 * 
	 * 所有者设置的重要性：
	 * - 安全控制：防止其他玩家随意访问
	 * - 权限管理：为后续的访问控制提供基础
	 * - 审计跟踪：记录方块的创建者信息
	 * 
	 * @param worldIn 放置的世界实例
	 * @param pos 放置位置
	 * @param state 放置的方块状态
	 * @param placer 放置方块的实体（通常是玩家）
	 * @param stack 用于放置的物品堆
	 */
	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (worldIn.isClientSide()) return;
		VoidLinkBehaviour behaviour = BlockEntityBehaviour.get(worldIn, pos, VoidLinkBehaviour.TYPE);
		if (placer instanceof Player player) behaviour.setOwner(player.getGameProfile());
	}

	/**
	 * 处理玩家右键交互
	 * 
	 * 实现虚空箱子的GUI开启逻辑：
	 * 1. 客户端直接返回成功，避免重复处理
	 * 2. 服务端通过NetworkHooks开启容器界面
	 * 3. 使用TileEntity的sendToMenu方法发送必要的同步数据
	 * 
	 * 交互流程：
	 * - 玩家右键方块
	 * - 服务端验证权限和状态
	 * - 打开虚空箱子的GUI界面
	 * - 同步库存数据到客户端
	 * 
	 * 这种设计确保了GUI的正确打开和数据同步，
	 * 同时遵循Minecraft的客户端-服务端架构原则。
	 * 
	 * @param state 方块状态
	 * @param level 世界实例
	 * @param pos 方块位置
	 * @param player 交互的玩家
	 * @param hand 使用的手（主手/副手）
	 * @param hit 射线追踪结果
	 * @return 交互结果（成功/失败/继续传递等）
	 */
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide) return InteractionResult.SUCCESS;
		withBlockEntityDo(level, pos, voidChest ->
				NetworkHooks.openScreen((ServerPlayer) player, voidChest, voidChest::sendToMenu)
		);
		return InteractionResult.SUCCESS;
	}

	/**
	 * 获取方块实体类的Class对象
	 * 
	 * IBE接口的实现方法，用于类型安全和反射操作。
	 * 返回与此方块关联的TileEntity类型。
	 * 
	 * @return VoidChestTileEntity的Class对象
	 */
	@Override
	public Class<VoidChestTileEntity> getBlockEntityClass() {
		return VoidChestTileEntity.class;
	}

	/**
	 * 获取方块实体类型
	 * 
	 * IBE接口的实现方法，返回在注册表中注册的BlockEntityType。
	 * 这个类型用于Minecraft内部的方块实体创建和管理。
	 * 
	 * 方块实体类型的作用：
	 * - 序列化和反序列化
	 * - 网络同步
	 * - 渲染管理
	 * - 生命周期控制
	 * 
	 * @return 虚空箱子的方块实体类型
	 */
	@Override
	public BlockEntityType<? extends VoidChestTileEntity> getBlockEntityType() {
		return CUTileEntities.VOID_CHEST.get();
	}

}
