//links between cities (railroads)
public class Edge{
    double length;//length of edge in miles
    Node start, goal;

    Edge(Node goal, Node start, double length){
        this.start = start;
        this.goal = goal;
        this.length = length;
    }
}
