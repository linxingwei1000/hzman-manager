package com.cn.hzm.core.task;

import java.util.List;

/**
 * @author linxingwei
 * @date 31.5.23 5:55 下午
 */
public interface ITask {


    void setSpiderSwitch(boolean spiderSwitch);

    void start();

    void close();

    String spideData(List<String> relationIds);

}
