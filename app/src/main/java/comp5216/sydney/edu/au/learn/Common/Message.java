package comp5216.sydney.edu.au.learn.Common;

public class Message {
    public enum MessageType {
        SENT, RECEIVED, PREVIEW
    }

    String content;
    MessageType type;

    public Message(String content, MessageType type) {
        this.content = content;
        this.type = type;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}
