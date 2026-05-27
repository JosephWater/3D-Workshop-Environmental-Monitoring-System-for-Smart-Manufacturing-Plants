# 微信小程序端说明

## 目录
- `miniprogram/`：微信小程序工程目录
- `pages/index/`：移动端入口页
- `pages/webview/`：承载 H5 页面功能的统一 web-view 页

## 使用方式
1. 将 `frontend` 构建后的 H5 部署到 HTTPS 域名。
2. 修改 [utils/config.js](./utils/config.js) 中的 `H5_BASE_URL`。
3. 用微信开发者工具打开 `miniprogram` 目录。
4. 如需真机调试，请在微信公众平台配置业务域名。

## 说明
- 小程序端通过 `web-view` 打开同一套 H5 页面，因此控制台、车间总览、历史查询、告警查询和阈值配置与 Web 端保持一致。
- 由于功能直接复用 H5，手机端排版以 `frontend/src/style.css` 中的响应式样式为准。
