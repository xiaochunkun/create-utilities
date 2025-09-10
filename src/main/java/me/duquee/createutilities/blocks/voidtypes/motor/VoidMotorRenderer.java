package me.duquee.createutilities.blocks.voidtypes.motor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import me.duquee.createutilities.blocks.voidtypes.VoidTileRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 虚空马达渲染器
 * 
 * 负责虚空马达方块的客户端3D渲染，结合了Create机械传动的动态效果和虚空技术的神秘外观。
 * 该渲染器将强大的动力输出能力通过视觉效果传达给玩家，展现虚空科技的先进性。
 * 
 * 渲染组件：
 * - 机械传动轴：显示旋转的传动轴，表现动力输出状态
 * - 虚空框架效果：在输出面显示虚空入口，强调无限动力来源
 * - 头颅装饰支持：可显示装饰性头颅，增强个性化外观
 * - 方向感知渲染：根据马达朝向调整各组件的显示位置和角度
 * 
 * 技术实现：
 * - 继承KineticBlockEntityRenderer，获得完整的Create机械渲染功能
 * - 实现VoidTileRenderer接口，支持虚空系统特有的渲染效果
 * - 使用CachedBuffers进行高效的模型缓存和重用
 * - 集成Create的PartialModels系统，确保视觉风格的一致性
 * - 支持动态旋转动画，反映马达的工作状态
 * 
 * 渲染特性：
 * - 双重渲染层：机械层（传动轴）+ 虚空层（框架效果）
 * - 精确定位：框架效果仅在输出面显示，避免视觉混乱
 * - 高性能优化：使用缓存的模型数据，减少GPU负载
 * - 兼容性良好：与Create模组的其他机械方块完美配合
 * 
 * 设计理念：
 * - 功能表达：通过视觉效果清晰表达马达的功能和状态
 * - 风格统一：保持与Create模组整体风格的协调性
 * - 性能平衡：在视觉效果和性能之间找到最佳平衡点
 * - 可扩展性：支持未来的视觉效果扩展和自定义
 * 
 * @author duquee
 */
public class VoidMotorRenderer extends KineticBlockEntityRenderer<VoidMotorTileEntity> implements VoidTileRenderer<VoidMotorTileEntity> {

	/**
	 * 头颅模型基类
	 * 用于支持装饰性头颅显示，为马达提供个性化外观选项
	 */
	private final SkullModelBase skullModelBase;

