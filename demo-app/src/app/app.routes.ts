import { Routes } from '@angular/router';
import {authGuard, roleGuard} from './guards/auth.guard';
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', loadComponent: () => import('./features/login/login.component').then(m => m.LoginComponent) },

  {
    path: 'admin/people',
    loadComponent: () => import('./features/person-list/person-list-page.component').then(m => m.PersonListPageComponent),
    canActivate: [roleGuard]
  },
  {
    path: 'admin/courses',
    loadComponent: () => import('./features/course-list/course-list-page.component').then(m => m.CourseListPageComponent),
    canActivate: [authGuard, roleGuard(['admin'])]
  },
  {
    path: 'admin/departments',
    loadComponent: () => import('./features/dept-list/dept-list-page.component').then(m => m.DeptListPageComponent),
    canActivate: [authGuard, roleGuard(['admin'])]
  },
  {
    path: 'professor-courses',
    loadComponent: () => import('./features/professor-courses/professor-courses.component').then(m => m.ProfessorCoursesComponent),
    canActivate: [authGuard, roleGuard(['professor'])]
  },
  { path: 'student-courses', loadComponent: () => import('./features/student-courses/student-courses.component').then(m => m.StudentCoursesComponent),
    canActivate: [authGuard, roleGuard(['student'])]
  },
  { path: 'error', loadComponent: () => import('./features/not-found/not-found-page.component').then(m => m.NotFoundPageComponent) },
  { path: '**', redirectTo: 'admin/people' }
];
