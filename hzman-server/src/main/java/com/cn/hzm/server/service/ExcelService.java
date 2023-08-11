package com.cn.hzm.server.service;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.core.cache.ThreadLocalCache;
import com.cn.hzm.core.exception.ExceptionCode;
import com.cn.hzm.core.exception.HzmException;
import com.cn.hzm.api.dto.AddItemDeallDto;
import com.cn.hzm.api.dto.CostDealDto;
import com.cn.hzm.api.dto.LocalDealDto;
import com.cn.hzm.core.misc.ItemService;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author linxingwei
 * @date 14.5.23 5:25 下午
 */
@Slf4j
@Service
public class ExcelService {

    @Autowired
    private ItemService itemService;

    private static final Map<Integer, Integer> EXCEL_FIELD_SIZE = Maps.newHashMap();

    static {
        EXCEL_FIELD_SIZE.put(1, 2);
        EXCEL_FIELD_SIZE.put(2, 3);
        EXCEL_FIELD_SIZE.put(3, 5);
    }

    /**
     * 创建模版excel
     *
     * @param excelType
     * @param response
     */
    public void createTemplateExcel(Integer excelType, HttpServletResponse response) {
        List<String> rowNameList;
        List<String> rowFiledList;
        List<String> defaultValue;
        String sheetName;
        if (excelType.equals(1)) {
            rowNameList = Lists.newArrayList("sku", "库存数量");
            rowFiledList = Lists.newArrayList("sku", "localNum");
            defaultValue = Lists.newArrayList("修改此行，输入正确sku", "输入sku对应库存");
            sheetName = "本地库存";
        } else if (excelType.equals(2)) {
            rowNameList = Lists.newArrayList("asin", "sku", "成本");
            rowFiledList = Lists.newArrayList("asin", "sku", "cost");
            defaultValue = Lists.newArrayList("修改此行，输入正确asin", "修改此行，输入正确sku", "输入sku对应成本");
            sheetName = "成本";
        } else {
            rowNameList = Lists.newArrayList("asin", "sku", "厂家", "成本", "备注");
            rowFiledList = Lists.newArrayList("asin", "sku", "FactoryId", "cost", "remark");
            defaultValue = Lists.newArrayList("修改此行，输入正确asin", "修改此行，输入正确sku", "输入sku对应的厂家id",
                    "0.0", "");
            sheetName = "批量添加";
        }

        //创建一个工作蒲
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet("sheet1");
        sheet.setDefaultColumnWidth(19);

        //全局样式
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);//居中
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);//上下居中

        //标题
        XSSFRow row = sheet.createRow(0);
        for (int i = 0; i < rowNameList.size(); i++) {
            row.setHeight((short) 450);
            XSSFCell cell = row.createCell(i);
            cell.setCellValue(rowNameList.get(i));
            cell.setCellStyle(cellStyle);
        }

        XSSFRow fieldRow = sheet.createRow(1);
        for (int i = 0; i < rowFiledList.size(); i++) {
            fieldRow.setHeight((short) 450);
            XSSFCell cell = fieldRow.createCell(i);
            cell.setCellValue(rowFiledList.get(i));
            cell.setCellStyle(cellStyle);
        }

        XSSFRow defaultValueRow = sheet.createRow(2);
        for (int i = 0; i < defaultValue.size(); i++) {
            defaultValueRow.setHeight((short) 450);
            XSSFCell cell = defaultValueRow.createCell(i);
            cell.setCellValue(defaultValue.get(i));
            cell.setCellStyle(cellStyle);
        }

        //数据输出流
        try {
            OutputStream output = response.getOutputStream();
            response.reset();
            response.setHeader("Content-Disposition",
                    "attachment;filename=" +
                            new String((sheetName + ".xlsx").getBytes(StandardCharsets.UTF_8), "ISO8859-1"));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            log.error("下载模版文件失败：", e);
            throw new HzmException(ExceptionCode.TEMPLATE_EXCEL_DOWNLOAD_ERROR);
        }
    }

    public String dealExcel(InputStream input, Integer excelType, Integer userMarketId) {
        StringBuilder dealLine = new StringBuilder("成功处理行：");
        StringBuilder emptyRow = new StringBuilder("空数据行：");
        StringBuilder dealErrorRow = new StringBuilder("处理失败行：");
        try {
            XSSFWorkbook wb = new XSSFWorkbook(input);
            XSSFSheet sheet = wb.getSheetAt(0);

            XSSFRow fieldRow = sheet.getRow(1);

            Integer fieldNum = EXCEL_FIELD_SIZE.get(excelType);
            List<String> fields = Lists.newArrayList();
            for (int i = 0; i < fieldNum; i++) {
                fields.add(fieldRow.getCell(i).getStringCellValue());
            }

            //跳过标题，从第三行数据开始读取
            for (int r = 2; r <= sheet.getLastRowNum(); r++) {
                XSSFRow row = sheet.getRow(r);
                if (row == null) {
                    emptyRow.append(r).append(",");
                    continue;
                }

                try {
                    JSONObject jo = new JSONObject();

                    for (int i = 0; i < fieldNum; i++) {
                        XSSFCell cell = row.getCell(i);
                        if (cell == null) {
                            //cell没有填，一律按空字符串处理
                            jo.put(fields.get(i), "");
                            continue;
                        }

                        if (cell.getCellTypeEnum().equals(CellType.BLANK)) {
                            jo.put(fields.get(i), "");
                        } else if (cell.getCellTypeEnum().equals(CellType.STRING)) {
                            jo.put(fields.get(i), cell.getStringCellValue());
                        } else if (cell.getCellTypeEnum().equals(CellType.NUMERIC)) {
                            jo.put(fields.get(i), cell.getNumericCellValue());
                        } else {
                            jo.put(fields.get(i), cell.getStringCellValue());
                        }
                    }

                    if (excelType.equals(1)) {
                        LocalDealDto dto = JSONObject.parseObject(jo.toJSONString(), LocalDealDto.class);
                        itemService.modLocalNum(dto.getSku(), dto.getLocalNum(), ThreadLocalCache.getUser().getAwsUserId(), ThreadLocalCache.getUser().getMarketId());
                    } else if (excelType.equals(2)) {
                        CostDealDto dto = JSONObject.parseObject(jo.toJSONString(), CostDealDto.class);
                        itemService.modSkuCost(dto.getAsin(), dto.getSku(), dto.getCost(), ThreadLocalCache.getUser().getUserMarketId());
                    } else {
                        AddItemDeallDto dto = JSONObject.parseObject(jo.toJSONString(), AddItemDeallDto.class);
                        itemService.excelProcessSync(dto, ThreadLocalCache.getUser().getAwsUserId(), ThreadLocalCache.getUser().getMarketId());
                    }
                } catch (Exception e) {
                    log.error("excel 处理失败：", e);
                    dealErrorRow.append(r).append("[").append(e.getMessage()).append("],");
                }
                dealLine.append(r).append(",");
            }

        } catch (Exception e) {
            log.error("批量处理文件读取失败：", e);
            throw new HzmException(ExceptionCode.TEMPLATE_EXCEL_DEAL_ERROR);
        }
        return String.format("文件处理结果：%s %s %s", dealLine, emptyRow, dealErrorRow);
    }
}
