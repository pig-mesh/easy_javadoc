![wn62Wy](https://minio.pigx.vip/oss/202505/wn62Wy.png)


# EasyDoc - OpenAI 协议的 Java Doc 生成插件

## 简介

EasyDoc 是一个基于 OpenAI 协议的 Java 文档生成插件，通过接入符合 OpenAI 接口规范的大模型，帮助开发人员快速生成高质量的 Java 文档注释。

## 使用说明

1. 在插件设置中配置 OpenAI 接口参数：
   - **Base URL**: 填写大模型的 baseURL，**注意最后不要带 `/v1` 或者其他版本号后缀**
   - API Key: 填写您的 OpenAI API 密钥
   - 模型: 选择使用的模型，如 gpt-3.5-turbo, gpt-4 等
   - 温度: 控制生成内容的创造性 (0.0-2.0)
   - 其他参数: 根据需要配置
2. 使用方法：

   - **生成单个注释**：将光标放置到要生成注释的类、方法或属性上，按下快捷键 `Ctrl + \`（Windows/Linux）或 `Command + \`（Mac）。
   
   - **批量生成注释**：将光标放置到要生成注释的类上，按下快捷键 `Ctrl + Shift + \`（Windows/Linux）或 `Command + Shift + \`（Mac），即可为整个类的所有元素批量生成文档注释。（注：KDoc 暂不支持）
   
   - **智能翻译中文为英文**：选中需要翻译的中文文本，按下快捷键 `Ctrl + \`（Windows/Linux）或 `Command + \`（Mac），系统会自动将其翻译为适合编程的英文表达，非常适合命名变量、方法和类。
   
   - **查看非中文翻译**：选中非中文文本，按下快捷键 `Ctrl + \`（Windows/Linux）或 `Command + \`（Mac），系统会弹出翻译结果，无需在IDE和词典之间切换。

3. 使用技巧：
   - 方法名和参数起得越贴切，生成的注释质量越高
   - 批量注释功能可大大提高文档编写效率
   - 翻译功能对于国际化项目和多语言团队协作非常有帮助

## 支持的服务商

- OpenAI 官方接口
- 支持任何兼容 OpenAI 协议的模型服务，如 Azure OpenAI、各种开源模型部署等

## 注意事项

- 确保 Base URL 配置正确，不要包含版本号如 `/v1`
- 请确保网络环境能够正常访问配置的 API 服务
- API Key 请妥善保管，不要泄露给他人
