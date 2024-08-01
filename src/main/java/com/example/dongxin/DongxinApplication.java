package com.example.dongxin;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.ServletContextInitializerBeans;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.util.Collection;

@SpringBootApplication
public class DongxinApplication {

    public static void main(String[] args) {
//        SpringApplication.run(DongxinApplication.class, args);

        String[] newArgs = args.clone();
        int defaultPort = 8080;
        boolean needChangePort = false;
        if (isPortInUse(defaultPort)) {
            newArgs = new String[args.length + 1];
            System.arraycopy(args, 0, newArgs, 0, args.length);
            newArgs[newArgs.length - 1] = "--server.port=9090";
            needChangePort = true;
        }
        ConfigurableApplicationContext run = SpringApplication.run(DongxinApplication.class, newArgs);
        if (needChangePort) {
//            String command = String.format("lsof -i :%d | grep LISTEN | awk '{print $2}' | xargs kill -9", defaultPort);
            //                Runtime.getRuntime().exec("netstat -ano | findstr : | ").waitFor();
            String processName = getProcessNameOnPort(defaultPort);
            if (processName != null) {
                System.out.println("Process found on port " + defaultPort + ": " + processName);
                killProcess(processName);
            } else {
                System.out.println("No process found on port " + defaultPort);
            }
            while (isPortInUse(defaultPort)) {
//                System.out.println("port : " + defaultPort + " still in use");
            }
            ServletWebServerFactory webServerFactory = getWebServerFactory(run);
            ((TomcatServletWebServerFactory) webServerFactory).setPort(defaultPort);
            WebServer webServer = webServerFactory.getWebServer(invokeSelfInitialize((ServletWebServerApplicationContext) run));
            webServer.start();

            ((ServletWebServerApplicationContext) run).getWebServer().stop();
        }


//        try {
//            Tomcat tomcat = new Tomcat();
//            tomcat.getConnector();
//            tomcat.getHost();
//            Context context = tomcat.addContext("/", null);
//            tomcat.addServlet("/", "index", new HttpServlet() {
//                @Override
//                protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//                    resp.getWriter().append("hello");
//                }
//            });
//            context.addServletMappingDecoded("/", "index");
//            tomcat.init();
//            tomcat.start();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
    }

    private static boolean isPortInUse(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    private static ServletWebServerFactory getWebServerFactory(ConfigurableApplicationContext context) {
        String[] beanNamesForType = context.getBeanFactory().getBeanNamesForType(ServletWebServerFactory.class);
        return (ServletWebServerFactory) context.getBeanFactory().getBean(beanNamesForType[0]);
    }

    protected static Collection<ServletContextInitializer> getServletContextInitializersBeans(ConfigurableApplicationContext context) {
        return new ServletContextInitializerBeans(context.getBeanFactory());
    }

    private static ServletContextInitializer invokeSelfInitialize(ServletWebServerApplicationContext context) {
        try {
            Method method = ServletWebServerApplicationContext.class.getDeclaredMethod("getSelfInitializer");
            method.setAccessible(true);
            return (ServletContextInitializer) method.invoke(context);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static String getProcessNameOnPort(int port) {
        String processName = null;
        try {
            Process process = Runtime.getRuntime().exec("netstat -ano | findstr :" + port);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(Integer.toString(port))) {
                    String[] parts = line.split("\\s+");
                    String pid = parts[parts.length - 1];
                    processName = getProcessNameFromPID(pid);
                    break;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return processName;
    }

    public static String getProcessNameFromPID(String pid) {
        String processName = null;
        try {
            Process process = Runtime.getRuntime().exec("tasklist /FI \"PID eq " + pid + "\"");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            reader.readLine(); // Skip the first line
            String line = reader.readLine();
            if (line != null) {
                processName = line.split("\\s+")[0];
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return processName;
    }

    public static void killProcess(String processName) {
        try {
            Process process = Runtime.getRuntime().exec("taskkill /F /IM " + processName);
            process.waitFor();
            System.out.println("Process " + processName + " killed successfully.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
