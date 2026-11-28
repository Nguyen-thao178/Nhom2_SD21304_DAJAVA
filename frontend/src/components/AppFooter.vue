<template>
  <footer class="w-full bg-tertiary text-on-tertiary pt-space-xl pb-space-lg shadow-[0_-1px_8px_rgba(0,0,0,0.04)]">
    <div class="max-w-7xl mx-auto px-margin grid grid-cols-1 md:grid-cols-3 gap-space-xl pb-space-xl">
      <div class="flex flex-col gap-space-sm">
        <div class="flex items-center gap-space-sm">
          <div class="w-8 h-8 rounded-lg bg-secondary-container flex items-center justify-center">
            <span class="font-headline-md text-headline-md text-on-secondary-container">P.</span>
          </div>
          <span class="font-headline-sm text-headline-sm text-surface-bright tracking-tight">PolyCafe</span>
        </div>
        <p class="font-body-sm text-body-sm text-tertiary-fixed-dim leading-relaxed max-w-sm">
          Tôn vinh hạt cà phê Robusta thượng hạng từ cao nguyên Việt Nam qua phương pháp pha phin truyền thống hòa quyện cùng phong cách ẩm thực hiện đại.
        </p>
      </div>

      <div class="flex flex-col gap-space-sm">
        <h3 class="font-headline-sm text-headline-sm text-surface-bright">Khám phá</h3>
        <div class="flex flex-col gap-space-xs font-body-sm text-body-sm text-tertiary-fixed-dim">
          <a class="hover:text-surface-bright transition-colors" data-path="thuc-don" href="#menu">Thực đơn đặc sản</a>
          <a class="hover:text-surface-bright transition-colors" data-path="cau-chuyen" href="#story">Nguồn gốc Robusta</a>
          <a class="hover:text-surface-bright transition-colors" data-path="cua-hang" href="#space">Không gian thưởng thức</a>
          <a class="hover:text-surface-bright transition-colors" data-path="dat-ban" href="#contact">Đặt chỗ riêng</a>
        </div>
      </div>

      <div class="flex flex-col gap-space-sm">
        <h3 class="font-headline-sm text-headline-sm text-surface-bright">Hỗ trợ</h3>
        <div class="flex flex-col gap-space-xs font-body-sm text-body-sm text-tertiary-fixed-dim">
          <a class="hover:text-surface-bright transition-colors" data-path="lien-he" href="#contact">Trung tâm liên hệ</a>
          <a class="hover:text-surface-bright transition-colors" href="#contact">Chính sách hội viên</a>
          <a class="hover:text-surface-bright transition-colors" href="#contact">Câu hỏi thường gặp</a>
          <a class="hover:text-surface-bright transition-colors" href="#contact">Góp ý dịch vụ</a>
        </div>
      </div>
    </div>

    <div class="max-w-7xl mx-auto px-margin pt-space-lg flex flex-col sm:flex-row items-center justify-between gap-space-sm text-tertiary-fixed-dim font-body-sm text-body-sm">
      <p>© 2026 PolyCafe. Bảo lưu mọi quyền.</p>
      <p class="font-label-sm text-label-sm">Tâm huyết cà phê Việt</p>
    </div>
  </footer>
</template>
