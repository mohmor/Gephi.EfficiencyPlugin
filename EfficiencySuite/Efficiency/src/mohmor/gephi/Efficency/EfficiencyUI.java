/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mohmor.gephi.Efficency;


import java.text.DecimalFormat;
import javax.swing.JPanel;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsUI;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mohsen moradi
 */
@ServiceProvider(service = StatisticsUI.class)
public class EfficiencyUI  implements StatisticsUI {

    //private EfficiencyPanel _PanelUI_Efficiency;
    private Efficiency _Efficiency; 

    @Override
    public JPanel getSettingsPanel() {
        //_PanelUI_Efficiency= new EfficiencyPanel();
        return null;//_PanelUI_Efficiency;
    }

    @Override
    public void setup(Statistics ststcs) {
        this._Efficiency = (Efficiency) ststcs;
    }

    @Override
    public void unsetup() {
        //_PanelUI_Efficiency = null;
        _Efficiency= null;
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return Efficiency.class;
    }

    @Override
    public String getValue() {
         DecimalFormat df = new DecimalFormat("###.###");
        return "" + df.format(_Efficiency._result_Efficiency);
        
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "Efficiency");
        
        
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage(getClass(), "EfficiencyUI.shortDescription");
    }

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    @Override
    public int getPosition() {
        return 2000;
    }
    
}