import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = async (_route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Wait for Keycloak initialization to complete
  if (authService.isLoading()) {
    // APP_INITIALIZER should have completed, but just in case
    return router.createUrlTree(['/']);
  }

  if (authService.isAuthenticated()) {
    return true;
  }

  // Not authenticated - redirect to login
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
