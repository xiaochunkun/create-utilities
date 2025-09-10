package me.duquee.createutilities.blocks.voidtypes.tank;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import net.createmod.catnip.platform.ForgeCatnipServices;
import me.duquee.createutilities.blocks.voidtypes.VoidTileRenderer;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/**
 * 虚空储罐渲染器
 * 
 * 负责虚空储罐方块的客户端3D渲染，包括流体内容显示和虚空框架效果。
 * 该渲染器将抽象的无限容量储罐概念转化为直观的视觉表现。
 * 
 * 渲染功能：
 * - 虚空框架效果：显示神秘的虚空入口视觉效果
 * - 流体内容渲染：将存储的流体以3D形式显示在储罐内部
 * - 动态液面高度：根据储罐内流体比例计算液面位置
 * - 状态相关渲染：根据开关状态决定是否显示内容
 * - 多方向框架：支持不同面的虚空框架显示
 * 
 * 技术实现：
 * - 继承SmartBlockEntityRenderer，获得Create模组的智能渲染特性
 * - 实现VoidTileRenderer接口，提供虚空方块特有的渲染功能
 * - 集成Catnip流体渲染服务，确保流体显示的兼容性和性能
 * - 使用头颅模型基类，支持装饰性头颅显示
 * - 精确的坐标计算，确保渲染效果的准确性
 * 
 * 渲染逻辑：
 * 1. 首先渲染基础的虚空框架效果
 * 2. 检查储罐状态（开启/关闭，是否含有流体）
 * 3. 计算流体液面高度（基于容量比例）
 * 4. 使用Catnip服务渲染流体3D盒子
 * 5. 处理光照和透明度效果
 * 
 * 性能优化：
 * - 状态检查避免不必要的渲染操作
 * - 使用服务层抽象，减少直接API调用
 * - 智能框架渲染，只在需要时显示效果
 * 
 * @author duquee
 */
public class VoidTankRenderer
		extends SmartBlockEntityRenderer<VoidTankTileEntity>
		implements VoidTileRenderer<VoidTankTileEntity> {

	/**
	 * 头颅模型基类
	 * 用于渲染装饰性的头颅效果，增强虚空储罐的神秘视觉感受
	 */
	private final SkullModelBase skullModelBase;

	/**
	 * 构造函数
	 * 
	 * 初始化虚空储罐渲染器，设置必要的模型资源和渲染上下文。
	 * 
	 * @param context 方块实体渲染器上下文，提供模型集合和资源访问
	 */
	public VoidTankRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
		// 初始化头颅模型，用于装饰效果
		skullModelBase = new SkullModel(context.getModelSet().bakeLayer(ModelLayers.PLAYER_HEAD));
	}

	/**
	 * 安全渲染方法
	 * 
	 * 在安全的环境中执行虚空储罐的完整渲染过程。
	 * 该方法由Create框架调用，确保渲染异常不会导致游戏崩溃。
	 * 
	 * 渲染流程：
	 * 1. 渲染虚空框架效果（发光边框、粒子效果等）
	 * 2. 检查储罐状态和流体内容
	 * 3. 计算流体的3D显示区域
	 * 4. 使用Catnip流体渲染服务显示流体
	 * 5. 处理透明度和光照效果
	 * 
	 * 流体渲染细节：
	 * - X坐标：从0.125到0.875（占方块宽度的75%）
	 * - Z坐标：从0.125到0.875（占方块深度的75%）
	 * - Y坐标：从0.25开始，高度根据流体比例动态计算
	 * - 最大高度：0.75（0.25 + 0.5），留出顶部空间
	 * 
	 * @param te 虚空储罐方块实体，包含状态和流体数据
	 * @param partialTicks 帧间插值时间，用于平滑动画
	 * @param ms 姿态矩阵堆栈，用于坐标变换
	 * @param buffer 多重缓冲区源，管理渲染层
	 * @param light 光照等级
	 * @param overlay 覆盖层数据
	 */
	@Override
	protected void renderSafe(VoidTankTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		// 首先渲染虚空框架效果
		renderVoid(te, partialTicks, ms, buffer, light, overlay);

		// 获取流体储罐实例
		FluidTank tank = te.getFluidStorage();
		
		// 只有在储罐开启且包含流体时才渲染流体内容
		if (!te.isClosed() && !tank.isEmpty()) {
			// 使用Catnip流体渲染服务渲染流体盒子
			ForgeCatnipServices.FLUID_RENDERER.renderFluidBox(
					tank.getFluid(), // 流体栈，包含流体类型和数量
					.125F, .25F, .125F, // 起始坐标 (x1, y1, z1)
					.875F, .25F + 0.5F * tank.getFluidAmount()/tank.getCapacity(), .875F, // 结束坐标 (x2, y2, z2)
					// Y2坐标计算：基础高度0.25 + 动态高度0.5 * 流体比例
					buffer, ms, light, 
					false, // 不使用默认纹理
					true // 启用流体动画效果
			);
		}
	}

	/**
	 * 获取头颅模型基类
	 * 
	 * 提供用于装饰性头颅渲染的模型基类。
	 * 虚空储罐可以显示玩家头像或其他头颅装饰，增强视觉效果。
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
	 * 对于虚空储罐，只有在储罐处于开启状态时才显示框架。
	 * 
	 * 设计理念：
	 * - 开启状态：显示虚空入口效果，表明储罐正在工作
	 * - 关闭状态：隐藏虚空效果，表明储罐已停用
	 * - 提供直观的视觉反馈，帮助玩家理解储罐状态
	 * 
	 * @param te 虚空储罐方块实体
	 * @param direction 检查的方向（参数存在但在此实现中未使用）
	 * @return 如果储罐未关闭则返回true
	 */
	@Override
	public boolean shouldRenderFrame(VoidTankTileEntity te, Direction direction) {
		return !te.isClosed();
	}

	/**
	 * 获取虚空框架的宽度
	 * 
	 * 定义虚空框架的渲染宽度，以方块单位表示。
	 * 0.75F表示框架占用方块宽度的75%，与内部流体渲染区域匹配。
	 * 
	 * @return 框架宽度，0.75F（3/4方块宽度）
	 */
	@Override
	public float getFrameWidth() {
		return 0.75f;
	}

	/**
	 * 获取虚空框架的偏移量
	 * 
	 * 定义框架相对于方块表面的偏移量，根据方向轴进行差异化处理。
	 * 
	 * 偏移策略：
	 * - Y轴方向（顶部/底部）：0.251F偏移，略高于流体起始高度
	 * - X/Z轴方向（侧面）：0.124F偏移，与侧面流体边界对齐
	 * 
	 * 这种差异化处理确保：
	 * - 顶部框架不与流体表面重叠
	 * - 侧面框架与流体边界完美对齐
	 * - 避免Z-fighting等渲染问题
	 * 
	 * @param direction 渲染方向
	 * @return 对应方向的框架偏移量
	 */
	@Override
	public float getFrameOffset(Direction direction) {
		return direction.getAxis() == Direction.Axis.Y ? 0.251f : 0.124f;
	}
}
