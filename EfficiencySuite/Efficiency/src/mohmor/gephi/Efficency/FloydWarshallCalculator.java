/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mohmor.gephi.Efficency;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
/**
 *
 * @author mohsen moradi
 */
public class FloydWarshallCalculator {
    
    MyArray2D FloydWarshallCalculator(final int nodeCount ,final MyArray2D matrix)
    {
        for (int k = 0; k < nodeCount; k++)
        {
            int _kk = k;
            List<Integer> range = IntStream.rangeClosed(0, nodeCount-1).boxed().collect(Collectors.toList());
       
            range.parallelStream().forEach((i) ->{
                for (int j = 0; j < nodeCount; j++){
                    if(i==_kk || i==j)
                        continue;
                    if(Double.isInfinite(matrix.get(i,_kk)) || Double.isInfinite(matrix.get(_kk,j)))
                        continue;
                    if (matrix.get(i,_kk) + matrix.get(_kk,j) <= matrix.get(i,j))
                    {
                        matrix.Set(i,j, matrix.get(i,_kk) + matrix.get(_kk,j));
                        //_dist.Set(i,j, _dist.get(_kk,j));
                    }
                }
            });
        }
        return matrix;
    }
}
