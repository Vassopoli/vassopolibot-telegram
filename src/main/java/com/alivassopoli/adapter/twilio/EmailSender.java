package com.alivassopoli.adapter.twilio;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;

@ApplicationScoped
public class EmailSender {
    private static final Logger LOG = Logger.getLogger(EmailSender.class);

    private final String sendgridApiKey;
    private final String senderEmail;
    private final String copyEmail;

    public EmailSender(@ConfigProperty(name = "vassopolibot-telegram-webhook.sendgrid.apikey") final String sendgridApiKey,
                       @ConfigProperty(name = "vassopolibot-telegram-webhook.sender-email") final String senderEmail,
                       @ConfigProperty(name = "vassopolibot-telegram-webhook.copy-email") final String copyEmail) {
        this.sendgridApiKey = sendgridApiKey;
        this.senderEmail = senderEmail;
        this.copyEmail = copyEmail;
    }

    public void execute(final String recipient, final String html) {

        final Email from = new Email(senderEmail);
        final String subject = "Shopping List";
        final Email to = new Email(recipient);
        final Email bcc = new Email(copyEmail);
        final Content content = new Content("text/html", html);
        final Mail mail = new Mail(from, subject, to, content);
        mail.getPersonalization().get(0).addBcc(bcc);
        final SendGrid sg = new SendGrid(sendgridApiKey);

        final Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            LOG.infof("sendgrid reponse statuscode %s", response.getStatusCode());
            LOG.infof("sendgrid reponse body %s", response.getBody());
            LOG.infof("sendgrid reponse headers %s", response.getHeaders());
        } catch (IOException ex) {
            ex.printStackTrace();
        }


        //TODO: Use AWS SES
//        SendEmailResponse sesReturn = ses.sendEmail(req -> req
//                .source(copyEmail)
//                .destination(d -> d
//                        .toAddresses(recipient)
//                        .bccAddresses(copyEmail))
//                .message(msg -> msg
//                        .subject(sub -> sub.data("Shopping List"))
//                        .body(b -> b.html(txt -> txt.data(reallyFinalResult)))));
//
//        LOG.infof("ses return: %s", sesReturn);
    }
}
