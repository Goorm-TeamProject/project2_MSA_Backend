package com.eouil.msa.users.domains;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@Table(name = "users")
public class User{

    @Id
    @Column(length = 36)
    private String userId;

    @Column(length = 16, nullable = false)
    private String name;
    @Column(length = 50, nullable = false, unique = true)
    private String email;
    @Column(length = 100, nullable = false)
    private String password;
    //Google MFA
    private String mfaSecret;
}
