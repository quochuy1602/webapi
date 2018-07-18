package com.api.ws.controller;

import com.api.ws.model.Customer;
import com.api.ws.service.CustomerService;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;

/**
 * Created by qgs on 7/3/18.
 */
@RestController
@RequestMapping("/abc")
public class IndexController {
    public static final Logger logger = LoggerFactory.getLogger(IndexController.class);
    public static String[] COLUMNs = {"Id", "Name", "Email", "Status"};
    @Autowired
    private CustomerService customerService;
    public Semaphore semaphore = new Semaphore(0);
    private Integer semaphorePermits;
    @Value("${semaphorePermits}")
    public void setSemaphorePermits(String value) {
        try {
            semaphorePermits = Integer.parseInt(value);
            semaphore.release(semaphorePermits);
        } catch(NumberFormatException ex) {
            // do nothing
        }
    }
    @RequestMapping(value="/hello" , method= RequestMethod.GET)
    public ResponseEntity<String> hello(){
        return new ResponseEntity<>("Hello",HttpStatus.OK);
    }
    @RequestMapping(value="/export-data" , method= RequestMethod.GET)
    public ResponseEntity<String> exportData(HttpServletResponse response,@RequestParam Date date) throws IOException {

        Workbook workbook = new XSSFWorkbook();

        CreationHelper createHelper = workbook.getCreationHelper();

        Sheet sheet = workbook.createSheet("Customers");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.BLUE.getIndex());

        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);

        // Row for Header
        Row headerRow = sheet.createRow(0);

        // Header
        for (int col = 0; col < COLUMNs.length; col++) {
            Cell cell = headerRow.createCell(col);
            cell.setCellValue(COLUMNs[col]);
            cell.setCellStyle(headerCellStyle);
        }

        // CellStyle for Age
        CellStyle ageCellStyle = workbook.createCellStyle();
        ageCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("#"));

        int rowIdx = 1;
        Sort.Direction direction =  Sort.Direction.DESC;
        int totalPage = 0;
        int size = 1000;
        List<Customer> customerList = new ArrayList<>();
        Page<Customer> customers = customerService.findByAll("",date,0,size,direction,"name");
        customerList.addAll(customers.getContent());
        totalPage = customers.getTotalPages();
        for(int i = 1 ; i <= totalPage;i++){
            customers = customerService.findByAll("",date,i,size,direction,"name");
            customerList.addAll(customers.getContent());
        }
        for (Customer customer : customerList) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(customer.getId());
            row.createCell(1).setCellValue(customer.getName());
            row.createCell(2).setCellValue(customer.getEmail());
            Cell ageCell = row.createCell(3);
            //ageCell.setCellValue("Active");
            //ageCell.setCellStyle(ageCellStyle);
        }
        String nameFile = "customers";
        String tempDir = System.getProperty("java.io.tmpdir");
        File tempFile = new File(tempDir + "/" + nameFile + ".xlsx");
        FileOutputStream fileOut = new FileOutputStream(tempFile);
        workbook.write(fileOut);
        fileOut.close();
        workbook.close();
        response.setHeader("Content-Disposition", "attachment; filename=\"" + nameFile + ".xlsx\"");
        InputStream is = null;
        is = new FileInputStream(tempFile);
        FileCopyUtils.copy(is, response.getOutputStream());
        // delete file on server file system
        tempFile.delete();
        return new  ResponseEntity<>("Ok", HttpStatus.OK);
    }
    @RequestMapping(value="/import-data",  headers = "content-type=multipart/*",method= RequestMethod.POST)
    @ResponseBody
    public Map<String,Object> importCustomer(@RequestParam("file") MultipartFile file) throws IOException, InvalidFormatException, InterruptedException {
       // logger.info("upload file invoked.");
        Map<String,Object> resultData = new HashMap<String, Object>();
        Map<String,Object> meta = new HashMap<String, Object>();
        Map<String,Object> data = new HashMap<String, Object>();
        int success = 0, skip = 0;
        logger.info("Export Excel" + " : acquiring lock...");
        logger.info("Export Excel" + " : available Semaphore permits now: "
                + semaphore.availablePermits());
        try{
            semaphore.acquire();
            int i = 1;
            Workbook wb = WorkbookFactory.create(file.getInputStream());
            logger.info("Workbook" + wb);
            Sheet worksheet = wb.getSheetAt(0);
            logger.info("list customer row " + worksheet.getLastRowNum());
            while (i <= worksheet.getLastRowNum()) {
                Row row = worksheet.getRow(i++);
                if(row == null) continue;
                logger.info("row" + row);
                try {
                    //get data from excel file
                    String customerName = row.getCell(0) != null ? row.getCell(0).getStringCellValue().trim() : "";
                    String email = row.getCell(1) != null ? row.getCell(1).getStringCellValue().trim() : "";

                    Customer customer = new Customer();
                    customer.setName(customerName);
                    customer.setEmail(email);
                    customer.setCreateDate(new Date());
                    customer = customerService.save(customer);
                    logger.info("customer" + customer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            meta.put("code", 200);
            resultData.put("meta", meta);
            resultData.put("data", data);
        }catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            meta.put("code", 404);
            resultData.put("meta", meta);
            resultData.put("data", data);
        } finally {
            logger.info("Export Excel" + " : releasing  lock...");
            semaphore.release();
            logger.info("Export Excel" + " : available Semaphore permits now: "
                    + semaphore.availablePermits());
        }

        return resultData;
    }
}
