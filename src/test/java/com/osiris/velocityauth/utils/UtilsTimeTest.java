package com.osiris.velocityauth.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTimeTest {

    @Test
    void getFormattedString() {
        System.out.println(new UtilsTime().getFormattedString(10000000000L));
    }
}