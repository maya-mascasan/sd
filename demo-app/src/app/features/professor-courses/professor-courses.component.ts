import {
  ChangeDetectionStrategy, Component, inject,
  OnInit, signal
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule, NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { LoginStore } from '../login/login.store';
import { CourseService } from '../../services/course.service';
import { PersonService } from '../../services/person.service';
import { AssignmentService, Assignment } from '../../services/assignment.service';
import { SubmissionService, Submission } from '../../services/submission.service';
import { Course } from '../../models/course.model';
import { ComponentType } from '@angular/cdk/portal';
import { ProfessorGradeDialogComponent } from '../../components/professor-grade-dialog/professor-grade-dialog.component';

@Component({
  selector: 'app-professor-courses',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule,
    MatButtonModule, MatIconModule, MatDialogModule,
    MatChipsModule, MatFormFieldModule, MatInputModule,
    MatDatepickerModule, MatNativeDateModule
  ],
  templateUrl: './professor-courses.component.html',
  styleUrl: './professor-courses.component.scss',
})
export class ProfessorCoursesComponent implements OnInit {
  protected readonly loginStore = inject(LoginStore);
  private readonly router = inject(Router);
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly dialog = inject(MatDialog);
  private readonly courseService = inject(CourseService);
  private readonly personService = inject(PersonService);
  private readonly assignmentService = inject(AssignmentService);
  private readonly submissionService = inject(SubmissionService);

  readonly myCourses = signal<Course[]>([]);
  readonly selectedCourse = signal<Course | null>(null);
  readonly assignments = signal<Assignment[]>([]);
  readonly selectedAssignment = signal<Assignment | null>(null);
  readonly submissions = signal<Submission[]>([]);
  readonly isLoading = signal(false);
  readonly showNewAssignmentForm = signal(false);

  readonly assignmentForm = this.fb.group({
    title: ['', Validators.required],
    description: ['', Validators.required],
    deadline: ['' as string, Validators.required],
  });

  ngOnInit(): void {
    this.isLoading.set(true);
    const email = this.loginStore.email();
    if (email) {
      this.personService.getByEmail(email).subscribe({
        next: person => {
          sessionStorage.setItem('person-id', person.id);
          this.courseService.getCoursesByProfessor(person.id).subscribe({
            next: courses => { this.myCourses.set(courses); this.isLoading.set(false); },
            error: () => this.isLoading.set(false)
          });
        }
      });
    }
  }

  selectCourse(course: Course): void {
    this.selectedCourse.set(course);
    this.selectedAssignment.set(null);
    this.submissions.set([]);
    this.showNewAssignmentForm.set(false);
    this.loadAssignments(course.id);
  }

  loadAssignments(courseId: string): void {
    this.assignmentService.getByCourse(courseId).subscribe({
      next: a => this.assignments.set(a)
    });
  }

  selectAssignment(a: Assignment): void {
    this.selectedAssignment.set(a);
    this.submissionService.getByAssignment(a.id).subscribe({
      next: subs => this.submissions.set(subs)
    });
  }

  createAssignment(): void {
    if (this.assignmentForm.invalid || !this.selectedCourse()) return;
    const { title, description, deadline } = this.assignmentForm.getRawValue();
    this.assignmentService.create({
      title, description,
      deadline: new Date(deadline).toISOString().split('T')[0],
      courseId: this.selectedCourse()!.id
    }).subscribe({
      next: a => {
        this.assignments.update(list => [...list, a]);
        this.assignmentForm.reset();
        this.showNewAssignmentForm.set(false);
      }
    });
  }

  deleteAssignment(a: Assignment): void {
    if (!confirm(`Delete assignment "${a.title}"?`)) return;
    this.assignmentService.delete(a.id).subscribe({
      next: () => {
        this.assignments.update(list => list.filter(x => x.id !== a.id));
        if (this.selectedAssignment()?.id === a.id) {
          this.selectedAssignment.set(null);
          this.submissions.set([]);
        }
      }
    });
  }

  openGradeDialog(sub: Submission): void {
    this.dialog.open(ProfessorGradeDialogComponent as ComponentType<unknown>, {
      width: '400px',
      data: { submission: sub }
    }).afterClosed().subscribe((grade: number | undefined) => {
      if (grade === undefined) return;
      this.submissionService.grade(sub.id, grade).subscribe({
        next: updated => this.submissions.update(list =>
          list.map(s => s.id === updated.id ? updated : s))
      });
    });
  }

  protected logout(): void {
    this.loginStore.logout();
    void this.router.navigate(['/login']);
  }
}
