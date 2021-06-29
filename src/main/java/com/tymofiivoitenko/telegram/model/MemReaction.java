package com.tymofiivoitenko.telegram.model;

import com.tymofiivoitenko.telegram.bot.MemReactionState;
import com.tymofiivoitenko.telegram.bot.userState.UserState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.interning.qual.InternedDistinct;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "mem_reaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemReaction extends AbstractBaseEntity {

    @Column(name = "mem_test_id", nullable = false)
    @NotNull
    private int memTestId;

    @Column(name = "mem_image_id", nullable = false)
    @NotBlank
    private int memImageId;

    @Column(name = "reaction", nullable = false)
    @NotBlank
    private MemReactionState memReactionState;

    @Column(name = "reacted_by_user", nullable = false)
    @NotBlank
    private int reactedByUser;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public MemReaction(int testId, int memImageId, int userId) {
        this.memTestId = testId;
        this.memReactionState = MemReactionState.NONE;
        this.memImageId = memImageId;
        this.reactedByUser = userId;
    }

    @Override
    public String toString() {
        return "MemReaction{" +
                "id=" + id +
                ", memTestId=" + memTestId +
                ", memImageId=" + memImageId +
                ", memReactionState=" + memReactionState +
                ", reactedByUser=" + reactedByUser +
                '}';
    }
}
