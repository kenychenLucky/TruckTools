package com.wonder.integration.truck.impl;

//import app.erpintegration.api.trucksession.picking.BOSearchTruckSessionPickingRequest;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wonder.integration.utils.OnlineTruckSession;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

import static java.lang.Thread.sleep;

public class TruckSessionServics implements TruckSessionInterface {
    private static Logger logger = LoggerFactory.getLogger(TruckSessionServics.class);

    public String COOKIE = "AuthSessionId=d16007e2-6483-4e7b-85e6-0da0a53d8e56";

    public void setServicsCookie(String sessionId){
        COOKIE=String.format("AuthSessionId=%s",sessionId);
    }


    public String getPickingListPlanningId(String jsonStr) {
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        JSONArray array = jsonObject.getJSONArray("picking_list_plannings");
        int len= array.size();
        JSONObject jsonObject2 = JSONObject.parseObject(array.get(len-1).toString());
        String id = jsonObject2.get("id").toString();
        logger.info("getPickingListPlanningId.id=" + id);

        return id;
    }

    public String dealCreateResponse(String jsonStr,int quantity ) {
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        String createPlanId = jsonObject.getString("name"); //939_1
        JSONArray array = jsonObject.getJSONArray("restaurants");


        JSONObject jsonObject2 = JSONObject.parseObject(array.get(0).toString());
        String id = jsonObject2.getString("id");
        String restname = jsonObject2.getString("name");

        logger.info("restaurants id  ="+id +" restaurants name="+restname);

        JSONArray taskContentArr = jsonObject2.getJSONArray("menu_items");
        JSONArray newmenu = new JSONArray();


        for (int i = 0; i < taskContentArr.size(); i++) {
            JSONObject insertObj = taskContentArr.getJSONObject(i);

            insertObj.put("quantity", quantity);
            newmenu.add(insertObj);

        }
        JSONObject postJsonOject = new JSONObject();

        postJsonOject.put("name", createPlanId);
        postJsonOject.put("type", "INITIAL_LOADING"); //:"",
        postJsonOject.put("from_roar_truck_session", null);
        postJsonOject.put("upload_items", null);
        JSONObject postJsonOject2 = new JSONObject();
        postJsonOject2.put("id", id);
        postJsonOject2.put("name", restname);
        postJsonOject2.put("menu_items", newmenu);

        JSONArray array3 = new JSONArray();
        array3.add(postJsonOject2);
        postJsonOject.put("restaurants", array3);
        return postJsonOject.toString();

    }

    public String dealPickingListJson(String jsonStr,String planid){
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        String pickingListid="";

        JSONArray array = jsonObject.getJSONArray("picking_lists");

        int len=array.size();

        for (int i =0;i<len ;i++){
            JSONObject temp = JSONObject.parseObject(array.get(i).toString());
            if (temp.get("picking_list_planning_id").toString().equals(planid)){
                pickingListid=temp.get("id").toString();
                //break;
            }
        }
        return pickingListid;

    }

    public String dealCreateOutResponse(String jsonStr,String planid,int quantity) {
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);

        JSONArray array = jsonObject.getJSONArray("picking_list_plannings");

        int len=array.size();
        JSONObject jsonObject2=null;

        for (int i =0;i<len ;i++){
            JSONObject temp = JSONObject.parseObject(array.get(i).toString());
            if (temp.get("id").toString().equals(planid)){
                 jsonObject2 =temp;
                 //break;
            }
        }

        JSONArray restaurantsArray= jsonObject2.getJSONArray("restaurants");

        JSONObject restJsonObject=JSONObject.parseObject(restaurantsArray.get(0).toString());

        JSONArray taskContentArr =restJsonObject.getJSONArray("menu_items");
        JSONArray newmenu = new JSONArray();

        //List list = new ArrayList();
        String[] strs = new String[]{"floz", "qt", "pt", "g", "oz",
                "l", "tsp", "gal", "lb", "mg", "ea", "kg", "tbsp", "ml", "cup"};

