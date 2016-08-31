package com.example.nohttptest.bean;

import java.util.List;

/**
 * Created by Administrator on 2016/8/28.
 */
public class MainBean {

    /**
     * error_code : 0
     * reason : success
     * result : [{"code":"150000","regionName":"内蒙古自治区"},{"code":"350000","regionName":"福建省"}]
     */

    private int error_code;
    private String reason;
    /**
     * code : 150000
     * regionName : 内蒙古自治区
     */

    private List<ResultBean> result;

    public int getError_code() {
        return error_code;
    }

    public void setError_code(int error_code) {
        this.error_code = error_code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<ResultBean> getResult() {
        return result;
    }

    public void setResult(List<ResultBean> result) {
        this.result = result;
    }

    public static class ResultBean {
        private String code;
        private String regionName;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getRegionName() {
            return regionName;
        }

        public void setRegionName(String regionName) {
            this.regionName = regionName;
        }
    }
}
