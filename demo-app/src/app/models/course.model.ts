import { Department } from './department.model';

export interface Course {
  id: string;
  title: string;
  credits: number;
  department: Department;
}

export interface CourseCreateDTO {
  title: string;
  credits: number;
  departmentId: string;
}
