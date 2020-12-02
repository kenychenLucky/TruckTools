package com.wonder.integration.truck.impl;

public interface TruckSessionInterface {

    public String getPickingListPlanningId(String jsonStr);
    public String PickingListPlanning(String id);

    public String createPickingListPlanning(String id);
    public String dealCreateResponse(String jsonStr,int quantity);

    public String dealPickingListJson(String jsonStr,String planid);
    public String dealCreateOutResponse(String jsonStr,String planid,int  quantity);


    public String postPicking(String id, String requestbody);

    public void putpickingListplanning(String id, String planid, String requestbody);

    public void putPickingList(String id, String planId, String jsonStrbody);

    public void loadinventory(String id, String planid, String loadJsonstr);

    public void setServicsCookie(String sessionId);
    public void inventory_management(String id);

    public String getPickingList(String id);
}
