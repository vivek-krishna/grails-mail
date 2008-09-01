/*
 * Copyright 2004-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grails.mail

import javax.mail.internet.MimeMessage
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.MailSender
import org.springframework.mail.MailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMailMessage
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine
import org.springframework.web.context.support.WebApplicationContextUtils
import org.springframework.web.context.request.RequestContextHolder
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes


class MailServiceTests extends GroovyTestCase {

    static transactional = false

    MailSender mailSender
    
    void testSendSimpleMessage() {
        def mailService = new MailService()
        

        def message = mailService.sendMail {
            to "fred@g2one.com"
            title "Hello John"
            body 'this is some text'
            from 'king@g2one.com'
        }

        assertEquals "Hello John", message.getSubject()        
        assertEquals 'this is some text', message.getText()
        assertEquals 'fred@g2one.com', message.to[0]
        assertEquals 'king@g2one.com', message.from
    }

    void testSendToMultipleRecipients() {
       def mailService = new MailService()

       def message = mailService.sendMail {
           to "fred@g2one.com","ginger@g2one.com"
           title "Hello John"
           body 'this is some text'
       }




        assertEquals "Hello John", message.getSubject()
        assertEquals 'this is some text', message.getText()
        assertEquals "fred@g2one.com", message.getTo()[0]
        assertEquals "ginger@g2one.com", message.getTo()[1]
    }

    void testSendToMultipleRecipientsAndCC() {
       def mailService = new MailService()

       def message = mailService.sendMail {
           to "fred@g2one.com","ginger@g2one.com"
           from "john@g2one.com"
           cc "marge@g2one.com", "ed@g2one.com"
           bcc "joe@g2one.com"
           title "Hello John"
           body 'this is some text'
       }



        assertEquals "Hello John", message.getSubject()
        assertEquals 'this is some text', message.getText()
        assertEquals 2, message.to.size()
        assertEquals "fred@g2one.com", message.getTo()[0]
        assertEquals "ginger@g2one.com", message.getTo()[1]
        assertEquals "john@g2one.com", message.from
        assertEquals 2, message.cc.size()
        assertEquals "marge@g2one.com", message.cc[0]
        assertEquals "ed@g2one.com", message.cc[1]

    }

    void testSendHtmlMail() {
        def mailService = new MailService()

        // stub send method
        MailSender.metaClass.send = { SimpleMailMessage smm -> }
        JavaMailSender.metaClass.send = { MimeMessage mm -> }

        mailService.mailSender = mailSender

        MimeMailMessage message = mailService.sendMail {
            to "fred@g2one.com"
            subject "Hello John"
            html '<b>Hello</b> World'
        }



        assertEquals "Hello John", message.getMimeMessage().getSubject()
        assertEquals '<b>Hello</b> World', message.getMimeMessage().getContent()                
    }

    void testSendMailView() {
        def mailService = new MailService()

        def ctx = RequestContextHolder.currentRequestAttributes().servletContext
        def applicationContext = ctx[GrailsApplicationAttributes.APPLICATION_CONTEXT]
        
        mailService.groovyPagesTemplateEngine = applicationContext.getBean(GroovyPagesTemplateEngine.BEAN_ID)

        // stub send method
        MailSender.metaClass.send = { SimpleMailMessage smm -> }
        JavaMailSender.metaClass.send = { MimeMessage mm -> }

        mailService.mailSender = mailSender

        MimeMailMessage message = mailService.sendMail {
            to "fred@g2one.com"
            subject "Hello John"
            body(view:'/emails/test', model:[msg:'hello'])
        }

        assertEquals "Hello John", message.getMimeMessage().getSubject()
        assertEquals 'Message is: hello', message.getMimeMessage().getContent().trim()                
    }
}

