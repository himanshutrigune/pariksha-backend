package com.pariksha.student;

import com.pariksha.academic.Standard;
import com.pariksha.common.BaseEntity;
import com.pariksha.institute.Institute;
import com.pariksha.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/** Student profile. Login credentials live in the linked {@link User} row (role = STUDENT). */
@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
public class Student extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "institute_id", nullable = false)
    private Institute institute;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_id")
    private Standard standard;

    @Column(name = "roll_number", nullable = false, length = 30)
    private String rollNumber;

    @Column(length = 11)
    private String gender;

    @Column(name = "parent_contact", length = 20)
    private String parentContact;

    @Column(name = "joining_date", nullable = false)
    private LocalDate joiningDate;

    @Column(nullable = false)
    private boolean active = true;
}
