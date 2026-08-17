<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'

const API = 'http://localhost:8080'
const menu = ['数据看板', '会员卡管理', '通知发布', '用户权限', '教练入驻', '退款审核', '课程安排', '系统配置']
const active = ref(0)
const message = ref('')

// 数据
const username = ref('')
const report = ref<any>(null)
const stats = ref<any>(null)
const trend = ref<any[]>([])
const topCourses = ref<any[]>([])
const cards = ref<any[]>([])
const users = ref<any[]>([])
const applications = ref<any[]>([])
const refunds = ref<any[]>([])
const courses = ref<any[]>([])
const configs = ref<any[]>([])

// 通知表单
const noticeTitle = ref('')
const noticeContent = ref('')
const noticeRole = ref('ALL')
const noticeUserIds = ref('')

// 会员卡表单
const cardName = ref('')
const cardDays = ref('30')
const cardPrice = ref('')
const cardDesc = ref('')

// 课程表单
const courseTitle = ref('')
const courseType = ref('GROUP')
const courseCoach = ref('')
const courseTime = ref('')
const courseCapacity = ref('20')
const coursePrice = ref('')

const duration = computed(() => {
  const sec = report.value?.totalDurationSeconds ?? 0
  return `${Math.floor(sec / 3600)}小时${Math.floor((sec % 3600) / 60)}分`
})
const maxTrend = computed(() => Math.max(1, ...trend.value.map((p) => p.count ?? 0)))

async function request(path: string, options?: RequestInit) {
  const response = await fetch(`${API}${path}`, { headers: { 'Content-Type': 'application/json' }, ...options })
  let body: any = null
  try { body = await response.json() } catch { /* no body */ }
  if (!response.ok) throw new Error(body?.msg || '请求失败')
  return body?.data !== undefined ? body.data : body
}

async function loadData() {
  try {
    ;[cards.value, users.value, applications.value, courses.value, refunds.value, configs.value] = await Promise.all([
      request('/api/cards?all=true'),
      request('/api/users'),
      request('/coach/admin/apply/list'),
      request('/api/courses'),
      request('/api/refunds/pending'),
      request('/api/config'),
    ])
  } catch (e) {
    message.value = '数据加载失败：' + e
  }
}
async function loadDashboard() {
  try {
    ;[stats.value, trend.value, topCourses.value] = await Promise.all([
      request('/api/admin/dashboard/stats'),
      request('/api/admin/dashboard/trend'),
      request('/api/admin/dashboard/top-courses'),
    ])
  } catch (e) {
    message.value = '看板数据加载失败：' + e
  }
}
async function queryReport() {
  if (!username.value.trim()) return
  try {
    report.value = await request(`/attendance/monthly/${encodeURIComponent(username.value.trim())}`)
    message.value = '统计已更新'
  } catch (e) {
    message.value = '查询失败：' + e
  }
}

async function publishNotice() {
  if (!noticeTitle.value || !noticeContent.value) return
  await request('/api/notifications', {
    method: 'POST',
    body: JSON.stringify({
      title: noticeTitle.value,
      content: noticeContent.value,
      targetRole: noticeRole.value,
      targetUserIds: noticeUserIds.value.trim() || null,
    }),
  })
  noticeTitle.value = ''
  noticeContent.value = ''
  noticeUserIds.value = ''
  message.value = '通知已发布'
}

async function review(id: number, pass: boolean) {
  if (pass) {
    await request(`/coach/admin/apply/pass/${id}`, { method: 'PUT' })
  } else {
    const reason = window.prompt('请输入拒绝原因（可选）') ?? ''
    await request(`/coach/admin/apply/reject/${id}?reason=${encodeURIComponent(reason)}`, { method: 'PUT' })
  }
  message.value = pass ? '已通过入驻申请' : '已拒绝入驻申请'
  await loadData()
}

async function approveRefund(id: number) {
  await request(`/api/refunds/${id}/approve`, { method: 'PUT' })
  message.value = '退款已通过，订单已退款'
  await loadData()
}
async function rejectRefund(id: number) {
  const reason = window.prompt('请输入拒绝原因') ?? ''
  await request(`/api/refunds/${id}/reject?reason=${encodeURIComponent(reason)}`, { method: 'PUT' })
  message.value = '退款已拒绝'
  await loadData()
}

