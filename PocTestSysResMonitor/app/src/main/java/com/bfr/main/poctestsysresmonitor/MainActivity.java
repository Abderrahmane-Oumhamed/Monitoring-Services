package com.bfr.main.poctestsysresmonitor;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.bfr.main.sysresmonitor.IMonitorService;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "PocTestSysResMonitor";
    private Handler handler = new Handler();
    private Runnable runnable;
    LineChart rateChart;
    LineDataSet dataSetCPU;
    LineDataSet dataSetCPU0;
    LineDataSet dataSetCPU1;
    LineDataSet dataSetCPU2;
    LineDataSet dataSetCPU3;
    LineDataSet dataSetCPU4;
    LineDataSet dataSetCPU5;
    LineDataSet dataSetCPU6;
    LineDataSet dataSetCPU7;
    LineDataSet dataSetGPU;

    ArrayList<Entry> dataValsCPU =  new ArrayList<Entry>();
    ArrayList<Entry> dataValsCPU0 =  new ArrayList<Entry>();
    ArrayList<Entry> dataValsCPU1 =  new ArrayList<Entry>();
    ArrayList<Entry> dataValsCPU2 =  new ArrayList<Entry>();
    ArrayList<Entry> dataValsCPU3 =  new ArrayList<Entry>();
    ArrayList<Entry> dataValsCPU4 =  new ArrayList<Entry>();
    ArrayList<Entry> dataValsCPU5 =  new ArrayList<Entry>();
    ArrayList<Entry> dataValsCPU6 =  new ArrayList<Entry>();
    ArrayList<Entry> dataValsCPU7 =  new ArrayList<Entry>();
    ArrayList<Entry> dataValsGPU = new ArrayList<Entry>();
    Legend legend;
    LineData data;
    TextView tv_rateCPU,tv_rateCPU0,tv_rateCPU1,tv_rateCPU2,tv_rateCPU3,tv_rateCPU4,tv_rateCPU5,tv_rateCPU6,tv_rateCPU7;
    TextView tv_freqCPU0,tv_freqCPU1,tv_freqCPU2,tv_freqCPU3,tv_freqCPU4,tv_freqCPU5,tv_freqCPU6,tv_freqCPU7;
    TextView tv_memTotal,tv_memAvailable,tv_memUtilized,tv_rateGPU,tv_tempCPU;
    private View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //cacher les barres systemUI
        hideSystemUI(this);
        decorView=getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if(visibility==0){
                    decorView.setSystemUiVisibility(hideSystemUI(MainActivity.this));
                }
            }
        });

        // Déclaration du Chart
        rateChart = (LineChart)findViewById(R.id.lineChart);

        tv_rateCPU = (TextView)findViewById(R.id.tv_rateCPU);
        tv_rateCPU0 = (TextView)findViewById(R.id.tv_rateCPU0);
        tv_rateCPU1 = (TextView)findViewById(R.id.tv_rateCPU1);
        tv_rateCPU2 = (TextView)findViewById(R.id.tv_rateCPU2);
        tv_rateCPU3 = (TextView)findViewById(R.id.tv_rateCPU3);
        tv_rateCPU4 = (TextView)findViewById(R.id.tv_rateCPU4);
        tv_rateCPU5 = (TextView)findViewById(R.id.tv_rateCPU5);
        tv_rateCPU6= (TextView)findViewById(R.id.tv_rateCPU6);
        tv_rateCPU7 = (TextView)findViewById(R.id.tv_rateCPU7);

        tv_freqCPU0 = (TextView)findViewById(R.id.tv_freqCPU0);
        tv_freqCPU1 = (TextView)findViewById(R.id.tv_freqCPU1);
        tv_freqCPU2 = (TextView)findViewById(R.id.tv_freqCPU2);
        tv_freqCPU3 = (TextView)findViewById(R.id.tv_freqCPU3);
        tv_freqCPU4 = (TextView)findViewById(R.id.tv_freqCPU4);
        tv_freqCPU5 = (TextView)findViewById(R.id.tv_freqCPU5);
        tv_freqCPU6 = (TextView)findViewById(R.id.tv_freqCPU6);
        tv_freqCPU7 = (TextView)findViewById(R.id.tv_freqCPU7);

        tv_memTotal = (TextView)findViewById(R.id.tv_memTotal);
        tv_memAvailable = (TextView)findViewById(R.id.tv_memAvailable);
        tv_memUtilized = (TextView)findViewById(R.id.tv_memUtilized);
        tv_rateGPU = (TextView)findViewById(R.id.tv_rateGPU);
        tv_tempCPU = (TextView)findViewById(R.id.tv_tempCPU);


        /*
         *  Connexion au service externe monitorService
         */
        Intent intent = new Intent();
        intent.setAction("services.monitorService");
        intent.setPackage("com.bfr.main.sysresmonitor");
        bindService(intent, mConnectionMonitorService, Context.BIND_AUTO_CREATE);

    }

    /**
     *  Callback de connexion au service externe monitorService
     */
    private IMonitorService monitorService1;
    private ServiceConnection mConnectionMonitorService = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(TAG,"POC connecté au service monitorService");
            monitorService1 = IMonitorService.Stub.asInterface(service);


            DecimalFormat df1 = new DecimalFormat("0.00");
            DecimalFormat df2 = new DecimalFormat("0.000");
            //Thread Renvoyer les taux d'utlisation, fréquences instantanées CPUs, quantité de la mémoire (totale,disponible,utilisé)
            // et la température du CPU pour chaque seconde
            runnable = new Runnable() {
                int i=0;
                float valueRateCPU,valueRateCPU0,valueRateCPU1,valueRateCPU2,valueRateCPU3,valueRateCPU4,valueRateCPU5,valueRateCPU6,valueRateCPU7;
                float valueFreqCPU0,valueFreqCPU1,valueFreqCPU2,valueFreqCPU3,valueFreqCPU4,valueFreqCPU5,valueFreqCPU6,valueFreqCPU7;
                float valueTotalMem, valueAvailableMem, valueUtilizedMem, valueRateGPU, valueTempCPU;
                public void run() {
                    try {

                        float[] rateCPU = monitorService1.cpuRate();
                        valueRateCPU  = rateCPU[0];
                        valueRateCPU0 = rateCPU[1];
                        valueRateCPU1 = rateCPU[2];
                        valueRateCPU2 = rateCPU[3];
                        valueRateCPU3 = rateCPU[4];
                        valueRateCPU4 = rateCPU[5];
                        valueRateCPU5 = rateCPU[6];
                        valueRateCPU6 = rateCPU[7];
                        valueRateCPU7 = rateCPU[8];

                        // On divise sur 1000 pour remettre les fréqence(KHz) en MHz
                        valueFreqCPU0 = monitorService1.cpuFreq(0)/1000;
                        valueFreqCPU1 = monitorService1.cpuFreq(1)/1000;
                        valueFreqCPU2 = monitorService1.cpuFreq(2)/1000;
                        valueFreqCPU3 = monitorService1.cpuFreq(3)/1000;
                        valueFreqCPU4 = monitorService1.cpuFreq(4)/1000;
                        valueFreqCPU5 = monitorService1.cpuFreq(5)/1000;
                        valueFreqCPU6 = monitorService1.cpuFreq(6)/1000;
                        valueFreqCPU7 = monitorService1.cpuFreq(7)/1000;


                        valueTempCPU = monitorService1.cpuTemp();
                        valueRateGPU = monitorService1.gpuRate();

                        //On divise sur 1000000 pour remettre les valeurs(KB) en GB
                        valueTotalMem = monitorService1.memoryInfo(0)/1000000;
                        valueAvailableMem = monitorService1.memoryInfo(1)/1000000;
                        valueUtilizedMem = monitorService1.memoryInfo(2)/1000000;



                        tv_memTotal.setText(df1.format(valueTotalMem)+"GB");
                        tv_memAvailable.setText(df2.format(valueAvailableMem)+"GB");
                        tv_memUtilized.setText(df2.format(valueUtilizedMem)+"GB");

                        //Remplissage dataset du Cpu
                        dataValsCPU.add(new Entry(i,valueRateCPU));
                        tv_rateCPU.setText(df1.format(valueRateCPU)+"%");

                        dataValsCPU0.add(new Entry(i,valueRateCPU0));
                        tv_rateCPU0.setText(df1.format(valueRateCPU0)+"%");
                        tv_freqCPU0.setText(df1.format(valueFreqCPU0)+"MHz");


                        dataValsCPU1.add(new Entry(i,valueRateCPU1));
                        tv_rateCPU1.setText(df1.format(valueRateCPU1)+"%");
                        tv_freqCPU1.setText(df1.format(valueFreqCPU1)+"MHz");

                        dataValsCPU2.add(new Entry(i,valueRateCPU2));
                        tv_rateCPU2.setText(df1.format(valueRateCPU2)+"%");
                        tv_freqCPU2.setText(df1.format(valueFreqCPU2)+"MHz");

                        dataValsCPU3.add(new Entry(i,valueRateCPU3));
                        tv_rateCPU3.setText(df1.format(valueRateCPU3)+"%");
                        tv_freqCPU3.setText(df1.format(valueFreqCPU3)+"MHz");

                        dataValsCPU4.add(new Entry(i,valueRateCPU4));
                        tv_rateCPU4.setText(df1.format(valueRateCPU4)+"%");
                        tv_freqCPU4.setText(df1.format(valueFreqCPU4)+"MHz");

                        dataValsCPU5.add(new Entry(i,valueRateCPU5));
                        tv_rateCPU5.setText(df1.format(valueRateCPU5)+"%");
                        tv_freqCPU5.setText(df1.format(valueFreqCPU5)+"MHz");

                        dataValsCPU6.add(new Entry(i,valueRateCPU6));
                        tv_rateCPU6.setText(df1.format(valueRateCPU6)+"%");
                        tv_freqCPU6.setText(df1.format(valueFreqCPU6)+"MHz");

                        dataValsCPU7.add(new Entry(i,valueRateCPU7));
                        tv_rateCPU7.setText(df1.format(valueRateCPU7)+"%");
                        tv_freqCPU7.setText(df1.format(valueFreqCPU7)+"MHz");

                        dataValsGPU.add(new Entry(i,valueRateGPU));
                        tv_rateGPU.setText(valueRateGPU+"%");

                        tv_tempCPU.setText(df1.format(valueTempCPU)+"°C");


                        // La Creation de lignes du taux d'utilisation (mémoire,Cpu,Gpu)


                        dataSetCPU = new LineDataSet(dataValsCPU,"CPU rate");
                        dataSetCPU0 = new LineDataSet(dataValsCPU0,"CPU rate0");
                        dataSetCPU1 = new LineDataSet(dataValsCPU1,"CPU rate1");
                        dataSetCPU2 = new LineDataSet(dataValsCPU2,"CPU rate2");
                        dataSetCPU3 = new LineDataSet(dataValsCPU3,"CPU rate3");
                        dataSetCPU4 = new LineDataSet(dataValsCPU4,"CPU rate4");
                        dataSetCPU5 = new LineDataSet(dataValsCPU5,"CPU rate5");
                        dataSetCPU6 = new LineDataSet(dataValsCPU6,"CPU rate6");
                        dataSetCPU7 = new LineDataSet(dataValsCPU7,"CPU rate7");
                        dataSetCPU.notifyDataSetChanged();

                        dataSetGPU = new LineDataSet(dataValsGPU,"GPU rate");
                        dataSetGPU.notifyDataSetChanged();


                        //Création d'une Dataset global contient tous les lignes de taux d'utilisation
                        ArrayList<ILineDataSet> dataSets = new ArrayList<>();

                        dataSets.add(dataSetCPU);
                        dataSets.add(dataSetCPU0);
                        dataSets.add(dataSetCPU1);
                        dataSets.add(dataSetCPU2);
                        dataSets.add(dataSetCPU3);
                        dataSets.add(dataSetCPU4);
                        dataSets.add(dataSetCPU5);
                        dataSets.add(dataSetCPU6);
                        dataSets.add(dataSetCPU7);
                        dataSets.add(dataSetGPU);
                        data = new LineData(dataSets);
                        data.notifyDataChanged();

                        //Afficher les lignes dans le Chart
                        rateChart.setData(data);
                        rateChart.invalidate();

                        YAxis rightAxis = rateChart.getAxisRight();
                        YAxis leftAxis = rateChart.getAxisLeft();
                        XAxis xAxis =  rateChart.getXAxis();

                        // Fix de l'axe gauche y-valeurs à partir 0% jusqu'à 100%
                        leftAxis.setAxisMaximum(100f);
                        leftAxis.setAxisMinimum(0f);
                        //Modifier les valeurs de l 'axe y-valeurs avec l'ajout du caractére "%" à la fin
                        leftAxis.setValueFormatter(new MyAxisValueFormatter());
                        // Désactiver l'axe droit y-valeurs
                        rightAxis.setEnabled(false);

                        // Désactiver la partie Legend et Description
                        legend = rateChart.getLegend();
                        legend.setEnabled(false);
                        rateChart.getDescription().setEnabled(false);


                        data.setValueTextSize(8f);

                        dataSetCPU.setColor(Color.BLUE);
                        dataSetCPU0.setColor(Color.YELLOW);
                        dataSetCPU1.setColor(Color.BLACK);
                        dataSetCPU2.setColor(Color.rgb(187,134,252));
                        dataSetCPU3.setColor(Color.rgb(233,30,99));
                        dataSetCPU4.setColor(Color.rgb(255,87,34));
                        dataSetCPU5.setColor(Color.rgb(103,58,183));
                        dataSetCPU6.setColor(Color.rgb(33,150,243));
                        dataSetCPU7.setColor(Color.rgb(255,152,0));
                        dataSetGPU.setColor(Color.GREEN);


                        dataSetCPU.setLineWidth(1f);
                        dataSetCPU0.setLineWidth(1f);
                        dataSetCPU1.setLineWidth(1f);
                        dataSetCPU2.setLineWidth(1f);
                        dataSetCPU3.setLineWidth(1f);
                        dataSetCPU4.setLineWidth(1f);
                        dataSetCPU5.setLineWidth(1f);
                        dataSetCPU6.setLineWidth(1f);
                        dataSetCPU7.setLineWidth(1f);
                        dataSetGPU.setLineWidth(1f);


                        dataSetCPU.setDrawCircles(false);
                        dataSetCPU0.setDrawCircles(false);
                        dataSetCPU1.setDrawCircles(false);
                        dataSetCPU2.setDrawCircles(false);
                        dataSetCPU3.setDrawCircles(false);
                        dataSetCPU4.setDrawCircles(false);
                        dataSetCPU5.setDrawCircles(false);
                        dataSetCPU6.setDrawCircles(false);
                        dataSetCPU7.setDrawCircles(false);
                        dataSetGPU.setDrawCircles(false);


                        i++;

                    } catch (RemoteException e) {
                    }



                    handler.postDelayed(this, 1000);
                }

            };
            handler.post(runnable);


        }
        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG,"POC déconnecté du service monitorService");
            monitorService1 = null;
        }

    };

    //Fonction de cache les barres SystemUI
    public int hideSystemUI(Activity myActivityReference) {
        View decorView = myActivityReference.getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
        return (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI(this);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUI(this);
    }

    // L'arrêt de Thread et Service
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if( handler != null && runnable != null ){
            handler.removeCallbacksAndMessages(null);
            handler.removeCallbacks(runnable);
        }
        try{
            if (mConnectionMonitorService != null) {
                unbindService(mConnectionMonitorService);
            }
        }
        catch (Exception ignored){}
    }
}