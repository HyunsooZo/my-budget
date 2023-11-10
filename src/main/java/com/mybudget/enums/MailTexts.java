package com.mybudget.enums;

public class MailTexts {
    public static final String VERIFICATION_SUBJECT =
            "[My-Budget] 이메일 인증을 완료해주세요.";

    public static final String VERIFICATION_TEXT =
            "아래 링크를 클릭해 이메일 인증을 완료해주세요.\n " +
                    "http://localhost:8080/api/v1/users/%s/verification";
}