async function toggleUser(user: any) {
  await request(`/api/users/${user.id}/status?value=${user.status === 'NORMAL' ? 'DISABLED' : 'NORMAL'}`, { method: 'PUT' })
  await loadData()
}

async function saveCard() {
  if (!cardName.value || !cardPrice.value) return
  await request('/api/cards', {
    method: 'POST',
    body: JSON.stringify({
      name: cardName.value,
      validDays: Number(cardDays.value),
      price: cardPrice.value,
      description: cardDesc.value,
      enabled: true,
    }),
  })
  cardName.value = ''
  cardPrice.value = ''
  cardDesc.value = ''
  message.value = '卡种已新增'
  await loadData()
}
async function toggleCard(card: any) {
  await request(`/api/cards/${card.id}`, {
    method: 'PUT',
    body: JSON.stringify({ name: card.name, validDays: card.validDays, price: card.price, description: card.description, enabled: !card.enabled }),
  })
  await loadData()
}

async function saveCourse() {
  if (!courseTitle.value || !courseTime.value) return
  await request('/api/courses', {
    method: 'POST',
    body: JSON.stringify({
      title: courseTitle.value,
      type: courseType.value,
      coachName: courseCoach.value,
      startTime: courseTime.value,
      capacity: Number(courseCapacity.value),
      price: coursePrice.value,
    }),
  })
  courseTitle.value = ''
  courseTime.value = ''
  courseCoach.value = ''
  message.value = '课程已新增'
  await loadData()
}

async function saveConfig(key: string, value: string) {
  await request(`/api/config/${key}?value=${encodeURIComponent(value)}`, { method: 'PUT' })
  message.value = '配置已更新'
  await loadData()
}

onMounted(() => {
  loadData()
  loadDashboard()
})
</script>

