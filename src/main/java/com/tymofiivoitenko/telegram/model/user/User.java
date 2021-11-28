package com.tymofiivoitenko.telegram.model.user;

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
    @NotNull
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "username")
    private String userName;

    @Column(name = "state", nullable = false)
    @NotNull
    private UserState state;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "role", nullable = false)
    private UserRole role;

    @Column(name = "password")
    private String password;

    public User(int chatId, String firstName, String lastName, String userName) {
        this.chatId = chatId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.state = UserState.START;
        this.role = UserRole.ROLE_CUSTOMER;
    }

    public User(int chatId) {
        this.chatId = chatId;
        this.firstName = String.valueOf(chatId);
        this.state = UserState.START;
    }

    @Override
    public String toString() {
        return "User{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", userName='" + userName + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
