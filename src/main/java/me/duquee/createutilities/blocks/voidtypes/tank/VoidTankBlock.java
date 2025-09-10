package me.duquee.createutilities.blocks.voidtypes.tank;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.FluidHelper;

import me.duquee.createutilities.blocks.CUTileEntities;
import me.duquee.createutilities.blocks.voidtypes.VoidLinkBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/**
 * 虚空储罐方块
 * 
 * 一个具有无限容量的虚空流体储存设备，能够存储任意数量的流体而不占用物理空间。
 * 该方块是虚空存储系统的流体存储组件，为自动化流体处理提供了革命性的解决方案。
 * 
 * 核心特性：
 * - 无限流体存储容量，突破了传统储罐的限制
 * - 虚空链接系统，允许跨维度的流体共享
 * - 开关控制机制，可通过扳手切换开启/关闭状态
 * - 创造模式流体交互，支持直接的流体输入输出操作
 * - 自动化兼容性，与Create模组的流体传输系统完美集成
 * 
 * 技术实现：
 * - 实现IWrenchable接口，支持Create扳手的交互操作
 * - 实现IBE接口，提供方块实体的生命周期管理
 * - 使用BooleanProperty状态管理开关状态，确保状态持久化
 * - 集成FluidHelper工具类，提供标准化的流体操作接口
 * - 支持GenericItemEmptying和GenericItemFilling，兼容多种流体容器
 * 
 * 工作机制：
 * - 放置时自动绑定玩家为所有者，确保访问权限控制
 * - 开启状态下显示虚空入口效果，提供视觉反馈
 * - 关闭状态下隐藏内部内容，节省渲染性能
 * - 创造模式下支持直接的流体桶类物品交互
 * 
 * @author duquee
 */
public class VoidTankBlock extends Block implements IWrenchable, IBE<VoidTankTileEntity> {

	/**
	 * 储罐关闭状态属性
	 * 
	 * 控制虚空储罐是否处于关闭状态的布尔属性。
	 * - true: 储罐关闭，隐藏虚空效果，停止流体传输
	 * - false: 储罐开启，显示虚空效果，允许流体传输
	 * 
	 * 这个状态影响：
	 * - 渲染效果的显示与隐藏
	 * - 流体输入输出的启用与禁用
	 * - 自动化系统的连接状态
	 */
	public static final BooleanProperty CLOSED = BooleanProperty.create("closed");

	/**
	 * 构造函数
	 * 
	 * 初始化虚空储罐方块，设置默认状态和基础属性。
	 * 
	 * @param properties 方块属性，定义硬度、抗性、工具需求等物理特性
	 */
	public VoidTankBlock(Properties properties) {
		super(properties);
		// 设置默认状态为未关闭（开启状态），允许立即使用
		registerDefaultState(defaultBlockState().setValue(CLOSED, false));
	}

