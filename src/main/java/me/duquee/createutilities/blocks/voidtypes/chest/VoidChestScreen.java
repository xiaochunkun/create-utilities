package me.duquee.createutilities.blocks.voidtypes.chest;

import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import me.duquee.createutilities.CreateUtilities;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * 虚空箱子GUI界面
 * 
 * 为虚空箱子提供图形用户界面，允许玩家与虚空存储系统进行交互。
 * 该界面整合了玩家背包和虚空箱子的存储空间，提供统一的物品管理体验。
 * 
 * 继承自Create模组的AbstractSimiContainerScreen，保持与Create模组风格的一致性，
 * 并获得了Create模组提供的高级GUI功能支持。
 * 
 * 界面特性：
 * - 使用自定义纹理资源，提供独特的虚空科技视觉风格
 * - 集成玩家背包显示，便于物品的快速转移
 * - 支持Create模组的高级交互特性（如物品过滤、自动化等）
 * - 适配不同分辨率和界面缩放设置
 * 
 * 技术实现：
 * - 基于Container-Screen架构，确保客户端-服务器数据同步
 * - 使用GuiGraphics进行现代化的渲染操作
 * - 自动处理物品槽位的点击、拖拽等交互逻辑
 * 
 * @author duquee
 */
public class VoidChestScreen extends AbstractSimiContainerScreen<VoidChestContainer> {

	/**
	 * GUI背景纹理资源位置
	 * 指向虚空箱子专用的GUI纹理文件，定义界面的视觉外观
	 */
	private static final ResourceLocation TEXTURE = CreateUtilities.asResource("textures/gui/void_chest.png");

	/**
	 * 构造函数
	 * 
	 * 初始化虚空箱子的GUI界面，设置界面尺寸和基础属性。
	 * 
	 * @param container 虚空箱子容器实例，管理界面的数据逻辑
	 * @param inv 玩家背包实例，用于物品转移和显示
	 * @param title 界面标题组件，通常显示为"虚空箱子"
	 */
	public VoidChestScreen(VoidChestContainer container, Inventory inv, Component title) {
		super(container, inv, title);
		// 设置界面高度为172像素，为容纳箱子存储槽位和玩家背包
		this.imageHeight = 172;
	}

	/**
	 * 渲染GUI背景
	 * 
	 * 绘制虚空箱子界面的背景纹理和文本标签。
	 * 该方法在每帧都会被调用，负责界面的基础视觉元素渲染。
	 * 
	 * 渲染流程：
	 * 1. 计算界面在屏幕中的居中位置
	 * 2. 绘制背景纹理
	 * 3. 绘制界面标题和玩家背包标签
	 * 4. 应用适当的文本颜色和样式
	 * 
	 * @param graphics GUI图形上下文，用于绘制操作
	 * @param partialTick 帧间插值时间，用于平滑动画效果
	 * @param mouseX 鼠标X坐标，用于悬停效果计算
	 * @param mouseY 鼠标Y坐标，用于悬停效果计算
	 */
	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		// 计算界面的起始坐标，使其在屏幕中居中
		int startX = (width - imageWidth) / 2;
		int startY = (height - imageHeight) / 2;
		
		// 绘制主背景纹理
		graphics.blit(TEXTURE, startX, startY, 0, 0, imageWidth, imageHeight);
		
		// 绘制界面标题，使用深灰色（0x404040）
		graphics.drawString(font, title, startX + 8, startY + 7, 0x404040);
		
		// 绘制玩家背包标签，位置位于背包区域上方
		graphics.drawString(font, playerInventoryTitle, startX + 8, startY + 78, 0x404040, false);
	}
}
