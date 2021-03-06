package br.com.camel.integration.credit.user.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlRootElement(name = "ERROR")
@XmlType(propOrder = {"id", "correlationId", "message"})
public class MessageError implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -1988118778324665102L;

    private String id;
    private String message;
    private String correlationId;

    @XmlElement(name="id")
    public String getId() {
        return id;
    }

    @XmlElement(name="message")
    public String getMessage() {
        return message;
    }

    @XmlElement(name="correlationId")
    public String getCorrelationId() {
        return correlationId;
    }

    public MessageError(){}

    public MessageError(String id, String message, String correlationId) {
        this.id = id;
        this.message = message;
        this.correlationId = correlationId;
    }


}
