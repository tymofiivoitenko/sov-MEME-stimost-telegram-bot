package com.tymofiivoitenko.telegram.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
// Аннотация, которая говорит нам, что это суперкласс для всех Entity
// https://vladmihalcea.com/how-to-inherit-properties-from-a-base-class-entity-using-mappedsuperclass-with-jpa-and-hibernate/
@MappedSuperclass
// http://stackoverflow.com/questions/594597/hibernate-annotations-which-is-better-field-or-property-access
@Access(AccessType.FIELD)

// Аннотации Lombok для автогенерации сеттеров и геттеров на все поля
@Getter
@Setter
public abstract class AbstractBaseEntity {

    public static final int START_SEQ = 100000;
    // Аннотации, описывающие механизм генерации id - разберитесь в документации каждой!
    @Id
    @SequenceGenerator(name = "global_seq", sequenceName = "global_seq", allocationSize = 1, initialValue = START_SEQ)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "global_seq")
//  See https://hibernate.atlassian.net/browse/HHH-3718 and https://hibernate.atlassian.net/browse/HHH-12034
//  Proxy initialization when accessing its identifier managed now by JPA_PROXY_COMPLIANCE setting
    protected Integer id;

    protected AbstractBaseEntity() {
    }
}
