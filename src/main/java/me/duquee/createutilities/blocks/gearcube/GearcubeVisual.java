package me.duquee.createutilities.blocks.gearcube;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.content.kinetics.gearbox.GearboxBlockEntity;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.AbstractInstance;
import dev.engine_room.flywheel.lib.instance.FlatLit;
import dev.engine_room.flywheel.lib.model.Models;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 齿轮立方体可视化类
 * <p>
 * 负责齿轮立方体方块的图形渲染和动画效果。该类继承自Create模组的KineticBlockEntityVisual，
 * 实现了六个面都有传动轴的齿轮箱的视觉效果，每个面的传动轴会根据动力传输方向和速度进行正确的旋转动画。
 * <p>
 * 核心功能：
 * 1. 管理六个方向的传动轴实例，每个轴对应一个方向
 * 2. 根据动力源方向和齿轮箱的传动逻辑计算每个轴的旋转速度
 * 3. 实现实时的速度更新和光照效果
 * 4. 支持方块破坏时的粒子效果
 * 
 * 设计理念：
 * - 使用EnumMap高效管理六个方向的渲染实例
 * - 实现精确的齿轮传动速度计算逻辑
 * - 采用Flywheel渲染引擎提供高性能的实时渲染
 */
public class GearcubeVisual extends KineticBlockEntityVisual<GearboxBlockEntity> {

	/**
	 * 六个方向的旋转实例映射
	 * <p>
	 * 使用EnumMap存储六个方向（上下前后左右）的旋转轴渲染实例，
	 * EnumMap提供比HashMap更高效的枚举键查找性能，非常适合Direction枚举的使用场景。
	 */
	protected final EnumMap<Direction, RotatingInstance> keys = new EnumMap<>(Direction.class);
	
	/**
	 * 动力源方向
	 * <p>
	 * 记录当前齿轮箱的动力来源方向，用于计算各个传动轴的旋转速度和方向。
	 * 当齿轮箱没有动力源时为null。
	 */
	protected Direction sourceFacing;

	/**
	 * 构造齿轮立方体视觉效果
	 * <p>
	 * 初始化所有六个方向的传动轴渲染实例，设置正确的位置、朝向和初始旋转速度。
	 * 
	 * @param context 可视化上下文，提供渲染环境和资源
	 * @param blockEntity 齿轮箱方块实体，包含动力传输状态
	 * @param partialTick 帧间插值时间，用于平滑动画
	 */

	public GearcubeVisual(VisualizationContext context, GearboxBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);

		// 更新动力源方向信息
		this.updateSourceFacing();
		
