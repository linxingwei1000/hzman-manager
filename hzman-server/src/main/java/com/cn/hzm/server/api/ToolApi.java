package com.cn.hzm.server.api;

import com.cn.hzm.core.common.HzmResponse;
import com.cn.hzm.core.exception.ExceptionCode;
import com.cn.hzm.core.exception.HzmException;
import com.cn.hzm.core.util.FtpFileUtil;
import com.cn.hzm.server.task.DailyStatTask;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
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
    private DailyStatTask dailyStatTask;

    @ApiOperation("修复某一天销量统计")
    @RequestMapping(value = "/sale/fix/daily", method = RequestMethod.GET)
    public HzmResponse fixDaily(@ApiParam("修复日期") @RequestParam String statDate) {
        dailyStatTask.statSaleInfoChooseDate(statDate);
        return HzmResponse.success("true");
    }

    @ApiOperation("修复一段时间销量统计")
    @RequestMapping(value = "/sale/fix/duration", method = RequestMethod.GET)
    public HzmResponse fixDuration(@ApiParam("修复开始日期") @RequestParam String statDate,
                                   @ApiParam("修复天数") @RequestParam Integer dayNum) {
        dailyStatTask.statSaleInfoDurationDay(statDate, dayNum);
        return HzmResponse.success("true");
    }

    @ApiOperation("ftp上传")
    @RequestMapping(value = "/ftp/upload", method = RequestMethod.POST)
    public HzmResponse uploadImg(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
        String fileName = file.getOriginalFilename();
        InputStream inputStream = file.getInputStream();
        String ftpPath = FtpFileUtil.uploadFile(fileName, inputStream, "pay");
        if(!StringUtils.isEmpty(ftpPath)){
            return HzmResponse.success(ftpPath);
        }
        throw new HzmException(ExceptionCode.FTP_UPLOAD_ERR);
    }
}
