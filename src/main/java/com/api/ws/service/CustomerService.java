package com.api.ws.service;

import com.api.ws.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.awt.print.Pageable;
import java.util.Date;
import java.util.List;

/**
 * Created by qgs on 7/5/18.
 */
public interface CustomerService {
    public Customer save(Customer customer);
    public Customer findById(String customerId);
    public int delete(String customerId);
    public Page<Customer> findByAll(String txtSearch,int page,int size,Sort.Direction sort,String column);
    public Page<Customer> findByAll(String txtSearch,Date date,int page,int size,Sort.Direction sort,String column);
}
