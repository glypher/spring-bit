import { Routes } from '@angular/router';
import {LoginComponent} from "./login/login.component";
import {AdminComponent} from "./admin/admin.component";
import {authGuard} from "./guard/auth.guard";
import {MainComponent} from "./main/main.component";

export const routes: Routes = [
  { path: '', component: MainComponent },
  { path: 'login', component: LoginComponent },
  { path: 'admin/:type', component: AdminComponent, canActivate: [authGuard], pathMatch: "prefix" },
  { path: '**', redirectTo: ''}
];
