package br.com.sd.middleware;

import java.io.Serializable;

/**
 * Mensagem de REQUISIÇÃO no protocolo requisição-resposta.
 *
 * Campos conforme a figura do livro texto:
 * | messageType | requestId | objectReference | methodID | arguments |
 */
public class RequestMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MESSAGE_TYPE = 0; // 0 = request

    private int messageType;
    private int requestId;
    private String objectReference;
    private String methodId;
    private byte[] arguments;

    public RequestMessage() {}

    public RequestMessage(int requestId, String objectReference,
                          String methodId, byte[] arguments) {
        this.messageType     = MESSAGE_TYPE;
        this.requestId       = requestId;
        this.objectReference = objectReference;
        this.methodId        = methodId;
        this.arguments       = arguments;
    }

    public int getMessageType() { 
        return messageType;      
    }
    public int getRequestId() { 
        return requestId;        
    }
    public String getObjectReference() { 
        return objectReference;  
    }
    public String getMethodId() { 
        return methodId;         
    }
    public byte[] getArguments() { 
        return arguments;        
    }

    public void setMessageType(int messageType) { 
        this.messageType = messageType;         
    }
    public void setRequestId(int requestId) { 
        this.requestId = requestId;             
    }
    public void setObjectReference(String objectReference) { 
        this.objectReference = objectReference; 
    }
    public void setMethodId(String methodId) { 
        this.methodId = methodId;               
    }
    public void setArguments(byte[] arguments) { 
        this.arguments = arguments;             
    }

    @Override
    public String toString() {
        return String.format("Request[id=%d, obj=%s, method=%s]",
                requestId, objectReference, methodId);
    }
}
