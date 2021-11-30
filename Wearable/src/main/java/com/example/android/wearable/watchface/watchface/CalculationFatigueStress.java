package com.example.android.wearable.watchface.watchface;

import java.util.ArrayList;
import java.util.Collections;

public class CalculationFatigueStress {

    private final ArrayList<SensorValueInfo> input = null;
    private final ArrayList<Integer> hrf;
    private final ArrayList<Integer> hrl;
    private ArrayList rr_interval = new ArrayList<Double>();

    CalculationFatigueStress(ArrayList<SensorValueInfo> Input){
        ArrayList<Integer> heart_list = new ArrayList<Integer>();
        for(SensorValueInfo sensorValueInfo: Input){
            int heartValue = sensorValueInfo.getValue();
            heart_list.add(heartValue);
        }
        hrf = new ArrayList<Integer>();
        Collections.sort(heart_list, Collections.<Integer>reverseOrder());
        for(int i = 0; i < 10; i++){
            hrf.add(heart_list.get(i));
        }
        hrl = new ArrayList<Integer>();
        Collections.sort(heart_list);
        for(int i = 0; i < 10; i++){
            hrl.add(heart_list.get(i));
        }
    }

    // Fatigue And Stress Calculation -------------------------------------------------
    public double slope(final double x1, final double y1, final double x2, final double y2) {
        double m = 0;
        double b = x2 - x1;
        double d = y2 - y1;
        if(b!=0){
            m = d/b;
        }
        return m;
    }


    public double getMean(ArrayList list){
        double avg;
        int sum = 0;
        for(int i = 0; i < list.size(); i++){
            sum += (int)list.get(i);
        }
        avg = sum/list.size();
        return avg;
    }

    public double getStdev(ArrayList list){
        double avg;
        int sum = 0;
        for(int i = 0; i < list.size(); i++){
            sum += (int)list.get(i);
        }
        avg = sum/list.size();
        sum = 0;
        for(int i =0; i < list.size(); i++) {
            sum += Math.pow((int)list.get(i) - avg, 2);
        }
        double var = (double)sum / list.size();
        double Stdev = Math.sqrt(var);
        return Stdev;
    }

    public int calculateFatigue() {
        int MinTiredUp = 10;
        int MaxTiredUp = 50;
        int MinTiredDn = 10;
        int MaxTiredDn = 50;
        int Timeline = 300;
        int Tired = 0;
        int LastTired = 0;
        int CurrentTired;


        System.out.println("HRF:"+hrf);
        System.out.println("HRL:"+hrl);


        CurrentTired = (int)slope(0, getMean(hrf), Timeline, getMean(hrl)*100);
        if(CurrentTired > 0){
            if(CurrentTired > MaxTiredUp){
                MaxTiredUp = (int)CurrentTired;
            }
            if(CurrentTired < MinTiredUp){
                MinTiredUp = (int)CurrentTired;
            }
        }

        if(CurrentTired < 0){
            if(CurrentTired > MaxTiredDn){
                MaxTiredDn = (int)CurrentTired;
            }
            if(CurrentTired < MinTiredDn){
                MinTiredDn = (int)CurrentTired;
            }
        }

        if(CurrentTired > 0){
            Tired = (int)((CurrentTired/(MaxTiredUp - MinTiredUp)) * 100);
            if (Tired > LastTired) {
                System.out.println("현재 피로도는 " + Tired + "이며, 휴식이 필요합니다.");
            }
            else {
                System.out.println("현재 피로도는 " + Tired + "이며, 회복중입니다.");
            }
        }

        if(CurrentTired < 0){
            Tired = (int)((CurrentTired/(MaxTiredDn - MinTiredDn)) * 100);
            if (Tired < LastTired) {
                System.out.println("현재 피로도는 " + Tired + "이며, 휴식이 필요합니다.");
            }
            else {
                System.out.println("현재 피로도는 " + Tired + "이며, 회복중입니다.");
            }
        }

        if(CurrentTired == 0){
            System.out.println("현재 피로도는 " + Tired + "이며, 안정된 상태입니다.");
        }

        LastTired = Tired;
        return LastTired;
    }

    public void getRRInterval(){
        rr_interval.add((int)1772.89);
        rr_interval.add((int)1770.01);
        rr_interval.add((int)1760.22);
        rr_interval.add((int)1810.88);
        rr_interval.add((int)1690.02);
        rr_interval.add((int)1700.28);
    }

    public void CalculateStress(){
        double MinStress = 25.0;
        double MaxStress = 75.0;

        double Measure = getStdev(rr_interval);
        if(MinStress > Measure){
            MinStress = Measure;
        }
        if(MaxStress < Measure){
            MaxStress = Measure;
        }
        double CurrentStress = ((Measure-MinStress)/(MaxStress - MinStress))*100;
        System.out.println("측정 RR-Interval 표준편차 : " + Measure);
        System.out.println("개인 최대 스트레스(%) : " + MaxStress + ", 개인 최소 스트레스(%) : " + MinStress);
        System.out.println("현재 스트레스 : " + (int)CurrentStress + "%");
    }


    // ----------------------------------------------------------------------------
}
