import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Assignment } from '../../services/assignment.service';

@Component({
  selector: 'app-student-submit-dialog',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  template: `
    <h2 mat-dialog-title>Submit: {{ data.assignment.title }}</h2>
    <mat-dialog-content>
      <p style="color:#666;font-size:0.875rem;margin:0 0 1rem">{{ data.assignment.description }}</p>
      <mat-form-field appearance="outline" style="width:100%">
        <mat-label>Your answer / work</mat-label>
        <textarea matInput [formControl]="content" rows="6" placeholder="Write your submission here..."></textarea>
        @if (content.hasError('required')) {
          <mat-error>Content is required.</mat-error>
        }
      </mat-form-field>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button (click)="cancel()">Cancel</button>
      <button mat-flat-button color="primary" (click)="submit()">Submit</button>
    </mat-dialog-actions>
  `
})
export class StudentSubmitDialogComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly dialogRef = inject(MatDialogRef<StudentSubmitDialogComponent>);
  readonly data = inject<{ assignment: Assignment; studentId: string }>(MAT_DIALOG_DATA);

  readonly content = this.fb.control('', Validators.required);

  submit(): void {
    if (this.content.invalid) { this.content.markAsTouched(); return; }
    this.dialogRef.close(this.content.value);
  }

  cancel(): void { this.dialogRef.close(); }
}
