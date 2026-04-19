import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { DepartmentService } from '../../services/department.service';
import { Department } from '../../models/department.model';


export interface CourseFormValue {
  title: string;
  credits: number;
  departmentId: string;
}

export interface CourseFormDialogData {
  title: string;
  submitLabel: string;
  initialValue?: CourseFormValue;
}

@Component({
  selector: 'app-course-form-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule
  ],
  templateUrl: './course-form-dialog.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CourseFormDialogComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly deptService = inject(DepartmentService);
  private readonly dialogRef = inject(MatDialogRef<CourseFormDialogComponent>);
  protected readonly data = inject<CourseFormDialogData>(MAT_DIALOG_DATA);

  protected readonly departments = signal<Department[]>([]);

  protected readonly form = this.fb.group({
    title: [this.data.initialValue?.title ?? '', [Validators.required]],
    credits: [this.data.initialValue?.credits ?? 1, [Validators.required, Validators.min(1)]],
    departmentId: [this.data.initialValue?.departmentId ?? '', [Validators.required]]
  });

  ngOnInit() {
    this.deptService.getDepartments().subscribe(list => this.departments.set(list));
  }

  submit() {
    if (this.form.valid) {
      this.dialogRef.close(this.form.getRawValue());
    }
  }
}
