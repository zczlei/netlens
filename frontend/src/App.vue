<template>
  <div id="app">
    <header>
      <h1>广告流量分析系统</h1>
    </header>
    <main>
      <router-view></router-view>
    </main>
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

header {
  margin-bottom: 30px;
  text-align: center;
}

header h1 {
  color: #409EFF;
}

main {
  background-color: #fff;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style> 