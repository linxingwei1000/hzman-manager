package com.cn.hzm.core.misc;

import com.alibaba.fastjson.JSONObject;
import com.cn.hzm.api.dto.*;
import com.cn.hzm.api.enums.FactoryOrderStatusEnum;
import com.cn.hzm.api.enums.FileDealStatusEnum;
import com.cn.hzm.api.enums.FileTypeEnum;
import com.cn.hzm.api.meta.HzmRoleType;
import com.cn.hzm.core.cache.ThreadLocalCache;
import com.cn.hzm.core.context.HzmContext;
import com.cn.hzm.core.exception.ExceptionCode;
import com.cn.hzm.core.exception.HzmException;
import com.cn.hzm.core.repository.dao.AsinItemDao;
import com.cn.hzm.core.repository.dao.DealFileDao;
import com.cn.hzm.core.repository.dao.DealFileDetailDao;
import com.cn.hzm.core.repository.entity.*;
import com.cn.hzm.core.util.TimeUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author linxingwei
 * @date 16.4.25 11:16 下午
 */
@Slf4j
@Component
public class UploadFileService {

    @Autowired
    private DealFileDao dealFileDao;

    @Autowired
    private DealFileDetailDao dealFileDetailDao;

    @Autowired
    private AsinItemDao asinItemDao;


    public JSONObject listUpdateFile(FileConditionDto conditionDTO) {
        Map<String, String> condition = (Map<String, String>) JSONObject.toJSON(conditionDTO);
        List<DealFileDo> list = dealFileDao.selectByCondition(condition);

        List<DealFileDto> dealFileDtos = conditionDTO.pageResult(list).stream().map(dealFileDo -> {
            DealFileDto dealFileDto = new DealFileDto();
            BeanUtils.copyProperties(dealFileDo, dealFileDto);
            dealFileDto.setStrFileType(FileTypeEnum.getEnumByCode(dealFileDo.getFileType()).getDesc());
            dealFileDto.setStrDealStatus(FileDealStatusEnum.getEnumByCode(dealFileDo.getDealStatus()).getDesc());
            dealFileDto.setCreateTime(TimeUtil.getDateFormat(dealFileDo.getCtime()));
            return dealFileDto;
        }).collect(Collectors.toList());
        JSONObject respJo = new JSONObject();
        respJo.put("total", list.size());
        respJo.put("data", JSONObject.toJSON(dealFileDtos));
        return respJo;
    }

    @Transactional
    public Boolean loadDealFile(String fileName, Integer fileType, InputStream input) {
        DealFileDo old = dealFileDao.selectByFileName(fileName);
        if (old != null) {
            throw new HzmException(ExceptionCode.DEAL_FILE_EXIST);
        }

        DealFileDo dealFileDo = new DealFileDo();
        dealFileDo.setFileName(fileName);
        dealFileDo.setFileType(fileType);
        dealFileDo.setDealStatus(FileDealStatusEnum.CREATE.getCode());
        dealFileDao.create(dealFileDo);

        StringBuilder dealLine = new StringBuilder("成功处理行：");
        StringBuilder emptyRow = new StringBuilder("空数据行：");
        StringBuilder dealErrorRow = new StringBuilder("处理失败行：");
        try {
            XSSFWorkbook wb = new XSSFWorkbook(input);
            int sheetNums = wb.getNumberOfSheets();
            for (int sheetNo = 0; sheetNo < sheetNums; sheetNo++) {
                XSSFSheet sheet = wb.getSheetAt(sheetNo);
                if (!sheet.getSheetName().equals("包装箱包装信息")) {
                    continue;
                }

                //跳过标题，从第三行数据开始读取
                for (int r = 5; r <= sheet.getLastRowNum(); r++) {
                    XSSFRow row = sheet.getRow(r);
                    if (row == null) {
                        emptyRow.append(r).append(",");
                        continue;
                    }

                    if (StringUtils.isEmpty(row.getCell(3).getStringCellValue())) {
                        continue;
                    }
                    try {
                        DealFileDetailDo dealFileDetailDo = new DealFileDetailDo();
                        dealFileDetailDo.setFileId(dealFileDo.getId());
                        dealFileDetailDo.setAsin(row.getCell(3).getStringCellValue());
                        dealFileDetailDo.setSku(row.getCell(0).getStringCellValue());
                        dealFileDetailDo.setFnsku(row.getCell(4).getStringCellValue());
                        dealFileDetailDo.setDealStatus(FileDealStatusEnum.CREATE.getCode());

                        //统计总量
                        int totalNum = 0;
                        int lastCellNum = row.getLastCellNum();
                        for (int beginNum = 12; beginNum < lastCellNum; beginNum++) {
                            XSSFCell cell = row.getCell(beginNum);
                            if(cell == null){
                                continue;
                            }

                            if (cell.getCellTypeEnum().equals(CellType.BLANK)) {
                                continue;
                            }
                            totalNum += (int) cell.getNumericCellValue();
                        }
                        dealFileDetailDo.setDealNum(totalNum);
                        dealFileDetailDo.setDealResult("");
                        dealFileDetailDao.create(dealFileDetailDo);
                    } catch (Exception e) {
                        log.error("excel 处理失败：", e);
                        dealErrorRow.append(r).append("[").append(e.getMessage()).append("],");
                    }
                    dealLine.append(r).append(",");
                }
            }
        } catch (Exception e) {
            log.error("批量处理文件读取失败：", e);
            throw new HzmException(ExceptionCode.TEMPLATE_EXCEL_DEAL_ERROR);
        }
        return true;
    }

