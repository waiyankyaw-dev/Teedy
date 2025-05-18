package com.sismics.docs.core.model.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "T_USER_REQUEST")
public class UserRequest {
    @Id
    @Column(name = "URQ_ID_C", length = 36)
    private String id;

    @Column(name = "URQ_USERNAME_C", nullable = false, length = 50)
    private String username;

    @Column(name = "URQ_PASSWORD_C", nullable = false, length = 200)
    private String password;

    @Column(name = "URQ_EMAIL_C", nullable = false, length = 100)
    private String email;

    @Column(name = "URQ_CREATEDATE_D", nullable = false)
    private Date createDate;

    @Column(name = "URQ_STATUS_C", nullable = false, length = 10)
    private String status;

    public UserRequest() {
        this.id = UUID.randomUUID().toString();
        this.createDate = new Date();
        this.status = "PENDING";
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public UserRequest setId(String id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public UserRequest setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public UserRequest setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public UserRequest setEmail(String email) {
        this.email = email;
        return this;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public UserRequest setCreateDate(Date createDate) {
        this.createDate = createDate;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public UserRequest setStatus(String status) {
        this.status = status;
        return this;
    }
}