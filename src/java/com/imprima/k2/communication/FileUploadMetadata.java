/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.k2.communication;

/**
 *
 * @author henrik
 */
public class FileUploadMetadata {

    private Integer ordernumber = null;
    private String originalFilename = null;
    private String email = null;
    private String fullname = null;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public Integer getOrdernumber() {
        return ordernumber;
    }

    public void setOrdernumber(Integer ordernumber) {
        this.ordernumber = ordernumber;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }
    
}
