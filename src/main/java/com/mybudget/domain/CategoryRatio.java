package com.mybudget.domain;

import com.mybudget.enums.Categories;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class CategoryRatio extends BaseEntity {

    @Id
    @Enumerated(EnumType.STRING)
    private Categories category;

    @Setter
    private Double ratio;

    @Setter
    private Integer count;
}
