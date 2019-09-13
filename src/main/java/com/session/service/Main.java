package com.session.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.session.service.entities.SessionCreated;

public class Main {

    public static void main(String[] args) {
        Client client = new Client("http://localhost:8080", "");
        Gson g = new GsonBuilder().setPrettyPrinting().create();

        try {
            SessionCreated sessionCreated = client.createNewSession("test@test.com");
            System.out.println(g.toJson(sessionCreated));


            Session session = client.getSessionByID(sessionCreated.getId());
            System.out.println(g.toJson(session));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
