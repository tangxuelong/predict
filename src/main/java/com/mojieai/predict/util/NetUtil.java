package com.mojieai.predict.util;

import com.mojieai.predict.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class NetUtil {
    public static final String DEFAULT_LOCAL_IP = "127.0.0.1";

    public static List<String> getAllLocalIP() {
        try {
            List<String> ar = new ArrayList<>();
            Enumeration<NetworkInterface> netInterfaces = null;
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            if (netInterfaces == null) {
                return ar;
            }
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> inetAdrsEnum = ni.getInetAddresses();
                if (inetAdrsEnum == null || !inetAdrsEnum.hasMoreElements()) {
                    continue;
                }
                InetAddress ip = inetAdrsEnum.nextElement();
                if ((!ip.isSiteLocalAddress() && !ip.isLoopbackAddress() && ip.getHostAddress().indexOf(":") == -1)
                        || ip
                        instanceof Inet6Address) {
                    //				System.out.println("Interface " + ni.getName() + " seems to be InternetInterface.
                    // I'll
                    // take it...");
                } else {
                    ar.add(ip.getHostAddress());
                }
            }
            return ar;
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }

    public static String getLocalIP() {
        try {
            List<String> ipList = getAllLocalIP();
            String localIp = null;
            for (String ip : ipList) {
                if (StringUtils.isNotBlank(ip) && !DEFAULT_LOCAL_IP.equals(ip)) {
                    localIp = ip;
                    break;
                }
            }
            return localIp;
        } catch (Exception e) {
            throw new BusinessException(e);
        }
    }

    public static boolean containsIp(String ip) {
        Enumeration<NetworkInterface> netInterfaces;
        try {
            netInterfaces = NetworkInterface.getNetworkInterfaces();
            while (netInterfaces.hasMoreElements()) {
                NetworkInterface ni = netInterfaces.nextElement();

                Enumeration<InetAddress> emu = ni.getInetAddresses();
                while (emu.hasMoreElements()) {
                    InetAddress ipaddr = emu.nextElement();
                    //					ScheduleControllerFactory.log.info(ipaddr.getHostAddress()
                    //							+ "ip:-------------------------------------");
                    if (ip.equals(ipaddr.getHostAddress()))
                        return true;
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 检查参数ips中是否包含本地服务器ip
     *
     * @param ips
     * @param separator
     * @return
     */
    public static boolean containsIps(String ips, String separator) {
        if (StringUtils.isEmpty(ips)) {
            return false;
        }
        String[] parts = ips.split(separator);
        for (String s : parts) {
            if (containsIp(s)) {
                return true;
            }
        }
        return false;
    }

    public static String getCurrentLoginUserIp(HttpServletRequest request) {
        String rip = request.getRemoteAddr();
        String xff = request.getHeader("X-Forwarded-For");
        String ip;
        if (xff != null && xff.length() != 0) {
            int px = xff.indexOf(',');
            if (px != -1) {
                ip = xff.substring(0, px);
            } else {
                ip = xff;
            }
        } else {
            ip = rip;
        }
        return ip.trim();
    }
}