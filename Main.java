import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;




class Main{

    // LinkedBlockingQueue<Message> messages = new LinkedBlockingQueue<Message>();
    ArrayList<BlockingQueue<Message>> messageQueue
            = new ArrayList<BlockingQueue<Message>>();
    ArrayList<Boolean> stopAlgo = new ArrayList<>();

    ReentrantLock lock = new ReentrantLock();

    BlockingQueue<Boolean> algoStops = new LinkedBlockingQueue<>();

    public void initAllNodes(String i_f, String o_f) throws NumberFormatException, IOException, InterruptedException{
        stopAlgo.add(false);
        File file = new File(i_f);
        BufferedReader br = new BufferedReader(new FileReader(file));
        ArrayList<ArrayList<Integer>> allEdges = new ArrayList<>();
        int numOfNodes = Integer.parseInt(br.readLine());
        String line;
        int numOfEdges = 0;
        while ((line = br.readLine()) != null){
            numOfEdges += 1;
            line = line.substring(1, line.length()-1);
            List<String> sp = Arrays.asList(line.split(","));
            ArrayList<Integer> ints = new ArrayList<>();
            ints.add(Integer.parseInt(sp.get(0).trim()));
            ints.add(Integer.parseInt(sp.get(1).trim()));
            ints.add(Integer.parseInt(sp.get(2).trim()));
            allEdges.add(ints);
        }
        // System.out.println(allEdges.toString());

        for (int i=0; i<numOfNodes; i++){
            messageQueue.add(new LinkedBlockingQueue<>());
        }

        ArrayList<Node> allNodes = new ArrayList<>();

        for (int i=0; i<numOfNodes; i++){
            HashMap<Integer, Integer> neighWtMap = new HashMap<>();
            for (int j = 0; j<allEdges.size(); j++){
                int n1 = allEdges.get(j).get(0);
                int n2 = allEdges.get(j).get(1);
                int wt = allEdges.get(j).get(2);
                if (i == n1){
                    neighWtMap.put(n2, wt);
                }
                if (i == n2){
                    neighWtMap.put(n1, wt);
                }
            }

            allNodes.add(new Node(i, neighWtMap, messageQueue, algoStops));

        }

        ArrayList<Thread> allThreads = new ArrayList<>();

        for (int i = 0; i<numOfNodes; i++){
            allThreads.add(new Thread(allNodes.get(i)));
        }

        for (int i = 0; i<numOfNodes; i++){
            allThreads.get(i).start();
        }

        for (int i = 0; i<numOfNodes; i++){
            allThreads.get(i).join();
        }

        

        int total = 0;

        for (Node node: allNodes){
            HashMap<Integer, Integer> mp = node.neighboursStatusMap;
            for (Integer i: mp.keySet()){
                if (mp.get(i) == 2){
                    total +=1;
                }
            }
        }

        HashSet<Integer> st = new HashSet<>();

        ArrayList<ArrayList<Integer>> ans = new ArrayList<>();

        int totalMessages = 0;

        for (Node node: allNodes){
            totalMessages += node.totalMessagesSent;
            HashMap<Integer, Integer> mp = node.neighboursStatusMap;
            HashMap<Integer, Integer> wtmp = node.neighboursWtMap;
            for (Integer i: mp.keySet()){
                if (mp.get(i) == 2 && !st.contains(wtmp.get(i))){
                    st.add(wtmp.get(i));
                    ArrayList<Integer> tmp = new ArrayList<>();
                    if (node.nodeId < i){
                        tmp.add(node.nodeId);
                        tmp.add(i);
                    } else {
                        tmp.add(i);
                        tmp.add(node.nodeId);
                    }
                    tmp.add(wtmp.get(i));
                    ans.add(tmp);
                }
            }

        }

        ans.sort(new Comparator<ArrayList<Integer>>(){

            @Override
            public int compare(ArrayList<Integer> a1, ArrayList<Integer> a2) {
                return Integer.compare(a1.get(2), a2.get(2));
            }
            
        });

        System.out.println("Total edges in ans are = " + ans.size());
        // System.out.println(ans.toString());

        FileWriter myWriter = new FileWriter(o_f);

        for (int i = 0; i < ans.size(); i++){

            if ( i == ans.size() - 1){
                myWriter.write("(" + ans.get(i).get(0) + " , " + ans.get(i).get(1) + ", " + ans.get(i).get(2) + ")");
            } else {
                myWriter.write("(" + ans.get(i).get(0) + " , " + ans.get(i).get(1) + ", " + ans.get(i).get(2) + ")\n");
            }

        }
        myWriter.close();

        System.out.println("Completed\n");

        System.out.println("Num of nodes = " + numOfNodes + "; Num of Edges = " + numOfEdges + "; Total msgs = " + totalMessages);

        
    }

    public static void main(String args[]) throws NumberFormatException, IOException, InterruptedException{
        String input_file = args[0];
        String output_file = args[1];
        // System.out.println(input_file+"\n");
        // System.out.println(output_file+"\n");
        // return ;
        new Main().initAllNodes(input_file, output_file);

    }
}