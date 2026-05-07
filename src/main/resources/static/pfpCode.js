window.selectAvatar = function(src) {
    const profileImage = document.getElementById("profileImage");
    const modal = document.getElementById("avatarModal");

    fetch('/tutors/update-pfp', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ profilePicture: src })
    })
    .then(response => {
        if (!response.ok) throw new Error();
        return response.text();
    })
    .then(data => {
        if (data !== "not_logged_in") {
            profileImage.src = src + "?t=" + new Date().getTime();
            modal.style.display = "none";
        }
    })
    .catch(err => {
        console.error(err);
    });
};

document.addEventListener("DOMContentLoaded", () => {
    const modal = document.getElementById("avatarModal");
    const changeBtn = document.getElementById("changePfpBtn");
    const closeModalBtn = document.getElementById("closeModal");

    if (changeBtn) {
        changeBtn.addEventListener("click", (e) => {
            e.preventDefault();
            if (typeof isLoggedIn !== 'undefined' && !isLoggedIn) {
                alert("You must be signed in to change your profile picture.");
                return;
            }
            modal.style.display = "block";
        });
    }

    if (closeModalBtn) {
        closeModalBtn.addEventListener("click", () => {
            modal.style.display = "none";
        });
    }

    window.addEventListener("click", (e) => {
        if (e.target === modal) {
            modal.style.display = "none";
        }
    });
});