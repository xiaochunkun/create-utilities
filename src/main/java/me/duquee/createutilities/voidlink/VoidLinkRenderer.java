package me.duquee.createutilities.voidlink;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxRenderer;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import me.duquee.createutilities.CreateUtilities;
import me.duquee.createutilities.blocks.voidtypes.VoidLinkBehaviour;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * 虚空连接渲染器
 * 
 * 这个类是虚空存储系统的视觉交互核心组件，负责处理虚空设备的
 * 用户界面渲染和交互反馈。它提供了直观的频率设置界面和
 * 所有者管理功能，让玩家能够轻松地配置和使用虚空存储网络。
 * 
 * 核心功能：
 * - 交互式频率配置：通过可视化的槽位设置频率参数
 * - 所有者管理系统：显示和管理设备所有者信息
 * - 实时视觉反馈：提供明确的操作提示和状态显示
 * - 鼠标悬停提示：在适当的时机显示操作指导
 * 
 * 设计特色：
 * - 非侵入式UI：与游戏世界渲染无缝集成，不阻挡游戏视野
 * - 上下文敏感：根据玩家的当前操作动态调整显示内容
 * - 视觉一致性：与Create模组的其他UI组件保持一致的视觉风格
 * - 多语言支持：支持国际化文本显示
 * 
 * 技术实现：
 * - 基于Create的ValueBox系统构建交互界面
 * - 使用Outliner系统实现高亮显示和边框效果
 * - 集成骨髅头渲染系统显示玩家头像
 * - 优化的距离检测和性能管理
 * 
 * @author duquee
 * @since 1.0.0
 */

public class VoidLinkRenderer {

	/**
	 * 虚空连接界面更新器（主要方法）
	 * 
	 * 这个方法在每个客户端刻度都会被调用，负责检测玩家的目标并
	 * 渲染相应的虚空设备交互界面。它是整个虚空存储系统
	 * 用户体验的核心入口点。
	 * 
	 * 工作流程：
	 * 1. 获取玩家当前的目标位置（十字准心指向的方块）
	 * 2. 检查目标方块是否具有VoidLinkBehaviour（虚空连接行为）
	 * 3. 遍历所有虚空设备的交互槽位（频率1、频率2、所有者）
	 * 4. 为每个槽位创建可视化的ValueBox和交互提示
	 * 5. 根据鼠标悬停状态提供上下文相关的操作指导
	 * 
	 * 交互设计：
	 * - 频率槽位：用于设置网络频率标识符，支持任意物品作为标识
	 * - 所有者槽位：用于管理设备所有权，支持声明和取消声明
	 * - 视觉反馈：不同状态使用不同的颜色和边框效果
	 * - 悬停提示：在鼠标悬停时显示详细的操作说明
	 * 
	 * 性能优化：
	 * - 早期退出：如果不是方块点击结果或无VoidLinkBehaviour则直接返回
	 * - 按需创建：只为实际悬停的槽位显示详细提示
	 * - 缓存优化：利用Create的ValueBox缓存系统
	 */
	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		HitResult target = mc.hitResult;
		if (!(target instanceof BlockHitResult result))
			return;

		ClientLevel world = mc.level;
		BlockPos pos = result.getBlockPos();

		VoidLinkBehaviour behaviour = BlockEntityBehaviour.get(world, pos, VoidLinkBehaviour.TYPE);
		if (behaviour == null)
			return;

		Component freq1 = CreateLang.translateDirect("logistics.firstFrequency");
		Component freq2 = CreateLang.translateDirect("logistics.secondFrequency");
		Component player = Component.translatable(CreateUtilities.ID + ".logistics.owner");

