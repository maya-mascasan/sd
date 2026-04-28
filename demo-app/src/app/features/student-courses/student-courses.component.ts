import {
  ChangeDetectionStrategy, Component, computed, inject,
  OnInit, signal
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';
import { MatBadgeModule } from '@angular/material/badge';
import { LoginStore } from '../login/login.store';
import { CourseService } from '../../services/course.service';
import { PersonService } from '../../services/person.service';
import { AssignmentService, Assignment } from '../../services/assignment.service';
import { SubmissionService, Submission } from '../../services/submission.service';
import { Course } from '../../models/course.model';
import { ComponentType } from '@angular/cdk/portal';
import { StudentSubmitDialogComponent } from '../../components/student-submit-dialog/student-submit-dialog.component';
import {PersonJson} from '../../models/person.model';

@Component({
  selector: 'app-student-courses',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    CommonModule, FormsModule,
    MatButtonModule, MatIconModule, MatDialogModule,
    MatChipsModule, MatTabsModule, MatBadgeModule
  ],
  templateUrl: './student-courses.component.html',
  styleUrl: './student-courses.component.scss',
})
export class StudentCoursesComponent implements OnInit {
  protected readonly loginStore = inject(LoginStore);
  private readonly router = inject(Router);
  private readonly dialog = inject(MatDialog);
  private readonly courseService = inject(CourseService);
  private readonly personService = inject(PersonService);
  private readonly assignmentService = inject(AssignmentService);
  private readonly submissionService = inject(SubmissionService);

  readonly allCourses = signal<Course[]>([]);
  readonly mySubmissions = signal<Submission[]>([]);
  readonly selectedCourse = signal<Course | null>(null);
  readonly assignments = signal<Assignment[]>([]);
  readonly isLoading = signal(false);
  readonly enrolling = signal<string | null>(null);
  readonly searchQuery = signal('');
  readonly enrolledCourseIds = signal<Set<string>>(new Set());

  private get studentId(): string {
    // Get person ID from backend by email
    return sessionStorage.getItem('person-id') ?? '';
  }

  readonly enrolledCourses = computed(() => {
    const ids = this.enrolledCourseIds();
    return this.allCourses().filter(c => ids.has(c.id));
  });

  // 3. Derive available courses
  readonly availableCourses = computed(() => {
    const query = this.searchQuery().toLowerCase();
    const ids = this.enrolledCourseIds();
    return this.allCourses()
      .filter(c => !ids.has(c.id))
      .filter(c => c.title.toLowerCase().includes(query) ||
        c.department?.name?.toLowerCase().includes(query));
  });

  ngOnInit(): void {
    this.loadData();
  }

  // 1. Update loadData to handle the 'courses' property
  loadData(): void {
    this.isLoading.set(true);
    this.courseService.getCourses().subscribe({
      next: courses => {
        this.allCourses.set(courses);
        const email = this.loginStore.email();
        if (email) {
          this.personService.getByEmail(email).subscribe({
            next: person => {
              sessionStorage.setItem('person-id', person.id);

              // Cast to PersonJson to safely access the 'courses' property
              const jsonPerson = person as unknown as PersonJson;

              // Now TypeScript knows 'courses' exists and is a Course[]
              const personCourses = jsonPerson.courses || person.enrolledCourses || [];
              const ids = new Set(personCourses.map((c: Course) => c.id));

              this.enrolledCourseIds.set(ids);

              this.submissionService.getByStudent(person.id).subscribe({
                next: subs => this.mySubmissions.set(subs)
              });
              this.isLoading.set(false);
            },
            error: () => this.isLoading.set(false)
          });
        }
      },
      error: () => this.isLoading.set(false)
    });
  }

// 2. Update enroll to handle the 'courses' property in the response
  enroll(course: Course): void {
    const personId = sessionStorage.getItem('person-id');
    if (!personId) return;
    this.enrolling.set(course.id);

    this.personService.enroll(personId, course.id).subscribe({
      next: (updatedPerson) => {
        this.enrolling.set(null);

        // Do the same cast here
        const jsonPerson = updatedPerson as unknown as PersonJson;

        const personCourses = jsonPerson.courses || updatedPerson.enrolledCourses || [];
        const ids = new Set(personCourses.map((c: Course) => c.id));

        this.enrolledCourseIds.set(ids);
      },
      error: () => this.enrolling.set(null)
    });
  }

  selectCourse(course: Course): void {
    this.selectedCourse.set(course);
    this.assignments.set([]);
    this.assignmentService.getByCourse(course.id).subscribe({
      next: a => this.assignments.set(a)
    });
  }

  unenroll(course: Course): void {
    const personId = sessionStorage.getItem('person-id');
    if (!personId) return;
    this.personService.unenroll(personId, course.id).subscribe({
      next: () => { this.selectedCourse.set(null); this.loadData(); }
    });
  }

  getSubmissionForAssignment(assignmentId: string): Submission | undefined {
    return this.mySubmissions().find(s => s.assignment?.id === assignmentId);
  }

  openSubmitDialog(assignment: Assignment): void {
    const existing = this.getSubmissionForAssignment(assignment.id);
    if (existing) return;
    const personId = sessionStorage.getItem('person-id');
    if (!personId) return;
    this.dialog.open(StudentSubmitDialogComponent as ComponentType<unknown>, {
      width: '500px',
      data: { assignment, studentId: personId }
    }).afterClosed().subscribe((content: string) => {
      if (!content) return;
      this.submissionService.submit({
        content,
        assignmentId: assignment.id,
        studentId: personId
      }).subscribe({
        next: sub => this.mySubmissions.update(list => [...list, sub])
      });
    });
  }

  protected logout(): void {
    this.loginStore.logout();
    void this.router.navigate(['/login']);
  }
}
