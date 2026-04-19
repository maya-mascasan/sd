import { Routes } from '@angular/router';
export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  { path: 'login', loadComponent: () => import('./features/login/login.component').then(m => m.LoginComponent) },

  {
    path: 'admin/people',
    loadComponent: () => import('./features/person-list/person-list-page.component').then(m => m.PersonListPageComponent)
  },
  {
    path: 'admin/courses',
    loadComponent: () => import('./features/course-list/course-list-page.component').then(m => m.CourseListPageComponent)
  },
  {
    path: 'admin/departments',
    loadComponent: () => import('./features/dept-list/dept-list-page.component').then(m => m.DeptListPageComponent)
  },
  {
    path: 'professor-courses',
    loadComponent: () => import('./features/professor-courses/professor-courses.component').then(m => m.ProfessorCoursesComponent)
  },
  { path: 'student-courses', loadComponent: () => import('./features/student-courses/student-courses.component').then(m => m.StudentCoursesComponent) },
  { path: 'error', loadComponent: () => import('./features/not-found/not-found-page.component').then(m => m.NotFoundPageComponent) },
  { path: '**', redirectTo: 'admin/people' }
];
