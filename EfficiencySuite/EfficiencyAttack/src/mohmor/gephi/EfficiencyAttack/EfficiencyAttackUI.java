/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mohmor.gephi.EfficiencyAttack;

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
public class EfficiencyAttackUI  implements StatisticsUI {

    private EfficiencyAttackPanel _PanelUI_EfficiencyAttack;
    private EfficiencyAttack _EfficiencyAttack; 
 
    
    @Override
    public JPanel getSettingsPanel() {
        _PanelUI_EfficiencyAttack= new EfficiencyAttackPanel();
        
        return _PanelUI_EfficiencyAttack;
    }

    @Override
    public void setup(Statistics ststcs) {
        this._EfficiencyAttack = (EfficiencyAttack) ststcs;
      
            
        
        
    }

    @Override
    public void unsetup() {
        if(_PanelUI_EfficiencyAttack!=null){
            boolean[] _settings= _PanelUI_EfficiencyAttack.GetIsEnabledAttacks();
            this._EfficiencyAttack.ApplySettings(_settings,_PanelUI_EfficiencyAttack.GetScaleX(),_PanelUI_EfficiencyAttack.GetScaleY()
                ,_PanelUI_EfficiencyAttack.GetRemoveCountPerStep());    
        }
        _PanelUI_EfficiencyAttack = null;
        _EfficiencyAttack= null;
    }

    @Override
    public Class<? extends Statistics> getStatisticsClass() {
        return EfficiencyAttack.class;
    }

    @Override
    public String getValue() {
         DecimalFormat df = new DecimalFormat("###.###");
        return "" + df.format(this._EfficiencyAttack._result_Efficiency);
        
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "EfficiencyAttackUI.DisplayName");
        
        
    }

    @Override
    public String getShortDescription() {
        return NbBundle.getMessage(getClass(), "EfficiencyAttackUI.shortDescription");
    }

    @Override
    public String getCategory() {
        return StatisticsUI.CATEGORY_NETWORK_OVERVIEW;
    }

    @Override
    public int getPosition() {
        return 2100;
    }
    
}
