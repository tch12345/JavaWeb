const sidebar = document.getElementById('sidebar');
const toggleBtn = document.getElementById('sidebarToggle');

toggleBtn.addEventListener('click', () => {
    sidebar.classList.toggle('open');
});

  
window.addEventListener('click', (e) => {
    if (!sidebar.contains(e.target) && !toggleBtn.contains(e.target)) {
      sidebar.classList.remove('open');
    }
});

const loginBtn = document.querySelector('.login-btn');
const loginModal = document.getElementById('loginModal');
const closeBtn = document.querySelector('.close-btn');

loginBtn.addEventListener('click', () => {
    loginModal.classList.add('show');
});

closeBtn.addEventListener('click', () => {
    loginModal.classList.remove('show');
});

  // 点击遮罩关闭
window.addEventListener('click', (e) => {
    if (e.target === loginModal) loginModal.classList.remove('show');
});