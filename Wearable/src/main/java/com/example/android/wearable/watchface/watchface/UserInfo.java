package com.example.android.wearable.watchface.watchface;

public class UserInfo {
    // User
    private String birthday;
    private String name;
    private String skin;
    private String protective;
    private String maxHeartRate;
    private String group;

    // TimerList
    private String tmrSeq;
    private String bizId;
    private String schSeq;
    private String tmrNm;
    private String onOff;
    private String intervalSec;
    private String durationSec;
    private String timerMillis;
    private String loopCount;
    private String tmrGbn;
    private String memo;
    private String startDt;
    private String endDt;

    UserInfo(){
        // User
        this.name = "홍길동";
        this.group = "가천대";
        this.birthday = "None";
        this.skin = "None";
        this.protective = "None";
        this.maxHeartRate = "None";

        // TimerList
        this.setTmrSeq("None");
        this.setBizId("None");
        this.setSchSeq("None");
        this.setTmrNm("None");
        this.setOnOff("None");
        this.setIntervalSec("None");
        this.setDurationSec("None");
        this.setTimerMillis("None");
        this.setLoopCount("None");
        this.setTmrGbn("None");
        this.setMemo("None");
        this.setStartDt("None");
        this.setEndDt("None");
    }

    public void setBirthday(String birthday){
        this.birthday = birthday;
    }
    public void setName(String name){
        this.name = name.substring(5);
    }
    public void setSkin(String skin){
        this.skin = skin;
    }
    public void setProtective(String protective){
        this.protective = protective;
    }
    public void setMaxHeartRate(String maxHeartRate){
        this.maxHeartRate = maxHeartRate;
    }
    public void setGroup(String group){
        this.group = group;
    }

    public String getBirthday(){
        return birthday;
    }
    public String getName(){
        return name;
    }
    public String getSkin(){
        return skin;
    }
    public String getProtective(){
        return protective;
    }
    public String getMaxHeartRate(){
        return maxHeartRate;
    }
    public String getGroup(){
        return group;
    }

    // TimerList
    public void setTmrSeq(String tmrSeq) {
        this.tmrSeq = tmrSeq;
    }
    public void setBizId(String bizId) {
        this.bizId = bizId;
    }
    public void setSchSeq(String schSeq) {
        this.schSeq = schSeq;
    }
    public void setTmrNm(String tmrNm) {
        this.tmrNm = tmrNm;
    }
    public void setOnOff(String onOff) {
        this.onOff = onOff;
    }
    public void setIntervalSec(String intervalSec) {
        this.intervalSec = intervalSec;
    }
    public void setDurationSec(String durationSec) {
        this.durationSec = durationSec;
    }
    public void setTimerMillis(String timerMillis) {
        this.timerMillis = timerMillis;
    }
    public void setLoopCount(String loopCount) {
        this.loopCount = loopCount;
    }
    public void setTmrGbn(String tmrGbn) {
        this.tmrGbn = tmrGbn;
    }
    public void setMemo(String memo) {
        this.memo = memo;
    }
    public void setStartDt(String startDt) {
        this.startDt = startDt;
    }
    public void setEndDt(String endDt) {
        this.endDt = endDt;
    }

    public String getTmrSeq() {
        return tmrSeq;
    }
    public String getBizId() {
        return bizId;
    }
    public String getSchSeq() {
        return schSeq;
    }
    public String getTmrNm() {
        return tmrNm;
    }
    public String getOnOff() {
        return onOff;
    }
    public String getIntervalSec() {
        return intervalSec;
    }
    public String getDurationSec() {
        return durationSec;
    }
    public String getTimerMillis() {
        return timerMillis;
    }
    public String getLoopCount() {
        return loopCount;
    }
    public String getTmrGbn() {
        return tmrGbn;
    }
    public String getMemo() {
        return memo;
    }
    public String getStartDt() {
        return startDt;
    }
    public String getEndDt() {
        return endDt;
    }
}
