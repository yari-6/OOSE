document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById('dropInForm');

    if (!form) return;

    form.addEventListener('submit', function (e) {
        const start = document.getElementById('start').value;
        const end = document.getElementById('end').value;

        if (start >= end) {
            e.preventDefault();
            alert("The end time must be later than the start time.");
            return;
        }

        const startTime = new Date(`1970-01-01T${start}:00`);
        const endTime = new Date(`1970-01-01T${end}:00`);
        const diffHours = (endTime - startTime) / 1000 / 60 / 60;

        if (diffHours > 5) {
            e.preventDefault();
            alert("Error: Sessions cannot exceed 5 hours.");
        }
    });
});