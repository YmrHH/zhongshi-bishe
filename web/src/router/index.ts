import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const MODULES = [
  { path: 'demand', title: '中试需求管理' },
  { path: 'evaluation', title: '中试评估管理' },
  { path: 'dispatch', title: '中试调度管理' },
  { path: 'feedback', title: '中试反馈管理' },
  { path: 'archive', title: '中试档案管理' }
]

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/common/LoginView.vue'),
      meta: { public: true }
    },
    {
      path: '/center',
      component: () => import('@/layouts/CenterLayout.vue'),
      meta: { roles: ['DISPATCHER', 'AUDITOR'] },
      children: [
        {
          path: 'home',
          name: 'CenterHome',
          component: () => import('@/views/center/HomeView.vue'),
          meta: { title: '管理工作台首页', module: 'home' }
        },
        {
          path: 'dispatch/demand/workbench',
          component: () => import('@/views/center/dispatch/demand/WorkbenchView.vue'),
          meta: { title: '需求受理工作台', module: 'demand', roles: ['DISPATCHER'] }
        },
        {
          path: 'dispatch/demand/reject',
          component: () => import('@/views/center/dispatch/demand/RejectView.vue'),
          meta: { title: '退回意见页', module: 'demand', roles: ['DISPATCHER'] }
        },
        {
          path: 'dispatch/demand/archive',
          component: () => import('@/views/center/dispatch/demand/ArchiveView.vue'),
          meta: { title: '需求受理归档页', module: 'demand', roles: ['DISPATCHER'] }
        },
        {
          path: 'audit/demand/verify',
          component: () => import('@/views/center/audit/demand/VerifyView.vue'),
          meta: { title: '材料核验页', module: 'demand', roles: ['AUDITOR'] }
        },
        {
          path: 'audit/demand/accept-result',
          component: () => import('@/views/center/audit/demand/AcceptResultView.vue'),
          meta: { title: '受理结果录入页', module: 'demand', roles: ['AUDITOR'] }
        },
        {
          path: 'dispatch/evaluation/precheck',
          component: () => import('@/views/center/dispatch/evaluation/PrecheckView.vue'),
          meta: { title: '评估前置核查页', module: 'evaluation', roles: ['DISPATCHER'] }
        },
        {
          path: 'dispatch/evaluation/condition',
          component: () => import('@/views/center/dispatch/evaluation/ConditionView.vue'),
          meta: { title: '条件评估页', module: 'evaluation', roles: ['DISPATCHER'] }
        },
        {
          path: 'dispatch/evaluation/rectify-notice',
          component: () => import('@/views/center/dispatch/evaluation/RectifyNoticeView.vue'),
          meta: { title: '条件整改通知页', module: 'evaluation', roles: ['DISPATCHER'] }
        },
        {
          path: 'dispatch/evaluation/resource',
          component: () => import('@/views/center/dispatch/evaluation/ResourceView.vue'),
          meta: { title: '资源核定页', module: 'evaluation', roles: ['DISPATCHER'] }
        },
        {
          path: 'dispatch/evaluation/archive',
          component: () => import('@/views/center/dispatch/evaluation/ArchiveView.vue'),
          meta: { title: '评估归档页', module: 'evaluation', roles: ['DISPATCHER'] }
        },
        {
          path: 'audit/evaluation/feasibility',
          component: () => import('@/views/center/audit/evaluation/FeasibilityView.vue'),
          meta: { title: '可行性审查页', module: 'evaluation', roles: ['AUDITOR'] }
        },
        {
          path: 'audit/evaluation/conclusion',
          component: () => import('@/views/center/audit/evaluation/ConclusionView.vue'),
          meta: { title: '评估结论页', module: 'evaluation', roles: ['AUDITOR'] }
        },
        {
          path: 'dispatch/dispatch/match',
          component: () => import('@/views/center/dispatch/dispatch/MatchView.vue'),
          meta: { title: '中试资源匹配页', module: 'dispatch', roles: ['DISPATCHER'] }
        },
        {
          path: 'dispatch/dispatch/assign',
          component: () => import('@/views/center/dispatch/dispatch/AssignView.vue'),
          meta: { title: '中试任务派发页', module: 'dispatch', roles: ['DISPATCHER'] }
        },
        {
          path: 'dispatch/dispatch/assign-notice',
          component: () => import('@/views/center/dispatch/dispatch/AssignNoticeView.vue'),
          meta: { title: '派单结果通知页', module: 'dispatch', roles: ['DISPATCHER'] }
        },
        {
          path: 'dispatch/dispatch/supervise',
          component: () => import('@/views/center/dispatch/dispatch/SuperviseView.vue'),
          meta: { title: '进度通报督办页', module: 'dispatch', roles: ['DISPATCHER'] }
        },
        {
          path: 'dispatch/dispatch/reassign',
          component: () => import('@/views/center/dispatch/dispatch/ReassignView.vue'),
          meta: { title: '异常任务重派页', module: 'dispatch', roles: ['DISPATCHER'] }
        },
        {
          path: 'dispatch/dispatch/exec-confirm',
          component: () => import('@/views/center/dispatch/dispatch/ExecConfirmView.vue'),
          meta: { title: '执行结果确认页', module: 'dispatch', roles: ['DISPATCHER'] }
        },
        {
          path: 'dispatch/dispatch/archive',
          component: () => import('@/views/center/dispatch/dispatch/ArchiveView.vue'),
          meta: { title: '调度信息归档页', module: 'dispatch', roles: ['DISPATCHER'] }
        }
      ]
    },
    {
      path: '/enterprise',
      component: () => import('@/layouts/EnterpriseLayout.vue'),
      meta: { roles: ['ENTERPRISE'] },
      children: [
        {
          path: 'home',
          name: 'EnterpriseHome',
          component: () => import('@/views/enterprise/HomeView.vue'),
          meta: { title: '企业门户首页', module: 'home' }
        },
        {
          path: 'demand/preview',
          component: () => import('@/views/enterprise/demand/PreviewView.vue'),
          meta: { title: '需求预览确认页', module: 'demand' }
        },
        {
          path: 'demand/submit',
          component: () => import('@/views/enterprise/demand/SubmitView.vue'),
          meta: { title: '需求填报页', module: 'demand' }
        },
        {
          path: 'demand/supplement',
          component: () => import('@/views/enterprise/demand/SupplementView.vue'),
          meta: { title: '材料补充页', module: 'demand' }
        },
        {
          path: 'demand/receipt',
          component: () => import('@/views/enterprise/demand/ReceiptView.vue'),
          meta: { title: '受理回执签收页', module: 'demand' }
        },
        {
          path: 'demand/progress',
          component: () => import('@/views/enterprise/demand/ProgressView.vue'),
          meta: { title: '受理进度详情页', module: 'demand' }
        },
        {
          path: 'evaluation/condition-supplement',
          component: () => import('@/views/enterprise/evaluation/ConditionSupplementView.vue'),
          meta: { title: '条件材料补充页', module: 'evaluation' }
        },
        {
          path: 'evaluation/eval-supplement',
          component: () => import('@/views/enterprise/evaluation/EvalSupplementView.vue'),
          meta: { title: '评估材料补充页', module: 'evaluation' }
        },
        {
          path: 'evaluation/conclusion-receipt',
          component: () => import('@/views/enterprise/evaluation/ConclusionReceiptView.vue'),
          meta: { title: '评估结论签收页', module: 'evaluation' }
        },
        {
          path: 'evaluation/feedback',
          component: () => import('@/views/enterprise/evaluation/FeedbackView.vue'),
          meta: { title: '评估意见反馈页', module: 'evaluation' }
        },
        {
          path: 'evaluation/conclusion-detail',
          component: () => import('@/views/enterprise/evaluation/ConclusionDetailView.vue'),
          meta: { title: '评估结论详情页', module: 'evaluation' }
        },
        {
          path: 'dispatch/progress-view',
          component: () => import('@/views/enterprise/dispatch/ProgressViewView.vue'),
          meta: { title: '执行进度查看页', module: 'dispatch' }
        }
      ]
    },
    {
      path: '/technician',
      component: () => import('@/layouts/TechnicianLayout.vue'),
      meta: { roles: ['TECHNICIAN'] },
      children: [
        {
          path: 'home',
          name: 'TechnicianHome',
          component: () => import('@/views/technician/HomeView.vue'),
          meta: { title: '技术人员首页', module: 'home' }
        },
        {
          path: 'dispatch/receive',
          component: () => import('@/views/technician/dispatch/ReceiveView.vue'),
          meta: { title: '接收任务执行页', module: 'dispatch' }
        },
        {
          path: 'dispatch/confirm',
          component: () => import('@/views/technician/dispatch/ConfirmView.vue'),
          meta: { title: '任务接收确认页', module: 'dispatch' }
        },
        {
          path: 'dispatch/progress-report',
          component: () => import('@/views/technician/dispatch/ProgressReportView.vue'),
          meta: { title: '填报执行进度页', module: 'dispatch' }
        }
      ]
    }
  ]
})

router.beforeEach(async (to, _from, next) => {
  if (to.meta.public) {
    next()
    return
  }
  const token = localStorage.getItem('token')
  if (!token) {
    next('/login')
    return
  }
  const store = useUserStore()
  if (!store.profile) {
    try {
      await store.loadProfile()
    } catch {
      store.clear()
      next('/login')
      return
    }
  }
  const leaf = to.matched[to.matched.length - 1]
  const routeRoles = (leaf.meta.roles as string[] | undefined)
    || (to.meta.roles as string[] | undefined)
  const parentRoles = to.matched.find((r) => r.meta.roles)?.meta.roles as string[] | undefined
  const roles = routeRoles || parentRoles
  if (roles && store.profile && !roles.includes(store.profile.role)) {
    next(store.profile.homePath)
    return
  }
  next()
})

export { MODULES }
export default router
