import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Submission } from '../../services/submission.service';

@Component({
  selector: 'app-professor-grade-dialog',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  template: `
    <h2 mat-dialog-title>Grade Submission</h2>
    <mat-dialog-content>
      <p style="color:#666;font-size:0.875rem">Student: <strong>{{ data.submission.student?.name }}</strong></p>
      <mat-form-field appearance="outline" style="width:100%">
        <mat-label>Grade (0–100)</mat-label>
        <input matInput type="number" [formControl]="grade" min="0" max="100" />
        @if (grade.hasError('required')) { <mat-error>Grade is required.</mat-error> }
        @if (grade.hasError('min') || grade.hasError('max')) { <mat-error>Must be 0–100.</mat-error> }
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="cancel()">Cancel</button>
      <button mat-flat-button color="primary" (click)="submit()">Save Grade</button>
    </mat-dialog-actions>
  `
})
export class ProfessorGradeDialogComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly dialogRef = inject(MatDialogRef<ProfessorGradeDialogComponent>);
  readonly data = inject<{ submission: Submission }>(MAT_DIALOG_DATA);

  readonly grade = this.fb.control(
    this.data.submission.grade ?? 0,
    [Validators.required, Validators.min(0), Validators.max(100)]
  );

  submit(): void {
    if (this.grade.invalid) { this.grade.markAsTouched(); return; }
    this.dialogRef.close(this.grade.value);
  }

  cancel(): void { this.dialogRef.close(); }
}
