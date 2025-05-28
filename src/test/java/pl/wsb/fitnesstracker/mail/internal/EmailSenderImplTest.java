package pl.wsb.fitnesstracker.mail.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import pl.wsb.fitnesstracker.mail.api.EmailDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class EmailSenderImplTest {

    private JavaMailSender javaMailSender;
    private MailProperties mailProperties;
    private EmailSenderImpl emailSender;


    private void setField(Object target, String fieldName, Object value) throws Exception {
        var field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @BeforeEach
    void setUp() throws Exception {
        javaMailSender = mock(JavaMailSender.class);
        mailProperties = mock(MailProperties.class);

        emailSender = new EmailSenderImpl();
        setField(emailSender, "javaMailSender", javaMailSender);
        setField(emailSender, "mailProperties", mailProperties);
    }


    @Test
    void send_shouldSendEmailWithCorrectFields() {
        EmailDto dto = new EmailDto("recipient@example.com", "Subject", "Hello!");
        when(mailProperties.getFrom()).thenReturn("noreply@example.com");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailSender.send(dto);

        verify(javaMailSender, times(1)).send(captor.capture());
        SimpleMailMessage sentMessage = captor.getValue();

        assertThat(sentMessage.getFrom()).isEqualTo("noreply@example.com");
        assertThat(sentMessage.getTo()).containsExactly("recipient@example.com");
        assertThat(sentMessage.getSubject()).isEqualTo("Subject");
        assertThat(sentMessage.getText()).isEqualTo("Hello!");
    }

    @Test
    void send_shouldLogErrorWhenExceptionOccurs() {
        EmailDto dto = new EmailDto("recipient@example.com", "Subject", "Hello!");
        when(mailProperties.getFrom()).thenReturn("noreply@example.com");

        doThrow(new RuntimeException("Boom!")).when(javaMailSender).send(any(SimpleMailMessage.class));

        emailSender.send(dto);

        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }
}
