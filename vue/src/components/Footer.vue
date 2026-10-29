<template>
  <footer class="site-footer">
    <div class="site-footer__inner">
      <div class="site-footer__brand">
        <img src="@/assets/imgs/logo.png" alt="校园小卖部" />
        <span>校园小卖部</span>
      </div>
      <p class="site-footer__slogan">校园里的小卖部，随时为你送达</p>

      <div class="site-footer__meta">
        <span>联系电话：040-1122334455</span>
        <span>邮箱：15871121233@163.com</span>
      </div>

      <div class="site-footer__divider"></div>

      <p class="site-footer__copy">© 2009-现在 Aiyouniganma.com 版权所有</p>
      <p class="site-footer__copy">备案：鄂ICP备050001343号 · 粤公安网备 442140442800号</p>
    </div>
  </footer>
</template>

<style scoped>
.site-footer {
  margin-top: 40px;
  background: linear-gradient(180deg, #262938 0%, #1d202c 100%);
  color: #b9bdcb;
}

.site-footer__inner {
  max-width: 1180px;
  margin: 0 auto;
  padding: 40px 20px 30px;
  text-align: center;
}

.site-footer__brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-size: 22px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 1px;
}

.site-footer__brand img {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  object-fit: cover;
}

.site-footer__slogan {
  margin: 10px 0 24px;
  font-size: 13px;
  color: #868b9c;
  letter-spacing: 2px;
}

.site-footer__meta {
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12px 48px;
  font-size: 13px;
  color: #9ba0b0;
}

.site-footer__divider {
  width: 100%;
  max-width: 420px;
  height: 1px;
  margin: 22px auto;
  background: rgba(255, 255, 255, 0.08);
}

.site-footer__copy {
  margin: 6px 0;
  font-size: 12px;
  color: #767b8c;
  line-height: 1.8;
}

@media (max-width: 768px) {
  .site-footer__inner {
    padding: 28px 16px 22px;
  }
  .site-footer__meta {
    flex-direction: column;
    gap: 6px;
  }
}
</style>
