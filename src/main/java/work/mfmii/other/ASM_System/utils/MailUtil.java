package work.mfmii.other.ASM_System.utils;

import org.jetbrains.annotations.NotNull;
import work.mfmii.other.ASM_System.Config;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

public class MailUtil {
    public MailUtil(){}

    public boolean sendMail(@NotNull String target, @NotNull String title, @NotNull String content) {
        Properties props = new Properties();
        props.put("mail.smtp.host", new Config(Config.ConfigType.JSON).getString("mail.server.smtp-addr"));
        props.put("mail.smtp.port", new Config(Config.ConfigType.JSON).getInt("mail.server.smtp-port"));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", !new Config(Config.ConfigType.JSON).getBoolean("mail.server.smtp-isLocal"));

        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        props.put("mail.debug", "true");

        String from = new Config(Config.ConfigType.JSON).getString("mail.send.address");
        String sender = new Config(Config.ConfigType.JSON).getString("mail.send.sender_name");
        String username = new Config(Config.ConfigType.JSON).getString("mail.server.username");
        String password = new Config(Config.ConfigType.JSON).getString("mail.server.password");
        String charset = new Config(Config.ConfigType.JSON).getString("mail.send.charset");
        String encoding = new Config(Config.ConfigType.JSON).getString("mail.send.encoding");
        String header = new Config(Config.ConfigType.JSON).getString("mail.send.header");
        String footer = new Config(Config.ConfigType.JSON).getString("mail.send.footer");

        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            MimeMessage message = new MimeMessage(session);

            // Set From:
            message.setFrom(new InternetAddress(from, sender));
            // Set ReplyTo(ユーザーが返信したとき、宛先となるところ):
            message.setReplyTo(new Address[]{new InternetAddress(from)});
            // Set To:
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(target));

            message.setSubject(title, charset);
            message.setText(header+"\n"+content+"\n"+footer, charset);

            message.setHeader("Content-Transfer-Encoding", encoding);

            Transport.send(message);
            return true;
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
