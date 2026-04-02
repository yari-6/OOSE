CREATE TABLE IF NOT EXISTS  `learning_center`.Tutors (
    TutorID VARCHAR(10) PRIMARY KEY,
    FirstName VARCHAR(50),
    LastName VARCHAR(50),
    TutorType VARCHAR(10),
    Pass VARCHAR(15)
);

CREATE TABLE IF NOT EXISTS `learning_center`.Courses (
    CourseSubject VARCHAR(5),
    CourseNumber INT,
    CourseTitle VARCHAR(200),
    CONSTRAINT CourseID PRIMARY KEY (CourseSubject, CourseNumber)
);


CREATE TABLE IF NOT EXISTS `learning_center`.Tutor_Courses (
    TutorID VARCHAR(10),
    CourseSubject VARCHAR(5),
    CourseNumber INT,
    FOREIGN KEY (TutorID) REFERENCES Tutors(TutorID),
    FOREIGN KEY (CourseSubject, CourseNumber) REFERENCES Courses(CourseSubject, CourseNumber),
    CONSTRAINT Tutor_CoursesID PRIMARY KEY (TutorID, CourseSubject, CourseNumber)
);