	/**
	 * 创建方块状态定义
	 * 
	 * 定义此方块可能具有的所有状态属性，用于状态管理和数据存储。
	 * 
	 * @param builder 状态定义构建器，用于注册状态属性
	 */
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(CLOSED);
	}

	/**
	 * 方块放置时的回调
	 * 
	 * 在虚空储罐被放置到世界中时执行初始化操作。
	 * 主要用于设置虚空链接行为和绑定所有者信息。
	 * 
	 * 执行流程：
	 * 1. 检查是否为服务器端，避免重复操作
	 * 2. 获取虚空链接行为实例
	 * 3. 如果放置者是玩家，则设置为所有者
	 * 
	 * @param worldIn 放置的世界实例
	 * @param pos 放置位置的坐标
	 * @param state 放置时的方块状态
	 * @param placer 放置方块的生物实体
	 * @param stack 用于放置的物品栈
	 */
	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		if (worldIn.isClientSide()) return; // 只在服务器端执行
		VoidLinkBehaviour behaviour = BlockEntityBehaviour.get(worldIn, pos, VoidLinkBehaviour.TYPE);
		if (placer instanceof Player player) behaviour.setOwner(player.getGameProfile());
	}

	/**
	 * 扳手交互处理
	 * 
	 * 处理Create扳手对虚空储罐的操作，主要用于切换开关状态。
	 * 使用扳手右键点击可以在开启和关闭状态之间切换。
	 * 
	 * 状态切换效果：
	 * - 从开启切换到关闭：隐藏虚空效果，暂停流体传输
	 * - 从关闭切换到开启：显示虚空效果，恢复流体传输
	 * 
	 * @param state 当前方块状态
	 * @param context 使用上下文，包含位置、玩家、世界等信息
	 * @return 交互结果，SUCCESS表示操作成功
	 */
	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		// 切换CLOSED状态并更新方块
		context.getLevel().setBlockAndUpdate(context.getClickedPos(), state.setValue(CLOSED, !state.getValue(CLOSED)));
		return InteractionResult.SUCCESS;
	}

	/**
	 * 玩家右键交互处理
	 * 
	 * 处理玩家使用流体容器与虚空储罐的交互操作。
	 * 主要支持创造模式下的直接流体传输功能。
	 * 
	 * 交互流程：
	 * 1. 检查玩家手持物品是否为空
	 * 2. 验证玩家是否为创造模式
	 * 3. 获取储罐的流体存储实例
	 * 4. 尝试执行流体传输操作（输入或输出）
	 * 5. 处理通用物品的流体填充/清空操作
	 * 
	 * 支持的操作：
	 * - 从流体桶向储罐倒入流体
	 * - 从储罐向空桶填充流体
	 * - 兼容通用流体容器物品
	 * 
	 * @param state 当前方块状态
	 * @param world 世界实例
	 * @param pos 方块位置
	 * @param player 交互的玩家
	 * @param hand 使用的手（主手或副手）
	 * @param hit 击中结果，包含精确的交互位置
	 * @return 交互结果
	 */
	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		
		ItemStack heldItem = player.getItemInHand(hand);

		// 基础检查：物品不能为空，玩家必须是创造模式
		if (heldItem.isEmpty()) return InteractionResult.PASS;
		if (!player.isCreative()) return InteractionResult.PASS;

		FluidHelper.FluidExchange exchange = null;
		if (!(world.getBlockEntity(pos) instanceof VoidTankTileEntity te)) return InteractionResult.FAIL;

		// 获取储罐的流体存储能力
		FluidTank fluidTank = te.getFluidStorage();
		if (fluidTank == null) return InteractionResult.PASS;

		// 尝试流体传输操作
		if (FluidHelper.tryEmptyItemIntoBE(world, player, hand, heldItem, te)){
			// 从物品向储罐输入流体
			exchange = FluidHelper.FluidExchange.ITEM_TO_TANK;
		} else if (FluidHelper.tryFillItemFromBE(world, player, hand, heldItem, te))
			// 从储罐向物品输出流体
			exchange = FluidHelper.FluidExchange.TANK_TO_ITEM;

		// 处理无法直接传输的情况
		if (exchange == null) {
			// 检查是否为可处理的流体容器
			if (GenericItemEmptying.canItemBeEmptied(world, heldItem) || GenericItemFilling.canItemBeFilled(world, heldItem))
				return InteractionResult.SUCCESS;
			return InteractionResult.PASS;
		}

		return InteractionResult.SUCCESS;
	}

	/**
	 * 获取方块实体类型
	 * 
	 * 返回与此方块关联的方块实体类，用于Create模组的IBE系统。
	 * 
	 * @return VoidTankTileEntity类
	 */
	@Override
	public Class<VoidTankTileEntity> getBlockEntityClass() {
		return VoidTankTileEntity.class;
	}

	/**
	 * 获取方块实体类型定义
	 * 
	 * 返回注册的方块实体类型，用于实例化和类型检查。
	 * 
	 * @return 虚空储罐方块实体类型
	 */
	@Override
	public BlockEntityType<? extends VoidTankTileEntity> getBlockEntityType() {
		return CUTileEntities.VOID_TANK.get();
	}
}
