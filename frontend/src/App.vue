<template>
  <div id="app">
    <header>
      <h1>博色广告流量分析系统</h1>
      <div class="subtitle">实时监控、智能评分、多维分析</div>
    </header>
    <main>
      <router-view></router-view>
    </main>
    <footer>
      <p>© 2025 博色广告流量分析系统 - 版本1.0.1</p>
    </footer>
  </div>
</template>

<script>
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'App',
});
</script>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: Arial, sans-serif;
  background-color: #f5f7fa;
}

#app {
  width: 100%;
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
  box-sizing: border-box;
}

header {
  margin-bottom: 20px;
  text-align: center;
  padding-bottom: 15px;
  border-bottom: 1px solid #EBEEF5;
}

header h1 {
  color: #409EFF;
  margin-bottom: 5px;
}

.subtitle {
  color: #909399;
  font-size: 14px;
}

main {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  min-height: 600px;
}

footer {
  margin-top: 20px;
  text-align: center;
  color: #909399;
  font-size: 14px;
  padding: 20px 0;
}

/* 全局样式 */
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2, .card-header h3 {
  margin: 0;
}
</style> 