document.addEventListener("DOMContentLoaded", function () {
    const sessionModal = document.getElementById('sessionModal');
    const sessionForm = document.getElementById('sessionForm');

    window.handleLocationChange = function(selectElement) {
        const customInput = document.getElementById('modalLocation');
        if (selectElement.value === 'Other') {
            customInput.style.display = 'block';
            customInput.value = '';
            customInput.focus();
        } else {
            customInput.style.display = 'none';
            customInput.value = selectElement.value;
        }
    };

    window.openModal = function (day, startTime) {
        document.getElementById('modalTitle').innerText = "Add New Session";
        document.getElementById('modalDeleteBtn').style.display = 'none';
        sessionForm.reset();

        document.getElementById('modalDay').value = day;
        document.getElementById('modalStartTime').value = startTime;
        document.getElementById('oldTutorId').value = "";

        const params = new URLSearchParams(window.location.search);
        const currentFilter = params.get('type');
        let defaultLoc = "Andruss Library";

        if (currentFilter === 'SSC Andruss Library') defaultLoc = "SSC Soltz 105";
        else if (currentFilter === 'Math Lab') defaultLoc = "Ben Franklin";
        else if (currentFilter === 'Drop-in') defaultLoc = "Andruss Library";

        const selector = document.getElementById('locationSelector');
        const customInput = document.getElementById('modalLocation');

        selector.value = defaultLoc;
        customInput.value = defaultLoc;
        customInput.style.display = 'none';

        if(document.getElementById('modalClassName')) document.getElementById('modalClassName').value = "";
        if(document.getElementById('modalProfessor')) document.getElementById('modalProfessor').value = "";
        if(document.getElementById('modalMeetingTimes')) document.getElementById('modalMeetingTimes').value = "";

        sessionForm.action = "/admin/save-master-session";
        sessionModal.style.display = 'flex';
    };

    window.openEditModal = function (tutorId, day, startTime, location = '', professor = '', classMeetingTimes = '', className = '', endTime = '') {
        const cleanTime = (t) => {
            if (!t || t === 'null' || t === '') return "";
            return t.substring(0, 5);
        };

        const formattedStart = cleanTime(startTime);
        const formattedEnd = cleanTime(endTime);

        document.getElementById('modalTitle').innerText = "Edit Session";
        document.getElementById('modalDeleteBtn').style.display = 'block';

        const urlParams = new URLSearchParams(window.location.search);
        const currentFilter = urlParams.get('type') || 'Drop-in';

        if(document.getElementById('modalCurrentFilter')) {
            document.getElementById('modalCurrentFilter').value = currentFilter;
        }

        if(document.getElementById('oldStartTime')) {
            document.getElementById('oldStartTime').value = formattedStart;
        }

        document.getElementById('oldTutorId').value = tutorId;
        document.getElementById('modalDay').value = day;
        document.getElementById('modalStartTime').value = formattedStart;
        document.getElementById('modalEndTime').value = formattedEnd;
        document.getElementById('modalTutorSelect').value = tutorId;

        const selector = document.getElementById('locationSelector');
        const customInput = document.getElementById('modalLocation');
        const standardLocations = ['Andruss Library', 'Ben Franklin', 'SSC Soltz 105'];

        if (location && location !== 'null' && standardLocations.includes(location)) {
            selector.value = location;
            customInput.value = location;
            customInput.style.display = 'none';
        } else {
            selector.value = 'Other';
            customInput.value = (location && location !== 'null') ? location : "";
            customInput.style.display = 'block';
        }

        if(document.getElementById('modalProfessor'))
            document.getElementById('modalProfessor').value = (professor && professor !== 'null') ? professor : "";
        if(document.getElementById('modalMeetingTimes'))
            document.getElementById('modalMeetingTimes').value = (classMeetingTimes && classMeetingTimes !== 'null') ? classMeetingTimes : "";
        if(document.getElementById('modalClassName'))
            document.getElementById('modalClassName').value = (className && className !== 'null') ? className : "";

        const sessionForm = document.getElementById('sessionForm');
        const sessionModal = document.getElementById('sessionModal');
        sessionForm.action = "/admin/save-master-session";
        sessionModal.style.display = 'flex';
    };

    sessionForm.onsubmit = function(e) {
        const start = document.getElementById('modalStartTime').value;
        const end = document.getElementById('modalEndTime').value;
        if (start && end && start >= end) {
            alert("Error: End time must be after the start time.");
            e.preventDefault();
            return false;
        }
    };

    window.submitDelete = function() {
        if (confirm("Are you sure you want to permanently delete this session?")) {
            sessionForm.action = "/admin/delete-session";
            sessionForm.submit();
        }
    };

    window.closeModal = function () {
        sessionModal.style.display = 'none';
    };

    window.onclick = function (event) {
        if (event.target === sessionModal) closeModal();
    };

    window.goToTutor = function () {
        const input = document.getElementById('tutorSearchInput');
        const options = document.getElementById('tutorOptions');
        if (!input || !options) return;

        const val = input.value;
        const opts = options.options;
        let id = null;

        for (let i = 0; i < opts.length; i++) {
            if (opts[i].value === val) {
                id = opts[i].getAttribute('data-id');
                break;
            }
        }

        if (id) {
            window.location.href = '/admin/schedule?tutorId=' + encodeURIComponent(id);
        } else {
            alert("Please select a valid tutor from the list.");
        }
    };
});