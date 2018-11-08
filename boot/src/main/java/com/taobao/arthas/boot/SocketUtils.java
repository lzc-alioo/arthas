package com.taobao.arthas.boot;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.List;

import javax.net.ServerSocketFactory;

import oshi.PlatformEnum;
import oshi.SystemInfo;
import oshi.util.ExecutingCommand;

/**
 *
 * @author hengyunabc 2018-11-07
 *
 */
public class SocketUtils {

    public static int findTcpListenProcess(int port) {
        try {
            PlatformEnum platformEnum = SystemInfo.getCurrentPlatformEnum();
            if (PlatformEnum.WINDOWS.equals(platformEnum)) {
                String[] command = { "netstat", "-ano", "-p", "TCP" };
                List<String> lines = ExecutingCommand.runNative(command);
                for (String line : lines) {
                    if (line.contains("LISTENING")) {
                        // TCP 0.0.0.0:49168 0.0.0.0:0 LISTENING 476
                        String[] strings = line.trim().split("\\s+");
                        if (strings.length == 5) {
                            if (strings[1].endsWith(":" + port)) {
                                return Integer.parseInt(strings[4]);
                            }
                        }
                    }
                }
            }

            if (PlatformEnum.MACOSX.equals(platformEnum) || PlatformEnum.LINUX.equals(platformEnum)) {
                String pid = ExecutingCommand.getFirstAnswer("lsof -t -s TCP:LISTEN -i TCP:" + port);
                if (!pid.trim().isEmpty()) {
                    return Integer.parseInt(pid);
                }
            }
        } catch (Throwable e) {
            // ignore
        }

        return -1;
    }

    public static boolean isTcpPortAvailable(int port) {
        try {
            ServerSocket serverSocket = ServerSocketFactory.getDefault().createServerSocket(port, 1,
                            InetAddress.getByName("localhost"));
            serverSocket.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

}
