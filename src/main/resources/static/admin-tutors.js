document.addEventListener("DOMContentLoaded", function () {
    const tutorModal = document.getElementById('tutorModal');
    const editTutorModal = document.getElementById('editTutorModal');
    const loadingOverlay = document.getElementById('loadingOverlay');

    window.onload = function() {
        if (loadingOverlay) {
            loadingOverlay.style.opacity = '0';
            setTimeout(() => loadingOverlay.style.display = 'none', 500);
        }
        populateDepartmentFilters();
    };

    window.filterTutors = function () {
        const input = document.getElementById('tutorSearch');
        const filter = input.value.toLowerCase();
        const rows = document.querySelectorAll('.tutor-row');
        const noResults = document.getElementById('noResults');
        let visibleCount = 0;

        rows.forEach(row => {
            const name = row.getAttribute('data-name').toLowerCase();
            const id = row.getAttribute('data-id').toLowerCase();

            if (name.includes(filter) || id.includes(filter)) {
                row.style.display = "";
                visibleCount++;
            } else {
                row.style.display = "none";
            }
        });

        noResults.style.display = visibleCount === 0 ? "" : "none";
    };

    window.openTutorModal = () => tutorModal.style.display = 'flex';
    window.closeTutorModal = () => tutorModal.style.display = 'none';

    window.openEditModal = function (id, first, last, type, assignedCourses) {
        document.getElementById('displayTutorName').innerText = first + " " + last;
        document.getElementById('editTutorID').value = id;
        document.getElementById('editFirstName').value = first;
        document.getElementById('editLastName').value = last;
        document.getElementById('editType').value = type;

        const checkboxes = document.querySelectorAll('#modalCourseList input[type="checkbox"]');
        checkboxes.forEach(cb => cb.checked = false);

        if (assignedCourses) {
            assignedCourses.forEach(courseKey => {
                const cb = document.getElementById('course-' + courseKey);
                if (cb) cb.checked = true;
            });
        }

        editTutorModal.style.display = 'flex';
    };

    window.closeEditModal = () => editTutorModal.style.display = 'none';

    function populateDepartmentFilters() {
        const filters = ['regSubjectFilter', 'modalSubjectFilter'];
        const courses = document.querySelectorAll('.course-checkbox-item');
        const subjects = [...new Set(Array.from(courses).map(c => c.getAttribute('data-subject')))].sort();

        filters.forEach(filterId => {
            const select = document.getElementById(filterId);
            if (!select) return;
            subjects.forEach(sub => {
                const opt = document.createElement('option');
                opt.value = sub;
                opt.textContent = sub;
                select.appendChild(opt);
            });
        });
    }

    window.filterModalCourses = function() {
        const search = document.getElementById('modalCourseSearch').value.toLowerCase();
        const dept = document.getElementById('modalSubjectFilter').value;
        const items = document.querySelectorAll('#modalCourseList .course-checkbox-item');

        items.forEach(item => {
            const text = item.textContent.toLowerCase();
            const subject = item.getAttribute('data-subject');
            const matchesSearch = text.includes(search);
            const matchesDept = !dept || subject === dept;
            item.style.display = (matchesSearch && matchesDept) ? "flex" : "none";
        });
    };
});