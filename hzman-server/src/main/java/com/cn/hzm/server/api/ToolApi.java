package com.cn.hzm.server.api;

import com.cn.hzm.core.common.HzmResponse;
import com.cn.hzm.core.enums.SpiderType;
import com.cn.hzm.core.exception.ExceptionCode;
import com.cn.hzm.core.exception.HzmException;
import com.cn.hzm.core.manager.TaskManager;
import com.cn.hzm.core.misc.ItemService;
import com.cn.hzm.core.processor.DailyStatProcessor;
import com.cn.hzm.core.util.FtpFileUtil;
import com.cn.hzm.core.cache.ThreadLocalCache;
import com.cn.hzm.api.dto.FixOrderDto;
import com.cn.hzm.server.service.ExcelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author xingweilin@clubfactory.com
 * @date 2021/1/9 8:16 下午
 */
@Api(tags = "工具Api")
@RestController
@RequestMapping("/tool")
public class ToolApi {

    @Autowired
    private DailyStatProcessor dailyStatProcessor;

    @Autowired
    private TaskManager taskManager;

    @Autowired
    private ExcelService excelService;

    @Autowired
    private ItemService itemService;

    @ApiOperation("修复某一天销量统计")
    @RequestMapping(value = "/sale/fix/daily", method = RequestMethod.GET)
    public HzmResponse fixDaily(@ApiParam("修复日期") @RequestParam String statDate) {
        dailyStatProcessor.statSaleInfoChooseDate(ThreadLocalCache.getUser().getUserMarketId(), statDate);
        return HzmResponse.success("true");
    }

    @ApiOperation("修复一段时间销量统计")
    @RequestMapping(value = "/sale/fix/duration", method = RequestMethod.GET)
    public HzmResponse fixDuration(@ApiParam("修复开始日期") @RequestParam String statDate,
                                   @ApiParam("修复天数") @RequestParam Integer dayNum) {
        dailyStatProcessor.statSaleInfoDurationDay(ThreadLocalCache.getUser().getUserMarketId(), statDate, dayNum);
        return HzmResponse.success("true");
    }

    @ApiOperation("修复订单数据")
    @RequestMapping(value = "/order/fix", method = RequestMethod.POST)
    public HzmResponse fixOrder(@RequestBody FixOrderDto fixOrderDTO) {
        taskManager.execTaskByRelationIds(fixOrderDTO.getUserMarketId(), SpiderType.CREATE_ORDER.getCode(), fixOrderDTO.getOrderIds());
        return HzmResponse.success("true");
    }

    @ApiOperation("ftp上传")
    @RequestMapping(value = "/ftp/upload", method = RequestMethod.POST)
    public HzmResponse uploadImg(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
        String fileName = file.getOriginalFilename();
        InputStream inputStream = file.getInputStream();
        String ftpPath = FtpFileUtil.uploadFile(fileName, inputStream, "pay");
        if (!StringUtils.isEmpty(ftpPath)) {
            return HzmResponse.success(ftpPath);
        }
        throw new HzmException(ExceptionCode.FTP_UPLOAD_ERR);
    }

    @ApiOperation("模版文档下载")
    @RequestMapping(value = "/template/excel/download", method = RequestMethod.GET)
    public HzmResponse downloadTemplateExcel(@RequestParam("excelType") Integer excelType, HttpServletResponse response) throws IOException {
        excelService.createTemplateExcel(excelType, response);
        return HzmResponse.success("下载成功");
    }

    @ApiOperation("excel文件上传处理")
    @RequestMapping(value = "/excel/upload", method = RequestMethod.POST)
    public HzmResponse downloadTemplateExcel(@RequestParam("file") MultipartFile file,
                                             @RequestParam("excelType") Integer excelType,
                                             HttpServletRequest request) throws IOException {
        InputStream inputStream = file.getInputStream();
        return HzmResponse.success(excelService.dealExcel(inputStream, excelType, null));
    }

    @ApiOperation("本地库存商品下载")
    @RequestMapping(value = "/stock/item/download", method = RequestMethod.GET)
    public HzmResponse stockItemDownload(HttpServletResponse response) throws IOException {
        itemService.stockItemDownload(response);
        return HzmResponse.success("下载成功");
    }

    @ApiOperation("未填写成本商品下载")
    @RequestMapping(value = "/uncost/item/download", method = RequestMethod.GET)
    public HzmResponse uncostItemDownload(HttpServletResponse response) throws IOException {
        itemService.costItemDownload(response);
        return HzmResponse.success("下载成功");
    }
}
