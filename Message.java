
import java.util.ArrayList;

public class Message {
    private int senderNodeId;

    private int receiverNodeId;
    private String messageType;
    private ArrayList<Integer> messageParams;

    public int getSenderNodeId() {
        return this.senderNodeId;
    }

    public void setSenderNodeId(int senderNodeId) {
        this.senderNodeId = senderNodeId;
    }

    public int getReceiverNodeId() {
        return this.receiverNodeId;
    }

    public void setReceiverNodeId(int receiverNodeId) {
        this.receiverNodeId = receiverNodeId;
    }

    public String getMessageType() {
        return this.messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public ArrayList<Integer> getMessageParams() {
        return this.messageParams;
    }

    public void setMessageParams(ArrayList<Integer> messageParams) {
        this.messageParams = messageParams;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        String params = "";
        if (messageParams == null){
            params = "null";
        } else{
            params = messageParams.toString();
        }
        String type = messageType;
        return type + " %%% " + params;
    }
    



}
