package com.eouil.msa.users.exceptions;

// 존재하는 중복 이메일 시 예외 발생
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException() {
        super("이미 가입된 이메일 입니다.");
    }
}
