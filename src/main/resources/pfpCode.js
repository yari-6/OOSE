const modal = document.getElementById("avatarModal");
const changeBtn = document.getElementById("changePfpBtn");
const closeModalBtn = document.getElementById("closeModal");
const profileImage = document.getElementById("profileImage");

changeBtn.addEventListener("click", () => {
    modal.style.display = "block";
});

closeModalBtn.addEventListener("click", () => {
    modal.style.display = "none";
});

window.addEventListener("click", (e) => {
    if (e.target == modal) {
        modal.style.display = "none";
    }
});

function selectAvatar(src) {
    profileImage.src = src;
    modal.style.display = "none";
}

(function(){
      emailjs.init("7uThrRxx3oK4X3ZxK");
    })();

document.getElementById("feedbackForm").addEventListener("submit", function(e) {
    e.preventDefault();

    const message = document.getElementById("message").value;

    emailjs.send("service_tqd9thp", "template_6rae5pq", {
        message: message
    })
        .then(function(response) {
        alert("Message sent successfully!");

        document.getElementById("message").value = "";
        }, function(error) {
            alert("Failed to send message.");
            console.log(error);
    });
});