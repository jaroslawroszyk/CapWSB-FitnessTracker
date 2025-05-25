package pl.wsb.fitnesstracker.mail.api;

import lombok.Getter;

@Getter
public class EmailDto {
    private final String toAddress;
    private final String subject;
    private final String content;

    public EmailDto(String toAddress, String subject, String content) {
        this.toAddress = toAddress;
        this.subject = subject;
        this.content = content;
    }
}
