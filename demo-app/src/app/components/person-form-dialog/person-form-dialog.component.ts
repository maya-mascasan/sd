import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { TitleCasePipe } from '@angular/common';
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';
import { Course } from '../../models/course.model';
import {Person} from '../../models/person.model';
import {CourseService} from '../../services/course.service';
export interface PersonFormDialogData {
  title: string;
  submitLabel?: string;
  showPasswordField?: boolean;
  initialValue?: Partial<Person> & { enrolledCourseIds?: string[] };
}

export interface PersonFormDialogResult {
  name: string;
  age: number;
  email: string;
  password?: string;
  role: string;
  //enrolledCourseIds: string[];
  courses: { id: string }[];
}
@Component({
  selector: 'app-person-form-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatOptionModule,
    TitleCasePipe
  ],
  templateUrl: './person-form-dialog.component.html',
  styleUrl: './person-form-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true
})
export class PersonFormDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly courseService = inject(CourseService);
  private readonly dialogRef = inject(MatDialogRef<PersonFormDialogComponent>);
  protected readonly data = inject<PersonFormDialogData>(MAT_DIALOG_DATA);

  protected allCourses = signal<Course[]>([]);

  protected readonly isPasswordVisible = signal(false);
  protected readonly roles = ['admin', 'professor', 'student'];
  protected readonly form = this.fb.group({
    name: [this.data.initialValue?.name ?? '', [Validators.required]],
    age: [this.data.initialValue?.age ?? 18, [Validators.required, Validators.min(0)]],
    email: [this.data.initialValue?.email ?? '', [Validators.required, Validators.email]],
    password: [this.data.initialValue?.password ?? '', this.data.showPasswordField ? [Validators.required] : []],
    role: [this.data.initialValue?.role ?? 'student', [Validators.required]],
    enrolledCourseIds: [this.data.initialValue?.enrolledCourseIds ?? [] as string[]]
  });


  ngOnInit(): void {
    console.log('Dialog Initialized!');
    this.courseService.getCourses().subscribe({
      next: (data: Course[]) => {
        console.log('Courses fetched for dropdown:', data);
        this.allCourses.set(data);
      },
      error: (err: unknown) => {
        console.error('Failed to load courses for dropdown', err);
      }
    });

    if (this.data.initialValue) {
      this.form.patchValue(this.data.initialValue);
    }
  }

  protected togglePasswordVisibility(): void {
    this.isPasswordVisible.update((v) => !v);
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();

    // Create the base result object
    const result: PersonFormDialogResult = {
      name: raw.name as string,
      age: raw.age as number,
      email: raw.email as string,
      role: raw.role as string,
      courses: (raw.enrolledCourseIds as string[] ?? []).map(id => ({ id }))
    };

    // Add password only if required
    if (this.data.showPasswordField && raw.password) {
      result.password = raw.password;
    }

    this.dialogRef.close(result);
  }

  cancel() {
  }
}
