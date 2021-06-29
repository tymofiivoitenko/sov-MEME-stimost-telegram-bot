package com.tymofiivoitenko.telegram.model;

import com.tymofiivoitenko.telegram.bot.userState.UserState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = "chat_id", name = "users_unique_chatid_idx")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends AbstractBaseEntity {
    @Column(name = "chat_id", unique = true, nullable = false)
    @NotNull
    private Integer chatId;

    @Column(name = "first_name")
    @NotBlank
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_state", nullable = false)
    @NotBlank
    private UserState userState;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Конструктор нужен для создания нового пользователя (а может и нет? :))
    public User(int chatId, String firstName, String lastName,String userName) {
        this.chatId = chatId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.userState = UserState.START;
    }

    // Конструктор нужен для создания нового пользователя (а может и нет? :))
    public User(int chatId) {
        this.chatId = chatId;
        this.firstName = String.valueOf(chatId);
        this.userState = UserState.START;
    }
}
