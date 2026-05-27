// 微信开发者工具、真机和宿主机对“本地后端”的访问方式不完全一致。
// 这里保留一组候选地址，由请求层自动探测并缓存可用地址。
const API_BASE_URL = 'http://10.27.227.64:8080'
const API_BASE_CANDIDATES = [
  'http://10.27.227.64:8080',
  'http://127.0.0.1:8080',
  'http://localhost:8080',
  'http://[::1]:8080',
]

module.exports = {
  API_BASE_URL,
  API_BASE_CANDIDATES,
}
