package com.tymofiivoitenko.telegram.model.meme;

import com.tymofiivoitenko.telegram.model.AbstractBaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "meme_image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemeImage extends AbstractBaseEntity {
    @Column(name = "url", nullable = false)
    @NotNull
    private String url;

    @Column(name = "active", nullable = false)
    @NotNull
    private boolean active;
}
