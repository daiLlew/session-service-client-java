package com.session.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.session.service.client.SessionClient;
import com.session.service.client.SessionClientImpl;
import com.session.service.entities.SessionCreated;

public class Main {

    public static void main(String[] args) {
        SessionClient sessionClient = new SessionClientImpl("http://localhost:8080", "");
        Gson g = new GsonBuilder().setPrettyPrinting().create();

        try {
            System.out.println("create session");
            SessionCreated sessionCreated = sessionClient.createNewSession("test@test.com");
            System.out.println(g.toJson(sessionCreated));

            System.out.println("Get session by ID");
            Session session = sessionClient.getSessionByID(sessionCreated.getId());
            System.out.println(g.toJson(session));

            System.out.println("Get session by email");
            Session session1 = sessionClient.getSessionByEmail("test@test.com");
            System.out.println(g.toJson(session1));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
