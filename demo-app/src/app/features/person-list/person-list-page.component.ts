import { ChangeDetectionStrategy, Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatToolbar } from '@angular/material/toolbar';
import { ConfirmDeleteDialogComponent } from '../../components/confirm-delete-dialog/confirm-delete-dialog.component';
import { PersonFormDialogComponent, PersonFormDialogData, PersonFormDialogResult } from '../../components/person-form-dialog/person-form-dialog.component';
import { PersonListStore } from './person-list.store';
import { LoginStore } from '../login/login.store';
import { RouterLink, RouterLinkActive, Router } from '@angular/router';
import {CreatePersonDto, Person, PersonJson, UpdatePersonDto} from '../../models/person.model';
import {Course} from '../../models/course.model';
@Component({
  selector: 'app-person-list-page',
  imports: [MatTableModule, MatButtonModule, MatIconModule, MatDialogModule, MatToolbar, RouterLinkActive, RouterLink],
  templateUrl: './person-list-page.component.html',
  styleUrl: './person-list-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  standalone: true
})
export class PersonListPageComponent {
  private readonly dialog = inject(MatDialog);
  protected readonly store = inject(PersonListStore);
  private readonly destroyRef = inject(DestroyRef);
  private readonly loginStore = inject(LoginStore);
  private readonly router = inject(Router);
  protected readonly persons = this.store.persons;
  protected readonly hasError = this.store.hasError;
  protected readonly isLoading = this.store.isLoading;
  protected readonly displayedColumns = ['role', 'name', 'age', 'email', 'courses', 'actions'];
  constructor() {
    this.store.load();
  }

  protected logout(): void {
    this.loginStore.logout();
    void this.router.navigate(['/login']);
  }

  protected openCreateDialog(): void {
    if (this.isLoading()) {
      return;
    }

    this.dialog
      .open<PersonFormDialogComponent, PersonFormDialogData, PersonFormDialogResult>(
        PersonFormDialogComponent,
        {
          data: {
            title: 'Create Person',
            submitLabel: 'Create',
            showPasswordField: true
          }
        },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result: PersonFormDialogResult | undefined) => {
        if (!result) return;
        this.store.create(result as CreatePersonDto);
      });
  }

  protected openEditDialog(person: Person): void {
    if (this.isLoading()) return;
    const jsonPerson = person as unknown as PersonJson;
    this.dialog
      .open<PersonFormDialogComponent, PersonFormDialogData, PersonFormDialogResult>(
        PersonFormDialogComponent,
        {
          data: {
            title: 'Edit Person',
            submitLabel: 'Save',
            initialValue: {
              ...person,
              enrolledCourseIds: jsonPerson.courses?.map(c => c.id) ?? [] }
          }
        },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result: PersonFormDialogResult | undefined) => {
        if (!result) return;

        this.store.update(person.id, result as UpdatePersonDto);
      });
  }

  protected openDeleteDialog(person: Person): void {
    if (this.isLoading()) {
      return;
    }

    this.dialog
      .open<ConfirmDeleteDialogComponent, { person: Person }, boolean>(
        ConfirmDeleteDialogComponent,
        { data: { person } },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmed) => {
        if (!confirmed) return;
        this.store.remove(person.id);
      });
  }

  protected getCourseNames(person: Person): string {
    const jsonPerson = person as unknown as PersonJson;
    const courseList = jsonPerson.courses || person.enrolledCourses;

    if (courseList && courseList.length > 0) {
      return courseList.map((c: Course) => c.title).join(', ');
    }

    return 'None';
  }

}
