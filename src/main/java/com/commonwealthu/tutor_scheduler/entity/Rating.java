package com.commonwealthu.tutor_scheduler.entity;

import jakarta.persistence.*;

@Entity
@Table (name = "ratings")
public class Rating {

    // Have Hibernate automatically generate the rating id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "rating_id", nullable = false, unique = true)
    private Long id;

    @Column (name = "tutor_id", nullable = false)
    private String tutorID;

    @Column (name = "rating_val", nullable = false)
    private int value;

    @ManyToOne(fetch = FetchType.LAZY)
    private Tutor tutor;

    protected Rating() {}

    public Rating(String tutorID, int value) {
        this.tutorID = tutorID;
        this.value = value;
    }

    public String getTutorID() {
        return tutorID;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
