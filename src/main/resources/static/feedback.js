(function(){
    emailjs.init("7uThrRxx3oK4X3ZxK");
})();

document.addEventListener("DOMContentLoaded", function () {

    const form = document.getElementById("feedbackForm");

    if (!form) return;

    form.addEventListener("submit", function(e) {
        e.preventDefault();

        const email = document.getElementById("user_email").value.trim();
        const message = document.getElementById("message").value.trim();

        //this checks for commonwealthu emails only
        const emailPattern = /^[a-zA-Z0-9._%+-]+@commonwealthu\.edu$/;

        if (!emailPattern.test(email)) {
            alert("You must use your commonwealth email.");
            return;
        }

        if (message === "") {
            alert("Please enter your feedback.");
            return;
        }

        emailjs.send("service_tqd9thp", "template_6rae5pq", {
            message: message
        })
        .then(function(response) {
            alert("Message sent successfully!");

            form.reset();  //clears the form
        }, function(error) {
            alert("Failed to send message.");
            console.log(error);
        });
    });

});