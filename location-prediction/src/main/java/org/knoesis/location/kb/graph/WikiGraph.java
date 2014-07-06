package org.knoesis.location.kb.graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.uci.ics.jung.algorithms.importance.BetweennessCentrality;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * Create a graph of a given wikipedia page. All the internal links of a Wikipedia page are the nodes in
 * this graph. There exists an edge from one node to another, if the Wikipedia page of the former contains
 * a link to the Wikipedia page of the latter.
 * @author revathy
 */

public class WikiGraph {

	static Logger log = Logger.getLogger(WikiGraph.class.getName());
	private DirectedGraph<String,Integer> cgraph;
	private Map<String, List<String>> edges;
	
	public WikiGraph(Map<String, List<String>> edges) {
		this.cgraph = new DirectedSparseGraph<String,Integer>();
		this.edges = new HashMap<String, List<String>>(edges);
		createGraph();
	}
	
	/**
	 * Draw a directed graph 
	 */
	private void createGraph() {	
		int cnt = 1;
		/**
		 * Add all the nodes
		 */
		Set<String> nodes = edges.keySet();
		for(String node : nodes) {
			cgraph.addVertex(node);
		}
		
		/**
		 * Add all the edges
		 */
		for(String sourceNode : nodes) {
			List<String> destNodes = edges.get(sourceNode);
			for(String destNode : destNodes) {				
					cgraph.addEdge(cnt++, sourceNode, destNode);			
			}
		}
		log.info("Number of edges in the graph: " + cgraph.getEdgeCount() + "\tNumber of vertices in the graph:" + cgraph.getVertexCount());		
	}	
	
	/**
	 * Compute the betweenness centrality of each node in the graph
	 * @return a map of node and corresponding betweenness centrality measure
	 */
	public Map<String, Double> getBetweennessCentralityScores() {
		Map<String,Double> bcScores = new HashMap<String,Double>();	
		BetweennessCentrality<String,Integer> ranker = new BetweennessCentrality<String,Integer>(cgraph);
		ranker.setRemoveRankScoresOnFinalize(false);
		ranker.evaluate();
		
		Set<String> nodes = edges.keySet();
		for(String node : nodes) {
				double n = (double)cgraph.getVertexCount();			
				/**
				 * Normalize the score by the size of the graph
				 */
				double normalizedScore = ranker.getVertexRankScore(node)/((n-1)*(n-2));
				bcScores.put(node,normalizedScore);							
		}
		return bcScores;
	}
		
}
