package me.duquee.createutilities.blocks.voidtypes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;

import me.duquee.createutilities.voidlink.VoidLinkRenderer;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;

import org.joml.Matrix4f;

/**
 * 虚空方块渲染器接口
 * 
 * 这个接口定义了所有虚空类型方块的通用渲染规范，是虚空存储系统视觉效果的核心抽象。
 * 它采用了模板方法模式，定义了虚空方块渲染的标准流程，同时允许具体实现类
 * 自定义特定的渲染细节。
 * 
 * 核心渲染功能：
 * - 虚空传送门特效渲染：营造神秘的虚空空间视觉效果
 * - 虚空连接骷髅头渲染：显示虚空连接状态和链接关系
 * - 多方向边框渲染：支持六个面的独立边框控制
 * 
 * 设计模式：
 * - 模板方法模式：定义渲染框架，子类实现具体细节
 * - 策略模式：不同虚空类型可以有不同的渲染策略
 * 
 * 渲染技术：
 * - 使用Minecraft的EndPortal渲染类型营造虚空效果
 * - 支持动态光照和覆盖效果
 * - 基于方向的精确几何渲染
 * 
 * @param <T> 具体的虚空方块实体类型，必须继承SmartBlockEntity
 * 
 * @author duquee
 * @since 1.0.0
 */
public interface VoidTileRenderer<T extends SmartBlockEntity> {

	/**
	 * 获取骷髅头模型基础对象
	 * 
	 * 骷髅头模型用于显示虚空连接的状态，是虚空存储系统中重要的视觉指示器。
	 * 不同的虚空类型可能使用不同的骷髅头模型来区分功能。
	 * 
	 * @return 用于渲染虚空连接状态的骷髅头模型
	 */
	SkullModelBase getSkullModelBase();

	/**
	 * 判断是否应该在指定方向渲染边框
	 * 
	 * 这个方法允许虚空方块根据其状态和配置来决定哪些面需要显示边框。
	 * 例如，某些面可能因为被其他方块遮挡或者连接状态而不需要渲染边框。
	 * 
	 * @param te 虚空方块实体，包含当前方块的状态信息
	 * @param direction 要检查的方向
	 * @return true表示应该在该方向渲染边框，false表示跳过该方向
	 */
	boolean shouldRenderFrame(T te, Direction direction);
	
	/**
	 * 获取边框宽度
	 * 
	 * 定义虚空传送门边框的宽度，影响视觉效果的大小。
	 * 较宽的边框会让虚空效果看起来更加明显，但也可能遮挡更多的内容。
	 * 
	 * @return 边框宽度，范围通常在0.0到1.0之间
	 */
	float getFrameWidth();
	
	/**
	 * 获取指定方向的边框偏移量
	 * 
	 * 不同方向的边框可能需要不同的偏移量来实现最佳的视觉效果。
	 * 偏移量用于调整边框相对于方块中心的位置。
	 * 
	 * @param direction 需要获取偏移量的方向
	 * @return 该方向的边框偏移量
	 */
	float getFrameOffset(Direction direction);

	/**
	 * 渲染完整的虚空效果（模板方法）
	 * 
	 * 这是主要的渲染方法，定义了虚空方块的完整渲染流程：
	 * 1. 首先渲染虚空连接的骷髅头模型
	 * 2. 然后渲染虚空传送门特效
	 * 
	 * 这个方法采用模板方法模式，定义了标准的渲染序列，
	 * 具体的渲染细节由接口的其他方法来控制。
	 * 
	 * @param te 要渲染的虚空方块实体
	 * @param partialTicks 部分tick时间，用于平滑动画
	 * @param ms 姿态栈，用于变换渲染坐标
	 * @param buffer 多缓冲源，提供各种渲染缓冲
	 * @param light 光照值
	 * @param overlay 覆盖值，用于特殊效果
	 */
	default void renderVoid(T te, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
		// 渲染虚空连接状态的骷髅头模型
		VoidLinkRenderer.renderOnTileEntity(te, partialTicks, ms, buffer, light, overlay, getSkullModelBase());
		// 渲染虚空传送门特效
		renderPortal(te, ms.last().pose(), buffer.getBuffer(RenderType.endPortal()));
	}