    public String processFile(Integer fileId) {
        DealFileDo dealFileDo = dealFileDao.selectByFileId(fileId);

        dealFileDo.setDealStatus(FileDealStatusEnum.PROCESSING.getCode());
        dealFileDao.update(dealFileDo);

        new Thread(() -> {
            List<DealFileDetailDo> dealFileDetailDos = dealFileDetailDao.selectByDealFileId(fileId);
            for (DealFileDetailDo dealFileDetailDo : dealFileDetailDos) {
                if (StringUtils.isEmpty(dealFileDetailDo.getAsin())) {
                    dealFileDetailDo.setDealStatus(FileDealStatusEnum.PROCESS_SKIP.getCode());
                    dealFileDetailDo.setDealResult("asin为空，跳过处理");
                    dealFileDetailDao.update(dealFileDetailDo);
                    continue;
                }
                try {
                    AsinItemDo asinItemDo = asinItemDao.getByAsin(dealFileDetailDo.getAsin());
                    if (asinItemDo == null) {
                        dealFileDetailDo.setDealStatus(FileDealStatusEnum.PROCESS_SKIP.getCode());
                        dealFileDetailDo.setDealResult("asin商品不存在，跳过处理");
                        dealFileDetailDao.update(dealFileDetailDo);
                        continue;
                    }
                    int localNum = asinItemDo.getLocalQuantity() - dealFileDetailDo.getDealNum();
                    asinItemDo.setLocalQuantity(Math.max(localNum, 0));
                    asinItemDao.updateItem(asinItemDo);

                    dealFileDetailDo.setDealStatus(FileDealStatusEnum.PROCESS_FINISH.getCode());
                    dealFileDetailDo.setDealResult(FileDealStatusEnum.PROCESS_FINISH.getDesc());
                    dealFileDetailDao.update(dealFileDetailDo);
                } catch (Exception e) {
                    log.error("[{}.{}]文件数据处理失败：", fileId, dealFileDetailDo.getId(), e);
                    dealFileDetailDo.setDealStatus(FileDealStatusEnum.PROCESS_FAIL.getCode());
                    dealFileDetailDo.setDealResult("处理失败：" + e.getMessage());
                    dealFileDetailDao.update(dealFileDetailDo);
                }
            }
            dealFileDo.setDealStatus(FileDealStatusEnum.PROCESS_FINISH.getCode());
            dealFileDao.update(dealFileDo);
        }).start();
        return "后台异步处理文件数据";
    }

    public List<DealFileDetailDto> fileDealDetail(Integer fileId) {
        List<DealFileDetailDo> dealFileDetailDos = dealFileDetailDao.selectByDealFileId(fileId);
        return dealFileDetailDos.stream().map(dealFileDetailDo -> {
            DealFileDetailDto dealFileDetailDto = new DealFileDetailDto();
            BeanUtils.copyProperties(dealFileDetailDo, dealFileDetailDto);
            dealFileDetailDto.setStrDealStatus(FileDealStatusEnum.getEnumByCode(dealFileDetailDo.getDealStatus()).getDesc());
            dealFileDetailDto.setCreateTime(TimeUtil.getDateFormat(dealFileDetailDo.getCtime()));
            return dealFileDetailDto;
        }).collect(Collectors.toList());
    }
}
