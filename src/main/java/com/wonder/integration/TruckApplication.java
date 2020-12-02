package com.wonder.integration;

import com.wonder.integration.truck.impl.TruckSessionServics;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TruckApplication {

	public static void main(String[] args) {
		SpringApplication.run(TruckApplication.class, args);

		String sessonId = "d16007e2-6483-4e7b-85e6-0da0a53d8e56";
		int start = 0, end = 0;
//        if (start != 0 || end != 0) {
//
//            TruckSessionServics t = new TruckSessionServics();
//            t.setServicsCookie(sessonId);
//            t.BatchPickingInventory(start, end);
//        } else {
//        }


		BatchPickingInventory(sessonId,2292,2300);

		//online
//        OnlineTruckSession onlineTruckSession = new OnlineTruckSession();
//        onlineTruckSession.setTruckSession(sessonId);
//        onlineTruckSession.TruckSessionOnline(2241, 2290);

	}

	public static void BatchPickingInventory(String sessonId, int start, int end) {
		int truckSessionStart = start;
		int truckSessionend = end;
		TruckSessionServics t = new TruckSessionServics();
		t.setServicsCookie(sessonId);

		t.BatchPickingInventory(truckSessionStart, truckSessionend);
	}


}
