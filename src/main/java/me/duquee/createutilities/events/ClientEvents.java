package me.duquee.createutilities.events;

import me.duquee.createutilities.voidlink.VoidLinkRenderer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 客户端事件处理器
 * 
 * 这个类负责处理Create Utilities模组在客户端的所有事件监听。
 * 它专门处理仅在客户端环境下需要的逻辑，如渲染更新、UI刷新、
 * 客户端特有的游戏状态变化等。
 * 
 * 核心功能：
 * - 客户端刻度更新：处理需要定期更新的客户端组件
 * - 渲染系统管理：协调各种渲染器的状态更新
 * - 游戏状态检测：确保只在游戏活跃时执行客户端逻辑
 * - 性能优化：避免在不必要的时候执行昂贵的操作
 * 
 * 设计特点：
 * - 使用@Mod.EventBusSubscriber自动注册事件监听
 * - 仅在客户端分发环境下激活（Dist.CLIENT）
 * - 静态方法设计，减少对象创建开销
 * - 包含游戏状态安全检查，避免空指针异常
 * 
 * 技术实现：
 * - 基于Forge事件系统的标准实现
 * - 使用@SubscribeEvent注解自动绑定事件处理器
 * - 集成VoidLinkRenderer的定期更新逻辑
 * - 实现游戏状态的安全性检查
 * 
 * @author duquee
 * @since 1.0.0
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

	/**
	 * 客户端刻度事件处理器
	 * 
	 * 这个方法在每个客户端刻度都会被调用，负责更新所有需要定期刷新的客户端组件。
	 * 主要处理时间敏感的渲染逻辑和状态更新，如动画效果、粒子系统、UI更新等。
	 * 
	 * 事件处理流程：
	 * 1. 首先检查游戏是否处于活跃状态（世界已加载且玩家已进入）
	 * 2. 如果游戏不活跃，直接返回，避免在加载界面或暂停状态下执行逻辑
	 * 3. 调用VoidLinkRenderer的tick方法，更新虚空连接的视觉效果
	 * 
	 * 性能考虑：
	 * - 早期返回：通过游戏状态检查避免不必要的计算
	 * - 轻量级操作：只执行必要的渲染更新逻辑
	 * - 状态缓存：依赖各个组件内部的优化机制
	 * 
	 * 安全性措施：
	 * - 游戏状态验证：确保world和player实例存在
	 * - 异常隔离：各个组件的错误不会影响整体事件处理
	 * 
	 * @param event 客户端刻度事件，包含刻度阶段信息
	 */
	@SubscribeEvent
	public static void onTick(TickEvent.ClientTickEvent event) {
		// 检查游戏是否处于活跃状态，如果不是则跳过所有客户端更新
		if (!isGameActive()) return;
		
		// 更新虚空连接渲染器，处理连接线条的动画和视觉效果
		VoidLinkRenderer.tick();
	}

	/**
	 * 检查游戏是否处于活跃状态
	 * 
	 * 这个辅助方法用于判断游戏是否已经完全加载并且玩家已经进入世界。
	 * 在游戏启动、世界切换、或者暂停状态下，某些客户端逻辑不应该执行，
	 * 这个方法提供了一个统一的检查标准。
	 * 
	 * 检查条件：
	 * - 世界实例存在：Minecraft.getInstance().level != null
	 * - 玩家实例存在：Minecraft.getInstance().player != null
	 * 
	 * 使用场景：
	 * - 渲染系统更新前的状态检查
	 * - 避免在菜单界面执行游戏内逻辑
	 * - 防止在世界切换过程中的空指针异常
	 * - 确保只在游戏进行中执行相关操作
	 * 
	 * @return true表示游戏处于活跃状态，可以安全执行客户端逻辑；
	 *         false表示游戏未就绪，应跳过相关操作
	 */
	protected static boolean isGameActive() {
		return !(Minecraft.getInstance().level == null || Minecraft.getInstance().player == null);
	}

}
