package com.fssc.invoicearchive.config;

import com.fssc.invoicearchive.service.ReimburseService;
import com.fssc.invoicearchive.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private ReimburseService reimburseService;

    @Override
    public void run(String... args) throws Exception {
        userService.initDefaultUsers();
        reimburseService.initDefaultReimburseBills();
    }
}
