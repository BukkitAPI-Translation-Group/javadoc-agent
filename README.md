# javadoc-agent

作用于 `Javadoc` 工具的 `Javaagnet`.  
项目和 `javadoc` 的版本均为 `java 17`.  
本项目的用途有:
- 补全一部分未被翻译的文本. 补全的内容见 [l10n-patch-doclets.properties](https://github.com/BukkitAPI-Translation-Group/Chinese_BukkitAPI/blob/master/l10n-patch-doclets.properties) 和 [l10n-patch-standard.properties](https://github.com/BukkitAPI-Translation-Group/Chinese_BukkitAPI/blob/master/l10n-patch-standard.properties).
- 为网页资源路径添加版本号, 便于缓存, 提升页面加载速度, 同时确保版本更新时正确加载新版的资源文件.
- 在网页底部插入自定义脚本 (使用 `-bottom` 参数需要同时加入 `--allow-script-in-comments` 参数, 而这也会允许代码注释中的脚本, 因此选择这种较麻烦的方法).

## 构建
> mvn package

## 如何使用

由于 `javadoc` 命令似乎不支持使用 `javaagent`, 需要用 `java` 命令启动 `javadoc` 工具.
> java -javaagent:javadoc-agent.jar --module jdk.javadoc/jdk.javadoc.internal.tool.Main args...

agent 需要的信息通过环境变量传递, 这些环境变量有:
- VERSION - 版本信息, 通常是某次 git commit id 的缩写.
- L10N_DOCLETS_PATCH_FILE - 译文补丁文件位置, 作用的类为 `jdk.javadoc.internal.doclets.toolkit.resources.doclets_zh_CN`.
- L10N_STANDARD_PATCH_FILE - 译文补丁文件位置, 作用的类为 `jdk.javadoc.internal.doclets.formats.html.resources.standard_zh_CN`.
- BOTTOM_SCRIPT_FILE - 自定义脚本的文件名, 此脚本应位于生成的 javadoc 的根目录下.
