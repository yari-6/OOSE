DROP TABLE IF EXISTS Tutor_Courses;
DROP TABLE IF EXISTS Courses;
DROP TABLE IF EXISTS Ratings;
DROP TABLE IF EXISTS Tutors;

CREATE TABLE IF NOT EXISTS Tutors (
    TutorID VARCHAR(10) PRIMARY KEY,
    FirstName VARCHAR(50),
    LastName VARCHAR(50),
    TutorType VARCHAR(10)

);


CREATE TABLE IF NOT EXISTS Courses (
    CourseSubject VARCHAR(5),
    CourseNumber INT,
    CourseTitle VARCHAR(50),
	CONSTRAINT CourseID PRIMARY KEY (CourseSubject, CourseNumber)
);


CREATE TABLE IF NOT EXISTS Tutor_Courses (
    TutorID VARCHAR(10),
    CourseSubject VARCHAR(5),
    CourseNumber INT,
    FOREIGN KEY (TutorID) REFERENCES Tutors(TutorID),
    FOREIGN KEY (CourseSubject, CourseNumber) REFERENCES Courses(CourseSubject, CourseNumber),
	CONSTRAINT Tutor_CoursesID PRIMARY KEY (TutorID, CourseSubject, CourseNumber)
);


INSERT INTO Tutors (TutorID, FirstName, LastName, TutorType)
VALUES
    ('jad90115', 'John', 'Doe', 'Drop-in'),
    ('jbs92520', 'Jane', 'Smith', 'SI'),
    ('mcj88808', 'Mike', 'Johnson', 'Math Lab'),
    ('edw91312', 'Emily', 'Williams', 'Tutor'),
    ('cea31125', 'Chris', 'Anderson', 'Tutor'),
    ('lfm89718', 'Lily', 'Martinez', 'SI'),
    ('dgt94228', 'Daniel', 'Taylor', 'Math Lab'),
    ('shb90804', 'Sophia', 'Brown', 'Drop-in'),
    ('eim92129', 'Ethan', 'Moore', 'Math Lab'),
    ('ojd87622', 'Olivia', 'Davis', 'SI'),
    ('rab24274', 'Rebecca', 'Berkheimer', 'Math Lab');

INSERT INTO Courses (CourseSubject, CourseNumber, CourseTitle)
VALUES
	('POLI', 212, 'State & Local Politics'),
    ('COMM', 103, 'Small Group Communications'),
    ('CMSC', 115, 'Python Programming'),
    ('ENGL', 300, 'Fiction Workshop'),
    ('CHEM', 261, 'Inorganic Chemistry'),
    ('EGGS', 107, 'Natural Disasters'),
    ('ECON', 256, 'Business and Economic Statitics'),
    ('ACCT', 310, 'Accounting Information Systems'),
    ('MUSI', 110, 'Introduction to Music'),
    ('ART', 290, 'Digital Draw - Paint'),
    ('HONR', 220, 'Honors Literature'),
    ('ALSI', 102, 'American Sign Language 2'),
    ('HIST', 132, 'Asian History Since 1500'),
    ('STAT', 240, 'Statistical Methods'),
    ('RUSS', 101, 'Russian 1');

INSERT INTO Tutor_Courses (TutorID, CourseSubject, CourseNumber)
VALUES
	('jad90115', 'CMSC', 115),
    ('jad90115', 'ACCT', 310),
    ('jad90115', 'ECON', 256),
    ('jad90115', 'STAT', 240),
	('jbs92520', 'RUSS', 101),
    ('jbs92520', 'ALSI', 102),
    ('jbs92520', 'ART', 290),
    ('jbs92520', 'STAT', 240),
    ('rab24274', 'CMSC', 115);