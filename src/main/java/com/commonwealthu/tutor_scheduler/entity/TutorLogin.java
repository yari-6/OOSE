package com.commonwealthu.tutor_scheduler.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Class to store only tutorID and password to avoid placing validation
 * in the entity class
 */

public class TutorLogin {

    @NotBlank(message = "Username is required")
    @Size(min = 8, message = "Username must be at least 8 characters")
    private String tutorID;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String pass;

    private String newPass;

    private String confirmPass;

    public TutorLogin() {}

    public String getTutorID() {
        return tutorID;
    }

    public void setTutorID(String tutorID) {
        this.tutorID = tutorID;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getNewPass() {
        return newPass;
    }

    public void setNewPass(String newPass) {
        this.newPass = newPass;
    }

    public String getConfirmPass() {
        return confirmPass;
    }

    public void setConfirmPass(String confirmPass) {
        this.confirmPass = confirmPass;
    }
}
