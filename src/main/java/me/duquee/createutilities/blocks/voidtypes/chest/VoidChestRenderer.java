package me.duquee.createutilities.blocks.voidtypes.chest;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import me.duquee.createutilities.blocks.CUPartialsModels;
import me.duquee.createutilities.blocks.voidtypes.VoidTileRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 虚空箱子渲染器
 * 
 * 负责处理虚空箱子方块的客户端3D渲染，包括：
 * - 箱子盖子的开关动画
 * - 虚空框架效果的渲染
 * - 玩家头颅模型的显示支持
 * 
 * 继承自Create的SafeBlockEntityRenderer，确保渲染过程的安全性和稳定性。
 * 实现VoidTileRenderer接口，提供虚空方块特有的渲染功能。
 * 
 * 技术特点：
 * - 使用Catnip的CachedBuffers进行高效的模型缓存
 * - 支持流畅的盖子旋转动画（基于方块状态的角度插值）
 * - 动态计算朝向，确保箱子在任何旋转角度下都能正确渲染
 * - 集成虚空框架渲染系统，提供视觉上的科技感效果
 * 
 * @author duquee
 */
public class VoidChestRenderer extends SafeBlockEntityRenderer<VoidChestTileEntity> implements VoidTileRenderer<VoidChestTileEntity> {

	/**
	 * 玩家头颅模型基类
	 * 用于在虚空箱子上显示头颅装饰效果
	 */
	private final SkullModelBase skullModelBase;

	/**
	 * 构造函数
	 * 
	 * 初始化渲染器并设置必要的模型资源。
	 * 
	 * @param context 方块实体渲染器提供的上下文，包含模型集合和资源访问器
	 */
	public VoidChestRenderer(BlockEntityRendererProvider.Context context) {
		skullModelBase = new SkullModel(context.getModelSet().bakeLayer(ModelLayers.PLAYER_HEAD));
	}

	/**
	 * 安全渲染方法
	 * 
	 * 在安全的环境中进行虚空箱子的渲染，包括虚空效果和箱子盖子动画。
	 * 该方法由Create框架调用，确保渲染过程不会因异常而崩溃游戏。
	 * 
	 * 渲染流程：
	 * 1. 调用虚空渲染器绘制基础框架效果
	 * 2. 获取方块状态和朝向信息
	 * 3. 渲染箱子盖子并应用开关动画
	 * 4. 处理光照和纹理坐标
	 * 
	 * @param te 虚空箱子方块实体，包含状态数据
	 * @param partialTicks 帧间插值时间，用于平滑动画
	 * @param ms 姿态矩阵堆栈，用于坐标变换
	 * @param buffer 多重缓冲区源，用于渲染层管理
	 * @param light 光照等级
	 * @param overlay 覆盖层数据
	 */
	@Override
	protected void renderSafe(VoidChestTileEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		// 首先渲染虚空框架效果
		renderVoid(te, partialTicks, ms, buffer, light, overlay);

		// 获取方块状态和朝向
		BlockState blockState = te.getBlockState();
		Direction facing = blockState.getValue(VoidChestBlock.FACING).getOpposite();

		// 获取箱子盖子模型并设置动画状态
		SuperByteBuffer lid = CachedBuffers.partial(CUPartialsModels.VOID_CHEST_LID, blockState);
		float lidAngle = te.lid.getValue(partialTicks); // 获取插值后的盖子角度

		// 设置渲染类型为cutoutMipped，支持透明纹理
		VertexConsumer builder = buffer.getBuffer(RenderType.cutoutMipped());
		lid.center() // 将模型中心化
				.rotateYDegrees(-facing.toYRot()) // 根据方块朝向进行Y轴旋转
				.uncenter() // 取消中心化
				.translate(0, 10 / 16f, 15 / 16f) // 移动到铰链位置
				.rotateXDegrees(135 * lidAngle) // 应用盖子开合角度（135度为最大开启角度）
				.translate(0, -10 / 16f, -15 / 16f) // 恢复到原始位置
				.light(light) // 应用光照
				.renderInto(ms, builder); // 执行渲染
	}

	/**
	 * 获取头颅模型基类
	 * 
	 * 提供用于渲染玩家头颅装饰的模型基类。
	 * 这允许虚空箱子显示玩家头像或其他头颅类型的装饰效果。
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
	 * 决定在特定方向上是否显示虚空框架效果。
	 * 对于虚空箱子，只有在顶部方向且箱子处于打开状态时才显示框架。
	 * 
	 * 设计理念：
	 * - 只在箱子开启时显示虚空入口效果
	 * - 框架只在顶部显示，符合箱子的使用逻辑
	 * - 提供视觉反馈，表明箱子的虚空连接状态
	 * 
	 * @param te 虚空箱子方块实体
	 * @param direction 检查的方向
	 * @return 如果是顶部方向且箱子未关闭则返回true
	 */
	@Override
	public boolean shouldRenderFrame(VoidChestTileEntity te, Direction direction) {
		return direction == Direction.UP && !te.isClosed();
	}

	/**
	 * 获取虚空框架的宽度
	 * 
	 * 定义虚空框架的渲染宽度，以方块单位表示。
	 * 0.625F意味着框架占用方块宽度的62.5%，留有边距以获得更好的视觉效果。
	 * 
	 * @return 框架宽度，0.625F（5/8方块宽度）
	 */
	@Override
	public float getFrameWidth() {
		return .625F;
	}

	/**
	 * 获取虚空框架的偏移量
	 * 
	 * 定义框架相对于方块表面的垂直偏移量。
	 * 0.626F的偏移确保框架略微突出方块表面，避免Z-fighting闪烁问题。
	 * 
	 * @param direction 渲染方向（虽然参数存在但在此实现中未使用）
	 * @return 框架偏移量，0.626F
	 */
	@Override
	public float getFrameOffset(Direction direction) {
		return .626F;
	}
}
