import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { DepartmentCreateDTO } from '../../models/department.model';

export interface DeptFormDialogData {
  title: string;
  submitLabel: string;
  initialValue?: Partial<DepartmentCreateDTO>;
}

@Component({
  selector: 'app-dept-form-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
  ],
  templateUrl: './dept-form-dialog.component.html',
  styleUrl: './dept-form-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DeptFormDialogComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly dialogRef = inject(MatDialogRef<DeptFormDialogComponent>);
  public readonly data = inject<DeptFormDialogData>(MAT_DIALOG_DATA);

  public readonly form = this.fb.group({
    name: [this.data.initialValue?.name ?? '', [Validators.required, Validators.minLength(2)]],
  });

  onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.dialogRef.close(this.form.getRawValue());
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
