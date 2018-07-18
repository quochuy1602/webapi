package com.api.ws.repository;

import com.api.ws.model.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Created by qgs on 7/3/18.
 */
@Transactional
public interface CustomerRepository extends PagingAndSortingRepository<Customer, String> {
    @Query("{'email': {$regex : ?0 } }")
    public Page<Customer> findByAll(String txtSearch,Pageable pageable);
    @Query("{'email': {$regex : ?0 } , 'createDate': {$lte : ?1} }")
    public Page<Customer> findByAll(String txtSearch,Date date,Pageable pageable);
}
