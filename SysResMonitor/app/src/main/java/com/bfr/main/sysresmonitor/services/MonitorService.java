package com.bfr.main.sysresmonitor.services;

import android.app.Service;
import android.content.Intent;
import android.os.HardwarePropertiesManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import com.bfr.main.sysresmonitor.IMonitorService;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class MonitorService extends Service {

    private final String TAG = "SysResMonitor";
    private final IMonitorService.Stub remoteBinder = new IMonitorService.Stub(){

        /**
         * Cette méthode permet de récupérer des informations concernant la mémoire à partir le fichier/proc/meminfo
         * @return (stat=0 -> mémoire total), (stat=1 -> mémoire disponible), (stat=2 -> mémoire utilisé)
         * @throws RemoteException
         */
        @Override
        public float memoryInfo(int stat) throws RemoteException {
            BufferedReader reader1;
            float totalMemory, disponibleMemory , utilizedMemory, stat_Memory = 0;
            String Content = ""; // on stocke le contenu de fichier /proc/meminfo dans "Content" ligne par ligne
            String out = ""; // out est un string contient seulement les valeurs numériques du fichier /proc/meminfo

            try {
                // Lire le fichier /proc/meminfo
                reader1 = new BufferedReader(new FileReader("/proc/meminfo"));
                Content =reader1.readLine()+ "\n";

                while ( reader1.ready()) {
                    //Sauvegarder le contnu du fichier ligne par ligne
                    Content =Content + reader1.readLine() + "\n";
                }
                reader1.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Utilisation d'un scanner pour afficher juste les nombres numériques
            Scanner scan = new Scanner(Content);
            scan.useDelimiter("[^0-9]+");
            while(scan.hasNext()){
                out = out + scan.next() + "\n";
            }
            scan.close();

            String[] parts = out.split("\n");

            //Obtenir les valeurs (Mémoire total, Mémoire Libre)
            totalMemory= Float.parseFloat(parts[0]);
            disponibleMemory = Float.parseFloat(parts[2]);

            //Calculer la mémoire utilisé
            utilizedMemory = totalMemory - disponibleMemory;

            if (stat == 0){stat_Memory=totalMemory;}
            if (stat == 1){stat_Memory=disponibleMemory;}
            if (stat == 2){stat_Memory=utilizedMemory;}
            return stat_Memory;
        }

        /** Cette méthode permet de lire un fichier de système
         *
         * @param nameFile représente le chemin de fichier dans le système
         * @return Contenu du fichier
         */
        public String ReadFile(String nameFile){
            BufferedReader readerCpu;
            String Content_File = ""; //on stocke le contenu de fichier  ligne par ligne

            try {
                // Lire le fichier
                readerCpu = new BufferedReader(new FileReader(nameFile));

                Content_File =readerCpu.readLine()+ "\n";
                while ( readerCpu.ready()) {
                    //Sanvgarder le contnu du fichier ligne par ligne
                    Content_File = Content_File + readerCpu.readLine() + "\n";
                }
                readerCpu.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Content_File;
        }

        /**
         * Cette méthode permet de récupérer le taux d'utilisation Moyen CPU à partir le fichier/proc/stat
         * @return Array contient des taux d'utilisation [CPU global, cpuO, ..., cpu7]
         * @throws RemoteException
         */
        @Override
        public float[] cpuRate() throws RemoteException {
            float[] rateCpu = new float[9]; //
            String Content1 = ReadFile("/proc/stat");//La première lecture de fichier /proc/stat
            try {
                TimeUnit.MILLISECONDS.sleep(1000); // Sleep in 1 second
            } catch (InterruptedException e) {
            }
            String Content2 = ReadFile("/proc/stat");//La deuxième lecture de fichier /proc/stat

            String[] split_Content1 = Content1.split("\n");
            String[] split_Content2 = Content2.split("\n");



            float[] cpu_last_sum_cpu = new float[9]; //Array contient la somme des valeurs de type de taches pour chaque ligne de la première lecture
            float[] cpu_current_sum_cpu = new float[9]; //Array contient la somme des valeurs de type de taches pour chaque ligne de la deuxième lecture
            float[] cpu_delta = new float[9];// Array contient les temps total instantanés pour chaque ligne
            float[] cpu_idle  = new float[9];// Array contient les temps inactif instantanés pour chaque ligne
            float[] cpu_used = new float[9];// Array contient les temps utilisés instantanés pour chaque ligne

            for (int i = 0; i <9 ; i++) {
                String[] ligne_Content1 = split_Content1[i].split("\\s+");
                for (int j = 1; j <10 ; j++) {
                    cpu_last_sum_cpu[i] = cpu_last_sum_cpu[i] + Float.parseFloat(ligne_Content1[j]);
                }
            }

            for (int i = 0; i <9 ; i++) {
                String[] ligne_Content2 = split_Content2[i].split("\\s+");
                for (int j = 1; j <10 ; j++) {
                    cpu_current_sum_cpu[i] = cpu_current_sum_cpu[i] + Float.parseFloat(ligne_Content2[j]);
                }
            }

            for (int i = 0; i <9 ; i++) {
                cpu_delta[i] = cpu_current_sum_cpu[i] - cpu_last_sum_cpu[i];
            }

            for (int i = 0; i <9 ; i++) {
                String[] ligne_Content1 = split_Content1[i].split("\\s+");
                String[] ligne_Content2 = split_Content2[i].split("\\s+");
                cpu_idle[i] = Float.parseFloat(ligne_Content2[4]) - Float.parseFloat(ligne_Content1[4]);
            }

            for (int i = 0; i <9 ; i++) {
                cpu_used[i] = cpu_delta[i] - cpu_idle[i];
            }


            //Calcul taux d'utilisation
            for (int i = 0; i <9 ; i++) {
                rateCpu[i] = 100 * (cpu_used[i] / cpu_delta[i]);

            }

            Log.i(TAG, "cpuRate global: "+rateCpu[0]+"%");
            Log.i(TAG, "cpuRate[0]: "+rateCpu[1]+"%");
            Log.i(TAG, "cpuRate[1]: "+rateCpu[2]+"%");
            Log.i(TAG, "cpuRate[2]: "+rateCpu[3]+"%");
            Log.i(TAG, "cpuRate[3]: "+rateCpu[4]+"%");
            Log.i(TAG, "cpuRate[4]: "+rateCpu[5]+"%");
            Log.i(TAG, "cpuRate[5]: "+rateCpu[6]+"%");
            Log.i(TAG, "cpuRate[6]: "+rateCpu[7]+"%");
            Log.i(TAG, "cpuRate[7]: "+rateCpu[8]+"%");
            return rateCpu;
        }


        /**
         * Cette méthode permet de récupérer le taux d'utilisation  GPU à partir /sys/class/kgsl/kgsl-3d0/gpu_busy_percentage
         * @return Taux d'utilisation total GPU
         * @throws RemoteException
         */
        @Override
        public float gpuRate() throws RemoteException {
            BufferedReader gpuFreqAll;
            String gFreq = ""; //on stocke le contenu de fichier /proc/meminfo dans "gFreq" ligne par ligne
            float gpurate;

            try {
                // Lire le fichier /sys/class/kgsl/kgsl-3d0/gpu_busy_percentage
                gpuFreqAll = new BufferedReader(new FileReader("/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage"));
                gFreq = gFreq + gpuFreqAll.readLine();
                gpuFreqAll.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            String[] C = gFreq.split("\\s+");
            gpurate= Float.parseFloat(C[0]);
            Log.i(TAG, "gpuRate : "+gpurate+"%");

            return gpurate;
        }


        /** Cette méthode permet de récupérer les fréquences  des coeurs CPU (CPU0, CPU1, ..., CPU7)
         *
         * @param id représente le coeur de CPU -> (CPU0, CPU1, ..., CPU7)
         * @return Fréquence  (CPU0, CPU1, ..., CPU7)
         * @throws RemoteException
         */
        @Override
        public float cpuFreq(int id) throws RemoteException {
            BufferedReader reader;
            float freqCPU;
            String Content = ""; // on stocke le contenu de fichier  dans "Content" ligne par ligne


            try {
                // Lire le fichier
                reader = new BufferedReader(new FileReader("/sys/devices/system/cpu/cpu" + id + "/cpufreq/scaling_cur_freq"));
                Content =reader.readLine();
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            freqCPU = Float.parseFloat(Content);
            Log.i(TAG, "cpuFreq("+id+"): "+Content+"KHz");
            return freqCPU;
        }

        /** Cette méthode permet de récupérer la température du CPU
         *
         * @return Température du CPU
         * @throws RemoteException
         */
        @Override
        public float cpuTemp() throws RemoteException {

            HardwarePropertiesManager hardwarePropertiesManager = getApplicationContext().getSystemService(HardwarePropertiesManager.class);

            //Array contient des valeurs qui correspondent aux températures des cœurs de CPU.
            float[] temp_CPUs_values = hardwarePropertiesManager.getDeviceTemperatures(HardwarePropertiesManager.DEVICE_TEMPERATURE_CPU,HardwarePropertiesManager.TEMPERATURE_CURRENT);


            //Calcul de la moyenne de ces valeurs pour avoir la température du CPU.
            float cpuTempSum = 0;
            for(int i =0; i<temp_CPUs_values.length;i++){
                cpuTempSum = cpuTempSum+temp_CPUs_values[i];
            }
            float cpuTempMoy= cpuTempSum/8;

            Log.i(TAG, "cpuTemp: "+cpuTempMoy+"°C");
            return cpuTempMoy;
        }

    };

    @Override
    public IBinder onBind(Intent intent) {
        return remoteBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

}
