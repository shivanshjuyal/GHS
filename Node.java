

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class Node implements Runnable{
    
    int BASIC = 1;
    int BRANCH = 2;
    int REJECT = 3;

    int SLEEP = 11;
    int FIND = 12;
    int FOUND = 13;

    public ArrayList<BlockingQueue<Message>> messageQueue;
    public int nodeId;
    public HashMap<Integer, Integer> neighboursWtMap;
    public HashMap<Integer, Integer> neighboursStatusMap = new HashMap<>();
    public Collection<Integer> allNeighbours;
    public int level;
    public int state;
    public int rec;
    public int name;
    public int parent;
    public int bestNode;
    public int bestWt;
    public int testNode;
    public int totalMessagesSent = 0;
    public BlockingQueue<Boolean> algoStops; 

    public Node(int nodeId, HashMap<Integer, Integer> neighboursWtMap, ArrayList<BlockingQueue<Message>> messageQueue, BlockingQueue<Boolean> algoStops) throws InterruptedException{

        this.nodeId = nodeId;
        this.neighboursWtMap = neighboursWtMap;
        this.allNeighbours = neighboursWtMap.keySet();
        this.messageQueue = messageQueue;
        this.algoStops = algoStops;
        this.totalMessagesSent = 0;
        for(Integer i: allNeighbours){
            neighboursStatusMap.put(i, BASIC);
        }
        int minWtNeigh = getMinWtNeighbour();
        neighboursStatusMap.put(minWtNeigh, BRANCH);
        level = 0;
        state = FOUND;
        rec = 0;
        name = this.nodeId;
        parent = -1;
        bestNode = -1;
        bestWt = Integer.MAX_VALUE;
        testNode = -1;

        Message tmp = new Message();
        tmp.setMessageType("connect");
        tmp.setSenderNodeId(nodeId);
        ArrayList<Integer> a = new ArrayList<>();
        a.add(0);
        tmp.setMessageParams(a);
        totalMessagesSent += 1;
        messageQueue.get(minWtNeigh).put(tmp);

    }

    int getMinWtNeighbour(){
        int minWt = Integer.MAX_VALUE;
        int ans = -1;
        for(Integer i: allNeighbours){
            if (neighboursWtMap.get(i) < minWt){
                minWt = neighboursWtMap.get(i);
                ans = i;
            }
        }
        return ans;
    }

    void handleConnectMessage(Message message) throws InterruptedException{
        int q = message.getSenderNodeId();
        int L = message.getMessageParams().get(0);

        if (L < level){
            neighboursStatusMap.put(q, BRANCH);

            Message tmp = new Message();
            tmp.setMessageType("initiate");
            tmp.setSenderNodeId(nodeId);
            ArrayList<Integer> a = new ArrayList<>();
            a.add(level);
            a.add(name);
            a.add(state);
            tmp.setMessageParams(a);
            totalMessagesSent += 1;
            messageQueue.get(q).put(tmp);
        }

        else if (neighboursStatusMap.get(q) == BASIC){
            // totalMessagesSent += 1;
            messageQueue.get(nodeId).put(message);
        }

        else {
            Message tmp = new Message();
            tmp.setMessageType("initiate");
            tmp.setSenderNodeId(nodeId);
            ArrayList<Integer> a = new ArrayList<>();
            a.add(level+1);
            a.add(neighboursWtMap.get(q));
            a.add(FIND);
            tmp.setMessageParams(a);
            totalMessagesSent += 1;
            messageQueue.get(q).put(tmp);
        }
    }

    void handleInitiateMessage(Message message) throws InterruptedException{
        int q = message.getSenderNodeId();
        int level1 = message.getMessageParams().get(0);
        int name1 = message.getMessageParams().get(1);
        int state1 = message.getMessageParams().get(2);
        level = level1;
        name = name1;
        state = state1;
        parent = q;
        bestNode = -1;
        bestWt = Integer.MAX_VALUE;
        testNode = -1;

        for(Integer i: allNeighbours){
            if (neighboursStatusMap.get(i) == BRANCH && i != q){
                Message tmp = new Message();
                tmp.setMessageType("initiate");
                tmp.setSenderNodeId(nodeId);
                ArrayList<Integer> a = new ArrayList<>();
                a.add(level1);
                a.add(name1);
                a.add(state1);
                tmp.setMessageParams(a);
                totalMessagesSent += 1;
                messageQueue.get(i).put(tmp);
            }
        }

        if (state == FIND){
            rec = 0;
            findMin();
        }
    }

    void findMin() throws InterruptedException{
        int nodeToSend = -1;
        int minWt = Integer.MAX_VALUE;

        for (Integer i: allNeighbours){
            if (neighboursStatusMap.get(i) == BASIC && neighboursWtMap.get(i) < minWt){
                minWt = neighboursWtMap.get(i);
                nodeToSend = i;
            }
        }

        if (nodeToSend != -1){
            testNode = nodeToSend;

            Message tmp = new Message();
            tmp.setMessageType("test");
            tmp.setSenderNodeId(nodeId);
            ArrayList<Integer> a = new ArrayList<>();
            a.add(level);
            a.add(name);
            tmp.setMessageParams(a);
            totalMessagesSent += 1;
            messageQueue.get(testNode).put(tmp);
        }

        else {
            testNode = -1;
            report();
        }
    }

    void handleTestMessage(Message message) throws InterruptedException{

        int q = message.getSenderNodeId();
        int level1 = message.getMessageParams().get(0);
        int name1 = message.getMessageParams().get(1);

        if (level1 > level){
            // totalMessagesSent += 1;
            messageQueue.get(nodeId).put(message);
        }

        else if (name1 == name){

            if (neighboursStatusMap.get(q) == BASIC){
                totalMessagesSent += 1;
                neighboursStatusMap.put(q, REJECT);
            }

            if (q != testNode){
                Message tmp = new Message();
                tmp.setMessageType("reject");
                tmp.setSenderNodeId(nodeId);
                ArrayList<Integer> a = new ArrayList<>();
                totalMessagesSent += 1;
                messageQueue.get(q).put(tmp);
            }

            else {
                findMin();
            }

        }
        else{
            Message tmp = new Message();
            tmp.setMessageType("accept");
            tmp.setSenderNodeId(nodeId);
            ArrayList<Integer> a = new ArrayList<>();
            totalMessagesSent += 1;
            messageQueue.get(q).put(tmp);
        }

    }

    void handleAcceptMessage(Message message) throws InterruptedException{

        int q = message.getSenderNodeId();
        testNode = -1;

        if (neighboursWtMap.get(q) < bestWt){
            bestWt = neighboursWtMap.get(q);
            bestNode = q;
        }

        report();

    }

    void handleRejectMessage(Message message) throws InterruptedException{

        int q = message.getSenderNodeId();
        if (neighboursStatusMap.get(q) == BASIC){
            
            neighboursStatusMap.put(q, REJECT);
        }
       findMin();
    }

    void report() throws InterruptedException{
        int size = 0;

        for (Integer i: allNeighbours){
            if (neighboursStatusMap.get(i) == BRANCH && i != parent){
                size += 1;
            }
        }

        if (rec == size && testNode == -1){
            state = FOUND;

            Message tmp = new Message();
            tmp.setMessageType("report");
            tmp.setSenderNodeId(nodeId);
            ArrayList<Integer> a = new ArrayList<>();
            a.add(bestWt);
            tmp.setMessageParams(a);
            totalMessagesSent += 1;
            messageQueue.get(parent).put(tmp);

        }
    }

    void handleReportMessage(Message message) throws InterruptedException{

        int q = message.getSenderNodeId();
        int omega = message.getMessageParams().get(0);

        if (q != parent){
            if (omega < bestWt){
                bestWt = omega;
                bestNode = q;
            }
            rec += 1;
            report();
        }
        else {
            if (state == FIND){
                // totalMessagesSent += 1;
                messageQueue.get(nodeId).put(message);
            }

            else if (omega > bestWt){
                changeRoot();
            }

            else if (omega == bestWt && omega == Integer.MAX_VALUE){
                System.out.println("Stops++++++++");
                algoStops.add(true);
            }
        }

    }

    void changeRoot() throws InterruptedException{

        if (neighboursStatusMap.get(bestNode) == BRANCH){

            Message tmp = new Message();
            tmp.setMessageType("changeroot");
            tmp.setSenderNodeId(nodeId);
            ArrayList<Integer> a = new ArrayList<>();
            totalMessagesSent += 1;
            messageQueue.get(bestNode).put(tmp);

        } else {
            neighboursStatusMap.put(bestNode, BRANCH);

            Message tmp = new Message();
            tmp.setMessageType("connect");
            tmp.setSenderNodeId(nodeId);
            ArrayList<Integer> a = new ArrayList<>();
            a.add(level);
            tmp.setMessageParams(a);
            totalMessagesSent += 1;
            messageQueue.get(bestNode).put(tmp);

        }

    }

    void handleChangeRootMessage(Message message) throws InterruptedException{

        changeRoot();

    }


    @Override
    public void run() {
        
        while (true){

            if (algoStops.size() > 0){
                break;
            }

            if (messageQueue.get(nodeId).size() == 0){
                continue;
            }

            // System.out.println(nodeId + " " + messageQueue.get(nodeId).size() + messageQueue.toString());
            // System.out.println(nodeId + " " + messageQueue.get(nodeId).size());
            Message message = messageQueue.get(nodeId).poll();

            

            try {

                if (message.getMessageType().equalsIgnoreCase("connect")){
                    handleConnectMessage(message);
                }

                else if (message.getMessageType().equalsIgnoreCase("initiate")){
                    handleInitiateMessage(message);
                }

                else if (message.getMessageType().equalsIgnoreCase("test")){
                    handleTestMessage(message);
                }

                else if (message.getMessageType().equalsIgnoreCase("accept")){
                    handleAcceptMessage(message);
                }

                else if (message.getMessageType().equalsIgnoreCase("reject")){
                    handleRejectMessage(message);
                }

                else if (message.getMessageType().equalsIgnoreCase("report")){
                    handleReportMessage(message);
                }

                else if (message.getMessageType().equalsIgnoreCase("changeroot")){
                    handleChangeRootMessage(message);
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            

        }
        
    }

    
    
}
