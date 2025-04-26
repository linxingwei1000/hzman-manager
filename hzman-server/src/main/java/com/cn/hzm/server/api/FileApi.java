package com.cn.hzm.server.api;

import com.cn.hzm.api.dto.FileConditionDto;
import com.cn.hzm.core.common.HzmResponse;
import com.cn.hzm.core.misc.UploadFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

/**
 * @author xingweilin@clubfactory.com
 * @date 2020/11/21 1:45 下午
 */
@Api(tags = "文件处理中心api")
@RestController
@RequestMapping("/file")
public class FileApi {

    @Autowired
    private UploadFileService uploadFileService;

    @ApiOperation("文件列表")
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public HzmResponse listItem(@RequestBody FileConditionDto conditionDTO){
        return HzmResponse.success(uploadFileService.listUpdateFile(conditionDTO));
    }

    @ApiOperation("上传处理文件")
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public HzmResponse localQuantityFileDeal(@RequestParam("file") MultipartFile file,
                                             @RequestParam("fileType") Integer fileType,
                                             HttpServletRequest request) throws IOException {
        InputStream inputStream = file.getInputStream();
        String fileName = file.getOriginalFilename();
        return HzmResponse.success(uploadFileService.loadDealFile(fileName, fileType, inputStream));
    }

    @ApiOperation("开始处理文件")
    @RequestMapping(value = "/process", method = RequestMethod.GET)
    public HzmResponse processUpdateFile(@ApiParam("fileId") @RequestParam Integer fileId) throws ParseException {
        return HzmResponse.success(uploadFileService.processFile(fileId));
    }

    @ApiOperation("文件处理详情")
    @RequestMapping(value = "/deal/detail", method = RequestMethod.GET)
    public HzmResponse dealDetail(@ApiParam("fileId") @RequestParam Integer fileId){
        return HzmResponse.success(uploadFileService.fileDealDetail(fileId));
    }
}
