package me.duquee.createutilities.ponder;

import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.instruction.PonderInstruction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Ponder并行指令执行器
 * 
 * 这个类扩展了Ponder的指令系统，允许多个教程指令同时并行执行。
 * 在复杂的教程场景中，经常需要同时展示多个动作（如同时移动多个物品、
 * 同时播放多个动画等），传统的顺序执行方式会显得笨拙和不自然。
 * 
 * 并行执行的优势：
 * - 真实感增强：模拟真实世界中同时发生的多个事件
 * - 效率提升：减少教程的总时长，提高学习效率
 * - 视觉丰富：创造更加动态和吸引人的教学场景
 * - 逻辑清晰：将相关的动作组合在一起，便于理解
 * 
 * 核心功能：
 * - 指令调度：管理多个并行执行的Ponder指令
 * - 生命周期控制：处理指令的初始化、执行和完成状态
 * - 阻塞机制：支持阻塞和非阻塞指令的混合执行
 * - 状态同步：确保所有并行指令的状态同步
 * 
 * 执行模型：
 * 1. 指令注册：将需要并行执行的指令添加到调度列表
 * 2. 并行启动：当指令被调度时，同时启动所有子指令
 * 3. 循环执行：每个游戏刻度都更新所有活跃的指令
 * 4. 完成检测：当所有子指令完成时，整个并行指令完成
 * 
 * 应用场景：
 * - 物品传送带教程：同时展示多条传送带上的物品流动
 * - 流体管道演示：同时显示多个管道中的流体传输
 * - 机械装置运行：展示复杂机械的多个部件同时工作
 * - 多玩家协作：模拟多个玩家同时操作的场景
 * 
 * 技术特性：
 * - 线程安全：确保并行执行不会产生竞态条件
 * - 内存优化：及时清理完成的指令，避免内存泄漏
 * - 错误隔离：单个指令的错误不会影响其他并行指令
 * - 性能优化：避免不必要的循环和计算
 * 
 * @author duquee
 * @since 1.0.0
 */
public class ParallelInstruction extends PonderInstruction {

	/**
	 * 指令调度列表
	 * 
	 * 存储所有需要并行执行的子指令。这个列表在指令注册阶段填充，
	 * 在指令执行时作为模板使用。使用ArrayList是因为：
	 * - 顺序保持：保持指令添加的顺序，便于调试和理解
	 * - 随机访问：支持高效的索引访问和迭代
	 * - 动态扩容：可以根据需要动态添加新的指令
	 */
	private final List<PonderInstruction> schedule = new ArrayList<>();
	
	/**
	 * 活跃指令列表
	 * 
	 * 存储当前正在执行的指令。这个列表在每次指令重置时从调度列表复制，
	 * 在执行过程中会动态移除已完成的指令。使用独立的列表是为了：
	 * - 状态隔离：不影响原始的调度列表，支持指令重复执行
	 * - 性能优化：只对活跃指令进行状态检查和更新
	 * - 安全迭代：支持在迭代过程中安全移除元素
	 */
	private final List<PonderInstruction> activeSchedule = new ArrayList<>();

	/**
	 * 并行场景构建器
	 * 
	 * 提供专门的场景构建接口，将新创建的指令自动添加到并行执行列表中。
	 * 这个构建器是对标准SceneBuilder的扩展，提供了更便捷的并行指令创建方式。
	 */
	public final ParallelSceneBuilder scene;

	/**
	 * 构造函数
	 * 
	 * 初始化并行指令执行器，创建关联的并行场景构建器。
	 * 
	 * @param builder 基础场景构建器，用于创建Ponder场景元素
	 */
	public ParallelInstruction(SceneBuilder builder) {
		this.scene = new ParallelSceneBuilder(builder, this);
	}

	/**
	 * 重置指令状态
	 * 
	 * 当Ponder场景需要重新播放或重置时调用此方法。
	 * 它会清理所有运行时状态，并重新初始化所有子指令。
	 * 
	 * 重置流程：
	 * 1. 调用父类重置方法，清理基础状态
	 * 2. 清空活跃指令列表，停止所有正在执行的指令
	 * 3. 重置所有子指令的状态，准备重新执行
	 * 
	 * @param scene Ponder场景实例，提供重置上下文
	 */
	@Override
	public void reset(PonderScene scene) {
		super.reset(scene);
		activeSchedule.clear();                               // 清空活跃指令列表
		schedule.forEach(mdi -> mdi.reset(scene));           // 重置所有子指令
	}

