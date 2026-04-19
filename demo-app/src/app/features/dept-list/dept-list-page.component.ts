import { ChangeDetectionStrategy, Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatToolbarModule } from '@angular/material/toolbar';

import { DeptListStore } from './dept-list.store';
import { LoginStore } from '../login/login.store';
import { Department, DepartmentCreateDTO } from '../../models/department.model';

import { ConfirmDeleteDialogComponent } from '../../components/confirm-delete-dialog/confirm-delete-dialog.component';
import { DeptFormDialogComponent } from '../../components/dept-form-dialog/dept-form-dialog.component';
@Component({
  selector: 'app-dept-list-page',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatToolbarModule,
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './dept-list-page.component.html',
  styleUrl: './dept-list-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeptListPageComponent {
  private readonly dialog = inject(MatDialog);
  protected readonly store = inject(DeptListStore);
  private readonly destroyRef = inject(DestroyRef);
  private readonly loginStore = inject(LoginStore);
  private readonly router = inject(Router);

  protected readonly departments = this.store.departments;
  protected readonly isLoading = this.store.isLoading;
  protected readonly hasError = this.store.hasError;

  protected readonly displayedColumns = ['name', 'actions'];

  constructor() {
    this.store.load();
  }

  protected logout(): void {
    this.loginStore.logout();
    void this.router.navigate(['/login']);
  }

  protected openCreateDialog(): void {
    if (this.isLoading()) return;

    const dialogRef = this.dialog.open(DeptFormDialogComponent, {
      width: '400px',
      data: { title: 'Create Department', submitLabel: 'Create' }
    });

    dialogRef.afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result: DepartmentCreateDTO) => {
        if (!result) return;
        this.store.create(result);
      });
  }

  protected openEditDialog(dept: Department): void {
    if (this.isLoading()) return;

    const dialogRef = this.dialog.open(DeptFormDialogComponent, {
      width: '400px',
      data: {
        title: 'Edit Department',
        submitLabel: 'Save',
        initialValue: { name: dept.name }
      }
    });

    dialogRef.afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result: DepartmentCreateDTO) => {
        if (!result) return;
        this.store.update(dept.id, result);
      });
  }

  protected openDeleteDialog(dept: Department): void {
    if (this.isLoading()) return;

    this.dialog.open(ConfirmDeleteDialogComponent, {
      data: { person: { name: dept.name } }
    })
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmed) => {
        if (!confirmed) return;
        this.store.remove(dept.id);
      });
  }
}
