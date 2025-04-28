package com.eouil.msa.users.dtos;

import lombok.Getter;
import org.springframework.stereotype.Service;

@Getter
public class JoinResponse {
    public String name;
    public String email;
    public String userId;

    public JoinResponse(String name, String email, String userId) {
        this.name = name;
        this.email = email;
        this.userId = userId;
    }
}
