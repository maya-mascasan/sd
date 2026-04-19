import { computed, inject, Injectable, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { Course, CourseCreateDTO } from '../../models/course.model';
import { CourseService } from '../../services/course.service';
import { HttpErrorResponse } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class CourseListStore {
  private readonly courseService = inject(CourseService);
  private readonly pendingRequests = signal(0);

  readonly courses = signal<Course[]>([]);
  readonly hasError = signal(false);
  readonly errorMsg = signal<string>('');

  // search, filter and sort
  readonly searchQuery = signal('');
  readonly minCredits = signal(0);
  readonly sortActive = signal<string>('title');
  readonly sortDirection = signal<'asc' | 'desc' | ''>('asc');
  readonly isLoading = computed(() => this.pendingRequests() > 0);

  readonly filteredCourses = computed(() => {
    const query = this.searchQuery().toLowerCase();
    const min = this.minCredits();
    const active = this.sortActive();
    const direction = this.sortDirection();

    // filter
    const result = this.courses()
      .filter(c => c.title.toLowerCase().includes(query))
      .filter(c => c.credits >= min);

    // sort
    if (direction !== '') {
      result.sort((a, b) => {
        let valA: string | number = '';
        let valB: string | number = '';

        if (active === 'title') {
          valA = a.title.toLowerCase();
          valB = b.title.toLowerCase();
        } else if (active === 'credits') {
          valA = a.credits;
          valB = b.credits;
        } else if (active === 'department') {
          valA = a.department?.name?.toLowerCase() || '';
          valB = b.department?.name?.toLowerCase() || '';
        }

        if (valA < valB) {
          return direction === 'asc' ? -1 : 1;
        } else if (valA > valB) {
          return direction === 'asc' ? 1 : -1;
        } else {
          return 0;
        }
      });
    }

    return result;
  });

  private beginRequest(): void {
    this.pendingRequests.update((count) => count + 1);
  }

  private endRequest(): void {
    this.pendingRequests.update((count) => Math.max(0, count - 1));
  }

  load(): void {
    this.errorMsg.set('');
    this.beginRequest();
    this.courseService.getCourses()
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (data) => this.courses.set(data),
        error: () => this.hasError.set(true),
      });
  }

  create(dto: CourseCreateDTO): void {
    this.errorMsg.set('');
    this.beginRequest();
    this.courseService.addCourse(dto)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (created) => this.courses.update((list) => [...list, created]),
        error: (err: HttpErrorResponse) => this.handleError(err)
      });
  }

  remove(id: string): void {
    this.beginRequest();
    this.courseService.deleteCourse(id)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: () => this.courses.update((list) => list.filter((c) => c.id !== id)),
        error: (err) => console.error('Delete failed', err),
      });
  }


  update(id: string, dto: CourseCreateDTO): void {
    this.errorMsg.set('');
    this.hasError.set(false);
    this.beginRequest();

    this.courseService
      .update(id, dto)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (updated) => {
          this.courses.update((list) =>
            list.map((course) => (course.id === updated.id ? updated : course))
          );
        },
        error: (err: HttpErrorResponse) => this.handleError(err)
      });
  }


  private handleError(err: HttpErrorResponse): void {
    this.hasError.set(true);
    const errorData = err.error as Record<string, unknown>;
    const textReal: string = (errorData?.['details'] as string) || 'An unexpected error occurred!';
    this.errorMsg.set(textReal);
  }
}
