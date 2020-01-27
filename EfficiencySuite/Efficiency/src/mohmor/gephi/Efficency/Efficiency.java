/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mohmor.gephi.Efficency;

import java.util.List;
import org.gephi.graph.api.GraphModel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.gephi.algorithms.shortestpath.DijkstraShortestPathAlgorithm;
import org.gephi.graph.api.*;
import org.gephi.utils.progress.Progress;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author mohsen moradi
 */
public final class Efficiency extends TopComponent implements Statistics , LongTask {

    private String _Final_Report = "";
    private ProgressTicket _ProgressTicket;
    public double _result_Efficiency=-1;
    boolean _isCanceled = false;
    int _tmpIndex_ForProgress = 0;

    @Override
    public void execute(GraphModel gm) {
        
        CalculateEfficiencyFoCurrentGraph();
      
    }

    @Override
    public String getReport() {
        return this._Final_Report;
    }

    @Override
    public boolean cancel() {
        _isCanceled = true;
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket pt) {
        this._ProgressTicket = pt;
    }
    
    
    
    void CalculateEfficiencyFoCurrentGraph(){
        _isCanceled = false; 
        _result_Efficiency = 0;
        _tmpIndex_ForProgress = 0;
        
        long _startTime = System.currentTimeMillis();
         
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        final Graph _graph = graphModel.getGraph();
        Node[] _nodes = _graph.getNodes().toArray();
        Edge[] _edges = _graph.getEdges().toArray();
        
       
        
        Progress.start(_ProgressTicket, _nodes.length);
       
           
        MyArray2D _g_matrix = new MyArray2D(_nodes.length); 
        _g_matrix.Fill_INF_Dist();
        
        if(_nodes.length>0)
            Progress.progress(_ProgressTicket, 1);
        
        for (Edge e : _edges) {
            Node _n1 = e.getSource();
            Node _n2 = e.getTarget();
            
            _g_matrix.Set(_n1.getStoreId(),_n2.getStoreId(), e.getWeight());
            if(!e.isDirected())
                _g_matrix.Set(_n2.getStoreId(),_n1.getStoreId(), e.getWeight());
        }
        
        if(_nodes.length>1)
            Progress.progress(_ProgressTicket, 5);
        
        final MyArray2D _dist = new FloydWarshallCalculator().FloydWarshallCalculator(_nodes.length, _g_matrix);
        
        
        if(_nodes.length>3)
            Progress.progress(_ProgressTicket, (_nodes.length*2)/3);
       
        double _sum = 0;  
        for (int i = 0; i < _nodes.length; i++) 
        { 
            for (int j = 0; j < _nodes.length; j++) 
            { 
                if(i!=j ){
                 
                    if(!Double.isInfinite(_dist._matrix[i][j]) &&   _dist._matrix[i][j]!=0 ){
                        _sum+= (1.0/_dist._matrix[i][j]);
                    }
                        
                                
                }
                
            } 
        }
        if(_nodes.length>0)
            Progress.progress(_ProgressTicket, (_nodes.length*5)/6);
        
         _result_Efficiency = 0;
        if(_nodes.length>1)
            _result_Efficiency = _sum/(_nodes.length*(_nodes.length-1));
      
        if(_isCanceled==true){
            
            _Final_Report ="Canacled By User";
            return;
        }
        
        Progress.finish(_ProgressTicket);
        
        _Final_Report =  "Process Complated (time in mili seconds: " + (System.currentTimeMillis() - _startTime) + ")\n";
        _Final_Report +=  "Efficiency = " + _result_Efficiency + " \n";
        
    }

    
    
}
