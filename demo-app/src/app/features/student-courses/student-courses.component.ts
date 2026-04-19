import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { LoginStore } from '../login/login.store';
import { CourseListStore } from '../course-list/course-list.store';

@Component({
  selector: 'app-student-courses',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatTableModule, MatIconModule],
  templateUrl: './student-courses.component.html',
  styleUrl: './student-courses.component.scss',
  providers: [CourseListStore]
})
export class StudentCoursesComponent implements OnInit {
  protected readonly loginStore = inject(LoginStore);
  protected readonly courseStore = inject(CourseListStore);
  private readonly router = inject(Router);

  ngOnInit(): void {
    this.courseStore.load();
  }

  protected logout(): void {
    this.loginStore.logout();
    void this.router.navigate(['/login']);
  }
}
