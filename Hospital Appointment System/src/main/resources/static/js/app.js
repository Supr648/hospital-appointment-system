/**
 * MediCare Hospital Appointment System — UI helpers
 */
(function () {
    'use strict';

    // Auto-dismiss toasts & errors
    function initToasts() {
        document.querySelectorAll('.toast, .error').forEach(function (el) {
            setTimeout(function () {
                el.style.transition = 'opacity 0.4s ease, transform 0.4s ease';
                el.style.opacity = '0';
                el.style.transform = 'translateX(30px)';
                setTimeout(function () { el.remove(); }, 420);
            }, 4000);
        });
    }

    // Animate stat numbers (simple count-up)
    function animateCounters() {
        document.querySelectorAll('.stat-number').forEach(function (el) {
            var text = el.textContent.trim().replace(/[^\d]/g, '');
            var target = parseInt(text, 10);
            if (isNaN(target) || target === 0) return;

            var duration = 800;
            var start = performance.now();
            var hasDot = el.querySelector('.live-dot');

            function frame(now) {
                var progress = Math.min((now - start) / duration, 1);
                var eased = 1 - Math.pow(1 - progress, 3);
                var value = Math.round(eased * target);
                if (hasDot) {
                    el.innerHTML = '<span class="live-dot"></span>' + value;
                } else {
                    el.textContent = value;
                }
                if (progress < 1) requestAnimationFrame(frame);
            }
            requestAnimationFrame(frame);
        });
    }

    // Smooth row hover highlight is pure CSS; add ripple on buttons
    function initButtonRipple() {
        document.querySelectorAll('button, .btn').forEach(function (btn) {
            btn.addEventListener('click', function (e) {
                var rect = btn.getBoundingClientRect();
                var ripple = document.createElement('span');
                var size = Math.max(rect.width, rect.height);
                ripple.style.cssText =
                    'position:absolute;border-radius:50%;background:rgba(255,255,255,0.35);' +
                    'width:' + size + 'px;height:' + size + 'px;' +
                    'left:' + (e.clientX - rect.left - size / 2) + 'px;' +
                    'top:' + (e.clientY - rect.top - size / 2) + 'px;' +
                    'pointer-events:none;transform:scale(0);animation:ripple 0.5s ease-out forwards;';
                if (getComputedStyle(btn).position === 'static') {
                    btn.style.position = 'relative';
                }
                btn.style.overflow = 'hidden';
                btn.appendChild(ripple);
                setTimeout(function () { ripple.remove(); }, 520);
            });
        });
    }

    // Inject ripple keyframes
    function injectStyles() {
        if (document.getElementById('mc-ripple-style')) return;
        var style = document.createElement('style');
        style.id = 'mc-ripple-style';
        style.textContent =
            '@keyframes ripple{to{transform:scale(2.5);opacity:0}}';
        document.head.appendChild(style);
    }

    // Sidebar active link transition polish
    function initNav() {
        document.querySelectorAll('.sidebar-nav a').forEach(function (link) {
            link.addEventListener('mouseenter', function () {
                this.style.transition = 'all 0.25s cubic-bezier(0.4,0,0.2,1)';
            });
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        injectStyles();
        initToasts();
        animateCounters();
        initButtonRipple();
        initNav();
    });
})();
