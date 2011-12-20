/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.util;

/**
 *
 * @author henrik
 */
public class StringUtility {

    public static String repeat(String str, int num) {

        int len = num * str.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < num; i++) {
            sb.append(str);
        }
        return sb.toString();

    }
}
