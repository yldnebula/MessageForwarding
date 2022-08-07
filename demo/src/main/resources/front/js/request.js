(function (win) {
  axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'
  // 创建axios实例
  win.$axios = axios.create({
   // axios中请求配置有baseURL选项，表示请求URL公共部分
   baseURL: 'http://'+baseUrl,
   // 超时
   timeout: 1000000
 })
})(window);