	/**
	 * 渲染虚空传送门效果（私有实现方法）
	 * 
	 * 这个方法负责渲染虚空传送门的核心视觉效果。
	 * 它会遍历所有六个方向，根据shouldRenderFrame的结果决定是否渲染该方向的边框。
	 * 
	 * 渲染逻辑：
	 * 1. 计算基础坐标和尺寸
	 * 2. 遍历所有方向
	 * 3. 对每个需要渲染的方向调用renderFrame方法
	 * 4. 使用EndPortal渲染类型营造神秘效果
	 * 
	 * @param te 虚空方块实体
	 * @param pose 4x4变换矩阵
	 * @param consumer 顶点消费者，用于构建渲染几何体
	 */
	private void renderPortal(T te, Matrix4f pose, VertexConsumer consumer) {

		// 根据边框宽度计算基础坐标
		float x = (1F - getFrameWidth())*0.5F;  // 左边界
		float z = (1F + getFrameWidth())*0.5F;  // 右边界

		// 遍历所有六个方向
		for (Direction direction : Direction.values()) {

			// 检查是否需要渲染这个方向的边框
			if (!shouldRenderFrame(te, direction)) continue;

			// 获取该方向的偏移量
			float offSetValue = getFrameOffset(direction);
			
			// 根据方向渲染对应的边框面
			// 每个方向的坐标计算都针对该方向的几何特点进行了优化
			switch (direction) {
				case DOWN -> renderFrame(pose, consumer, x, z, 1 - offSetValue, 1 - offSetValue, x, x, z, z);
				case UP -> renderFrame(pose, consumer, x, z, offSetValue, offSetValue, z, z, x, x);
				case NORTH -> renderFrame(pose, consumer, x, z, z, x, 1 - offSetValue, 1 - offSetValue, 1 - offSetValue, 1 - offSetValue);
				case SOUTH -> renderFrame(pose, consumer, x, z, x, z, offSetValue, offSetValue, offSetValue, offSetValue);
				case WEST -> renderFrame(pose, consumer, 1 - offSetValue, 1 - offSetValue, x, z, x, z, z, x);
				case EAST -> renderFrame(pose, consumer, offSetValue, offSetValue, z, x, x, z, z, x);
			}

		}

	}

	/**
	 * 渲染单个边框面（私有辅助方法）
	 * 
	 * 这个方法负责渲染虚空传送门的单个矩形面。
	 * 它接收8个坐标参数来定义一个四边形，然后按照正确的顶点顺序
	 * 提交给顶点消费者进行渲染。
	 * 
	 * 顶点顺序：按照逆时针方向定义四个顶点，确保面向玩家的面是可见的
	 * 
	 * @param pose 4x4变换矩阵
	 * @param consumer 顶点消费者
	 * @param x0 第一个顶点的X坐标
	 * @param x1 第二个顶点的X坐标  
	 * @param y0 第一/四个顶点的Y坐标
	 * @param y1 第二/三个顶点的Y坐标
	 * @param z0 第一个顶点的Z坐标
	 * @param z1 第二个顶点的Z坐标
	 * @param z2 第三个顶点的Z坐标
	 * @param z3 第四个顶点的Z坐标
	 */
	private void renderFrame(Matrix4f pose, VertexConsumer consumer,
							 float x0, float x1, float y0, float y1, float z0, float z1, float z2, float z3) {
		// 按照逆时针顺序定义四个顶点，形成一个矩形面
		consumer.vertex(pose, x0, y0, z0).endVertex();  // 左下角
		consumer.vertex(pose, x1, y0, z1).endVertex();  // 右下角
		consumer.vertex(pose, x1, y1, z2).endVertex();  // 右上角
		consumer.vertex(pose, x0, y1, z3).endVertex();  // 左上角
	}

}
