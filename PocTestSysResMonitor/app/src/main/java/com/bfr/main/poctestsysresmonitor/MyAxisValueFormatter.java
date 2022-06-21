package com.bfr.main.poctestsysresmonitor;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

//La classe de modification les valeurs axis
class MyAxisValueFormatter implements IAxisValueFormatter {
    @Override
    public String getFormattedValue(float value, AxisBase axis){
        axis.setLabelCount(21,true);
        return value + " %";
    }

}