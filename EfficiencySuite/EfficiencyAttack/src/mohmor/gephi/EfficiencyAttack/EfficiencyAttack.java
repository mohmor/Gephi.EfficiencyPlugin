/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mohmor.gephi.EfficiencyAttack;


import java.util.LinkedList;
import java.util.List;
import org.gephi.graph.api.GraphModel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.gephi.algorithms.shortestpath.DijkstraShortestPathAlgorithm;
import org.gephi.datalab.api.GraphElementsController;
import org.gephi.graph.api.*;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceInformation;
import org.gephi.statistics.plugin.ChartUtils;
import org.gephi.utils.progress.Progress;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.gephi.utils.TempDirUtils;
import org.gephi.utils.TempDirUtils.TempDir;

/**
 *
 * @author mohsen moradi
 */
public class EfficiencyAttack extends TopComponent implements Statistics , LongTask  {
    
    public String _Final_Report = "";
    private ProgressTicket _ProgressTicket;
    public double _result_Efficiency=-1;
    boolean _isCanceled = false;
    int _tmpIndex_ForProgress = 0;
    boolean _isEnabled_InitDegreeAttack = true;
    boolean _isEnabled_RecalculateDegreeAttack = true;
    boolean _isEnabled_InitBetweennessAttack = true;
    boolean _isEnabled_RecalculateBetweennessAttack = true;
    float _size = 2f;
    float _scaleX = 10f;
    float _scaleY = 1000f;
    int _RemoveCountforeachStep = 1;
    @Override
    public void execute(GraphModel gm) {
       
        
        SimulateAttack();
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
    public void ApplySettings(boolean[] _settings,float scaleX , float scaleY,int removeCountforeachStep){
        _isEnabled_InitDegreeAttack = _settings[0];
        _isEnabled_RecalculateDegreeAttack = _settings[1];
        _isEnabled_InitBetweennessAttack = _settings[2];
        _isEnabled_RecalculateBetweennessAttack = _settings[3];
        _scaleX= scaleX;
        _scaleY = scaleY;
        _RemoveCountforeachStep = removeCountforeachStep;
        if(_RemoveCountforeachStep<1)
            _RemoveCountforeachStep=1;
    }
        
    void SimulateAttack(){
        _isCanceled=false;
        long _startTime = System.currentTimeMillis();
        
         
        ProjectController _pc = Lookup.getDefault().lookup(ProjectController.class);
        Workspace _wkS_base = _pc.getCurrentWorkspace();
        
        GraphModel _gM_Base = Lookup.getDefault().lookup(GraphController.class).getGraphModel(_wkS_base);
        Graph _g_Base = _gM_Base.getGraph();
        
        
        Node[] _nodes_Base = _g_Base.getNodes().toArray();
        Edge[] _edges_Base = _g_Base.getEdges().toArray();
        
        Progress.start(_ProgressTicket, 100);
        
        ArrayList<NodeModel_ForEfficiency> _lst_nodesModel = new ArrayList();
        for (Node _n : _nodes_Base) {
            NodeModel_ForEfficiency _nm = new NodeModel_ForEfficiency();
            _nm.NodeId =  (String)_n.getId();
            _nm.StoreId =  _n.getStoreId();
            
            _lst_nodesModel.add(_nm);
        }
        
        ArrayList<EdgeModel_ForEfficiency> _lst_edgesModel = new ArrayList();
        for (Edge _e : _edges_Base) {
            EdgeModel_ForEfficiency _model = new EdgeModel_ForEfficiency();
            _model.IsDirected = _e.isDirected();
            _model.Weight = _e.getWeight();
            _model.Source =  _lst_nodesModel.stream().filter(n->n.StoreId ==_e.getSource().getStoreId()).findFirst().get();
            _model.Target =_lst_nodesModel.stream().filter(n->n.StoreId == _e.getTarget().getStoreId()).findFirst().get();
            
            _lst_edgesModel.add(_model);
            
            //set degree for nodes
            _lst_nodesModel.stream().filter((n) ->   n.StoreId == _model.Source.StoreId || n.StoreId == _model.Target.StoreId ).forEach(n -> n.Degree++);
            
        }
        
        
      
        
        Double _Efficiency_Base = CalculateEfficiencyForCurrentGraph(_nodes_Base.length,_lst_edgesModel);
        _Efficiency_Base.toString();
        _result_Efficiency = _Efficiency_Base;
        
        Progress.progress(this._ProgressTicket,3);
        try { TimeUnit.SECONDS.sleep(1);  } catch (InterruptedException ex) { Exceptions.printStackTrace(ex); }
        
        LinkedList<Double> _res_lst_Efficiencies_ID = new LinkedList();
        LinkedList<Double> _res_lst_Efficiencies_RD = new LinkedList();
        LinkedList<Double> _res_lst_Efficiencies_IB = new LinkedList();
        LinkedList<Double> _res_lst_Efficiencies_RB = new LinkedList();
        
        
        int _section = 0;
        int _sectionSize = 100;
        int _sectionSum = 0;
        if(_isEnabled_InitDegreeAttack) _section++;
        if(_isEnabled_RecalculateDegreeAttack) _section++;
        if(_isEnabled_InitBetweennessAttack) _section++;
        if(_isEnabled_RecalculateBetweennessAttack)_section++;
        
        if(_section>0)
            _sectionSize = 100/_section;
        
        if(_isCanceled==true){
            Progress.finish(_ProgressTicket);
            _Final_Report ="Canacled By User";
            return;
        }
        
        if(_isEnabled_InitDegreeAttack){
            _res_lst_Efficiencies_ID = Simulate_InitDegreeAttack(_Efficiency_Base,_nodes_Base.length,(ArrayList<EdgeModel_ForEfficiency>)_lst_edgesModel.clone(),(ArrayList<NodeModel_ForEfficiency> ) _lst_nodesModel.clone());
            _sectionSum+=_sectionSize;
            Progress.progress(this._ProgressTicket,_sectionSum);
            try { TimeUnit.SECONDS.sleep(1);  } catch (InterruptedException ex) { Exceptions.printStackTrace(ex); }
        }
        if(_isCanceled==true){
            Progress.finish(_ProgressTicket);
            _Final_Report ="Canacled By User";
            return;
        }
        if(_isEnabled_RecalculateDegreeAttack){
            _res_lst_Efficiencies_RD = Simulate_RecalculateDegreeAttack(_Efficiency_Base,_nodes_Base.length,(ArrayList<EdgeModel_ForEfficiency>)_lst_edgesModel.clone(),(ArrayList<NodeModel_ForEfficiency> ) _lst_nodesModel.clone());
            _sectionSum+=_sectionSize;
            Progress.progress(this._ProgressTicket,_sectionSum);
            try { TimeUnit.SECONDS.sleep(1);  } catch (InterruptedException ex) { Exceptions.printStackTrace(ex); }
        }
        if(_isCanceled==true){
            Progress.finish(_ProgressTicket);
            _Final_Report ="Canacled By User";
            return;
        }
        if(_isEnabled_InitBetweennessAttack){
            _res_lst_Efficiencies_IB = Simulate_InitBetweennessAttack(_Efficiency_Base,_nodes_Base.length,(ArrayList<EdgeModel_ForEfficiency>)_lst_edgesModel.clone(),(ArrayList<NodeModel_ForEfficiency> ) _lst_nodesModel.clone(),_g_Base.isDirected());
            _sectionSum+=_sectionSize;
            Progress.progress(this._ProgressTicket,_sectionSum);
            try { TimeUnit.SECONDS.sleep(1);  } catch (InterruptedException ex) { Exceptions.printStackTrace(ex); }
        }
        if(_isCanceled==true){
            Progress.finish(_ProgressTicket);
            _Final_Report ="Canacled By User";
            return;
        }
        if(_isEnabled_RecalculateBetweennessAttack){
            _res_lst_Efficiencies_RB = Simulate_RecalculateBetweennessAttack(_Efficiency_Base,_nodes_Base.length,(ArrayList<EdgeModel_ForEfficiency>)_lst_edgesModel.clone(),(ArrayList<NodeModel_ForEfficiency> ) _lst_nodesModel.clone(),_g_Base.isDirected());
            _sectionSum+=_sectionSize;
            Progress.progress(this._ProgressTicket,_sectionSum);
            try { TimeUnit.SECONDS.sleep(1);  } catch (InterruptedException ex) { Exceptions.printStackTrace(ex); }
        }
        if(_isCanceled==true){
            Progress.finish(_ProgressTicket);
            _Final_Report ="Canacled By User";
            return;
        }
        
       
          
        Workspace _workSp_result = _pc.duplicateWorkspace(_wkS_base);
        _pc.renameWorkspace(_workSp_result, "Result_of_" + _wkS_base.getLookup().lookup(WorkspaceInformation.class).getName());
        
        if(_isEnabled_InitDegreeAttack)
            DrawAttackInCurrentGraph(_res_lst_Efficiencies_ID,1);
        if(_isEnabled_RecalculateDegreeAttack)
            DrawAttackInCurrentGraph(_res_lst_Efficiencies_RD,2);
        if(_isEnabled_InitBetweennessAttack)
            DrawAttackInCurrentGraph(_res_lst_Efficiencies_IB,3);
        if(_isEnabled_RecalculateBetweennessAttack)
            DrawAttackInCurrentGraph(_res_lst_Efficiencies_RB,4);
        
        DrawSystemOfCoordinatesInCurrentGraph(_g_Base.getNodeCount());
        DrawAxisColorHelperInCurrentGraph();
        
        _Final_Report =  "Process Complated (time in mili seconds: " + (System.currentTimeMillis() - _startTime) + ")\n";
        _Final_Report +=  "Efficiency = " + _result_Efficiency + " \n";
        
        createPlotReport(false,_res_lst_Efficiencies_ID,_res_lst_Efficiencies_RD,_res_lst_Efficiencies_IB,_res_lst_Efficiencies_RB);
        
       _pc.openWorkspace(_wkS_base);
        Progress.finish(_ProgressTicket);
      
    }
    
    
    LinkedList<Double> Simulate_InitDegreeAttack(Double efficiency_Base,int _nodeCount ,List<EdgeModel_ForEfficiency> _edges,List<NodeModel_ForEfficiency> _nodesM){
        LinkedList<Double> _res_lst_Efficiencies = new LinkedList<>();
        _res_lst_Efficiencies.add(efficiency_Base);
        //sort nodes with degree
        List<NodeModel_ForEfficiency> _nodesM_Sorted = _nodesM.parallelStream().sorted((NodeModel_ForEfficiency n1, NodeModel_ForEfficiency n2) -> {
            return Integer.compare(n2.Degree,n1.Degree);
        }).collect(Collectors.toList());
        // remove each node and caculate Efficiency
        while(_nodesM_Sorted.size()>0)
        {
            if(_isCanceled==true){
           // Progress.finish(_ProgressTicket);
            _Final_Report ="Canacled By User";
            return _res_lst_Efficiencies;
            }
            if(_nodesM_Sorted.size()<2){
                //remove first node with top degree
                 NodeModel_ForEfficiency _n0 = _nodesM_Sorted.get(0);
                _edges.removeIf(n -> (n.Source.StoreId  == _n0.StoreId ||n.Target.StoreId  == _n0.StoreId ));
                _nodesM_Sorted.remove(0);
            }
            for (int i = 0; i < _RemoveCountforeachStep; i++) {
                if(_nodesM_Sorted.size()<2)
                    break;
                //remove first node with top degree
                NodeModel_ForEfficiency _n0 = _nodesM_Sorted.get(0);
                _edges.removeIf(n -> (n.Source.StoreId  == _n0.StoreId ||n.Target.StoreId  == _n0.StoreId ));
                _nodesM_Sorted.remove(0);
            }
            Double _efficiency_afterDelete = CalculateEfficiencyForCurrentGraph(_nodesM_Sorted.size(),_edges);
            _res_lst_Efficiencies.add(_efficiency_afterDelete);
            System.gc();
        }
        return _res_lst_Efficiencies;
    }
    
    LinkedList<Double> Simulate_RecalculateDegreeAttack(Double efficiency_Base,int _nodeCount ,List<EdgeModel_ForEfficiency> _edges,List<NodeModel_ForEfficiency> _nodesM){
        LinkedList<Double> _res_lst_Efficiencies = new LinkedList<>();
        _res_lst_Efficiencies.add(efficiency_Base);
        //sort nodes with degree
        List<NodeModel_ForEfficiency> _nodesM_sorted = _nodesM.parallelStream().sorted((NodeModel_ForEfficiency n1, NodeModel_ForEfficiency n2) -> {
            return Integer.compare(n2.Degree,n1.Degree);
        }).collect(Collectors.toList());
        // remove each node and caculate Efficiency
        while(_nodesM_sorted.size()>0)
        {
            if(_isCanceled==true){
            _Final_Report ="Canacled By User";
            return _res_lst_Efficiencies;
            }
            if(_nodesM_sorted.size()<2){
                 //remove first node with top degree
                NodeModel_ForEfficiency _n0 = _nodesM_sorted.get(0);
                List<EdgeModel_ForEfficiency> _edgeRemoved =_edges.stream().filter(n -> (n.Source.StoreId  == _n0.StoreId ||n.Target.StoreId  == _n0.StoreId )).collect(Collectors.toList());
                _edges.removeIf(n -> (n.Source.StoreId  == _n0.StoreId ||n.Target.StoreId  == _n0.StoreId ));
                _nodesM_sorted.remove(0);
            }
            for (int i = 0; i < _RemoveCountforeachStep; i++) {
                if(_nodesM_sorted.size()<2)
                    break;   
                 //sort nodes with degree
                _nodesM_sorted = _nodesM_sorted.parallelStream().sorted((NodeModel_ForEfficiency n1, NodeModel_ForEfficiency n2) -> {
                   return Integer.compare(n2.Degree,n1.Degree);
                }).collect(Collectors.toList());
                //remove first node with top degree
                NodeModel_ForEfficiency _n0 = _nodesM_sorted.get(0);
                final List<EdgeModel_ForEfficiency> _edgeRemoved =_edges.stream().filter(n -> (n.Source.StoreId  == _n0.StoreId ||n.Target.StoreId  == _n0.StoreId )).collect(Collectors.toList());
                _edges.removeIf(n -> (n.Source.StoreId  == _n0.StoreId ||n.Target.StoreId  == _n0.StoreId ));
                _nodesM_sorted.remove(0);
                for(EdgeModel_ForEfficiency _e_removed : _edgeRemoved){
                    _nodesM_sorted.stream().filter((n) ->   n.StoreId == _e_removed.Source.StoreId || n.StoreId == _e_removed.Target.StoreId ).forEach(n -> n.Degree--);
                }
            }
            Double _efficiency_afterDelete = CalculateEfficiencyForCurrentGraph(_nodesM_sorted.size(),_edges);
            _res_lst_Efficiencies.add(_efficiency_afterDelete);
             System.gc();
        }
        return _res_lst_Efficiencies;
    }
    
    LinkedList<Double> Simulate_InitBetweennessAttack(Double efficiency_Base,int _nodeCount ,List<EdgeModel_ForEfficiency> _edges,List<NodeModel_ForEfficiency> _nodesM,boolean _isgraphDirected){
        LinkedList<Double> _res_lst_Efficiencies = new LinkedList<>();
        _res_lst_Efficiencies.add(efficiency_Base);
        //calculateBetweenness(_isgraphDirected,false,_nodeCount,_edges,_nodesM);
        new GraphBetweenness(_isgraphDirected, _nodeCount, _nodesM , _edges,false).calculateBetweenness(this);
        //sort nodes with Betweenness
        _nodesM = _nodesM.parallelStream().sorted((NodeModel_ForEfficiency n1, NodeModel_ForEfficiency n2) -> {
            return Double.compare(n2.Betweenness,n1.Betweenness);
        }).collect(Collectors.toList());
        // remove each node and caculate Efficiency
        while(_nodesM.size()>0)
        {
            if(_isCanceled==true){
           // Progress.finish(_ProgressTicket);
            _Final_Report ="Canacled By User";
            return _res_lst_Efficiencies;
            }
            if(_nodesM.size()<2){
                NodeModel_ForEfficiency _n0 = _nodesM.get(0);
                _edges.removeIf(n -> (n.Source.StoreId  == _n0.StoreId ||n.Target.StoreId  == _n0.StoreId ));
                _nodesM.remove(0);
            }
            for (int i = 0; i < _RemoveCountforeachStep; i++) {
                if(_nodesM.size()<2)
                    break;
                NodeModel_ForEfficiency _n0 = _nodesM.get(0);
                _edges.removeIf(n -> (n.Source.StoreId  == _n0.StoreId ||n.Target.StoreId  == _n0.StoreId ));
                _nodesM.remove(0);
            }
            Double _efficiency_afterDelete = CalculateEfficiencyForCurrentGraph(_nodesM.size(),_edges);
            _res_lst_Efficiencies.add(_efficiency_afterDelete);
             System.gc();
        }
        return _res_lst_Efficiencies;
    }
    
    LinkedList<Double> Simulate_RecalculateBetweennessAttack(Double efficiency_Base,int _nodeCount ,List<EdgeModel_ForEfficiency> _edges,List<NodeModel_ForEfficiency> _nodesM,boolean _isgraphDirected){
        LinkedList<Double> _res_lst_Efficiencies = new LinkedList<>();
        _res_lst_Efficiencies.add(efficiency_Base);
        // remove each node and caculate Efficiency
        while(_nodesM.size()>0)
        {
            if(_isCanceled==true){
               // Progress.finish(_ProgressTicket);
              _Final_Report ="Canacled By User";
              return _res_lst_Efficiencies;
            }
            if(_nodesM.size()<2){
                //remove first node with top degree
                NodeModel_ForEfficiency _n0 = _nodesM.get(0);
                _edges.removeIf(n -> (n.Source.StoreId  == _n0.StoreId ||n.Target.StoreId  == _n0.StoreId ));
                _nodesM.remove(0);
                
                _nodeCount--;
            }
            for (int i = 0; i < _RemoveCountforeachStep; i++) {
                if(_nodesM.size()<2)
                    break;
                //calculate Betweenness
                new GraphBetweenness(_isgraphDirected, _nodeCount, _nodesM , _edges,false).calculateBetweenness(this);
                //calculateBetweenness(_isgraphDirected,false,_nodeCount,_edges,_nodesM);
                //sort nodes with Betweenness
                _nodesM = _nodesM.parallelStream().sorted((NodeModel_ForEfficiency n1, NodeModel_ForEfficiency n2) -> {
                    return Double.compare(n2.Betweenness,n1.Betweenness);
                }).collect(Collectors.toList());
                //remove first node with top degree
                NodeModel_ForEfficiency _n0 = _nodesM.get(0);
                _edges.removeIf(n -> (n.Source.StoreId  == _n0.StoreId ||n.Target.StoreId  == _n0.StoreId ));
                _nodesM.remove(0);
                _nodeCount--;
            }
            Double _efficiency_afterDelete = CalculateEfficiencyForCurrentGraph(_nodesM.size(),_edges);
            _res_lst_Efficiencies.add(_efficiency_afterDelete);
            System.gc();
        }
        
        return _res_lst_Efficiencies;
    }
    
    double CalculateEfficiencyForCurrentGraph(int _nodesCount,List<EdgeModel_ForEfficiency> _edges){
        //create new matrix
        MyArray2D _g_matrix = new MyArray2D(_nodesCount); 
        _g_matrix.Fill_INF_Dist();
        _edges.parallelStream().forEach((EdgeModel_ForEfficiency _edge) -> {
            
            _g_matrix.Set(_edge.Source.StoreId,_edge.Target.StoreId, _edge.Weight);
            if(!_edge.IsDirected)
                _g_matrix.Set(_edge.Target.StoreId,_edge.Source.StoreId, _edge.Weight);
        });
        
        //calculate shotest Path FloydWarshall
        new FloydWarshallCalculator().FloydWarshallCalculator(_nodesCount, _g_matrix);
        
        double _sum = Arrays.stream( _g_matrix._matrix).flatMapToDouble(x-> Arrays.stream((Double[])x).mapToDouble(z-> { 
            if(!Double.isInfinite(z) &&   z!=0 ){
                return (1.0/z);
            }  
            return 0.0; 
        }) ).sum();
         
        double _result_efficiency0 = 0;
        if(_nodesCount>1)
            _result_efficiency0 = _sum/(_nodesCount*(_nodesCount-1));
        return _result_efficiency0;
    }
    
    
    void DrawAttackInCurrentGraph(LinkedList<Double> _res_lst_Efficiencies,int colorNo){
        
        GraphModel _gM = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        Graph _g = _gM.getGraph();
        GraphElementsController _GraphElementsController_result = Lookup.getDefault().lookup(GraphElementsController.class);
        
        Color _color;
        switch (colorNo) {
            case 1:
                _color = Color.CYAN;
                break;
            case 2:
                _color = Color.BLUE;
                break;
            case 3:
                _color = Color.ORANGE;
                break;
            case 4:
                _color = Color.RED;
                break;
            default:
                _color = Color.CYAN;
                break;
        }
        
        Node _n0old=null;
        for (int i = 0; i < _res_lst_Efficiencies.size(); i++) {
            
            Double _eff= _res_lst_Efficiencies.get(i);
            float _tmp = _eff.floatValue() * _scaleY;
            Node n0 = _gM.factory().newNode();
            n0.setSize(_size);
            n0.setLabel(String.valueOf(_eff));
            n0.setColor(_color);
            n0.setPosition(i*_scaleX,_tmp);
            _g.addNode(n0);  
           
            
            if(_n0old!= null){
                Edge e1 = _GraphElementsController_result.createEdge(_n0old, n0, true);
                e1.setWeight(2.0f);
                //e1.setLabel(String.valueOf(i));
                e1.setColor(_color);
                _g.addEdge(e1);
            }
            _n0old = n0;
            
        }
       
    }
    
    void DrawSystemOfCoordinatesInCurrentGraph(int countNode){
        
        GraphModel _gM = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        Graph _g = _gM.getGraph();
        GraphElementsController _GraphElementsController_result = Lookup.getDefault().lookup(GraphElementsController.class);
        
        //for line nemoodaar 
        //y down
        Node _nod_line_0 = _gM.factory().newNode();
        _nod_line_0.setSize(1f);
        _nod_line_0.setColor(Color.BLACK);
        _nod_line_0.setPosition(0,-0.1f*_scaleY);
        _g.addNode(_nod_line_0);

        // y top
        Node _nod_line_1 = _gM.factory().newNode();
        _nod_line_1.setSize(1f);
        
        _nod_line_1.setColor(Color.BLACK);
        _nod_line_1.setPosition(0,(_scaleY));
        _g.addNode(_nod_line_1);

        //x left
        Node _nod_line_2 = _gM.factory().newNode();
        _nod_line_2.setSize(1f);
        _nod_line_2.setColor(Color.BLACK);
        _nod_line_2.setPosition(-2*_scaleX,0);
        _g.addNode(_nod_line_2);

        //x right
        Node _nod_line_3 = _gM.factory().newNode();
        _nod_line_3.setSize(1f);
        _nod_line_3.setColor(Color.BLACK);
        _nod_line_3.setPosition((countNode/_RemoveCountforeachStep+1f)* _scaleX,0);
        _g.addNode(_nod_line_3);

        //x axis
        Edge e0 = _GraphElementsController_result.createEdge(_nod_line_0, _nod_line_1, true);
        e0.setWeight(2.0f);
        e0.setColor(Color.black);
        e0.setLabel(NbBundle.getMessage(getClass(), "EfficiencyText"));
        _g.addEdge(e0);
        

        //y axis
        Edge e1 = _GraphElementsController_result.createEdge(_nod_line_2, _nod_line_3, true);
        e1.setWeight(2.0f);
        e1.setColor(Color.black);
        e1.setLabel(NbBundle.getMessage(getClass(), "EfficiencyAttackUI.Countofnodedeleted"));
        _g.addEdge(e1);
        
    }
    
    void DrawAxisColorHelperInCurrentGraph(){
        
        GraphModel _gM = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        Graph _g = _gM.getGraph();
        
        Node _nod_desc_0 = _gM.factory().newNode();
        _nod_desc_0.setSize(10f);
        _nod_desc_0.setColor(Color.cyan);
        _nod_desc_0.setLabel("Degree");
        _nod_desc_0.setPosition(15,-50);
        _g.addNode(_nod_desc_0);

        ///show each color description
        Node _nod_desc_1 = _gM.factory().newNode();
        _nod_desc_1.setSize(10f);
        _nod_desc_1.setColor(Color.orange);
        _nod_desc_1.setLabel("Betweeness");
        _nod_desc_1.setPosition(65,-50);
        _g.addNode(_nod_desc_1);

        ///show each color description
        Node _nod_desc_2 = _gM.factory().newNode();
        _nod_desc_2.setSize(10f);
        _nod_desc_2.setColor(Color.blue);
        _nod_desc_2.setLabel("ReCalculate Degree");
        _nod_desc_2.setPosition(115,-50);
        _g.addNode(_nod_desc_2);

        ///show each color description
        Node _nod_desc_3 = _gM.factory().newNode();
        _nod_desc_3.setSize(10f);
        _nod_desc_3.setColor(Color.red);
        _nod_desc_3.setLabel("ReCalculate Betweeness");
        _nod_desc_3.setPosition(165,-50);
        _g.addNode(_nod_desc_3);
        
    }
    
    
    private void createPlotReport(boolean isNormalized,LinkedList<Double> initDegree,LinkedList<Double> RecalculateDegree
            ,LinkedList<Double> initBetweenness,LinkedList<Double> RecalculateBetweenness){
        String htmlIMG1 = "";
        String htmlIMG2 = "";
        String htmlIMG3 = "";
        String htmlIMG4 = "";
        try {
            TempDir tempDir = TempDirUtils.createTempDir();
            if(initDegree.size()>0)
                htmlIMG1 = createImageFile(tempDir, initDegree, "Init Degree Attack","Count of Removed Nodes", "Efficiency",isNormalized);
            if(RecalculateDegree.size()>0)
                htmlIMG2 = createImageFile(tempDir, RecalculateDegree, "Recalculate Degree Attack","Count of Removed Nodes", "Efficiency",isNormalized);
            if(initBetweenness.size()>0)
                htmlIMG3 = createImageFile(tempDir, initBetweenness, "Init Betweenness Attack","Count of Removed Nodes", "Efficiency",isNormalized);
            if(RecalculateBetweenness.size()>0)
                htmlIMG4 = createImageFile(tempDir, RecalculateBetweenness, "Recalculate Betweenness Attack","Count of Removed Nodes", "Efficiency",isNormalized);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        _Final_Report += "<HTML> <BODY> <h1>Graph Efficiency Attack Simulation  Report </h1> "
                + "<hr>"
                + "<br>"
               
                + htmlIMG1 + "<br /><br />"
                + htmlIMG2 + "<br /><br />"
                + htmlIMG3 + "<br /><br />"
                + htmlIMG4
                + "<br /><br />" + "<h2> Algorithm: </h2>"
                + "Ulrik Brandes, <i>A Faster Algorithm for Betweenness Centrality</i>, in Journal of Mathematical Sociology 25(2):163-177, (2001)<br />"
                + "Anu Pradhan, M.ASCE1; and G. (Kumar) Mahinthakumar, M.ASCE2, <i>Finding All-Pairs Shortest Path for a Large-Scale </i><br />"
                +", DOI: 10.1061/(ASCE)CP.1943-5487.0000220.<br />"
                
                + "</BODY> </HTML>";
    }
    
     private String createImageFile(TempDir tempDir, LinkedList<Double> pVals, String pName, String pX, String pY,boolean isNormalized) {
        //distribution of values
        Map<Integer ,Double> dist = new HashMap<>();
        for (int i = 0; i < pVals.size(); i++) {
            Double d = pVals.get(i);
            
            dist.put(i,d);
            
        }

        //Distribution series
        XYSeries dSeries = ChartUtils.createXYSeries(dist, pName);

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(dSeries);

        JFreeChart chart = ChartFactory.createXYLineChart(
                pName,
                pX,
                pY,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false);
        chart.removeLegend();
        ChartUtils.decorateChart(chart);
        ChartUtils.scaleChart(chart, dSeries, isNormalized);
        return ChartUtils.renderChart(chart, pName + ".png");
    }
    
    
    
}
