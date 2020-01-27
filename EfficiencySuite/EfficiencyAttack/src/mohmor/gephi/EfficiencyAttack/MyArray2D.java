/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mohmor.gephi.EfficiencyAttack;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 *
 * @author mohsen moradi
 */
public class MyArray2D{
    public MyArray2D(int Count){
        _matrix= new Double[Count][Count];
        _count = Count;
    }
    public void Fill_INF_Dist()
    {
        List<Integer> range = IntStream.rangeClosed(0, _count-1).boxed().collect(Collectors.toList());
        range.parallelStream().forEach((i) ->{
                for (int j = 0; j < _count; j++){
                    if(i!=j)
                        Set(i,j,Double.POSITIVE_INFINITY);
                    else
                        Set(i,j,0.0);
                }
            });
    }
    int _count =0;
    public Double _matrix[][];
    public Double get(int i ,int j){
        return _matrix[i][j];
    }
    public void Set(int i ,int j, Double value){
        try
        {
        if(Double.isInfinite(value))
            value.toString();
        _matrix[i][j] = value;
        }
        catch(Exception exx){
            exx.toString();
        }
    }
}
