/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.k2.datastore;

import java.sql.Timestamp;

/**
 *
 * @author henrik
 */
public final class ProductionStep implements Comparable<ProductionStep> {

    public static long WORKDAYSTARTCORRECTION = 18000000l;
    public static long WORKDDAYLENGTH = 43200000l;
    private Integer state;
    private String details;
    private String queuename;
    private Timestamp starttime = new Timestamp(0);
    private Timestamp stoptime = new Timestamp(0);
    private Timestamp laststarted = new Timestamp(0);
    private String subcontractor = "";
    private Integer timespan = 0;
    private Integer ordering = 0;
    private String imposition = "";
    private String paperinfo = "";
    private String printpart = "";
    private Integer progress = 0;
    private Integer queueid = 0;
    private Integer dbId = 0;

    public ProductionStep() {
    }

    public ProductionStep(Integer state, String details, String queuename, Integer queueid) {

        setState(state);
        setDetails(details);
        setQueuename(queuename);
        setQueueid(queueid);

    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {

        if (details != null) {
            this.details = details.trim();
        }

    }

    public Timestamp getLaststarted() {
        return laststarted;
    }

    public void setLaststarted(Timestamp laststarted) {

        if (laststarted != null) {
            this.laststarted = laststarted;
        }

    }

    public Timestamp getStarttime() {
        return starttime;
    }

    public void setStarttime(Timestamp starttime) {

        if (starttime != null) {
            this.starttime = applyWorkDayCorrection(starttime);
        }

    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {

        if (state != null) {
            this.state = state;
        }

    }

    public Timestamp getStoptime() {
        return stoptime;
    }

    public void setStoptime(Timestamp stoptime) {

        if (stoptime != null) {
            this.stoptime = stoptime;
        }

    }

    public String getSubcontractor() {
        return subcontractor;
    }

    public void setSubcontractor(String subcontractor) {

        if (subcontractor != null) {
            this.subcontractor = subcontractor.trim();
        }

    }

    public Integer getTimespan() {
        return timespan;
    }

    public void setTimespan(Integer timespan) {

        if (timespan != null) {
            this.timespan = timespan;
        }

    }

    public Integer getOrdering() {
        return ordering;
    }

    public void setOrdering(Integer ordering) {

        if (ordering != null) {
            this.ordering = ordering;
        }

    }

    public String getQueuename() {
        return queuename;
    }

    public void setQueuename(String queuename) {

        if (queuename != null) {
            this.queuename = queuename.trim();
        }

    }

    public Integer getQueueid() {
        return queueid;
    }

    public void setQueueid(Integer queueid) {

        if (queueid != null) {
            this.queueid = queueid;
        }

    }

    public String getImposition() {
        return imposition;
    }

    public void setImposition(String imposition) {

        if (imposition != null) {
            this.imposition = imposition.trim();
        }

    }

    public String getPaperinfo() {
        return paperinfo;
    }

    public void setPaperinfo(String paperinfo) {

        if (paperinfo != null) {
            this.paperinfo = paperinfo.trim();
        }

    }

    public String getPrintpart() {
        return printpart;
    }

    public void setPrintpart(String printpart) {

        if (printpart != null) {
            this.printpart = printpart.trim();
        }

    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {

        this.progress = progress <= 100 ? progress : 100;
        this.progress = this.progress >= 0 ? progress : 0;

    }

    public Integer getDbId() {
        return dbId;
    }

    public void setDbId(Integer dbId) {

        if (dbId != null) {
            this.dbId = dbId;
        }

    }

    private Timestamp applyWorkDayCorrection(Timestamp timestamp) {

        if (timestamp.toString().split("\\s")[1].startsWith("00:00:00")) {

            return new Timestamp(timestamp.getTime() + WORKDAYSTARTCORRECTION);

        } else {

            return timestamp;

        }

    }

    @Override
    public int compareTo(ProductionStep productionStep) {

        int comparedOrdering = productionStep.getOrdering();

        if (this.getOrdering() < comparedOrdering) {
            return -1;
        } else {
            return 1;
        }

    }
}
