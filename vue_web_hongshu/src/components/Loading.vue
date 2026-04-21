<template>
  <div class="hs-loading" aria-hidden="true">
    <span class="hs-loading__ring" />
  </div>
</template>

<style lang="less" scoped>
.hs-loading {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1em;
  height: 1em;
  vertical-align: middle;
}

.hs-loading__ring {
  box-sizing: border-box;
  width: 100%;
  height: 100%;
  border: 2px solid rgba(24, 144, 255, 0.22);
  border-top-color: #1890ff;
  border-radius: 50%;
  animation: hs-loading-spin 0.7s linear infinite;
}

@keyframes hs-loading-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