		for (int index : VoidLinkHandler.arr012) {
			AABB bb = new AABB(Vec3.ZERO, Vec3.ZERO).inflate(.25f);
			Component label = index < 2 ? (index == 0 ? freq1 : freq2) : player;
			boolean hit = behaviour.testHit(index, target.getLocation());
			ValueBoxTransform transform = behaviour.getSlot(index);

			ValueBox box = new ValueBox(label, bb, pos).passive(!hit).withColor(0x601F18);

			boolean isEmpty = index == 2 ? behaviour.getOwner() == null : behaviour.getFrequencyStack(index == 0).isEmpty();

			if (!isEmpty) box.wideOutline();
			Outliner.getInstance().showOutline(Pair.of(index, pos), box.transform(transform))
					.highlightFace(result.getDirection());

			if (!hit) continue;

			List<MutableComponent> tip = new ArrayList<>();
			if (index < 2) {
				tip.add(label.copy());
				tip.add(CreateLang.translateDirect(isEmpty ? "logistics.filter.click_to_set" : "logistics.filter.click_to_replace"));
			} else {
				tip.add(label.copy());
				tip.add(Component.translatable(CreateUtilities.ID +
						(isEmpty ? ".logistics.void.click_to_set_owner" : ".logistics.void.click_to_remove_owner")));
			}

			CreateClient.VALUE_SETTINGS_HANDLER.showHoverTip(tip);

		}
	}

	/**
	 * 在方块实体上渲染虚空连接元素
	 * 
	 * 这个方法负责在世界中渲染虚空设备的可视化元素，包括
	 * 频率物品和所有者头像。它被各种虚空设备渲染器调用，
	 * 提供统一的视觉表现。
	 * 
	 * 渲染内容：
	 * - 频率槽位0和1：显示代表频率的物品图标
	 * - 所有者槽位2：显示所有者的玩家头像（骨髅头模型）
	 * 
	 * 性能优化：
	 * - 距离检查：超过配置距离则不渲染，减少性能开销
	 * - 实体状态校验：检查方块实体是否有效和未被移除
	 * - 虚拟实体支持：兼容Create的虚拟方块实体系统
	 * 
	 * 视觉效果：
	 * - 物品渲染：使用Create的ValueBoxRenderer实现标准化物品显示
	 * - 头像渲染：使用Minecraft的骨髅头渲染系统显示玩家头像
	 * - 缩放调整：适当调整头像大小和位置以适配方块尺寸
	 * 
	 * @param te 方块实体，必须包含VoidLinkBehaviour
	 * @param partialTicks 部分tick时间，用于平滑动画
	 * @param ms 姿态变换矩阵栈，用于定位和旋转
	 * @param buffer 多缓冲源，提供各种渲染缓冲区
	 * @param light 光照值，影响渲染明度
	 * @param overlay 覆盖层效果，用于受伤红色等特效
	 * @param skullModelBase 骨髅头模型基类，用于渲染玩家头像
	 */
	public static void renderOnTileEntity(SmartBlockEntity te, float partialTicks, PoseStack ms,
										  MultiBufferSource buffer, int light, int overlay, SkullModelBase skullModelBase) {

		if (te == null || te.isRemoved()) return;

		Entity cameraEntity = Minecraft.getInstance().cameraEntity;
		float max = AllConfigs.client().filterItemRenderDistance.getF();
		if (!te.isVirtual() && cameraEntity != null && cameraEntity.position()
				.distanceToSqr(VecHelper.getCenterOf(te.getBlockPos())) > (max * max))
			return;

		VoidLinkBehaviour behaviour = te.getBehaviour(VoidLinkBehaviour.TYPE);
		if (behaviour == null) return;

		for (int index : VoidLinkHandler.arr012) {
			ValueBoxTransform transform = behaviour.getSlot(index);

			if (index < 2) {

				ItemStack stack = behaviour.getFrequencyStack(index == 0);

				ms.pushPose();
				transform.transform(te.getLevel(), te.getBlockPos(), te.getBlockState(), ms);
				ValueBoxRenderer.renderItemIntoValueBox(stack, ms, buffer, light, overlay);
				ms.popPose();

			} else {

				GameProfile owner = behaviour.getOwner();
				if (owner == null) continue;

				ms.pushPose();

				transform.transform(te.getLevel(), te.getBlockPos(), te.getBlockState(), ms);
				float scale = 1.01f;
				ms.scale(scale, scale, scale);
				ms.translate(0, -.25f, 0);

				renderSkull(owner, ms, buffer, light, skullModelBase);

				ms.popPose();

			}

		}

	}

	/**
	 * 渲染玩家骨髅头模型
	 * 
	 * 这个辅助方法专门用于渲染玩家的头像，以显示虚空设备的所有者。
	 * 它采用了Minecraft的标准骨髅头渲染系统，保证了视觉效果的
	 * 一致性和兼容性。
	 * 
	 * 渲染特性：
	 * - 使用玩家类型的骨髅头模型，与游戏中的玩家头部一致
	 * - 支持玩家皮肤和材质，包括自定义皮肤
	 * - 自动获取和应用正确的渲染类型
	 * - 兼容光照和覆盖层效果
	 * 
	 * 数学变换：
	 * - X轴翻转：修正Minecraft骨髅头的渲染方向
	 * - Y轴翻转：调整垂直方向以适应方块空间
	 * - Z轴保持：保持正常的深度方向
	 * 
	 * 颜色和材质：
	 * - 使用全白色（1.0f, 1.0f, 1.0f）保持原始皮肤颜色
	 * - 全不透明（1.0f alpha）确保头像完全不透明
	 * - 使用标准光照和覆盖层参数
	 * 
	 * @param owner 玩家的游戏资料，包含玩家名和皮肤信息
	 * @param poseStack 姿态变换矩阵栈，已经应用了位置和旋转
	 * @param bufferSource 多缓冲源，提供渲染所需的缓冲区
	 * @param packedLight 打包的光照值，包含天空光和方块光
	 * @param model 骨髅头模型实例，用于实际的几何渲染
	 */
	public static void renderSkull(GameProfile owner, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, SkullModelBase model) {
		RenderType renderType = SkullBlockRenderer.getRenderType(SkullBlock.Types.PLAYER, owner);
		poseStack.scale(-1.0F, -1.0F, 1.0F);
		VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
		model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
	}

}