		// 创建传动轴渲染实例生成器，使用半轴模型
		Instancer<RotatingInstance> instancer = this.instancerProvider()
				.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF));

		// 为每个方向创建传动轴渲染实例
		for (Direction direction : Iterate.directions) {
			final Direction.Axis axis = direction.getAxis();
			
			// 创建旋转实例并配置
			RotatingInstance shaft = instancer.createInstance();
			shaft.setup(blockEntity, axis, this.getSpeed(direction))
					.setPosition(this.getVisualPosition())           // 设置渲染位置
					.rotateToFace(Direction.SOUTH, direction)        // 设置朝向方向
					.setChanged();                                   // 标记为已更改

			// 将实例存储到映射中
			keys.put(direction, shaft);
		}
	}

	/**
	 * 计算指定方向传动轴的旋转速度
	 * <p>
	 * 根据齿轮箱的动力源方向和传动逻辑，计算各个方向传动轴的正确旋转速度。
	 * 齿轮箱的传动规则：
	 * - 同轴传动：与动力源同方向的轴保持相同方向，反方向的轴反向旋转
	 * - 垂直传动：根据轴向方向决定旋转方向，实现90度动力传输
	 * 
	 * @param direction 要计算速度的传动轴方向
	 * @return 该方向传动轴的旋转速度（正数为正转，负数为反转）
	 */

	private float getSpeed(Direction direction) {
		float speed = blockEntity.getSpeed();

		// 只有在有转速且有动力源时才进行方向计算
		if (speed != 0 && sourceFacing != null) {
			if (sourceFacing.getAxis() == direction.getAxis())
				// 同轴传动：同方向保持原速，反方向反转
				speed *= sourceFacing == direction ? 1 : -1;
			else if (sourceFacing.getAxisDirection() == direction.getAxisDirection())
				// 垂直传动：相同轴向方向时反转速度
				speed *= -1;
		}
		return speed;
	}

	/**
	 * 更新动力源方向信息
	 * <p>
	 * 根据齿轮箱方块实体的动力源位置，计算并更新动力源的方向。
	 * 这个方向信息用于确定各个传动轴的旋转方向和速度。
	 */

	protected void updateSourceFacing() {
		if (blockEntity.hasSource()) {
			// 计算动力源相对位置并确定方向
			BlockPos source = blockEntity.source.subtract(pos);
			sourceFacing = Direction.getNearest(source.getX(), source.getY(), source.getZ());
		} else {
			// 没有动力源时清空方向信息
			sourceFacing = null;
		}
	}

	/**
	 * 更新可视化效果
	 * <p>
	 * 每帧调用此方法来更新传动轴的旋转状态。会重新计算动力源方向，
	 * 然后更新所有传动轴的旋转速度，确保动画效果与实际的动力传输状态同步。
	 * 
	 * @param partialTick 帧间插值时间，用于平滑动画效果
	 */

	@Override
	public void update(float partialTick) {
		// 更新动力源方向信息
		updateSourceFacing();
		
		// 遍历所有方向的传动轴，更新其旋转状态
		for (Map.Entry<Direction, RotatingInstance> key : keys.entrySet()) {
			Direction direction = key.getKey();
			Direction.Axis axis = direction.getAxis();
			// 更新该方向传动轴的旋转速度
			updateRotation(key.getValue(), axis, getSpeed(direction));
		}
	}

	/**
	 * 更新单个传动轴的旋转状态
	 * <p>
	 * 为指定的旋转实例设置新的轴向和旋转速度，并标记为已更改以触发重新渲染。
	 * 
	 * @param value 要更新的旋转实例
	 * @param axis 旋转轴向
	 * @param speed 旋转速度
	 */

	private void updateRotation(RotatingInstance value, Direction.Axis axis, float speed) {
		value.setup(this.blockEntity, axis, speed).setChanged();
	}

	/**
	 * 收集破坏粒子效果实例
	 * <p>
	 * 当方块被破坏时，此方法收集所有需要显示破坏粒子效果的渲染实例。
	 * 所有六个方向的传动轴都会参与破坏效果的显示。
	 * 
	 * @param consumer 实例消费者，用于收集渲染实例
	 */

	@Override
	public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
		// 将所有传动轴实例添加到破坏效果中
		this.keys.values().forEach(consumer);
	}

	/**
	 * 更新光照效果
	 * <p>
	 * 重新计算所有传动轴的光照效果，确保在光照条件变化时渲染效果正确。
	 * 使用FlatLit数组批量更新光照，提高性能。
	 * 
	 * @param partialTick 帧间插值时间
	 */

	@Override
	public void updateLight(float partialTick) {
		// 将所有传动轴实例转换为FlatLit数组并重新计算光照
		this.relight(this.keys.values().toArray(FlatLit[]::new));
	}

	/**
	 * 销毁可视化对象
	 * <p>
	 * 当齿轮立方体方块被移除或不再需要渲染时调用此方法。
	 * 清理所有创建的渲染实例，释放GPU内存资源，防止内存泄漏。
	 */

	@Override
	protected void _delete() {
		// 删除所有传动轴渲染实例
		keys.values().forEach(AbstractInstance::delete);
		// 清空映射表，彻底释放引用
		keys.clear();
	}
}
