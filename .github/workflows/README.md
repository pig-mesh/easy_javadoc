# GitHub Actions Workflows

本项目包含以下 GitHub Actions workflows：

## 1. build-jar.yml - JAR 构建工作流
**触发条件：** 每次代码提交到任何分支
**功能：**
- 使用 JDK 8 和 Gradle 5.6.4
- 构建标准 JAR 文件
- 构建包含所有依赖的 Fat JAR
- 构建 IntelliJ 插件分发包
- 上传构建产物作为 artifacts

**产物：**
- `jar-files-{run_number}`: 包含所有 JAR 文件
- `plugin-zip-{run_number}`: 包含插件分发 ZIP 文件

## 2. ci.yml - 持续集成工作流
**触发条件：** 代码提交和 Pull Request
**功能：**
- 运行测试
- 构建项目
- 上传测试结果和构建产物

## 3. build.yml - 完整构建和发布工作流
**触发条件：** 主分支提交
**功能：**
- 完整构建流程
- 自动创建 GitHub Release
- 上传 JAR 文件到 Release

## 使用说明

1. **每次提交**都会触发 `build-jar.yml`，自动构建 JAR 文件
2. 构建完成后，可以在 Actions 页面下载对应的 artifacts
3. 主分支的提交会额外触发完整的发布流程

## 下载构建产物

1. 进入 GitHub 项目的 Actions 页面
2. 选择对应的 workflow run
3. 在页面底部的 Artifacts 部分下载需要的文件

## 构建产物说明

- **标准 JAR**: `easy_javadoc-{version}.jar`
- **Fat JAR**: `easy_javadoc-{version}-all.jar` (包含所有依赖)
- **插件 ZIP**: `Easy Javadoc-{version}.zip` (IntelliJ 插件分发包) 