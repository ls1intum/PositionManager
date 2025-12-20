import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = async (_route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  // If OAuth callback just failed, redirect to landing page instead of looping
  if (authService.callbackFailed()) {
    return router.createUrlTree(['/']);
  }

  await authService.login(window.location.origin + state.url);
  return false;
};

export const roleGuard =
  (requiredRoles: string[]): CanActivateFn =>
  () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
      return router.createUrlTree(['/']);
    }

    const hasRequiredRole = requiredRoles.some((role) => authService.hasRole(role));
    if (!hasRequiredRole) {
      return router.createUrlTree(['/unauthorized']);
    }

    return true;
  };
