package me.duquee.createutilities.blocks.voidtypes.battery;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import me.duquee.createutilities.blocks.CUPartialsModels;
import me.duquee.createutilities.blocks.voidtypes.VoidTileRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

/**
 * 虚空电池渲染器
 * 
 * 负责虚空电池方块的客户端3D渲染，提供直观的能量存储状态显示。
 * 该渲染器通过动态表盘和虚空效果，将抽象的能量概念可视化。
 * 
 * 渲染组件：
 * - 虚空框架效果：展现虚空技术的神秘科技感
 * - 能量指示表盘：实时显示电池的能量存储状态
 * - 方向感知渲染：根据方块朝向调整表盘位置和角度
 * - 动态进度显示：表盘旋转角度直接反映能量百分比
 * 
 * 技术实现：
 * - 继承SafeBlockEntityRenderer，确保渲染稳定性
 * - 实现VoidTileRenderer接口，支持虚空系统特有的渲染功能
 * - 使用CachedBuffers进行高效的模型缓存和重用
 * - 集成JOML向量库，实现精确的3D坐标变换
 * - 支持头颅模型基类，可扩展装饰性显示功能
 * 
 * 表盘设计：
 * - 位置计算：基于方块朝向动态定位表盘
 * - 旋转逻辑：0-180度范围映射到0-100%能量状态
 * - 视觉反馈：通过表盘角度直观表示能量充满程度
 * - 实时更新：能量变化时表盘立即响应显示
 * 
 * 性能优化：
 * - 使用cached buffers减少模型重建开销
 * - 精确的渲染条件判断，避免不必要的渲染操作
 * - 不渲染虚空框架，专注于能量状态显示
 * - 简化的光照处理，保持良好的性能表现
 * 
 * @author duquee
 */
public class VoidBatteryRenderer extends SafeBlockEntityRenderer<VoidBatteryTileEntity> implements VoidTileRenderer<VoidBatteryTileEntity> {

	/**
	 * 头颅模型基类
	 * 用于支持装饰性头颅显示，为电池提供个性化外观选项
	 */
	private final SkullModelBase skullModelBase;

	/**
	 * 构造函数
	 * 
	 * 初始化虚空电池渲染器，设置必要的模型资源和渲染上下文。
	 * 
	 * @param context 方块实体渲染器上下文，提供模型集合和资源访问
	 */
	public VoidBatteryRenderer(BlockEntityRendererProvider.Context context) {
		// 初始化头颅模型，支持装饰性显示功能
		skullModelBase = new SkullModel(context.getModelSet().bakeLayer(ModelLayers.PLAYER_HEAD));
	}

	/**
	 * 安全渲染方法
	 * 
	 * 执行虚空电池的完整渲染过程，包括虚空效果和能量指示表盘。
	 * 该方法由Create框架调用，确保渲染异常不会影响游戏稳定性。
	 * 
	 * 渲染步骤：
	 * 1. 渲染虚空基础效果（如果启用）
	 * 2. 渲染能量指示表盘
	 * 3. 处理光照和透明度效果
	 * 
	 * @param te 虚空电池方块实体，包含能量状态数据
	 * @param partialTicks 帧间插值时间，用于平滑动画
	 * @param ms 姿态矩阵堆栈，用于坐标变换
	 * @param buffer 多重缓冲区源，管理渲染层
	 * @param light 光照等级
	 * @param overlay 覆盖层数据
	 */
	@Override
	protected void renderSafe(VoidBatteryTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		// 渲染虚空效果（如果需要的话）
		renderVoid(te, partialTicks, ms, buffer, light, overlay);
		
		// 渲染能量指示表盘
		renderDial(te, partialTicks, ms, buffer, light, overlay);
	}

	/**
	 * 渲染能量指示表盘
	 * 
	 * 绘制显示电池能量状态的动态表盘。表盘的旋转角度直接反映当前能量存储百分比，
	 * 为玩家提供直观的能量状态反馈。
	 * 
	 * 表盘渲染流程：
	 * 1. 获取方块状态和朝向信息
	 * 2. 计算能量存储百分比
	 * 3. 确定表盘的3D位置
	 * 4. 应用相应的旋转变换
	 * 5. 执行实际的几何体渲染
	 * 
	 * 位置计算逻辑：
	 * - 基础位置：方块中心 (0.5, 0.375, 0.5)
	 * - 方向偏移：根据朝向在对应方向上偏移0.625个单位
	 * - 这确保表盘显示在电池的"前面"
	 * 
	 * 旋转逻辑：
	 * - Y轴旋转：180° - 朝向角度，确保表盘正面朝向玩家
	 * - Z轴旋转：180° × 能量百分比，表盘指针位置表示能量状态
	 * 
	 * @param te 虚空电池方块实体
	 * @param partialTicks 帧间插值时间
	 * @param ms 姿态矩阵堆栈
	 * @param buffer 多重缓冲区源
	 * @param light 光照等级
	 * @param overlay 覆盖层数据
	 */
	protected void renderDial(VoidBatteryTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		
		// 获取方块状态和渲染上下文
		BlockState state = te.getBlockState();
		VertexConsumer vb = buffer.getBuffer(RenderType.solid());

		// 计算能量存储百分比
		VoidBattery battery = te.getBattery();
		float progress = (float) battery.getEnergyStored() / battery.getMaxEnergyStored();

		// 获取方块朝向，用于表盘定位
		Direction direction = state.getValue(VoidBatteryBlock.FACING);
		
		// 计算表盘的3D位置
		// 基础位置 + 朝向偏移，确保表盘显示在电池正面
		Vector3f vec = new Vector3f(.5f, .375f, .5f)
				.add(direction.step().mul(.625f));

		// 保存当前矩阵状态
		ms.pushPose();
		
		// 渲染表盘模型
		CachedBuffers.partial(CUPartialsModels.VOID_BATTERY_DIAL, state)
				.translate(vec) // 移动到计算的位置
				.rotateY(180 - direction.toYRot()) // Y轴旋转，面向玩家
				.rotateZ(180 * progress) // Z轴旋转，表示能量百分比
				.light(light) // 应用光照
				.renderInto(ms, vb); // 执行渲染
		
		// 恢复矩阵状态
		ms.popPose();
	}

	/**
	 * 获取头颅模型基类
	 * 
	 * 提供用于装饰性头颅渲染的模型基类。
	 * 虽然虚空电池通常不显示头颅，但保留此功能为未来的扩展留出空间。
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
	 * 虚空电池不显示虚空框架效果，因为其主要视觉焦点是能量指示表盘。
	 * 这样的设计选择有助于保持电池外观的简洁和专业。
	 * 
	 * @param te 虚空电池方块实体（未使用）
	 * @param direction 检查的方向（未使用）
	 * @return 始终返回false，不渲染虚空框架
	 */
	@Override
	public boolean shouldRenderFrame(VoidBatteryTileEntity te, Direction direction) {
		return false; // 电池不显示虚空框架
	}

	/**
	 * 获取虚空框架的宽度
	 * 
	 * 由于虚空电池不渲染框架，返回0表示无框架宽度。
	 * 
	 * @return 0.0F，表示无框架
	 */
	@Override
	public float getFrameWidth() {
		return .0F;
	}

	/**
	 * 获取虚空框架的偏移量
	 * 
	 * 由于虚空电池不渲染框架，返回0表示无偏移量。
	 * 
	 * @param direction 渲染方向（未使用）
	 * @return 0.0F，表示无偏移
	 */
	@Override
	public float getFrameOffset(Direction direction) {
		return .0F;
	}
}