        JSONArray units = new JSONArray(Arrays.asList(strs));

        for (int i = 0; i < taskContentArr.size(); i++) {
            JSONObject insertObj = taskContentArr.getJSONObject(i);
            insertObj.remove("option_value_id");
            insertObj.remove("option_value_name");

            insertObj.put("quantity", quantity);
            insertObj.put("calculated_quantity", quantity);
            insertObj.put("selected_unit", "ea");
            insertObj.put("description", "OUTBOUND_COOLER");
            insertObj.put("warehouse", "NJ0002");
            insertObj.put("wms_location_id", "OUT_01");
            insertObj.put("erp_description", null);
            insertObj.put("erp_wms_location_id", null);
            insertObj.put("inventory_uom", "ea");
            insertObj.put("units", units);
            String tmp = insertObj.get("item_number").toString();
            //logger.info(tmp);

            int newtmp = newmenu.toJSONString().indexOf(tmp);
            if (newtmp > 0) {
                continue;
            }
            newmenu.add(insertObj);
        }
        JSONObject postJsonOject = new JSONObject();
        postJsonOject.put("status", "UPDATED"); //:"",
        postJsonOject.put("from_roar_truck_session", null);
        postJsonOject.put("validate_inventory", false);
        postJsonOject.put("items", newmenu); //:"",
        return postJsonOject.toJSONString();
    }

    public String PickingListPlanning(String id){
        String web_base = "https://fmt.foodtruck-staging.com";
        String web_url = web_base + "/ajax/truck-session/" + id + "/picking-list-planning";
        String referer_url = web_base + "/truck-session/picking/" + id ;
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(web_url)
                .method("GET", null)
                .addHeader("authority", "fmt.foodtruck-staging.com")
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36")
                .addHeader("sec-fetch-site", "same-origin")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("referer", referer_url)
                .addHeader("accept-language", "en-US,en;q=0.9")
                .addHeader("cookie", COOKIE)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String strjson = response.body().string();
            logger.info("PickingListPlanning= "+strjson);
            return strjson;


        } catch (IOException e) {
            e.printStackTrace();
        }
        return "ok";
    }

    public String createPickingListPlanning(String id) {
        String web_base = "https://fmt.foodtruck-staging.com";
        String web_url = web_base + "/ajax/truck-session/" + id + "/create-picking-list-planning";
        String referer_url = web_base + "/truck-session/picking/" + id + "?planningId=-1";

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(web_url)
                .method("GET", null)
                .addHeader("authority", "fmt.foodtruck-staging.com")
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36")
                .addHeader("sec-fetch-site", "same-origin")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("referer", referer_url)
                .addHeader("accept-language", "en-US,en;q=0.9")
                .addHeader("cookie", COOKIE)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String strjson = response.body().string();
            logger.info(strjson);
            return strjson;


        } catch (IOException e) {
            e.printStackTrace();
        }
        return "ok";

    }

    public String postPicking(String id, String requestbody) {
        String web_base = "https://fmt.foodtruck-staging.com";
        String web_url = web_base + "/ajax/truck-session/" + id + "/picking-list-planning";
        String referer_url = web_base + "/truck-session/picking/" + id + "?planningId=-1";

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");
        RequestBody body = RequestBody.create(mediaType, requestbody);
        Request request = new Request.Builder()
                .url(web_url)
                .method("POST", body)
                .addHeader("authority", "fmt.foodtruck-staging.com")
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36")
                .addHeader("content-type", "application/json;charset=UTF-8")
                .addHeader("origin", "https://fmt.foodtruck-staging.com")
                .addHeader("sec-fetch-site", "same-origin")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("referer", referer_url)
                .addHeader("accept-language", "en-US,en;q=0.9")
                .addHeader("cookie", COOKIE)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String jsonStr = response.body().string();
            //logger.info("postPicking jsonStr=" + jsonStr);
            JSONObject jsonObject2 = JSONObject.parseObject(jsonStr);

            String newplanid = jsonObject2.get("id").toString();
            logger.info("postPicking newplanid=" + newplanid);
            return newplanid;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "OK";
    }

    public void putpickingListplanning(String id, String planid, String requestbody) {
        String web_base = "https://fmt.foodtruck-staging.com";
        String web_url = web_base + "/ajax/truck-session/" + id + "/picking-list-planning/" + planid;
        String referer_url = web_base + "/truck-session/picking/" + id;

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");
        RequestBody body = RequestBody.create(mediaType, requestbody);
        Request request = new Request.Builder()
                .url(web_url)
                .method("PUT", body)
                .addHeader("authority", "fmt.foodtruck-staging.com")
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36")
                .addHeader("content-type", "application/json;charset=UTF-8")
                .addHeader("origin", "https://fmt.foodtruck-staging.com")
                .addHeader("sec-fetch-site", "same-origin")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("referer", referer_url)
                .addHeader("accept-language", "en-US,en;q=0.9")
                .addHeader("cookie", COOKIE)
                .build();
        try {
            Response response = client.newCall(request).execute();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void putPickingList(String id, String planId, String jsonStrbody) {
        String web_base = "https://fmt.foodtruck-staging.com";
        //"https://fmt.foodtruck-staging.com/ajax/truck-session/1130/picking-list/6b613fc5-111c-4184-9b1b-96f054208611"
        //loadJsonstr
        String web_url = web_base + "/ajax/truck-session/" + id + "/picking-list/" + planId;
        String referer_url = web_base + "/truck-session/picking/" + id;
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/json;charset=UTF-8");
        RequestBody body = RequestBody.create(mediaType, jsonStrbody);
        Request request = new Request.Builder()
                .url(web_url)
                .method("PUT", body)
                //.addHeader("authority", "fmt.foodtruck-staging.com")
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36")
                .addHeader("content-type", "application/json;charset=UTF-8")
                //  .addHeader("origin", "https://fmt.foodtruck-staging.com")
                //  .addHeader("sec-fetch-site", "same-origin")
                //  .addHeader("sec-fetch-mode", "cors")
                //  .addHeader("sec-fetch-dest", "empty")
                //  .addHeader("referer", referer_url)
                .addHeader("accept-language", "en-US,en;q=0.9")
                .addHeader("cookie", COOKIE)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String jsonStr = response.body().string();
            logger.info(jsonStr);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void loadinventory(String id, String planid, String loadJsonstr) {
        String web_base = "https://fmt.foodtruck-staging.com";
        String web_url = web_base + "/ajax/truck-session/" + id + "/picking-list/" + planid + "/load-inventory";
        String referer_url = web_base + "/truck-session/picking/" + id;

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
        RequestBody body = RequestBody.create(mediaType, loadJsonstr);
        Request request = new Request.Builder()
                .url(web_url)
                .method("POST", body)
                .addHeader("authority", "fmt.foodtruck-staging.com")
                .addHeader("content-length", "0")
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36")
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .addHeader("origin", "https://fmt.foodtruck-staging.com")
                .addHeader("sec-fetch-site", "same-origin")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("referer", referer_url)
                .addHeader("accept-language", "en-US,en;q=0.9")
                .addHeader("cookie", COOKIE)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String jsonStr = response.body().string();
            //JSONObject jsonObject = JSONObject.parseObject(jsonStr);
            //String retid=jsonObject.getString("id"); //939_1
            logger.info(jsonStr);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    public void inventory_management(String id) {
        String web_base = "https://fmt.foodtruck-staging.com";
        String web_url = web_base + "/ajax/truck-session/" + id + "/inventory-management";
        String referer_url = web_base + "/truck-session/picking/" + id;

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(web_url)
                .method("GET", null)
                .addHeader("authority", "fmt.foodtruck-staging.com")
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36")
                .addHeader("sec-fetch-site", "same-origin")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("referer", referer_url)
                .addHeader("accept-language", "en-US,en;q=0.9")
                .addHeader("cookie", COOKIE)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String jsonStr = response.body().string();
            logger.info("inventory_management=" + jsonStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public  String getPickingListPlanning(String id) {
        String web_base = "https://fmt.foodtruck-staging.com";
        String web_url = web_base + "/ajax/truck-session/" + id + "/picking-list-planning";
        String referer_url = web_base + "/truck-session/picking/" + id;
        String pickingListplanningId = "";

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(web_url)
                .method("GET", null)
                .addHeader("authority", "fmt.foodtruck-staging.com")
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36")
                .addHeader("sec-fetch-site", "same-origin")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("referer", referer_url)
                .addHeader("accept-language", "en-US,en;q=0.9")
                .addHeader("cookie", COOKIE)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String jsonStr = response.body().string();
            //logger.info("pickingListPlanning=" + jsonStr);
            pickingListplanningId = getPickingListPlanningId(jsonStr);
            return pickingListplanningId;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "OK";

    }

//    public void search(String id) {
//        //BOSearchTruckSessionPickingResponse search(BOSearchTruckSessionPickingRequest var1);
//        try {
//            BOSearchTruckSessionPickingRequest request = new BOSearchTruckSessionPickingRequest();
//            request.truckSessionId = id;
//            request.status = 1;
//            request.limit = 10;
//            request.skip = 1;
//
//            SERVICES_LIBRARY.bOTruckSessionPickingWebService.search(request);
//        } catch (core.framework.web.service.RemoteServiceException e) {
//            e.printStackTrace();
//        }
//    }

    public String getPickingList(String id) {
        String web_base = "https://fmt.foodtruck-staging.com";
        String web_url = web_base + "/ajax/truck-session/" + id + "/picking-list";
        String referer_url = web_base + "/truck-session/picking/" + id;
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(web_url)
                .method("GET", null)
                .addHeader("authority", "fmt.foodtruck-staging.com")
                .addHeader("accept", "application/json, text/plain, */*")
                .addHeader("user-agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36")
                .addHeader("sec-fetch-site", "same-origin")
                .addHeader("sec-fetch-mode", "cors")
                .addHeader("sec-fetch-dest", "empty")
                .addHeader("referer", referer_url)
                .addHeader("accept-language", "en-US,en;q=0.9")
                .addHeader("cookie", COOKIE)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String jsonStr = response.body().string();
            return jsonStr;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "OK";
    }


    public static void EachPickingInventory(String truckSessionId) {
        try {
            TruckSessionServics servics = new TruckSessionServics();
            int quantity=999;
            String retstr = servics.createPickingListPlanning(truckSessionId);

            String jsonstr = servics.dealCreateResponse(retstr,quantity);

            String planid = servics.postPicking(truckSessionId, jsonstr);

            //put
            servics.putpickingListplanning(truckSessionId, planid, jsonstr);
            sleep(100);

            String newnewJsonstr=servics.PickingListPlanning(truckSessionId);


            String loadJsonstr = servics.dealCreateOutResponse(newnewJsonstr,planid,quantity);
            logger.info(loadJsonstr);
            //save
            String pickingListjsonStr=servics.getPickingList(truckSessionId);
            String pickingListId=servics.dealPickingListJson(pickingListjsonStr,planid);

            servics.putpickingListplanning(truckSessionId, pickingListId, loadJsonstr);
            sleep(100);

            servics.loadinventory(truckSessionId,pickingListId,loadJsonstr);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void BatchPickingInventory(int truckSessionIdStart,int truckSessionIdEnd) {
        try {
            if (truckSessionIdEnd<truckSessionIdStart) {logger.info("Please input truckSessionIdEnd > truckSessionIdStart number! "); return;}

            for(int i=truckSessionIdStart;i<=truckSessionIdEnd; i++) {

                EachPickingInventory(String.valueOf(i));

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