	/**
	 * 构造函数
	 * 
	 * 初始化虚空马达渲染器，设置必要的模型资源和渲染上下文。
	 * 
	 * @param context 方块实体渲染器上下文，提供模型集合和资源访问
	 */
	public VoidMotorRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
		// 初始化头颅模型，支持装饰性显示功能
		skullModelBase = new SkullModel(context.getModelSet().bakeLayer(ModelLayers.PLAYER_HEAD));
	}

	/**
	 * 安全渲染方法
	 * 
	 * 执行虚空马达的完整渲染过程，包括机械传动效果和虚空框架。
	 * 该方法由Create框架调用，确保渲染异常不会影响游戏稳定性。
	 * 
	 * 渲染层次结构：
	 * 1. 机械层：调用父类方法渲染传动轴和机械组件
	 * 2. 虚空层：渲染虚空框架效果，突出无限动力特性
	 * 3. 装饰层：处理头颅等装饰性元素（如果存在）
	 * 
	 * 渲染优化：
	 * - 分层渲染，减少不必要的状态切换
	 * - 利用父类的机械渲染经验，避免重复开发
	 * - 虚空效果作为叠加层，不影响基础机械功能
	 * 
	 * @param te 虚空马达方块实体，包含状态和配置数据
	 * @param partialTicks 帧间插值时间，用于平滑动画
	 * @param ms 姿态矩阵堆栈，用于坐标变换
	 * @param buffer 多重缓冲区源，管理渲染层
	 * @param light 光照等级
	 * @param overlay 覆盖层数据
	 */
	@Override
	protected void renderSafe(VoidMotorTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		// 首先渲染机械传动部分
		super.renderSafe(te, partialTicks, ms, buffer, light, overlay);
		
		// 然后渲染虚空效果
		renderVoid(te, partialTicks, ms, buffer, light, overlay);
	}

	/**
	 * 获取头颅模型基类
	 * 
	 * 提供用于装饰性头颅渲染的模型基类。
	 * 虚空马达可以显示玩家头像或其他头颅装饰，增强个性化效果。
	 * 
	 * @return 头颅模型基类实例
	 */
	@Override
	public SkullModelBase getSkullModelBase() {
		return skullModelBase;
	}

	/**
	 * 判断是否应该渲染虚空框架
	 * 
	 * 决定在指定方向上是否显示虚空框架效果。
	 * 对于虚空马达，只在输出面（朝向方向）显示框架，这样设计的原因：
	 * 
	 * 功能性考虑：
	 * - 框架表示动力的"虚空来源"
	 * - 只在输出面显示，直观表达动力流向
	 * - 避免在非功能面显示，减少视觉噪音
	 * 
	 * 视觉设计：
	 * - 清晰的方向指示，帮助玩家理解连接逻辑
	 * - 与Create模组的"输入-输出"设计理念一致
	 * - 突出马达的主要功能面
	 * 
	 * @param te 虚空马达方块实体
	 * @param direction 检查的方向
	 * @return 如果是马达朝向方向则返回true
	 */
	@Override
	public boolean shouldRenderFrame(VoidMotorTileEntity te, Direction direction) {
		return te.getBlockState().getValue(VoidMotorBlock.FACING) == direction;
	}

	/**
	 * 获取虚空框架的宽度
	 * 
	 * 定义虚空框架的渲染宽度，以方块单位表示。
	 * 0.375F（3/8方块宽度）的设计考虑：
	 * 
	 * 比例协调：
	 * - 与传动轴的尺寸相匹配
	 * - 不会遮挡重要的机械细节
	 * - 保持整体视觉平衡
	 * 
	 * 功能表达：
	 * - 足够显眼，表达虚空入口的概念
	 * - 不过于突兀，保持工业风格
	 * - 与其他虚空方块的框架尺寸保持一致性
	 * 
	 * @return 框架宽度，0.375F（3/8方块宽度）
	 */
	@Override
	public float getFrameWidth() {
		return .375F;
	}

	/**
	 * 获取虚空框架的偏移量
	 * 
	 * 定义框架相对于方块表面的偏移量。
	 * 0.876F的较大偏移值设计考虑：
	 * 
	 * 机械集成：
	 * - 确保框架不与传动轴几何体重叠
	 * - 给传动轴的旋转留出足够空间
	 * - 避免与连接的机械设备产生视觉冲突
	 * 
	 * 视觉效果：
	 * - 框架显示在马达"前端"，表示动力输出口
	 * - 足够的偏移产生立体感和深度感
	 * - 与整体机械美学保持协调
	 * 
	 * @param direction 渲染方向（在此实现中未直接使用）
	 * @return 框架偏移量，0.876F
	 */
	@Override
	public float getFrameOffset(Direction direction) {
		return .876F;
	}

	/**
	 * 获取旋转模型
	 * 
	 * 返回用于动态旋转渲染的模型缓冲区。这个方法是Create机械渲染系统的核心，
	 * 负责提供能够表现马达工作状态的旋转组件。
	 * 
	 * 模型选择：
	 * - 使用AllPartialModels.SHAFT_HALF：半轴模型
	 * - 符合马达作为动力源的设计定位
	 * - 与Create模组的其他马达保持视觉一致性
	 * 
	 * 缓存机制：
	 * - 使用CachedBuffers.partialFacing进行高效缓存
	 * - 根据方块状态自动调整模型朝向
	 * - 减少重复的几何体计算开销
	 * 
	 * 渲染效果：
	 * - 传动轴会根据马达转速进行旋转动画
	 * - 直观表现马达的工作状态和动力输出
	 * - 与连接的机械设备形成连贯的动画系统
	 * 
	 * @param te 虚空马达方块实体
	 * @param state 方块状态，包含朝向等信息
	 * @return 配置好的旋转模型缓冲区
	 */
	@Override
	protected SuperByteBuffer getRotatedModel(VoidMotorTileEntity te, BlockState state) {
		return CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state);
	}
}
