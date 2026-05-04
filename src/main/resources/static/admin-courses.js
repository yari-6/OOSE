document.addEventListener("DOMContentLoaded", function () {
    const subjectFilter = document.getElementById('subjectFilter');
    const courseRows = document.querySelectorAll('.course-row');
    const noCourseResults = document.getElementById('noCourseResults');

    function populateSubjectFilter() {
        if (!subjectFilter || courseRows.length === 0) return;

        const subjects = [...new Set(
            Array.from(courseRows).map(row => row.getAttribute('data-subject'))
        )].sort();

        subjects.forEach(subject => {
            if (subject) {
                const option = document.createElement('option');
                option.value = subject;
                option.textContent = subject;
                subjectFilter.appendChild(option);
            }
        });
    }

    window.filterCourses = function () {
        const search = document.getElementById('courseSearch')?.value.toLowerCase() || "";
        const selectedSubject = subjectFilter?.value || "";

        let visibleCount = 0;

        courseRows.forEach(row => {
            const subject = (row.getAttribute('data-subject') || "").toLowerCase();
            const number = (row.getAttribute('data-number') || "").toLowerCase();
            const title = (row.getAttribute('data-title') || "").toLowerCase();

            const matchesSearch = subject.includes(search) ||
                                  number.includes(search) ||
                                  title.includes(search);

            const matchesSubject = !selectedSubject || row.getAttribute('data-subject') === selectedSubject;

            const shouldShow = matchesSearch && matchesSubject;

            row.style.display = shouldShow ? '' : 'none';
            if (shouldShow) visibleCount++;
        });

        if (noCourseResults) {
            noCourseResults.style.display = visibleCount === 0 ? '' : 'none';
        }
    };

    populateSubjectFilter();
});