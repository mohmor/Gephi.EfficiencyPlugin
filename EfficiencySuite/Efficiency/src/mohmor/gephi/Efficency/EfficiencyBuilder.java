/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mohmor.gephi.Efficency;

import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author mohsen moradi
 */
@ServiceProvider(service = StatisticsBuilder.class)
public class EfficiencyBuilder implements StatisticsBuilder  {

    @Override
    public String getName() {
         return NbBundle.getMessage(getClass(), "Efficiency");
    }

    @Override
    public Statistics getStatistics() {
         return new Efficiency();
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return Efficiency.class;
    }
    
}
