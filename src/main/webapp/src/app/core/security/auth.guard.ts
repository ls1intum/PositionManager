import { inject, Injector } from '@angular/core';
import { toObservable } from '@angular/core/rxjs-interop';
import { CanActivateFn, Router } from '@angular/router';
import { SecurityStore } from './security-store.service';
import { filter, map, switchMap, take } from 'rxjs';

export const authGuard: CanActivateFn = () => {
  const injector = inject(Injector);
  const securityStore = inject(SecurityStore);
  const router = inject(Router);

  return toObservable(securityStore.isLoading, { injector }).pipe(
    filter((loading) => !loading),
    take(1),
    switchMap(() => toObservable(securityStore.user, { injector }).pipe(take(1))),
    map((user) => {
      if (user !== undefined) {
        return true;
      }
      securityStore.signIn(router.url);
      return false;
    }),
  );
};
