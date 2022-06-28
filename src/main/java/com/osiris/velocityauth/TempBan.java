package com.osiris.velocityauth;

import com.osiris.velocityauth.database.FailedLogin;

import java.util.List;

public class TempBan {
    public String username;
    public String ipAddress;
    public long msLeft;
    public List<FailedLogin> failedLogins;

    public TempBan(String username, String ipAddress, long msLeft, List<FailedLogin> failedLogins) {
        this.username = username;
        this.ipAddress = ipAddress;
        this.msLeft = msLeft;
        this.failedLogins = failedLogins;
    }
}
