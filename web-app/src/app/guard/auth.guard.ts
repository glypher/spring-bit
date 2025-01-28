import {CanActivateFn, Router} from '@angular/router';
import {inject} from "@angular/core";
import {AuthService} from "../service/auth.service";

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  } else {
    // Save current url for redirect after login
    localStorage.setItem("returnUrlLogin", state.url)
    // Redirect to login if not authenticated
    router.navigate(['/login'],  { skipLocationChange: true });
    return false;
  }
};
