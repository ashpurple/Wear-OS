package com.example.android.wearable.watchface.watchface;

import java.util.ArrayList;

public class CalculationFatigueStress {
    // Fatigue And Stress Calculation -------------------------------------------------
    ArrayList HRF = new ArrayList<Double>();
    ArrayList HRL = new ArrayList<Double>();
    ArrayList RR_interval = new ArrayList<Double>();


    public double slope(final double x1, final double y1, final double x2, final double y2) {
        double m = 0;
        double b = x2 - x1;
        double d = y2 - y1;
        if(b!=0){
            m = d/b;
        }
        return m;
    }

    public void getHRF(){
        HRF.add(78);
        HRF.add(79);
        HRF.add(78);
        HRF.add(80);
        HRF.add(81);
        HRF.add(82);
        HRF.add(82);
        HRF.add(82);
        HRF.add(83);
        HRF.add(84);
    }

    public void getHRL(){
        HRL.add(64);
        HRL.add(63);
        HRL.add(63);
        HRL.add(62);
        HRL.add(62);
        HRL.add(62);
        HRL.add(61);
        HRL.add(61);
        HRL.add(60);
        HRL.add(60);
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

    public int CalculateFatigue() {
        int MinTiredUp = 10;
        int MaxTiredUp = 50;
        int MinTiredDn = 10;
        int MaxTiredDn = 50;
        int Timeline = 300;
        int Tired = 0;
        int LastTired = 0;
        int CurrentTired;

        CurrentTired = (int)slope(0, getMean(HRF), Timeline, getMean(HRL)*100);
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
        // 약간 이상한데?
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
        RR_interval.add((int)1772.89);
        RR_interval.add((int)1770.01);
        RR_interval.add((int)1760.22);
        RR_interval.add((int)1810.88);
        RR_interval.add((int)1690.02);
        RR_interval.add((int)1700.28);
    }

    public void CalculateStress(){
        double MinStress = 25.0;
        double MaxStress = 75.0;

        double Measure = getStdev(RR_interval);
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
