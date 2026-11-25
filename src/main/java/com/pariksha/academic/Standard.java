package com.pariksha.academic;

import com.pariksha.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "standards")
@Getter
@Setter
@NoArgsConstructor
public class Standard extends BaseEntity {

    @Column(nullable = false, length = 128)
    private String name;
}
