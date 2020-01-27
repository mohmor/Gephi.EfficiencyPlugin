/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mohmor.gephi.EfficiencyAttack;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Stack;
import java.util.stream.Collectors;
/**
 *
 * @author mohsen moradi
 */
public class GraphBetweenness  {

    public static final String BETWEENNESS = "betweenesscentrality";
    private boolean isDirected;
    private boolean IsNormalized;
    private int _NodeCount = 0;
    private List<NodeModel_ForEfficiency> _NodesModels;
    private List<EdgeModel_ForEfficiency> _edgesModels;
    
    public GraphBetweenness(boolean _isDirected, int nodeCount,List<NodeModel_ForEfficiency> _nodesM,List<EdgeModel_ForEfficiency> _edges, boolean normalized) {
        isDirected = _isDirected;
        IsNormalized = normalized;
        _NodeCount = nodeCount;
        _NodesModels = _nodesM.parallelStream().sorted((NodeModel_ForEfficiency n1, NodeModel_ForEfficiency n2) -> {
                return Double.compare(n1.StoreId,n2.StoreId);
            }).collect(Collectors.toList());
        _edgesModels = _edges;
    }

    public double[] calculateBetweenness(EfficiencyAttack EfficiencyAttackForCancel) {  
        HashMap<NodeModel_ForEfficiency, Integer> indicies = createIndiciesMap();
        double[] nodeBetweenness = new double[_NodeCount];
        
        for (NodeModel_ForEfficiency s : _NodesModels) {
            if(EfficiencyAttackForCancel._isCanceled==true){
                EfficiencyAttackForCancel._Final_Report ="Canacled By User";
                return nodeBetweenness;
            }
            
            Stack<NodeModel_ForEfficiency> S = new Stack<>();
            LinkedList<NodeModel_ForEfficiency>[] P = new LinkedList[_NodeCount];
            double[] theta = new double[_NodeCount];
            int[] d = new int[_NodeCount];
            int s_index = indicies.get(s);
            setInitParametetrsForNode(s, P, theta, d, s_index, _NodeCount);
            LinkedList<NodeModel_ForEfficiency> Q = new LinkedList<>();
            Q.addLast(s);
            while (!Q.isEmpty()) {
                if(EfficiencyAttackForCancel._isCanceled==true){
                    EfficiencyAttackForCancel._Final_Report ="Canacled By User";
                    return nodeBetweenness;
                }
                NodeModel_ForEfficiency v = Q.removeFirst();
                S.push(v);
                int v_index = indicies.get(v);
                List<EdgeModel_ForEfficiency>  edgeIter = getEdgeIter( v, isDirected);
                
                for (EdgeModel_ForEfficiency edge : edgeIter) {
                    if(EfficiencyAttackForCancel._isCanceled==true){
                        EfficiencyAttackForCancel._Final_Report ="Canacled By User";
                        return nodeBetweenness;
                    }
                    NodeModel_ForEfficiency reachable;
                    if(v.StoreId==edge.Source.StoreId)
                        reachable = edge.Target;
                    else
                        reachable = edge.Source;

                    int r_index = indicies.get(reachable);
                    if (d[r_index] < 0) {
                        Q.addLast(reachable);
                        d[r_index] = d[v_index] + 1;
                    }
                    if (d[r_index] == (d[v_index] + 1)) {
                        theta[r_index] = theta[r_index] + theta[v_index];
                        P[r_index].addLast(v);
                    }
                }
                
            }
            double[] delta = new double[_NodeCount];
            while (!S.empty()) {
                NodeModel_ForEfficiency w = S.pop();
                int w_index = indicies.get(w);
                ListIterator<NodeModel_ForEfficiency> iter1 = P[w_index].listIterator();
                while (iter1.hasNext()) {
                    NodeModel_ForEfficiency u = iter1.next();
                    int u_index = indicies.get(u);
                    delta[u_index] += (theta[u_index] / theta[w_index]) * (1 + delta[w_index]);
                }
                if (w != s) {
                    nodeBetweenness[w_index] += delta[w_index];
                }
            }
        }
        calculateCorrection(indicies, nodeBetweenness, isDirected, IsNormalized);
        return nodeBetweenness;
    }

    private void setInitParametetrsForNode(NodeModel_ForEfficiency s, LinkedList<NodeModel_ForEfficiency>[] P, double[] theta, int[] d, int index, int n) {
        for (int j = 0; j < n; j++) {
            P[j] = new LinkedList<>();
            theta[j] = 0;
            d[j] = -1;
        }
        theta[index] = 1;
        d[index] = 0;
    }

    private List<EdgeModel_ForEfficiency> getEdgeIter(NodeModel_ForEfficiency v, boolean directed) {
        
        List<EdgeModel_ForEfficiency> _edgesOfNode ;
        if (directed) {
            _edgesOfNode = _edgesModels.stream().filter(e->e.Source.StoreId==v.StoreId ).collect(Collectors.toList());
        } else {
            _edgesOfNode = _edgesModels.stream().filter(e->e.Source.StoreId ==v.StoreId || e.Target.StoreId == v.StoreId).collect(Collectors.toList());
        }
        return _edgesOfNode;        
    }

    public HashMap<NodeModel_ForEfficiency, Integer> createIndiciesMap(){//(Graph graph) {
        HashMap<NodeModel_ForEfficiency, Integer> indicies = new HashMap<>();
       
        int index = 0;
        for (NodeModel_ForEfficiency s : _NodesModels) {
            indicies.put(s, index);
            index++;
        }
        return indicies;
        
    }

    private void calculateCorrection( HashMap<NodeModel_ForEfficiency, Integer> indicies,double[] nodeBetweenness, boolean directed, boolean normalized) {
        for (NodeModel_ForEfficiency s : _NodesModels) {
            int s_index = indicies.get(s);
            if (!directed) {
                nodeBetweenness[s_index] /= 2;
            }
            if (normalized) {
                nodeBetweenness[s_index] /= directed ? (_NodeCount - 1) * (_NodeCount - 2) : (_NodeCount - 1) * (_NodeCount - 2) / 2;
            }
            s.Betweenness=nodeBetweenness[s_index];
        }
    }
}

