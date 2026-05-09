package br.com.sd.middleware;

import java.io.Serializable;

/**
 * Mensagem de RESPOSTA no protocolo requisição-resposta.
 *
 * Campos conforme a figura do livro texto:
 * | messageType | requestId | result |
 */
public class ReplyMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MESSAGE_TYPE = 1; // 1 = reply

    private int messageType;
    private int requestId;
    private byte[] result;
    private boolean success;
    private String errorMessage;

    public ReplyMessage() {}

    public ReplyMessage(int requestId, byte[] result) {
        this.messageType  = MESSAGE_TYPE;
        this.requestId    = requestId;
        this.result       = result;
        this.success      = true;
        this.errorMessage = null;
    }

    public ReplyMessage(int requestId, String errorMessage) {
        this.messageType  = MESSAGE_TYPE;
        this.requestId    = requestId;
        this.result       = new byte[0];
        this.success      = false;
        this.errorMessage = errorMessage;
    }

    public int     getMessageType()  { return messageType;  }
    public int     getRequestId()    { return requestId;    }
    public byte[]  getResult()       { return result;       }
    public boolean isSuccess()       { return success;      }
    public String  getErrorMessage() { return errorMessage; }

    public void setMessageType(int mt) { this.messageType = mt; }
    public void setRequestId(int id)   { this.requestId = id;   }
    public void setResult(byte[] r)    { this.result = r;       }
    public void setSuccess(boolean s)  { this.success = s;      }
    public void setErrorMessage(String m) { this.errorMessage = m; }

    @Override
    public String toString() {
        return String.format("Reply[id=%d, success=%b]", requestId, success);
    }
}