<template>
  <div class="layout">
    <aside>
      <div class="brand"><span>G</span><div>STAR GYM<small>管理后台</small></div></div>
      <button v-for="(item, i) in menu" :key="item" :class="{ active: active === i }" @click="active = i">{{ item }}</button>
      <div class="operator">管理员<small>系统运营账户</small></div>
    </aside>
    <main>
      <header>
        <div><p>健身房运营中心</p><h1>{{ menu[active] }}</h1></div>
        <span class="date">{{ new Date().toLocaleDateString('zh-CN') }}</span>
      </header>
      <p v-if="message" class="message">{{ message }}</p>

      <!-- 数据看板 -->
      <template v-if="active === 0">
        <section class="cards">
          <article><p>会员总数</p><strong>{{ stats?.totalUsers ?? '--' }}</strong><small>当前注册用户</small></article>
          <article><p>今日入场人数</p><strong>{{ stats?.todayVisits ?? '--' }}</strong><small>今日进出场扫码</small></article>
          <article><p>本月新注册</p><strong>{{ stats?.monthNewUsers ?? '--' }}</strong><small>本月新增用户</small></article>
          <article><p>本月总收入</p><strong>¥{{ stats?.monthRevenue ?? '--' }}</strong><small>本月会员卡收入</small></article>
        </section>
        <section class="panel">
          <h2>近 7 天入场趋势</h2>
          <div class="trend">
            <div v-for="p in trend" :key="p.date" class="bar-col">
              <div class="bar" :style="{ height: (p.count / maxTrend) * 100 + '%' }"></div>
              <span class="bar-val">{{ p.count }}</span>
              <span class="bar-label">{{ p.date.slice(5) }}</span>
            </div>
          </div>
        </section>
        <section class="panel">
          <h2>热门课程 TOP 3</h2>
          <div v-for="(c, i) in topCourses" :key="c.courseId" class="row">
            <b>#{{ i + 1 }} {{ c.title }}</b>
            <span>{{ c.type }}</span>
            <em>{{ c.bookings }} 人预约</em>
          </div>
          <p v-if="!topCourses.length">暂无预约数据</p>
        </section>
        <section class="panel">
          <h2>会员月度运动统计</h2>
          <p>同一自然日去重；入场扫码开始计时，出场扫码停止计时。</p>
          <div class="search">
            <input v-model="username" placeholder="输入会员用户名" @keyup.enter="queryReport" />
            <button @click="queryReport">查询</button>
          </div>
          <div v-if="report" class="days">
            <b>运动日期</b>
            <span v-for="day in report.activeDayNumbers" :key="day">{{ day }} 日</span>
            <small>共 {{ report.activeDays }} 天，累计 {{ duration }}</small>
          </div>
        </section>
      </template>

      <!-- 会员卡管理 -->
      <section v-else-if="active === 1" class="panel">
        <h2>新增卡种</h2>
        <div class="form-row">
          <input v-model="cardName" placeholder="卡名（如 月卡）" />
          <input v-model="cardDays" type="number" placeholder="有效天数" />
          <input v-model="cardPrice" placeholder="价格" />
          <input v-model="cardDesc" placeholder="描述" />
          <button @click="saveCard">新增</button>
        </div>
        <h2>卡种列表</h2>
        <div v-for="card in cards" :key="card.id" class="row">
          <div><b>{{ card.name }}</b><small>{{ card.validDays }} 天 · {{ card.description }}</small></div>
          <span>¥{{ card.price }}</span>
          <em>{{ card.enabled ? '上架中' : '已下架' }}</em>
          <button class="ghost" @click="toggleCard(card)">{{ card.enabled ? '下架' : '上架' }}</button>
        </div>
      </section>

      <!-- 通知发布 -->
      <section v-else-if="active === 2" class="panel">
        <h2>发布通知</h2>
        <input v-model="noticeTitle" placeholder="通知标题" />
        <textarea v-model="noticeContent" placeholder="通知内容"></textarea>
        <div class="form-row">
          <select v-model="noticeRole">
            <option value="ALL">所有人</option>
            <option value="USER">会员</option>
            <option value="COACH">教练</option>
            <option value="ADMIN">管理员</option>
          </select>
          <input v-model="noticeUserIds" placeholder="定向用户 ID（逗号分隔，留空=全部）" />
        </div>
        <button @click="publishNotice">发布通知</button>
      </section>

      <!-- 用户权限 -->
      <section v-else-if="active === 3" class="panel">
        <h2>管理用户权限</h2>
        <div v-for="user in users" :key="user.id" class="row">
          <div><b>{{ user.username }}</b><small>{{ user.phone || '无手机号' }} · 注册于 {{ user.createTime }}</small></div>
          <span>{{ user.role }}</span>
          <button @click="toggleUser(user)">{{ user.status === 'NORMAL' ? '禁用' : '恢复' }}</button>
        </div>
      </section>

      <!-- 教练入驻 -->
      <section v-else-if="active === 4" class="panel">
        <h2>处理教练入驻</h2>
        <div v-for="item in applications" :key="item.id" class="row">
          <div><b>{{ item.name }}</b><small>{{ item.phone }} · {{ item.description }} · 资质：{{ item.proofMaterial }}</small></div>
          <span>{{ item.status }}</span>
          <div v-if="item.status === 'PENDING'">
            <button @click="review(item.id, true)">通过</button>
            <button class="ghost" @click="review(item.id, false)">拒绝</button>
          </div>
        </div>
      </section>

      <!-- 退款审核 -->
      <section v-else-if="active === 5" class="panel">
        <h2>退款审核</h2>
        <p v-if="!refunds.length">暂无待审核的退款申请</p>
        <div v-for="r in refunds" :key="r.id" class="row">
          <div><b>{{ r.cardName }}</b><small>订单 #{{ r.orderId }} · {{ r.reason }} · {{ r.description }}</small></div>
          <span>¥{{ r.refundAmount }}</span>
          <button @click="approveRefund(r.id)">通过</button>
          <button class="ghost" @click="rejectRefund(r.id)">拒绝</button>
        </div>
      </section>

      <!-- 课程安排 -->
      <section v-else-if="active === 6" class="panel">
        <h2>新增课程</h2>
        <div class="form-row">
          <input v-model="courseTitle" placeholder="课程标题" />
          <select v-model="courseType">
            <option value="GROUP">团课</option>
            <option value="PT">私教</option>
          </select>
          <input v-model="courseCoach" placeholder="教练姓名" />
          <input v-model="courseTime" placeholder="开始时间 2026-08-20T19:00" />
          <input v-model="courseCapacity" type="number" placeholder="容量" />
          <input v-model="coursePrice" placeholder="价格" />
          <button @click="saveCourse">新增</button>
        </div>
        <h2>课程列表</h2>
        <div v-for="course in courses" :key="course.id" class="row">
          <div><b>{{ course.title }}</b><small>{{ course.type }} · {{ course.coachName || '待安排' }}</small></div>
          <span>容量 {{ course.capacity }} · {{ course.startTime }}</span>
        </div>
      </section>

      <!-- 系统配置 -->
      <section v-else class="panel">
        <h2>系统配置</h2>
        <div v-for="c in configs" :key="c.id" class="row">
          <div><b>{{ c.configKey }}</b><small>{{ c.description }}</small></div>
          <input :value="c.configValue" @blur="saveConfig(c.configKey, ($event.target as HTMLInputElement).value)" />
        </div>
      </section>
    </main>
  </div>
