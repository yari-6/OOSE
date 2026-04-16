document.addEventListener("DOMContentLoaded", () => {

    const modal = document.getElementById("avatarModal");
    const changeBtn = document.getElementById("changePfpBtn");
    const closeModalBtn = document.getElementById("closeModal");
    const profileImage = document.getElementById("profileImage");

    if (changeBtn) {
        changeBtn.addEventListener("click", () => {

            if (!isLoggedIn) {
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

    window.selectAvatar = function(src) {
        fetch('/tutors/update-pfp', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                profilePicture: src
            })
        })
        .then(response => response.text())
        .then(data => {
            profileImage.src = src + "?t=" + new Date().getTime();
            modal.style.display = "none";
        })
        .catch(err => {
            alert("Could not update profile picture.");
            console.error(err);
        });
    };

});