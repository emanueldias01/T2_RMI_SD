package br.com.sd.middleware;

import java.io.Serializable;

/**
 * Referência para um objeto remoto.
 */
public class RemoteObjectRef implements Serializable {
    private static final long serialVersionUID = 1L;

    private String host;
    private int port;
    private String objectName;

    public RemoteObjectRef(String host, int port, String objectName) {
        this.host = host;
        this.port = port;
        this.objectName = objectName;
    }

    public String getHost() { 
        return host;       
    }
    public int getPort() { 
        return port;        
    }
    public String getObjectName() { 
        return objectName;  
    }

    @Override
    public String toString() {
        return String.format("RemoteObjectRef(%s:%d/%s)", host, port, objectName);
    }
}
