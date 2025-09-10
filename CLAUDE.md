# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

Create Utilities是一个基于Minecraft Forge 1.20.1的模组，专为Create模组添加实用的传送和存储功能。项目使用Java 17开发，采用Gradle构建系统。

## 核心架构

### 主要包结构
- **`me.duquee.createutilities`** - 主包，包含模组入口点
  - **`blocks/`** - 方块相关代码，包含各种功能方块实现
    - **`voidtypes/`** - 虚空类型方块（箱子、电池、储罐、电机）
    - **`gearcube/`** - 齿轮立方体方块
    - **`lgearbox/`** - L型齿轮箱方块
  - **`items/`** - 物品注册和定义
  - **`networking/`** - 网络数据包处理
  - **`ponder/`** - Ponder演示系统集成
  - **`mountedstorage/`** - 安装式存储系统
  - **`voidlink/`** - 虚空连接系统
  - **`tabs/`** - 创造模式选项卡
  - **`events/`** - 事件处理器

### 关键类说明
- **`CreateUtilities.java`** - 主模组类，负责初始化所有组件
- **`CreateUtilitiesClient.java`** - 客户端特定的初始化逻辑
- **`CUBlocks.java`** - 所有方块的注册入口
- **`CUItems.java`** - 所有物品的注册入口
- **`CUTileEntities.java`** - 方块实体的注册
- **`CUPackets.java`** - 网络数据包注册

### 技术栈
- **Minecraft**: 1.20.1
- **Forge**: 47.3.0
- **Create Mod**: 6.0.6-150
- **Ponder**: 1.0.80（用于交互式教程）
- **Registrate**: MC1.20-1.3.3（简化注册流程）
- **Mixin**: 0.8.2（用于代码注入）

## 构建与开发命令

### Gradle任务
```bash
# 构建项目（编译和打包JAR文件）
./gradlew build

# 清理构建文件
./gradlew clean

# 完整的清理和构建
./gradlew clean build

# 运行客户端（开发测试）
./gradlew runClient

# 运行服务器（开发测试）
./gradlew runServer

# 生成数据文件（配方、战利品表等）
./gradlew runData

# 发布到本地Maven仓库
./gradlew publishToMavenLocal
```

### 开发环境
- **IDE配置**: 支持Eclipse和IntelliJ IDEA
- **工作目录**: `run/` - Minecraft开发实例运行目录
- **调试端口**: 默认Forge调试配置
- **资源生成**: `src/generated/resources/` - 自动生成的数据文件

## 代码规范与架构模式

### Registrate模式
项目使用Create模组的Registrate系统进行注册管理：
- 所有注册都通过静态初始化块完成
- 使用链式调用配置属性和行为
- 自动生成模型、配方和数据文件

### 虚空存储系统
核心功能基于"虚空存储"概念：
- **VoidChest** - 跨维度共享物品存储
- **VoidTank** - 跨维度共享流体存储  
- **VoidBattery** - 跨维度共享能量存储
- **VoidMotor** - 跨维度旋转力传输

### 渲染系统
- 使用Create的Flywheel渲染引擎
- 自定义方块实体渲染器
- Ponder集成用于游戏内教程

## 资源文件结构

### 关键目录
- **`assets/createutilities/`** - 客户端资源
  - **`blockstates/`** - 方块状态定义
  - **`models/`** - 3D模型文件
  - **`textures/`** - 纹理资源
  - **`lang/`** - 多语言支持（en_us, zh_cn, pt_br等）
  - **`ponder/`** - Ponder演示文件（.nbt格式）
- **`data/createutilities/`** - 数据生成
  - **`recipes/`** - 合成配方
  - **`loot_tables/`** - 战利品表

## 特殊注意事项

### Mixin使用
项目配置了Mixin支持但当前没有Mixin类。如需添加：
- 将Mixin类放在专门的包中
- 更新`createutilities.mixins.json`配置
- 确保目标类和方法的兼容性

### 网络同步
虚空存储系统需要客户端-服务器数据同步：
- 使用自定义数据包处理更新
- 客户端缓存系统用于性能优化
- 世界保存时持久化数据

### Create模组集成
- 依赖Create的旋转动力系统
- 使用Create的Registrate注册框架
- 集成Ponder教程系统
- 遵循Create的视觉设计风格

## 版本控制说明

- **主分支**: `1.19` - 主要开发分支
- **当前分支**: `forge-1.20.1` - Forge 1.20.1版本
- **版本号**: 格式为`${mod_version}+${minecraft_version}`（当前：0.3.2+1.20.1）