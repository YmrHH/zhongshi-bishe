<script>
export default {
  onLaunch() {
    const token = uni.getStorageSync('token')
    const profile = uni.getStorageSync('profile')
    if (!token || !profile) return
    if (profile.role === 'TECHNICIAN') {
      uni.reLaunch({ url: '/pages/technician/home' })
    } else if (profile.role === 'ENTERPRISE') {
      uni.reLaunch({ url: '/pages/enterprise/home' })
    }
  },
  onShow() {},
  onHide() {}
}
</script>

<style>
page {
  background-color: #f5f5f5;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}
</style>
