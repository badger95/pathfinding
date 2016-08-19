import java.util.HashMap;

//represent railroad stations
 class Node implements Comparable<Object>{
    String id, state, station;
    double latitude,longitude, fScore, gScore; //fscore: hueristic calculated from the distance method plus dist.
    double dist = 999999;//dist from source;
    Node parent = null;//previous node in path;
    //stores the neighbors of a Node, and the corresponding lengths between the Node and its neighbors
    HashMap<Node,Double> adjNodes = new HashMap<>();

    Node(String id,String state, String station, double latitude,double longitude){
        this.state = state;
        this.id = id;
        this.station = station;
        this.latitude = latitude;
        this.longitude = longitude;
    }
    Node(){
        //null Node
    }
    @Override
    public int compareTo(Object n){
        Node node = (Node)n;
        if (dist < node.getDist()) {
            return -1;
        } else if (dist > node.getDist()) {
            return 1;
        } else {
            return 0;
        }
    }

    private double getDist() {
        return dist;
    }
}
