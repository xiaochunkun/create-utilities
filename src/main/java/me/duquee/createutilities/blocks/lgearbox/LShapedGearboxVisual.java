package me.duquee.createutilities.blocks.lgearbox;

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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * L型齿轮箱可视化类
 * <p>
 * 负责L型齿轮箱的Flywheel渲染效果，管理两个传动轴的旋转动画。
 * 与标准齿轮箱不同，L型齿轮箱只有两个传动接口，需要精确控制这两个轴的渲染。
 * <p>
 * 核心功能：
 * 1. 管理两个特定方向的传动轴渲染实例
 * 2. 根据方块状态动态创建传动轴渲染
 * 3. 实现与标准齿轮箱一致的旋转逻辑
 * 4. 提供高性能的Flywheel渲染支持
 * 
 * 设计理念：
 * - 只渲染实际存在的传动轴，避免不必要的渲染开销
 * - 复用GearcubeVisual的核心渲染逻辑
 * - 支持动态的传动轴配置变更
 */

public class LShapedGearboxVisual extends KineticBlockEntityVisual<GearboxBlockEntity> {

	/**
	 * 传动轴方向到旋转实例的映射
	 * <p>
	 * 存储L型齿轮箱两个传动轴的渲染实例，键为方向，值为对应的旋转渲染实例。
	 * 使用EnumMap提供高效的Direction枚举查找性能。
	 */
	protected final EnumMap<Direction, RotatingInstance> keys = new EnumMap<>(Direction.class);
	
	/**
	 * 动力源方向
	 * <p>
	 * 记录当前齿轮箱的动力来源方向，用于计算传动轴的正确旋转方向。
	 * 当没有动力源时为null。
	 */
	protected Direction sourceFacing;

	/**
	 * 构造L型齿轮箱可视化效果
	 * <p>
	 * 初始化L型齿轮箱的渲染，根据方块状态创建两个传动轴的渲染实例。
	 * 与齿轮立方体不同，此处只创建两个特定方向的传动轴。
	 * 
	 * @param context 可视化上下文，提供渲染环境
	 * @param blockEntity 齿轮箱方块实体，包含动力传输状态
	 * @param partialTick 帧间插值时间
	 */

	public LShapedGearboxVisual(VisualizationContext context, GearboxBlockEntity blockEntity, float partialTick) {
		super(context, blockEntity, partialTick);

		// 更新动力源方向信息
		updateSourceFacing();
		
		// 获取创建渲染实例的生成器（虽然这里创建了但实际在putShaft中使用）
		Instancer<RotatingInstance> instancer = this.instancerProvider()
				.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF));

		// 创建第一个传动轴（水平方向）
		Direction facing1 = blockState.getValue(LShapedGearboxBlock.FACING_1);
		putShaft(blockEntity, facing1);

		// 创建第二个传动轴（相对方向转换为绝对方向）
		Direction facing2 = LShapedGearboxBlock.getAbsolute(facing1, blockState.getValue(LShapedGearboxBlock.FACING_2));
		putShaft(blockEntity, facing2);
	}

	/**
	 * 创建并放置传动轴渲染实例
	 * <p>
	 * 为指定方向创建一个传动轴的渲染实例，包括正确的位置、朝向和初始旋转速度设置。
	 * 这个方法被构造函数调用两次，分别为两个传动轴创建渲染。
	 * 
	 * @param blockEntity 齿轮箱方块实体
	 * @param direction 传动轴的方向
	 */

	private void putShaft(GearboxBlockEntity blockEntity, Direction direction) {
		// 获取传动轴的旋转轴向
		final Direction.Axis axis = direction.getAxis();

		// 创建旋转实例生成器
		Instancer<RotatingInstance> instancer = this.instancerProvider()
				.instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF));
		RotatingInstance shaft = instancer.createInstance();

		// 配置传动轴渲染实例
		shaft.setup(blockEntity, axis, blockEntity.getSpeed())
				.setPosition(this.getVisualPosition())    // 设置渲染位置
				.rotateToFace(Direction.SOUTH, direction) // 设置朝向
				.setChanged();                            // 标记更改

		// 将传动轴实例添加到映射中
		keys.put(direction, shaft);
	}

	/**
	 * 更新动力源方向信息
	 * <p>
	 * 根据齿轮箱方块实体的动力源位置，计算并更新动力源的方向。
	 * 这个信息用于确定传动轴的正确旋转方向。
	 */

	protected void updateSourceFacing() {
		if (blockEntity.hasSource()) {
			// 计算动力源的相对位置并确定方向
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
	 * 然后更新所有传动轴的旋转速度，确保动画效果与实际传输状态同步。
	 * 
	 * @param partialTick 帧间插值时间
	 */

	@Override
	public void update(float partialTick) {
		// 更新动力源方向
		updateSourceFacing();
		
		// 遍历所有传动轴，更新其旋转状态
		for (Map.Entry<Direction, RotatingInstance> key : keys.entrySet()) {
			Direction direction = key.getKey();
			Direction.Axis axis = direction.getAxis();
			// 更新旋转速度（注意：这里使用基础速度，不进行方向调整）
			updateRotation(key.getValue(), axis, blockEntity.getSpeed());
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
	 * 更新光照效果
	 * <p>
	 * 重新计算所有传动轴的光照效果，确保在光照条件变化时渲染效果正确。
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
	 * 清理所有创建的渲染实例，释放GPU内存资源。
	 * 当L型齿轮箱方块被移除时调用此方法。
	 */

	@Override
	public void _delete() {
		// 删除所有传动轴渲染实例
		keys.values().forEach(AbstractInstance::delete);
		// 清空映射表，释放引用
		keys.clear();
	}

	/**
	 * 收集破坏粒子效果实例
	 * <p>
	 * 当方块被破坏时，收集所有需要显示破坏粒子效果的渲染实例。
	 * 两个传动轴都会参与破坏效果的显示。
	 * 
	 * @param consumer 实例消费者，用于收集渲染实例
	 */

	@Override
	public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
		// 将所有传动轴实例添加到破坏效果中
		this.keys.values().forEach(consumer);
	}
}
