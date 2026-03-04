package com.commonwealthu.tutor_scheduler.entity;

import jakarta.persistence.*;

@Entity
@Table (name = "Ratings")
public class Rating {

    // Have Hibernate automatically generate the rating id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "rating_id", nullable = false, unique = true)
    private Long id;

    // All categories must be submitted for a rating to be complete
    @Column (name = "Communication", nullable = false)
    private int communication;

    @Column (name = "Approachability", nullable = false)
    private int approach;

    // EffLearn = Effective Learning Strategies
    @Column (name = "EffLearn", nullable = false)
    private int effLearn;

    @Column (name = "Helpfulness", nullable = false)
    private int helpfulness;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TutorID", nullable = false)
    private Tutor tutor;

    protected Rating() {}

    public Rating (int communication, int approach, int effLearn, int helpfulness) {
        this.communication = communication;
        this.approach = approach;
        this.effLearn = effLearn;
        this.helpfulness = helpfulness;
    }

    public int getCommunication() {
        return communication;
    }

    public int getApproach() {
        return approach;
    }

    public int getEffLearn() {
        return effLearn;
    }

    public int getHelpfulness() {
        return helpfulness;
    }
}
