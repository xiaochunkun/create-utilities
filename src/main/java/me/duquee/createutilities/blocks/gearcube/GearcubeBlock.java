package me.duquee.createutilities.blocks.gearcube;

import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.gearbox.GearboxBlockEntity;
import com.simibubi.create.foundation.block.IBE;

import me.duquee.createutilities.blocks.CUTileEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * 齿轮立方体方块
 * 
 * 一个创新的多面齿轮传动装置，能够同时向六个方向传输动力。
 * 该方块是Create Utilities模组对传统齿轮箱概念的扩展和改进，
 * 提供了更加灵活和紧凑的机械传动解决方案。
 * 
 * 核心特性：
 * - 全向传动能力：支持向上下东西南北六个方向同时输出动力
 * - 紧凑设计：14×14×14像素的精巧体积，节省建造空间
 * - 高效传动：基于Y轴主旋转轴的统一传动系统
 * - 无损传输：动力在传输过程中不产生额外的能量损耗
 * - 即插即用：无需复杂配置，连接即可工作
 * 
 * 技术实现：
 * - 继承KineticBlock，获得完整的Create机械传动功能
 * - 实现IBE接口，使用GearboxBlockEntity管理传动逻辑
 * - 固定Y轴为主旋转轴，确保传动的一致性和稳定性
 * - 全方向轴连接，任意面都可以连接传动装置
 * - 使用Create的成熟齿轮箱实体，确保兼容性和稳定性
 * 
 * 设计理念：
 * - 简化复杂的传动网络布局
 * - 提供更多的设计自由度和创造空间
 * - 保持与Create模组原有风格的一致性
 * - 优化空间利用，适合紧凑型机械设计
 * 
 * 应用场景：
 * - 复杂机械装置的核心传动枢纽
 * - 多方向动力分配的集中控制点
 * - 紧凑空间内的高效传动解决方案
 * - 艺术性机械装置的关键组件
 * 
 * 与传统齿轮箱的区别：
 * - 更小的体积占用（14³ vs 16³）
 * - 支持更多连接方向（6方向全支持）
 * - 统一的旋转轴系统，简化了传动逻辑
 * - 更适合用作传动网络的分配节点
 * 
 * @author duquee
 */
public class GearcubeBlock extends KineticBlock implements IBE<GearboxBlockEntity> {

	/**
	 * 方块碰撞形状
	 * 
	 * 定义齿轮立方体的物理边界，采用14×14×14像素的紧凑设计。
	 * 相比标准方块16×16×16的尺寸，这种设计：
	 * - 留出边缘空间，便于管道和线缆的布置
	 * - 提供更精细的视觉效果和工业美感
	 * - 减少视觉上的笨重感，更适合精密机械装置
	 * - 与其他精密机械方块保持尺寸协调性
	 */
	private static final VoxelShape SHAPE = Block.box(1, 1, 1, 15, 15, 15);

	/**
	 * 构造函数
	 * 
	 * 初始化齿轮立方体方块，设置基础属性和物理特性。
	 * 
	 * @param properties 方块属性，定义硬度、抗性、工具需求等物理特性
	 */
	public GearcubeBlock(Properties properties) {
		super(properties);
	}

	/**
	 * 获取方块碰撞形状
	 * 
	 * 返回齿轮立方体的精确碰撞体积。这个较小的碰撞盒设计
	 * 不仅提供了更好的视觉效果，还为复杂的机械装置布局留出了空间。
	 * 
	 * @param state 方块状态（在此实现中未使用）
	 * @param level 世界/关卡实例
	 * @param pos 方块位置
	 * @param context 碰撞检测上下文
	 * @return 14×14×14像素的立方体形状
	 */
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	/**
	 * 获取旋转轴
	 * 
	 * 返回齿轮立方体的主旋转轴。固定使用Y轴作为主旋转轴的设计
	 * 基于以下考虑：
	 * 
	 * 技术优势：
	 * - 简化传动逻辑计算，提高性能
	 * - 与Minecraft的重力方向保持一致
	 * - 便于垂直传动塔的构建
	 * - 降低多轴传动的复杂性
	 * 
	 * 实用性：
	 * - 大多数机械装置采用垂直主轴设计
	 * - 与Create模组的其他传动组件兼容性更好
	 * - 简化了玩家的理解和使用成本
	 * 
	 * @param state 方块状态（在此实现中未使用）
	 * @return Y轴（垂直轴）
	 */
	@Override
	public Direction.Axis getRotationAxis(BlockState state) {
		return Direction.Axis.Y;
	}

	/**
	 * 检查是否向指定方向提供轴连接
	 * 
	 * 齿轮立方体的核心特性：全方向轴连接支持。
	 * 无论从哪个方向尝试连接，都返回true，这使得齿轮立方体
	 * 成为理想的多向传动枢纽。
	 * 
	 * 全向连接的优势：
	 * - 极大简化了复杂传动网络的设计
	 * - 消除了方向性约束，提供完全的设计自由
	 * - 减少了传动路径规划的复杂性
	 * - 支持任意角度和方向的机械连接
	 * 
	 * 应用场景：
	 * - 作为传动网络的中央分配器
	 * - 连接不同平面上的机械装置
	 * - 构建复杂的三维传动结构
	 * - 实现紧凑的多向动力输出
	 * 
	 * @param world 世界读取器（在此实现中未使用）
	 * @param pos 方块位置（在此实现中未使用）
	 * @param state 方块状态（在此实现中未使用）
	 * @param face 检查的连接方向（在此实现中未使用）
	 * @return 始终返回true，表示支持全方向连接
	 */
	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return true; // 支持全方向轴连接
	}

	/**
	 * 获取方块实体类型
	 * 
	 * 返回与此方块关联的方块实体类。齿轮立方体重用了Create模组
	 * 成熟的GearboxBlockEntity，确保了：
	 * 
	 * 兼容性优势：
	 * - 与现有的Create机械系统完全兼容
	 * - 继承了所有经过验证的传动逻辑
	 * - 支持Create的高级功能（如应力系统、转速控制等）
	 * - 减少了潜在的bug和兼容性问题
	 * 
	 * 开发效率：
	 * - 避免重复开发相似功能
	 * - 利用Create团队的优化和bug修复
	 * - 保持代码的简洁性和可维护性
	 * 
	 * @return GearboxBlockEntity类
	 */
	@Override
	public Class<GearboxBlockEntity> getBlockEntityClass() {
		return GearboxBlockEntity.class;
	}

	/**
	 * 获取方块实体类型定义
	 * 
	 * 返回注册的方块实体类型，用于实例化和类型检查。
	 * 使用自定义的GEARCUBE类型而非Create原生类型，
	 * 这样可以：
	 * 
	 * 技术控制：
	 * - 在需要时进行特殊配置或优化
	 * - 独立管理齿轮立方体的注册和生命周期
	 * - 为未来的功能扩展预留空间
	 * - 避免与Create模组的内部更改产生冲突
	 * 
	 * 模块化设计：
	 * - 保持Create Utilities的独立性
	 * - 便于后续的功能扩展和维护
	 * - 支持模组特有的配置和行为定制
	 * 
	 * @return 齿轮立方体的注册方块实体类型
	 */
	@Override
	public BlockEntityType<GearboxBlockEntity> getBlockEntityType() {
		return CUTileEntities.GEARCUBE.get();
	}
}
