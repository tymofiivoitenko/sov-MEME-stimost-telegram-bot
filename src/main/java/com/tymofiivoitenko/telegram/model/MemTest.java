package com.tymofiivoitenko.telegram.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "mem_test")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemTest extends AbstractBaseEntity {
    @Column(name = "created_by_user", nullable = false)
    @NotNull
    private int createByUser;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Override
    public String toString() {
        return "MemTest{" +
                "createByUser=" + createByUser +
                ", createdAt=" + createdAt +
                '}';
    }
}
