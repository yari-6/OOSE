document.addEventListener("DOMContentLoaded", function () {

    const sessionModal = document.getElementById('sessionModal');
    const tutorModal = document.getElementById('tutorModal');

    window.onclick = function (event) {
        if (sessionModal && event.target === sessionModal) {
            closeModal();
        }
        if (tutorModal && event.target === tutorModal) {
            closeTutorModal();
        }
    };

    // For editing
    // the tutors
    window.openModal = function () {
        if (tutorModal) tutorModal.classList.add('show');
    };

    window.closeTutorModal = function () {
        if (tutorModal) tutorModal.classList.remove('show');
    };

    window.filterTutors = function () {
        const searchInput = document.getElementById('tutorSearch');
        const table = document.getElementById('tutorTable');
        const noResults = document.getElementById('noResults');

        if (!searchInput || !table) return;

        const value = searchInput.value.toLowerCase();
        const rows = table.querySelectorAll('.tutor-row');

        let visibleCount = 0;

        rows.forEach(row => {
            const name = (row.getAttribute('data-name') || "").toLowerCase();
            const id = (row.getAttribute('data-id') || "").toLowerCase();

            const match = name.includes(value) || id.includes(value);

            row.style.display = match ? '' : 'none';
            if (match) visibleCount++;
        });

        if (noResults) {
            noResults.style.display = visibleCount === 0 ? '' : 'none';
        }
    };


    // For editing
    // the schedules
    window.goToTutor = function () {
        const input = document.getElementById('tutorSearchInput');
        const options = document.getElementById('tutorOptions');

        if (!input || !options) return;

        const val = input.value;
        const opts = options.childNodes;

        let id = null;

        for (let i = 0; i < opts.length; i++) {
            if (opts[i].value === val) {
                id = opts[i].getAttribute('data-id');
                break;
            }
        }

        if (id) {
            window.location.href = '/schedule-builder?targetTutorID=' + id;
        } else {
            alert("Please select a valid tutor from the list.");
        }
    };

    window.openModal = function (day, time) {
        const modal = document.getElementById('sessionModal');
        if (!modal) return;

        document.getElementById('modalTitle').innerText = "Add Session";
        document.getElementById('sessionForm').action = "/admin/save-master-session";

        document.getElementById('modalDay').value = day;
        document.getElementById('modalTime').value = time;

        modal.style.display = 'block';
    };

    window.openEditModal = function (tutorId, day, time) {
        const modal = document.getElementById('sessionModal');
        if (!modal) return;

        document.getElementById('modalTitle').innerText = "Edit Session";
        document.getElementById('sessionForm').action = "/admin/save-master-session";

        document.getElementById('oldTutorId').value = tutorId;
        document.getElementById('modalTutorSelect').value = tutorId;
        document.getElementById('modalDay').value = day;
        document.getElementById('modalTime').value = time;

        modal.style.display = 'block';
    };

    window.closeModal = function () {
        if (sessionModal) sessionModal.style.display = 'none';
    };

    // For editing
    // the courses
    const subjectFilter = document.getElementById('subjectFilter');
    const courseRows = document.querySelectorAll('.course-row');
    const noCourseResults = document.getElementById('noCourseResults');

    function populateSubjectFilter() {
        if (!subjectFilter) return;

        const subjects = [...new Set(
            Array.from(courseRows).map(row =>
                row.getAttribute('data-subject')
            )
        )].sort();

        subjects.forEach(subject => {
            const option = document.createElement('option');
            option.value = subject;
            option.textContent = subject;
            subjectFilter.appendChild(option);
        });
    }

    window.filterCourses = function () {
        const search = document.getElementById('courseSearch')?.value.toLowerCase() || "";
        const subject = subjectFilter?.value.toLowerCase() || "";

        let visible = 0;

        courseRows.forEach(row => {
            const s = (row.getAttribute('data-subject') || "").toLowerCase();
            const n = (row.getAttribute('data-number') || "").toLowerCase();
            const t = (row.getAttribute('data-title') || "").toLowerCase();

            const matchSearch =
                s.includes(search) || n.includes(search) || t.includes(search);

            const matchSubject = !subject || s === subject;

            const show = matchSearch && matchSubject;

            row.style.display = show ? '' : 'none';
            if (show) visible++;
        });

        if (noCourseResults) {
            noCourseResults.style.display = visible === 0 ? '' : 'none';
        }
    };

    populateSubjectFilter();
});