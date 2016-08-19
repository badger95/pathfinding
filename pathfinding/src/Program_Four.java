import java.util.*;
import java.io.*;

/**
This program demonstrates two pathfinding algorithms:
dijkstra's and Astar. They are implemented using data read in as
the global coordinates of many of the railstations in the US,
and the railroads that link them together. The user is then asked
to input the station IDS of the desired starting and ending locations.
If there is no path the user will be notified; otherwise a shortest path
will be printed in the order it is taken, including information on the 
nearest railstation, current railroad, and the current longitude and latitude.

*/

public class Program_Four{
	private static HashMap<String,Node> graph = new HashMap<>();
	private static Set<Edge> edges = new HashSet<>();//all edges
	private static Scanner sc = new Scanner(System.in);//user input
	
	//files provided by instructor Hankins
	private static void readFile(){
		try{
			BufferedReader br = new BufferedReader(new FileReader("qc48.ndr"));
			String line = br.readLine();
			String idNum,stateName,stationName,start,goal;
			double longit,latit,length;
			while(line != null){
				idNum = line.substring(1,8);
				longit = Double.parseDouble(line.substring(8,20));
				latit = Double.parseDouble(line.substring(20,32));
				stateName = line.substring(32,34);
				stationName = line.substring(34,57).trim();
				Node newNode = new Node(idNum,stateName,stationName, latit,longit);
				graph.put(idNum,newNode);
				line = br.readLine();
			}
			br = new BufferedReader(new FileReader("qc48.llr"));
			line = br.readLine();
			while(line != null){
				start = line.substring(10,17);
				Node startN = graph.get(start);
				goal = line.substring(17,24);
				Node goalN = graph.get(goal);
				length = Double.parseDouble(line.substring(24,30));
				Edge edge = new Edge(startN, goalN, length);
				Edge backEdge = new Edge(goalN,startN,length);
				edges.add(backEdge);
				edges.add(edge);
				line = br.readLine();
			}
			br.close();
		}
		catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	//Uses a min heap to compute the shortest path between two inputted nodes
	private static boolean dijkstra(String source, String goal){
		//shortest next Node
		Node u;
		//first node requires no distance
		initSource(source);
		PriorityQueue<Node> unvisited = new PriorityQueue<>(); //unvisited nodes
		//copying into unvisited
		unvisited.addAll(graph.values());
		for(Edge e: edges){
			e.start.adjNodes.put(e.goal,e.length);
			e.goal.adjNodes.put(e.start,e.length);
		}
		while(!unvisited.isEmpty()){
			u = unvisited.poll();
			//relax nodes adj to u
			for(Map.Entry<Node,Double> entry : u.adjNodes.entrySet()){
				relax(u,entry.getKey(),entry.getValue(),unvisited);
				if(entry.getKey().id.equals(goal)){
					getPath(entry.getKey(),source,goal);
					return true;
				}
			}
		}
		return false;//should not happen
	}

	//distance to the start node should be zero
	private static void initSource(String source){
		Node sourceNode = graph.get(source);
		sourceNode.dist = 0.0;
	}
	
	//better paths may be found while examining neighbors so we relax all adjence edges
	private static void relax(Node u, Node adjN, double w, PriorityQueue<Node> unvisited){
		if(adjN.dist > u.dist + w){
			adjN.dist = u.dist + w;
			adjN.parent = u;
			if(unvisited.contains(adjN)){
				unvisited.remove(adjN);
				unvisited.add(adjN);
			}
			else{
				unvisited.add(adjN);
			}
		}
	}
	
	//prints the final shortest path for both algorithms
	private static void getPath(Node current, String source, String goal){
		Stack<Node> s = new Stack<>();
		float pathLength;
		if(current.id.equals(source))
			System.out.println("No distance traveled");
		else if (current.parent != null){
			while(current.parent != null){
				s.push(current);
				if(current.parent.id.equals(source)){
					current = current.parent;
					while(!s.isEmpty()){
						Node next = s.pop();
						pathLength = (float)next.dist - (float)current.dist;
						System.out.println("Arrive at ("+current.latitude+
						","+current.longitude+") "+current.station+", "+current.state+
						"\nTravel "+pathLength+
						" miles "+bearing(current,next)+" degrees ("+compass(current,next)+
						").\nDistane so far="+(float)next.dist+" miles");
						current = next;
						if(next.id.equals(goal)){
							System.out.println("Arrive at ("+current.latitude+
							","+current.longitude+") "+current.station+", "+current.state);
						}
					}
					break;
				}
				current = current.parent;
			}
		}
		else{
				System.out.println("No such route exists. "+current.id);
		}
	}
	
	//aStar searching algorithm. Does not use a heap to get smallest f value
	private static void aStar(String source, String goal){
		Set<Node> openSet = new HashSet<>();
		Set<Node> closedSet = new HashSet<>();
		Node sourceN = graph.get(source);
		Node goalN = graph.get(goal); 
		sourceN.fScore = distance(sourceN,goalN);
		sourceN.gScore = 0;
		Node current = sourceN;	
		openSet.add(current);
		while(openSet.size() > 0){
			current = minFScore(openSet);
			if(current.id.equals(goal)){
				getPath(current,source,goal);
				System.out.println("Goal Found "+current.gScore);
				return;
			}
			closedSet.add(current);
			openSet.remove(current);
			for(Map.Entry<Node,Double> entry : current.adjNodes.entrySet()){
				if(closedSet.contains(entry.getKey()))
					continue;
				double tentativeG = current.gScore + entry.getValue();
				if(!openSet.contains(entry.getKey()))
					openSet.add(entry.getKey());
				else if(tentativeG >= entry.getKey().gScore)
					continue;
				entry.getKey().parent = current;
				entry.getKey().gScore = tentativeG;
				entry.getKey().fScore = entry.getKey().gScore + distance(entry.getKey(),goalN);
			}
		}
		System.out.println("Path doesn't exist.");
	}
	
	//finds the smallest Fscore in a set
	private static Node minFScore(Set<Node> openSet){
		Node min = new Node();
		min.fScore = 999999;
		for(Node n: openSet){
			if(min.fScore > n.fScore){
				min = n;
			}
		}
		return min;
	}
	
	// Adapted from http://stackoverflow.com/questions/365826/calculate-distance-between-2-gps-coordinates
	private static double distance(Node sNode,Node gNode){
		final double DTWOR = (Math.PI/180.0);
		double dlong = (gNode.longitude - sNode.longitude) * DTWOR;
		double dlat = (gNode.latitude - sNode.latitude)* DTWOR;
		double a = Math.pow(Math.sin(dlat/2.0), 2.0) + Math.cos(sNode.latitude*DTWOR)*
		Math.cos(gNode.latitude*DTWOR)*Math.pow(Math.sin(dlong/2.0),2.0);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		return 3956 * c;
	}
	
	// Adapted from http://stackoverflow.com/questions/8502795/get-direction-compass-with-two-longitude-latitude-points
	private static double bearing(Node sNode, Node gNode){
		final double DTWOR = (Math.PI/180.0);
		double dlong = (gNode.longitude - sNode.longitude)*DTWOR;
		double y = Math.sin(dlong)*Math.cos(gNode.latitude*DTWOR);
		double x = Math.cos(sNode.latitude * DTWOR)*Math.sin(gNode.latitude*DTWOR) -
		Math.sin(sNode.latitude * DTWOR)*Math.cos(gNode.latitude *DTWOR)*Math.cos(dlong);
		double brng = (Math.atan2(y,x))*180/Math.PI;
		if(brng < 0){
			brng = (360 - Math.abs(brng));
		}
		return brng;
	}
	
	// Given nodes n1 and n2, return the compass bearing from n1 to n2.
	private static String compass(Node sNode, Node gNode){
  double brng = bearing(sNode, gNode);
  if (brng >= 337.5 || brng <= 22.5) {
   return "N";
  } else if (brng >= 22.5 && brng <= 67.5) {
   return "NE";
  } else if (brng >= 67.5 && brng <= 112.5) {
   return "E";
  } else if (brng >= 112.5 && brng <= 157.5) {
   return "SE";
  } else if (brng >= 157.5 && brng <= 202.5) {
   return "S";
  } else if (brng >= 202.5 && brng <= 247.5) {
   return "SW";
  } else if (brng >= 247.5 && brng <= 292.5) {
   return "W";
  } else if (brng >= 292.5 && brng <= 337.5) {
   return "NW";
  }
   return "You are so lost";
}
	
	public static void main(String [] args){
		readFile();
		
		System.out.print("Enter a source ID in format: ####### ");
		String source = sc.nextLine();
		System.out.print("\nEnter a goal ID in format: ####### ");
		String goal = sc.nextLine();
		System.out.println("\nResults using Dijkstra's");
		dijkstra(source, goal);
		System.out.println("\n\nResults using A_* Search");
		aStar(source,goal);
	}
	


}