</template>

<style scoped>
* { box-sizing: border-box }
.layout { min-height: 100vh; display: flex; background: #f7f8fb; color: #19152a }
aside { width: 238px; background: #171325; color: #ece8f9; padding: 26px 14px; display: flex; flex-direction: column; gap: 6px }
.brand { display: flex; align-items: center; gap: 10px; font-weight: 800; letter-spacing: 1px; margin: 0 8px 34px }
.brand span { width: 40px; height: 40px; display: grid; place-items: center; border-radius: 12px; background: #a75dea; color: #fff; font-size: 22px }
.brand small, small { display: block; font-size: 12px; opacity: .65; font-weight: 400; letter-spacing: 0; margin-top: 3px }
aside button { border: 0; background: transparent; color: #c9c3db; padding: 12px 14px; border-radius: 8px; text-align: left; font-size: 14px; cursor: pointer }
aside button.active, aside button:hover { background: #302547; color: #fff }
.operator { margin: auto 8px 0; padding-top: 16px; border-top: 1px solid #3a304f }
main { flex: 1; padding: 30px 48px; max-width: 1300px }
header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 22px }
header p { margin: 0; color: #746d84 }
h1 { margin: 3px 0 0; font-size: 30px }
.date { background: #ece9f4; padding: 9px 12px; border-radius: 8px; color: #655e75 }
.message { background: #efe5fa; border-left: 3px solid #a75dea; padding: 10px 12px }
.cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px }
article, .panel { background: #fff; border: 1px solid #ebe8f0; border-radius: 14px; padding: 22px; box-shadow: 0 4px 16px #2015320b; margin-bottom: 20px }
article p { margin: 0; color: #746d84 }
article strong { font-size: 26px; display: block; margin: 12px 0 4px }
.panel h2 { margin: 0 0 8px }
.panel > p { color: #746d84 }
.search { display: flex; gap: 10px; max-width: 520px }
input, textarea, select { width: 100%; border: 1px solid #d9d4e2; border-radius: 8px; padding: 11px 12px; font: inherit; margin: 7px 0; background: #fff }
textarea { min-height: 120px; resize: vertical }
button { border: 0; border-radius: 8px; background: #9053d9; color: #fff; padding: 10px 18px; cursor: pointer }
.search input { margin: 0 }
.form-row { display: flex; gap: 8px; flex-wrap: wrap }
.form-row input, .form-row select { flex: 1; min-width: 120px; margin: 4px 0 }
.days { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 20px; padding-top: 18px; border-top: 1px solid #efedf2; align-items: center }
.days span { background: #eee4fb; color: #7b3fbb; border-radius: 20px; padding: 6px 10px }
.days small { opacity: .8; margin-left: 8px }
.trend { display: flex; align-items: flex-end; gap: 14px; height: 200px; padding-top: 20px }
.bar-col { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: flex-end; height: 100% }
.bar { width: 100%; max-width: 40px; background: linear-gradient(180deg, #a75dea, #9053d9); border-radius: 6px 6px 0 0; min-height: 2px; transition: height .3s }
.bar-val { font-size: 12px; margin: 4px 0; color: #5d566b }
.bar-label { font-size: 11px; color: #746d84 }
.row { display: flex; align-items: center; gap: 18px; padding: 14px 0; border-bottom: 1px solid #f0edf3 }
.row:last-child { border-bottom: 0 }
.row > div:first-child { flex: 1 }
.row span { color: #746d84 }
.row em { color: #7b3fbb; font-style: normal }
.row input { max-width: 240px; margin: 0 }
.ghost { background: #ece8f2; color: #5d566b; margin-left: 6px }
@media (max-width: 760px) {
  aside { width: 68px; padding: 16px 8px }
  .brand div, aside button, .operator { font-size: 0 }
  .brand { margin: 0 6px 25px }
  main { padding: 22px 16px }
  .cards { grid-template-columns: 1fr 1fr }
  .date { display: none }
  .row { align-items: flex-start; flex-wrap: wrap }
}
</style>
