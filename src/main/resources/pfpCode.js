const modal = document.getElementById("avatarModal");
const changeBtn = document.getElementById("changePfpBtn");
const closeModalBtn = document.getElementById("closeModal");
const profileImage = document.getElementById("profileImage");


//this is fro the pfp button, works only for logged in tutors
if (changeBtn) {
    changeBtn.addEventListener("click", () => {

        if (!isLoggedIn) {
            alert("You must be signed in to change your profile picture.");
            return;
        }

        modal.style.display = "block";
    });
}

closeModalBtn.addEventListener("click", () => {
    modal.style.display = "none";
});

window.addEventListener("click", (e) => {
    if (e.target === modal) {
        modal.style.display = "none";
    }
});

function selectAvatar(src) {
    profileImage.src = src;
    modal.style.display = "none";
}