package com.cn.hzm.server.api;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.cn.hzm.api.dto.InventoryDto;
import com.cn.hzm.api.dto.PackageDimensionDto;
import com.cn.hzm.core.common.HzmResponse;
import com.cn.hzm.core.enums.SpiderType;
import com.cn.hzm.core.exception.ExceptionCode;
import com.cn.hzm.core.exception.HzmException;
import com.cn.hzm.core.manager.TaskManager;
import com.cn.hzm.core.misc.ItemService;
import com.cn.hzm.core.processor.DailyStatProcessor;
import com.cn.hzm.core.repository.dao.AsinItemDao;
import com.cn.hzm.core.repository.dao.ItemDao;
import com.cn.hzm.core.repository.dao.ItemInventoryDao;
import com.cn.hzm.core.repository.entity.AsinItemDo;
import com.cn.hzm.core.repository.entity.ItemDo;
import com.cn.hzm.core.repository.entity.ItemInventoryDo;
import com.cn.hzm.core.util.FtpFileUtil;
import com.cn.hzm.core.cache.ThreadLocalCache;
import com.cn.hzm.api.dto.FixOrderDto;
import com.cn.hzm.server.service.ExcelService;
import com.google.common.collect.Maps;
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
import java.util.Date;
import java.util.List;

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

    @ApiOperation("删除历史亚马逊订单数据")
    @RequestMapping(value = "/delete/his/order", method = RequestMethod.GET)
    public HzmResponse deleteHisOrder(@ApiParam("删除开始日期") @RequestParam String statDate,
                                      @ApiParam("删除天数") @RequestParam Integer dayNum) {
        dailyStatProcessor.deleteAmazonOrder(statDate, dayNum);
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


    @Autowired
    private ItemDao itemDao;

    @Autowired
    private AsinItemDao asinItemDao;

    @Autowired
    private ItemInventoryDao inventoryDao;


    @RequestMapping(value = "/fix/asin/item", method = RequestMethod.GET)
    public HzmResponse fixAsinItem() {
        List<ItemDo> items = itemDao.getListByCondition(Maps.newHashMap(),
                new String[]{"sku", "asin", "icon", "title", "package_dimension", "item_type", "user_market_id"});


        items.forEach(itemDo -> {
            ItemInventoryDo inventoryDO = inventoryDao.getInventoryBySku(itemDo.getSku(), itemDo.getUserMarketId());

            AsinItemDo asinItemDo = asinItemDao.getByAsin(itemDo.getAsin());
            if (asinItemDo != null) {
                if (inventoryDO != null) {
                    Integer localQuantity = asinItemDo.getLocalQuantity() ==null ? 0 : asinItemDo.getLocalQuantity();
                    asinItemDo.setLocalQuantity(localQuantity + inventoryDO.getLocalQuantity());
                    asinItemDao.updateItem(asinItemDo);
                }
            } else {
                asinItemDo = new AsinItemDo();
                asinItemDo.setAsin(itemDo.getAsin());
                asinItemDo.setTitle(itemDo.getTitle());
                asinItemDo.setIcon(itemDo.getIcon());

                JSONObject packageJo = JSONObject.parseObject(itemDo.getPackageDimension());
                if (packageJo != null && packageJo.containsKey("package")) {
                    JSONObject targetJo = packageJo.getJSONObject("package");
                    PackageDimensionDto dto = new PackageDimensionDto();
                    if (targetJo.containsKey("height")) {
                        dto.setHeight(targetJo.getJSONObject("height").getString("value"));
                    }
                    if (targetJo.containsKey("length")) {
                        dto.setLength(targetJo.getJSONObject("length").getString("value"));
                    }
                    if (targetJo.containsKey("weight")) {
                        dto.setWeight(targetJo.getJSONObject("weight").getString("value"));
                    }
                    if (targetJo.containsKey("width")) {
                        dto.setWidth(targetJo.getJSONObject("width").getString("value"));
                    }
                    asinItemDo.setPackageDimension(JSONObject.toJSONString(dto));
                } else {
                    asinItemDo.setPackageDimension(itemDo.getPackageDimension());
                }
                asinItemDo.setItemType(itemDo.getItemType());
                asinItemDo.setLocalQuantity(inventoryDO != null ? inventoryDO.getLocalQuantity() : 0);
                asinItemDo.setActive(1);
                asinItemDao.createItem(asinItemDo);
            }

        });
        return HzmResponse.success("true");
    }
}
