package com.github.onsdigital.session.service.client;

import java.util.Date;

public interface Session {

    String getId();

    String getEmail();

    Date getStart();

    Date getLastAccess();

    void setId(String id);

    void setEmail(String email);

    void setStart(Date start);

    void setLastAccess(Date lastAccess);
}
