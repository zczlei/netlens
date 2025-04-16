module.exports = {
  devServer: {
    port: 8081,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://192.168.1.187:8080',
        changeOrigin: true
      }
    }
  }
} 