import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

/**
 * Instructor course student edit page.
 */
@Component({
  selector: 'tm-instructor-course-student-edit-page',
  templateUrl: './instructor-course-student-edit-page.component.html',
  styleUrls: ['./instructor-course-student-edit-page.component.scss'],
})
export class InstructorCourseStudentEditPageComponent implements OnInit {

  user: string = '';

  constructor(private route: ActivatedRoute) { }

  ngOnInit(): void {
    this.route.queryParams.subscribe((queryParams: any) => {
      this.user = queryParams.user;
    });
  }

}
