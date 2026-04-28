import { Course } from './course.model';

export interface Person {
  id: string;
  name: string;
  age: number;
  email: string;
  role: string;
  password?: string;
  enrolledCourses: Course[];
}

export interface CreatePersonDto {
  name: string;
  age: number;
  email: string;
  role: string;
  password?: string;
  courses?: { id: string }[];
}

export type UpdatePersonDto = Partial<CreatePersonDto>
export interface PersonJson extends Person {
  courses: Course[];
}
