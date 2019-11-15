package com.zijin.music.musicvote.utils;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    /**
     * @function
     * */
    public static Response RespFormat(PageQueryResult pageQuery){
        Response response2 = Response.ok();
        ResponseParam rd = new ResponseParam();
        rd.setTable(pageQuery.getResult());
        rd.setTotal(pageQuery.getCount());
        response2.setData(rd);
        return response2;
    }
    static String getMacAddrByIp(String ip) {
        String macAddr = null;
        try {
            Process process = Runtime.getRuntime().exec("nbtstat -a " + ip);
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            Pattern pattern = Pattern.compile("([A-F0-9]{2}-){5}[A-F0-9]{2}");
            Matcher matcher;
            for (String strLine = br.readLine(); strLine != null;
                 strLine = br.readLine()) {
                matcher = pattern.matcher(strLine);
                if (matcher.find()) {
                    macAddr = matcher.group();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(macAddr);
        return macAddr;
    }

    public static Response RespListFormat(List list){
        Response response2 = Response.ok();
        ResponseParam rd = new ResponseParam();
        rd.setTable(list);
        rd.setTotal(list==null?0:Long.valueOf(new Integer(list.size())));
        response2.setData(rd);
        return response2;
    }
    /**
     * 获取今天开始时间
     */
    public static Date getStartTime() {
        Calendar todayStart = Calendar.getInstance();
        todayStart.set(Calendar.HOUR, 0);
        todayStart.set(Calendar.MINUTE, 0);
        todayStart.set(Calendar.SECOND, 0);
        todayStart.set(Calendar.MILLISECOND, 0);
        return todayStart.getTime() ;
    }

    /**
     * 获取今天结束时间
     */
    public static Date getEndTime() {
        Calendar todayEnd = Calendar.getInstance();
        todayEnd.set(Calendar.HOUR, 23);
        todayEnd.set(Calendar.MINUTE, 59);
        todayEnd.set(Calendar.SECOND, 59);
        todayEnd.set(Calendar.MILLISECOND, 999);
        return todayEnd.getTime();
    }

    /**
     * 获取客户端ip
     * @return
     */
    public static String getIpAddress( ) {
        HttpServletRequest request=  ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
            System.out.println("Proxy-Client-IP:"+ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
            System.out.println("WL-Proxy-Client-IP:"+ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
            System.out.println("HTTP_CLIENT_IP:"+ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            System.out.println("HTTP_X_FORWARDED_FOR:"+ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("RemoteAddress");
            System.out.println("RemoteAddress:"+ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            System.out.println("RemoteAddr:"+ip);
        }
        ip=ip==null?"":ip;
        return "0:0:0:0:0:0:0:1".equals(ip) ? "127.0.0.1" : ip;
    }
    public static String[] getNullPropertyNames (Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for(java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }
    public static String getToken() {

        HttpServletRequest request=
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest();
        return request.getHeader("token");
    }
}
