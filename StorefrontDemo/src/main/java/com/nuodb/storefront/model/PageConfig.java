package com.nuodb.storefront.model;

import java.io.IOException;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;

public class PageConfig {
    private String pageName;
    private Object pageData;
    private Customer customer;
    private List<Message> messages;
    
    public PageConfig() {
    }
    
    public PageConfig(String pageName, Object pageData, Customer customer, List<Message> messages) {
        this.pageName = pageName;
        this.pageData = pageData;
        this.customer = customer;
        this.messages = messages;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public Object getPageData() {
        return pageData;
    }

    public void setPageData(Object pageData) {
        this.pageData = pageData;
    }
    
    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }
    
    public String toJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }
}
