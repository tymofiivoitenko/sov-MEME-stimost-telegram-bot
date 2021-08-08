package com.tymofiivoitenko.telegram.model.meme;

import com.tymofiivoitenko.telegram.model.AbstractBaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "meme_reaction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemeReaction extends AbstractBaseEntity {

    @Column(name = "meme_test_id", nullable = false)
    @NotNull
    private int memeTestId;

    @Column(name = "meme_image_id", nullable = false)
    @NotNull
    private int memeImageId;

    @Column(name = "reaction", nullable = false)
    @NotNull
    private MemeReactionState memeReactionState;

    @Column(name = "reacted_by_user", nullable = false)
    @NotNull
    private int reactedByUser;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public MemeReaction(int testId, int memeImageId, int userId) {
        this.memeTestId = testId;
        this.memeReactionState = MemeReactionState.NONE;
        this.memeImageId = memeImageId;
        this.reactedByUser = userId;
    }

    @Override
    public String toString() {
        return "MemeReaction{" +
                "memeTestId=" + memeTestId +
                ", memeImageId=" + memeImageId +
                ", memeReactionState=" + memeReactionState +
                ", reactedByUser=" + reactedByUser +
                ", createdAt=" + createdAt +
                '}';
    }
}
