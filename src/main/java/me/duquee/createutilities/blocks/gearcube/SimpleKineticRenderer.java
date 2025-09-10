package me.duquee.createutilities.blocks.gearcube;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 简化动力学渲染器
 * <p>
 * 为不支持Flywheel渲染引擎的情况提供后备渲染方案。当Flywheel不可用时，
 * 此渲染器使用传统的即时渲染模式来显示动力学方块的传动轴动画效果。
 * <p>
 * 核心功能：
 * 1. 检测并处理不支持Flywheel的环境
 * 2. 为多方向传动轴提供传统渲染支持
 * 3. 实现与Flywheel版本一致的旋转逻辑和视觉效果
 * 4. 确保在所有环境下的渲染兼容性
 * 
 * 设计理念：
 * - 作为高性能Flywheel渲染的后备方案
 * - 保持与GearcubeVisual相同的旋转逻辑
 * - 使用缓存优化提升传统渲染性能
 * 
 * @param <T> 动力学方块实体类型
 */

public class SimpleKineticRenderer<T extends KineticBlockEntity> extends KineticBlockEntityRenderer<T> {

	/**
	 * 构造简化动力学渲染器
	 * <p>
	 * 初始化渲染器上下文，为传统即时渲染模式做准备。
	 * 
	 * @param context 方块实体渲染器提供的上下文，包含渲染资源和环境信息
	 */

	public SimpleKineticRenderer(BlockEntityRendererProvider.Context context) {
		super(context);
	}

	/**
	 * 安全渲染方块实体
	 * <p>
	 * 主要渲染入口点，负责在不支持Flywheel时渲染动力学方块的传动轴。
	 * 实现了完整的多方向传动轴渲染，包括正确的旋转速度和方向计算。
	 * 
	 * 渲染流程：
	 * 1. 检查Flywheel支持情况，如支持则跳过传统渲染
	 * 2. 验证方块是否为可旋转方块类型
	 * 3. 遍历所有六个方向，渲染存在传动轴的方向
	 * 4. 为每个传动轴计算正确的旋转角度和速度
	 * 5. 应用动力学变换并渲染到缓冲区
	 * 
	 * @param be 方块实体，包含动力传输状态
	 * @param partialTicks 帧间插值时间，用于平滑动画
	 * @param ms 变换矩阵栈，用于定位和旋转
	 * @param buffer 多缓冲源，用于渲染输出
	 * @param light 光照级别
	 * @param overlay 覆盖层效果（如受伤红色）
	 */

	@Override
	protected void renderSafe(T be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		// 如果支持Flywheel可视化，则跳过传统渲染
		if (VisualizationManager.supportsVisualization(be.getLevel())) return;

		BlockState state = be.getBlockState();
		// 确保方块实现了IRotate接口
		if (!(state.getBlock() instanceof IRotate block)) return;

		final BlockPos pos = be.getBlockPos();
		// 获取当前渲染时间，用于计算动画帧
		float time = AnimationTickHolder.getRenderTime(be.getLevel());

		// 遍历所有六个方向，渲染每个方向的传动轴
		for (Direction direction : Iterate.directions) {
			// 检查该方向是否有传动轴
			if (!block.hasShaftTowards(be.getLevel(), pos, state, direction)) continue;
			
			Direction.Axis axis = direction.getAxis();

			// 获取传动轴的渲染缓冲，使用半轴模型
			SuperByteBuffer shaft = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, state, direction);
			// 计算位置偏移量
			float offset = getRotationOffsetForPosition(be, pos, axis);
			// 计算基础旋转角度
			float angle = (time * be.getSpeed() * 3f / 10) % 360;

			// 根据动力源方向调整旋转方向
			if (be.getSpeed() != 0 && be.hasSource()) {
				BlockPos source = be.source.subtract(pos);
				Direction sourceFacing = Direction.getNearest(source.getX(), source.getY(), source.getZ());
				
				if (sourceFacing.getAxis() == axis)
					// 同轴传动：同方向保持原速，反方向反转
					angle *= sourceFacing == direction ? 1 : -1;
				else if (sourceFacing.getAxisDirection() == direction.getAxisDirection())
					// 垂直传动：相同轴向方向时反转速度
					angle *= -1;
			}

			// 应用偏移和角度转换
			angle += offset;
			angle = angle / 180f * (float) Math.PI;  // 转换为弧度

			// 应用动力学旋转变换
			kineticRotationTransform(shaft, be, axis, angle, light);
			// 渲染到固体缓冲区
			shaft.renderInto(ms, buffer.getBuffer(RenderType.solid()));
		}
	}
}