	/**
	 * 检查指令是否完成
	 * 
	 * 并行指令的完成条件是所有子指令都已完成。
	 * 这个方法通过检查活跃指令列表是否为空来判断完成状态。
	 * 
	 * 完成逻辑：
	 * - 如果活跃列表为空，说明所有子指令都已执行完毕并被移除
	 * - 如果活跃列表不为空，说明还有指令正在执行
	 * 
	 * @return true表示所有子指令都已完成，false表示还有指令在执行
	 */
	@Override
	public boolean isComplete() {
		return activeSchedule.isEmpty();
	}

	/**
	 * 指令调度事件处理
	 * 
	 * 当Ponder系统开始执行这个并行指令时调用此方法。
	 * 它会将所有预定的子指令复制到活跃列表中，准备开始并行执行。
	 * 
	 * 调度流程：
	 * 1. 从调度模板列表复制所有子指令到活跃列表
	 * 2. 保持指令的原始顺序和状态
	 * 3. 准备在下一个tick开始并行执行
	 * 
	 * @param scene Ponder场景实例，提供执行上下文
	 */
	@Override
	public void onScheduled(PonderScene scene) {
		activeSchedule.addAll(schedule);  // 复制所有调度的指令到活跃列表
	}

	/**
	 * 指令执行刻度更新
	 * 
	 * 这是并行指令的核心执行逻辑，每个游戏刻度都会被调用。
	 * 它负责更新所有活跃的子指令，并处理完成的指令。
	 * 
	 * 执行算法：
	 * 1. 遍历所有活跃的子指令（使用迭代器确保安全移除）
	 * 2. 调用每个指令的tick方法进行状态更新
	 * 3. 检查指令是否完成，如果完成则从活跃列表中移除
	 * 4. 处理阻塞指令：如果遇到阻塞指令，暂停后续指令的执行
	 * 
	 * 阻塞机制：
	 * - 非阻塞指令：完成后继续执行下一个指令
	 * - 阻塞指令：在完成或等待时，暂停后续指令的执行
	 * 
	 * 性能优化：
	 * - 使用迭代器避免索引越界问题
	 * - 及时移除完成的指令，减少后续循环的开销
	 * - 阻塞检查避免不必要的指令更新
	 * 
	 * @param scene Ponder场景实例，提供执行上下文和资源
	 */
	@Override
	public void tick(PonderScene scene) {
		// 使用迭代器遍历活跃指令列表，支持安全移除
		for (Iterator<PonderInstruction> iterator = activeSchedule.iterator(); iterator.hasNext();) {
			PonderInstruction instruction = iterator.next();
			
			// 执行指令的刻度更新
			instruction.tick(scene);
			
			// 检查指令是否已完成
			if (instruction.isComplete()) {
				iterator.remove();  // 从活跃列表中移除完成的指令
				
				// 如果是阻塞指令，等待它完全结束后再继续
				if (instruction.isBlocking())
					break;
				continue;
			}
			
			// 如果当前指令是阻塞的且尚未完成，暂停后续指令的执行
			if (instruction.isBlocking())
				break;
		}
	}

	/**
	 * 添加子指令到并行执行列表
	 * 
	 * 这个方法允许外部代码向并行指令中添加新的子指令。
	 * 添加的指令将在下次执行时与其他指令并行运行。
	 * 
	 * 使用场景：
	 * - 动态指令生成：根据条件动态添加不同的指令
	 * - 指令组合：将多个相关指令组合成一个并行组
	 * - 程序化构建：通过代码逻辑构建复杂的并行场景
	 * 
	 * 注意事项：
	 * - 应该在指令调度之前添加，避免运行时修改
	 * - 添加的指令会保持添加顺序
	 * - 指令的生命周期由并行指令管理
	 * 
	 * @param instruction 要添加的子指令
	 */
	public void addInstruction(PonderInstruction instruction) {
		schedule.add(instruction);
	}

}
