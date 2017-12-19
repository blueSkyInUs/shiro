package com.init.rabbitmq;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * @author lesson
 * @date 2017/12/5 15:27
 */
@Component
public class ProtobufMessageConvert implements MessageConverter {

    private static final String DEFAULT_CHARSET="utf-8";

    private static final String PLAIN_TEXT_TYPE="text";

    @Override
    public org.springframework.amqp.core.Message toMessage(Object object, MessageProperties messageProperties) throws MessageConversionException {
        if (messageProperties == null) {
            messageProperties = new MessageProperties();
        }
        Message message = this.createMessage(object, messageProperties);
        messageProperties = message.getMessageProperties();
        if (messageProperties.getMessageId() == null) {
            messageProperties.setMessageId(UUID.randomUUID().toString());
        }
        return message;
    }

    @Override
    public Object fromMessage(org.springframework.amqp.core.Message message) throws MessageConversionException {
        Object content = null;
        MessageProperties properties = message.getMessageProperties();
        if (properties != null) {
            String contentType = properties.getContentType();
            if (contentType != null && contentType.startsWith(PLAIN_TEXT_TYPE)) {
                String encoding = properties.getContentEncoding();
                if (encoding == null) {
                    encoding = DEFAULT_CHARSET;
                }
                try {
                    content = new String(message.getBody(), encoding);
                } catch (UnsupportedEncodingException var10) {
                    throw new MessageConversionException("failed to convert text-based Message content", var10);
                }
            }
        }
        if (content == null) {
            content = message.getBody();
        }
        return content;
    }

    protected Message createMessage(Object object, MessageProperties messageProperties) throws MessageConversionException {
        byte[] bytes = null;
        if (object instanceof byte[]) {
            bytes = (byte[])object;
            messageProperties.setContentType("application/octet-stream");
        } else if (object instanceof String) {
            try {
                bytes = ((String)object).getBytes(DEFAULT_CHARSET);
            } catch (UnsupportedEncodingException var6) {
                throw new MessageConversionException("failed to convert to Message content", var6);
            }
            messageProperties.setContentType("text/plain");
            messageProperties.setContentEncoding(DEFAULT_CHARSET);
        }
        if (bytes != null) {
            messageProperties.setContentLength((long)bytes.length);
        }
        return new Message(bytes, messageProperties);
    }
}
