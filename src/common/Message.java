package common;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum MessageType {
        REGISTER,
        REGISTER_CONFIRM,
        CLIENT_LIST_REQUEST,
        CLIENT_LIST_RESPONSE,
        FILE_REQUEST,
        FILE_RESPONSE,
        FILE_TRANSFER,
        ERROR,
        PING,
        PONG
    }
    
    private MessageType type;
    private String senderId;
    private String targetId;
    private String content;
    private Object data;
    private LocalDateTime timestamp;
    
    public Message(MessageType type, String senderId, String targetId, String content) {
        this.type = type;
        this.senderId = senderId;
        this.targetId = targetId;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }
    
    public Message(MessageType type, String senderId, String targetId, Object data) {
        this.type = type;
        this.senderId = senderId;
        this.targetId = targetId;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    
    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    @Override
    public String toString() {
        return String.format("Message[%s: %s->%s: %s]", 
            type, senderId, targetId, content != null ? content : "data");
    }
}