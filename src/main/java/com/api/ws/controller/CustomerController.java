package com.api.ws.controller;

import com.api.ws.model.Customer;
import com.api.ws.repository.CustomerRepository;
import com.api.ws.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by qgs on 7/3/18.
 */
@RestController
@RequestMapping("/api/customer")
public class CustomerController {
    @Autowired
    private CustomerService customerService;
    /**
     * GET /create  --> Create a new customer and save it in the database.
     */
    @RequestMapping(value = "",method = RequestMethod.POST)
    public Map<String,Object> create(@RequestBody Customer customer){
        customer.setCreateDate(new Date());
        customer = customerService.save(customer);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("message", "Customer created successfully");
        dataMap.put("status", "1");
        dataMap.put("customer", customer);
        return dataMap;
    }
    /**
     * GET /read  --> Read a booking by booking id from the database.
     */
    @RequestMapping(value ="/{id}" , method = RequestMethod.GET)
    public Map<String, Object> read(@PathVariable String id) {
        Customer customer = customerService.findById(id);
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("message", "Customer found successfully");
        dataMap.put("status", "1");
        dataMap.put("customer", customer);
        return dataMap;
    }

    /**
     * GET /update  --> Update a customer record and save it in the database.
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public Map<String, Object> update(@PathVariable String id, @RequestBody Customer customer) {
        Map<String, Object> dataMap = new HashMap<String, Object>();
        try {
            Customer customerUpdate = customerService.findById(id);
            customerUpdate.setEmail(customer.getEmail());
            customerUpdate.setName(customer.getName());
            customerUpdate = customerService.save(customerUpdate);
            dataMap.put("message", "Customer updated successfully");
            dataMap.put("status", "1");
            dataMap.put("customer", customerUpdate);
        }catch (Exception e){
            dataMap.put("message", e.getMessage());
            dataMap.put("status", "0");
            dataMap.put("customer", null);
        }
        return dataMap;
    }
    /**
     * GET /delete  --> Delete a customer from the database.
     */
    @RequestMapping(value = "/{id}" , method = RequestMethod.DELETE)
    public Map<String, Object> delete(@PathVariable String id) {
        int result = customerService.delete(id);
        Map<String, Object> dataMap = new HashMap<String, Object>();
        if(result == 1) {
            dataMap.put("message", "Customer deleted successfully");
            dataMap.put("statusCode", "1");
        }else{
            dataMap.put("message", "Customer deleted failed");
            dataMap.put("status", "0");
        }
        return dataMap;
    }
    /**
     * GET /  --> Read all customer from the database.
     */
    @RequestMapping(value = "/find-all",method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> findAll(@RequestParam("page") int page,@RequestParam("size") int size,@RequestParam("sort") String sort,@RequestParam("column") String column) throws ParseException {

        Sort.Direction direction =  Sort.Direction.DESC;
        if(sort.equalsIgnoreCase("asc")) direction = Sort.Direction.ASC;
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS");
        Date dateNow = new Date();
        String t = dateFormat.format(dateNow);
        dateNow = dateFormat.parse(t);
        System.out.println("date: "+ t);
        Page<Customer> customers = customerService.findByAll("",dateNow,page,size,direction,column);

        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("message", "Customer found successfully");
        dataMap.put("totalPage", customers.getTotalPages());
        dataMap.put("number", customers.getNumber());
        dataMap.put("status", "1");
        dataMap.put("totalSize", customers.getTotalElements());
        dataMap.put("response", customers.getContent());
        dataMap.put("date", t);
        return dataMap;
    }
    /*@RequestMapping(value = "/find-all",method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> findAll(@RequestParam("page") int page,@RequestParam("size") int size,@RequestParam("sort") String sort,@RequestParam("column") String column,@RequestParam("date") Date date) {
        Sort.Direction direction =  Sort.Direction.DESC;
        if(sort.equalsIgnoreCase("asc")) direction = Sort.Direction.ASC;
        Page<Customer> customers = customerService.findByAll("",date,page,size,direction,column);
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("message", "Customer found successfully");
        dataMap.put("totalPage", customers.getTotalPages());
        dataMap.put("number", customers.getNumber());
        dataMap.put("status", "1");
        dataMap.put("totalSize", customers.getTotalElements());
        dataMap.put("response", customers.getContent());
        return dataMap;
    }*/
}
