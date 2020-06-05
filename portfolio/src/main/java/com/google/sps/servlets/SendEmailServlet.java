// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.appengine.mail;

import java.io.IOException;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

/** Servlet that sends an email when contact form is submitted */
@WebServlet("/send-email")
public class SendEmailServlet extends HttpServlet {

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        //get values from form
        String from = getParameter(request, "email").orElse("anonymous");
        String subject = "Message from " + getParameter(request, "name").orElse("Anonymous") + ": " + from;
        String message = getParameter(request, "message").orElse("");

        try {
            Message msg = new MimeMessage(session);
            //appengine lets us use anything@[PROJECT_ID].appspotmail.com for email sending: https://cloud.google.com/appengine/docs/standard/java/mail
            msg.setFrom(new InternetAddress("contact@kashisharora-step-2020.appspotmail.com", "Kashish's Portfolio Contact"));
            msg.addRecipient(Message.RecipientType.TO,
                            new InternetAddress("kashisharora@google.com", "Kashish Arora"));
            msg.setSubject(subject);
            msg.setText(message);
            Transport.send(msg);
        } catch (AddressException e) {
            response.getWriter().println("Failed to send email.");
        } catch (MessagingException e) {
            response.getWriter().println("Failed to send email.");
        }

        response.sendRedirect("/index.html");

    }

    /**
   * @return an Optional of the request parameter
   */
    private Optional<String> getParameter(HttpServletRequest request, String name) {
        String value = request.getParameter(name);
        return Optional.ofNullable(value);
    }

}