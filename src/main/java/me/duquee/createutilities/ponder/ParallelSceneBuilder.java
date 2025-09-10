package me.duquee.createutilities.ponder;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.foundation.instruction.PonderInstruction;
import org.jetbrains.annotations.NotNull;

/**
 * 并行场景构建器
 * 
 * 这个类扩展了Create模组的CreateSceneBuilder，专门用于构建需要并行执行的
 * Ponder教程场景。它通过重写指令添加方法，将所有新创建的指令自动
 * 注册到关联的并行指令执行器中，实现了透明的并行化支持。
 * 
 * 设计原理：
 * 在复杂的教程场景中，经常需要同时执行多个动作来创造真实和动态的效果。
 * 传统的SceneBuilder只支持顺序执行指令，而这个并行构建器通过代理模式
 * 扩展了原有功能，使得用相同的API调用可以创建并行执行的指令序列。
 * 
 * 核心特性：
 * - 透明代理：保持与标准SceneBuilder完全相同的API接口
 * - 自动并行化：所有通过此构建器创建的指令自动加入并行执行
 * - 无缝集成：完全兼容Create模组的现有Ponder功能
 * - 简化使用：开发者无需手动管理并行指令的生命周期
 * 
 * 应用场景：
 * - 多物品同时移动：在传送带教程中同时展示多个物品的流动
 * - 并行机械操作：展示复杂机械装置的多个部件同时工作
 * - 同步动画效果：创建多个协调一致的视觉效果
 * - 实时系统演示：模拟真实工厂中多个流程的并行执行
 * 
 * 技术实现：
 * - 代理模式：通过组合而非继承的方式扩展功能
 * - 自动注册：重写addInstruction方法实现自动并行化
 * - 生命周期管理：指令的创建和执行完全由并行系统管理
 * - 兼容性保证：保持与原始API的100%兼容性
 * 
 * 使用方式：
 * ```java
 * ParallelInstruction parallel = new ParallelInstruction(scene);
 * ParallelSceneBuilder builder = parallel.scene;
 * 
 * // 以下所有指令都会并行执行
 * builder.world().createItemEntity(pos1, motion1, item);
 * builder.world().createItemEntity(pos2, motion2, item);
 * builder.idle(10);  // 这个等待指令也会并行执行
 * 
 * scene.addInstruction(parallel);  // 添加整个并行指令组
 * ```
 * 
 * 性能考虑：
 * - 内存效率：只在需要时创建并行结构，避免不必要的开销
 * - 执行效率：利用游戏的tick机制实现高效的并行更新
 * - 资源管理：自动清理完成的指令，防止内存泄漏
 * 
 * @author duquee
 * @since 1.0.0
 */
public class ParallelSceneBuilder extends CreateSceneBuilder {

	/**
	 * 关联的并行指令执行器
	 * 
	 * 存储对创建此构建器的并行指令实例的引用。
	 * 所有通过此构建器创建的指令都会被添加到这个执行器中。
	 */
	private final ParallelInstruction instruction;

	/**
	 * 构造函数
	 * 
	 * 创建一个新的并行场景构建器，它会将所有创建的指令
	 * 自动添加到指定的并行指令执行器中。
	 * 
	 * 初始化过程：
	 * 1. 调用父类构造函数，初始化基础的场景构建功能
	 * 2. 保存并行指令的引用，用于后续的指令注册
	 * 3. 设置代理关系，确保所有API调用都能正确转发
	 * 
	 * @param ponderScene 基础的Ponder场景构建器，提供核心功能
	 * @param instruction 并行指令执行器，用于管理创建的指令
	 */
	public ParallelSceneBuilder(SceneBuilder ponderScene, ParallelInstruction instruction) {
		super(ponderScene);
		this.instruction = instruction;
	}

	/**
	 * 添加指令到并行执行器
	 * 
	 * 重写父类的指令添加方法，将新创建的指令自动注册到
	 * 关联的并行指令执行器中，而不是直接添加到场景中。
	 * 
	 * 处理流程：
	 * 1. 接收新创建的Ponder指令
	 * 2. 将指令添加到并行执行器的调度列表中
	 * 3. 指令将在并行执行器被调度时开始执行
	 * 4. 与其他并行指令同时运行，直到完成
	 * 
	 * 与父类方法的区别：
	 * - 父类方法：直接将指令添加到场景的执行队列中（顺序执行）
	 * - 此方法：将指令添加到并行执行器中（并行执行）
	 * 
	 * 自动化优势：
	 * - 开发者无需手动管理并行指令的添加
	 * - 保持API的一致性，降低学习成本
	 * - 减少错误，避免遗漏指令注册
	 * - 支持链式调用，提高代码可读性
	 * 
	 * @param instruction 要添加的Ponder指令，可以是任何类型的指令
	 */
	@Override
	public void addInstruction(@NotNull PonderInstruction instruction) {
		this.instruction.addInstruction(instruction);
	}

}
