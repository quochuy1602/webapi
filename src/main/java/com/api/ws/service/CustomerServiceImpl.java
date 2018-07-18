package com.api.ws.service;

import com.api.ws.model.Customer;
import com.api.ws.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.awt.print.Pageable;
import java.util.Date;

/**
 * Created by qgs on 7/5/18.
 */
@Service
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    private CustomerRepository customerRepository;
    @Override
    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public Customer findById(String customerId) {
        return customerRepository.findById(customerId).get();
    }

    @Override
    public int delete(String customerId) {
        Customer customer = customerRepository.findById(customerId).get();
        if(customer != null){
            customerRepository.deleteById(customerId);
            return 1;
        }else{
            return 0;
        }
    }

    @Override
    public Page<Customer> findByAll(String txtSearch, int page, int size, Sort.Direction sort, String column) {
        PageRequest pageRequest = new PageRequest(page, size, sort, column);
        return customerRepository.findByAll(txtSearch,pageRequest);
    }

    public Page<Customer> findByAll(String txtSearch, Date date,int page,int size,Sort.Direction sort,String column){
        PageRequest pageRequest = new PageRequest(page, size, sort, column);
        return customerRepository.findByAll(txtSearch,date,pageRequest);
    }

}
