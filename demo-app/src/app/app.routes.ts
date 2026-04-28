import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', loadComponent: () => import('./features/login/login.component').then(m => m.LoginComponent) },
  {
    path: 'admin/people',
    canActivate: [authGuard, roleGuard(['admin'])],
    loadComponent: () => import('./features/person-list/person-list-page.component').then(m => m.PersonListPageComponent)
  },
  {
    path: 'admin/courses',
    canActivate: [authGuard, roleGuard(['admin'])],
    loadComponent: () => import('./features/course-list/course-list-page.component').then(m => m.CourseListPageComponent)
  },
  {
    path: 'admin/departments',
    canActivate: [authGuard, roleGuard(['admin'])],
    loadComponent: () => import('./features/dept-list/dept-list-page.component').then(m => m.DeptListPageComponent)
  },
  {
    path: 'professor-courses',
    canActivate: [authGuard, roleGuard(['professor'])],
    loadComponent: () => import('./features/professor-courses/professor-courses.component').then(m => m.ProfessorCoursesComponent)
  },
  {
    path: 'student-courses',
    canActivate: [authGuard, roleGuard(['student'])],
    loadComponent: () => import('./features/student-courses/student-courses.component').then(m => m.StudentCoursesComponent)
  },
  { path: 'forgot-password', loadComponent: () => import('./features/forgot-password/forgot-password.component').then(m => m.ForgotPasswordComponent) },
  { path: 'error', loadComponent: () => import('./features/not-found/not-found-page.component').then(m => m.NotFoundPageComponent) },
  { path: '**', redirectTo: 'login' }
];
