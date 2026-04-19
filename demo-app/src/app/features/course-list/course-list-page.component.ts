import { ChangeDetectionStrategy, Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatToolbarModule } from '@angular/material/toolbar';
import {Router, RouterLink, RouterLinkActive} from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

import { CourseListStore } from './course-list.store';
import { LoginStore } from '../login/login.store';
import { Course, CourseCreateDTO } from '../../models/course.model';
import { CourseFormDialogComponent } from '../../components/course-form-dialog/course-form-dialog.component';
import { ComponentType } from '@angular/cdk/portal';

import { MatSortModule, Sort } from '@angular/material/sort';
@Component({
  selector: 'app-course-list-page',
  standalone: true,
  imports: [
    CommonModule, MatTableModule, MatButtonModule, MatIconModule,
    MatDialogModule, MatToolbarModule, MatFormFieldModule, MatInputModule, FormsModule, RouterLink, RouterLinkActive, MatSortModule
  ],
  templateUrl: './course-list-page.component.html',
  styleUrl: './course-list-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CourseListPageComponent {
  private readonly dialog = inject(MatDialog);
  protected readonly store = inject(CourseListStore);
  private readonly destroyRef = inject(DestroyRef);
  private readonly loginStore = inject(LoginStore);
  private readonly router = inject(Router);

  protected readonly filteredCourses = this.store.filteredCourses;
  protected readonly isLoading = this.store.isLoading;
  protected readonly hasError = this.store.hasError;
  protected readonly displayedColumns = ['title', 'credits', 'department', 'actions'];

  constructor() {
    this.store.load();
  }

  protected logout(): void {
    this.loginStore.logout();
    void this.router.navigate(['/login']);
  }

  protected openCreateDialog(): void {
    if (this.isLoading()) return;

    this.dialog.open(CourseFormDialogComponent as ComponentType<unknown>, {
      width: '400px',
      data: { title: 'Create Course', submitLabel: 'Create' }})
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result: CourseCreateDTO) => {
        if (!result) return;
        this.store.create(result);
      });
  }

  protected openDeleteDialog(course: Course): void {
    if (this.isLoading()) return;

    if (confirm(`Delete course ${course.title}?`)) {
      this.store.remove(course.id);
    }
  }

  protected openEditDialog(course: Course): void {
    if (this.isLoading()) return;

    this.dialog.open(CourseFormDialogComponent as ComponentType<unknown>, {
      width: '400px',
      data: {
        title: 'Edit Course',
        submitLabel: 'Save',
        initialValue: {
          title: course.title,
          credits: course.credits,
          departmentId: course.department?.id
        }
      }
    })
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result: CourseCreateDTO) => {
        if (!result) return;
        this.store.update(course.id, result);
      });
  }
  protected onSortChange(sortState: Sort): void {
    this.store.sortActive.set(sortState.active);
    this.store.sortDirection.set(sortState.direction);
  }
}
