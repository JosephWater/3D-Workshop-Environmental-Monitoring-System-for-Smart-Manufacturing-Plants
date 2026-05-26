将 Blender、SketchUp、3ds Max 等建模工具导出的车间模型放到这里，命名为：

workshop.glb

自动生成流程（推荐）：
1. 安装 Blender，当前项目脚本已优先适配 E:\PluginsAndTool\Blender\blender.exe
2. 在项目根目录执行：
   powershell -ExecutionPolicy Bypass -File scripts/generate_workshop_model.ps1
3. 脚本会自动导出 public/models/workshop.glb

手动建模流程：
1. 在 Blender / SketchUp / 3ds Max 中完成车间模型细化
2. 导出为 GLB，或先导出 FBX / OBJ / DAE
3. 如有需要，使用 Blender 转换为 GLB
4. 复制到 public/models/workshop.glb

前端会优先加载该模型；如果文件不存在，则自动回退到程序化三维车间。
