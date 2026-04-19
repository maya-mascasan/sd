import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { finalize } from 'rxjs';
import { Department, DepartmentCreateDTO } from '../../models/department.model';
import { DepartmentService } from '../../services/department.service';

@Injectable({ providedIn: 'root' })
export class DeptListStore {
  private readonly deptService = inject(DepartmentService);
  private readonly pendingRequests = signal(0);

  readonly departments = signal<Department[]>([]);
  readonly hasError = signal(false);
  readonly errorMsg = signal<string>('');

  readonly isLoading = computed(() => this.pendingRequests() > 0);

  private beginRequest(): void {
    this.pendingRequests.update((count) => count + 1);
  }

  private endRequest(): void {
    this.pendingRequests.update((count) => Math.max(0, count - 1));
  }

  load(): void {
    this.errorMsg.set('');
    this.hasError.set(false);
    this.beginRequest();
    this.deptService.getDepartments()
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (data) => this.departments.set(data),
        error: (err: HttpErrorResponse) => this.handleError(err),
      });
  }

  create(dto: DepartmentCreateDTO): void {
    this.errorMsg.set('');
    this.hasError.set(false);
    this.beginRequest();
    this.deptService.addDepartment(dto)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (created) => this.departments.update((list) => [...list, created]),
        error: (err: HttpErrorResponse) => this.handleError(err)
      });
  }

  update(id: string, dto: DepartmentCreateDTO): void {
    this.errorMsg.set('');
    this.hasError.set(false);
    this.beginRequest();
    this.deptService.updateDepartment(id, dto)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (updated) => {
          this.departments.update((list) =>
            list.map((dept) => (dept.id === updated.id ? updated : dept))
          );
        },
        error: (err: HttpErrorResponse) => this.handleError(err)
      });
  }

  remove(id: string): void {
    this.beginRequest();
    this.deptService.deleteDepartment(id)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: () => this.departments.update((list) => list.filter((d) => d.id !== id)),
        error: (err: HttpErrorResponse) => this.handleError(err),
      });
  }


  private handleError(err: HttpErrorResponse): void {
    this.hasError.set(true);

    const errorData = err.error as Record<string, unknown> | null;
    const details = errorData?.['details'];
    const errors = errorData?.['errors'] as Record<string, string> | null;
    const firstErrorDetail = errors ? Object.values(errors)[0] : null;
    const firstGenericValue = errorData ? Object.values(errorData)[0] : null;

    const rawMessage = details || firstErrorDetail || firstGenericValue;

    if (typeof rawMessage === 'string') {
      this.errorMsg.set(rawMessage);
    } else {
      this.errorMsg.set('An unexpected error occurred!');
    }
  }
}
