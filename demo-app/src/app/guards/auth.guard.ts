import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { LoginStore } from '../features/login/login.store';

export const authGuard: CanActivateFn = () => {
  const loginStore = inject(LoginStore);
  const router = inject(Router);
  return loginStore.isAuthenticated() ? true : router.createUrlTree(['/login']);
};

export const guestGuard: CanActivateFn = () => {
  const loginStore = inject(LoginStore);
  const router = inject(Router);

  if (loginStore.isAuthenticated()) {
    const role = loginStore.role();
    if (role === 'admin') return router.createUrlTree(['/people']);
    if (role === 'professor') return router.createUrlTree(['/professor-courses']);
    return router.createUrlTree(['/student-courses']);
  }
  return true;
};

export const roleGuard = (allowedRoles: string[]): CanActivateFn => {
  return () => {
    const loginStore = inject(LoginStore);
    const router = inject(Router);

    let role = loginStore.role();
    const isAuth = loginStore.isAuthenticated();

    if (!role) {
      const stored = sessionStorage.getItem('demo-app-auth');
      if (stored) {
        try {
          const snapshot = JSON.parse(stored) as { role: string | null };
          role = snapshot.role;
        } catch {
          role = null;
        }
      }
    }

    console.error('🛡️ RoleGuard Reject!', {
      foundRole: role,
      requiredRoles: allowedRoles,
      isAuth: isAuth
    });

    if (isAuth && role && allowedRoles.includes(role)) {
      return true;
    }

    console.error('Error: /error');
    return router.createUrlTree(['/error']);
  };

};
