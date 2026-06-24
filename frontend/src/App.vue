<template>
  <router-view />
</template>

<style>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap');

:root {
  --primary: #6366f1;
  --primary-light: #818cf8;
  --primary-dark: #4f46e5;
  --success: #10b981;
  --warning: #f59e0b;
  --danger: #ef4444;
  --info: #6b7280;

  --bg-main: #f5f6f8;
  --bg-card: #ffffff;
  --bg-sidebar: #ffffff;
  --bg-sidebar-light: #f0f2f5;

  --text-primary: #1e293b;
  --text-secondary: #64748b;
  --text-muted: #94a3b8;

  --border-color: #e2e8f0;
  --border-radius: 12px;
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.04);
  --shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.07), 0 2px 4px -2px rgba(0, 0, 0, 0.05);
  --shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.08), 0 4px 6px -4px rgba(0, 0, 0, 0.05);
  --shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.05);

  --transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: 'Inter', 'PingFang SC', 'Microsoft YaHei', -apple-system, BlinkMacSystemFont, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: var(--text-primary);
  background: var(--bg-main);
}

/* Element Plus Overrides */
.el-button--primary {
  background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
  border: none;
  border-radius: 10px;
  font-weight: 500;
  transition: var(--transition);
}
.el-button--primary:hover {
  background: linear-gradient(135deg, var(--primary-light) 0%, var(--primary) 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(99, 102, 241, 0.4);
}
.el-button--primary:active {
  transform: translateY(0);
}

.el-button {
  border-radius: 10px;
  font-weight: 500;
  transition: var(--transition);
}

.el-card {
  border-radius: var(--border-radius);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-sm);
  transition: var(--transition);
}
.el-card:hover {
  box-shadow: var(--shadow-md);
}

.el-table {
  border-radius: var(--border-radius);
  overflow: hidden;
}
.el-table th.el-table__cell {
  background: #f8fafc !important;
  color: var(--text-secondary);
  font-weight: 600;
  font-size: 13px;
  text-transform: uppercase;
  letter-spacing: 0.03em;
}
.el-table td.el-table__cell {
  color: var(--text-primary);
}
.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell {
  background: #fafbfc;
}

.el-dialog {
  border-radius: 20px !important;
  overflow: hidden;
}
.el-dialog__header {
  background: linear-gradient(135deg, #f8fafc 0%, #eef2ff 100%);
  padding: 20px 24px !important;
  border-bottom: 1px solid var(--border-color);
}
.el-dialog__title {
  font-weight: 600 !important;
  font-size: 17px !important;
  color: var(--text-primary);
}

.el-tag {
  border-radius: 8px;
  font-weight: 500;
}

.el-pagination {
  --el-pagination-button-bg-color: transparent;
}
.el-pagination .btn-prev, .el-pagination .btn-next, .el-pagination .el-pager li {
  border-radius: 8px !important;
}

/* Scrollbar */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* Page transitions */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.25s ease;
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
