# AeroplaneChess<span style="color: red">Ex</span>

Java Course Project: 飞行棋重构版

[![Version](https://img.shields.io/badge/Version-1.0.0.re-blue.svg)](https://github.com/Vkango/AeroplaneChessEx)[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)[![JavaFX](https://img.shields.io/badge/JavaFX-21.0.8-green.svg)](https://openjfx.io/)[![License](https://img.shields.io/badge/license-MIT-green.svg)](LICENSE)

- 📡 **事件驱动**: 基于事件总线实现松耦合设计
- 🔌 **插件化架构**: 用户可以很方便地通过加载不同扩展改变游戏内容, 对插件开发者友好.
- 👥 **多人游戏**: 支持 `min_Players` ~ `max_Player` 名玩家同时游戏, 每个玩家可拥有 1 ~ `Chessman` 个飞机 (具体由扩展内容决定).



## 🚀 快速开始

以 Windows 平台为例.

### 环境要求

- **Java**: JDK 17 或更高版本
- **JavaFX**: 21.0.8, 请[手动下载](https://gluonhq.com/products/javafx/)对应平台库取代占位文件.

### 编译项目

```bash
compile.bat
```

### 运行游戏

```bash
run.bat
```

有关命令行说明, 请运行时指定 `--help` 参数以获得帮助信息.

### 扩展包构建

```
build-api.bat
```



## 🎲 游戏玩法

1. **起飞**: 投掷骰子, 满足起飞条件才能将棋子从基地移到起点
2. **移动**: 按照骰子点数移动棋子
4. **胜利**: 第一个将所有棋子移动到终点的玩家获胜

这些规则都可被扩展重写.



## 🧩 扩展开发

游戏采用**SPI插件化架构**, 支持运行时动态加载插件. 插件开发者只需实现相应接口并打包为 `JAR` , 即可扩展游戏内容.

**关键步骤**:

1. 实现 `IMapProvider` 与 `IRuleSetProvider` 接口
2. 创建 SPI 配置文件 `META-INF/services/plugin.api.IMapProvider` `META-INF/services/plugin.api.IRuleSetProvider`
3. 打包为 JAR 文件 (注意插件目录下的 `compile.bat` 同步你的包名)
4. 主程序使用 `--plugin` 参数加载

**示例**: 参见 `plugin-example` 目录下的插件示例.



### 📡 事件系统

游戏使用事件总线 (`EventBus`) 实现组件间通信. 您可以订阅和发布自定义事件. 

#### 内置事件类型

| 事件名称 | 触发时机 | 数据 |
|---------|---------|------|
| `GameStarted` | 游戏开始 | - |
| `GameEnded` | 游戏结束 | winner |
| `TurnStarted` | 回合开始 | player |
| `TurnEnded` | 回合结束 | player |
| `DiceRolled` | 骰子投掷 | player, diceValue |
| `ChessmanMoved` | 棋子移动 | chessman, from, to |
| `BlockEffect` | 方块效果触发 (UI呈现) | chessman, message |

#### 事件订阅示例

```java
import game.engine.EventBus;

// 订阅棋子移动事件
EventBus.getInstance().subscribe("ChessmanMoved", event -> {
    System.out.println("棋子移动: " + event.getData());
    // 执行自定义逻辑. 也可以不使用 Lambda 表达式.
});
```

#### 发布自定义事件

```java
import game.engine.GameEvent;
import game.engine.EventBus;
import java.util.HashMap;

// 创建事件数据
Map<String, Object> data = new HashMap<>();
data.put("key", "value");

// 发布事件
GameEvent event = new GameEvent("CustomEvent", data, "自定义事件描述");
EventBus.getInstance().publish(event);
```



## ⚖️ LICENSE

本项目采用 MIT 许可证.

---

**⭐ 如果这个项目对你有帮助, 请给它一个 Star！**